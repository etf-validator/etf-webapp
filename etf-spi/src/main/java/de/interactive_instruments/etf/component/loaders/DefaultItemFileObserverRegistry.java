/**
 * Copyright 2010-2020 interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.interactive_instruments.etf.component.loaders;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.interactive_instruments.IFile;
import de.interactive_instruments.Releasable;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.io.DirWatcher;
import de.interactive_instruments.io.FileChangeListener;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class DefaultItemFileObserverRegistry implements ItemFileObserverRegistry, Releasable {

    // Maps a path to a path observer
    private final Map<Path, PathObserver> observers = new HashMap<>();
    private final static Logger logger = LoggerFactory.getLogger(DefaultItemFileObserverRegistry.class);

    private final static class ChangeListenersAndState implements Releasable {
        private long lastModified;
        private final Path path;
        private final ItemFileLoaderFactory.FileChangeListener changeListener;

        void triggerIfModified() {
            long lastModified;
            try {
                lastModified = Files.getLastModifiedTime(path).toMillis();
            } catch (IOException e) {
                logger.error("Error getting last modified time: ", e);
                lastModified = -1;
            }
            if (lastModified != this.lastModified) {
                this.lastModified = lastModified;
                try {
                    changeListener.eventFileUpdated();
                } catch (Exception err) {
                    logger.error("Error updating state [item updated] from file '{}' ", path, err);
                }
            }
        }

        boolean releaseIfFileNotExists() {
            if (!Files.exists(this.path)) {
                try {
                    changeListener.eventFileDeleted();
                } catch (Exception err) {
                    logger.error("Error updating state [item deleted] from file '{}' ", path, err);
                }
                return true;
            }
            return false;
        }

        private ChangeListenersAndState(final Path p,
                final ItemFileLoaderFactory.FileChangeListener changeListener) {
            this.path = p;
            try {
                lastModified = Files.getLastModifiedTime(path).toMillis();
            } catch (IOException e) {
                logger.error("Error getting last modified time: ", e);
                lastModified = -1;
            }
            this.changeListener = changeListener;
        }

        public Path getPath() {
            return this.path;
        }

        @Override
        public void release() {
            lastModified = -1;
            this.changeListener.release();
        }
    }

    private final static class PathObserver implements FileChangeListener {
        private final Path root;
        private final Map<Path, ChangeListenersAndState> indexedFiles = new ConcurrentHashMap<>();

        // Maps the factories to all paths and indirectly through the indexedFiles map
        // to the change listeners. This is required for clean up the FileChangeListeners
        // if the factories are deleted.
        private final Map<ItemFileLoaderFactory, List<Path>> factories = new ConcurrentHashMap<>();

        public PathObserver(final Path root, final List<? extends ItemFileLoaderFactory> factories) {
            for (final ItemFileLoaderFactory factory : factories) {
                this.factories.put(factory, new ArrayList<>());
            }
            this.root = root;
        }

        @Override
        public void filesChanged(final Map<Path, WatchEvent.Kind> eventMap, final Set<Path> dirs) {
            // rerun
            final Set<Path> parentLessDirs = dirs.stream().filter(dir -> !dirs.contains(dir.getParent()))
                    .collect(Collectors.toSet());
            final ItemIndexer visitor = new ItemIndexer(factories.keySet());
            parentLessDirs.forEach(d -> {
                logger.trace("Items have changed in directory: " + d.toString());
                try {
                    Files.walkFileTree(d, visitor);
                } catch (IOException e) {
                    logger.error("Failed to walk path tree: " + e.getMessage());
                }
            });
            afterIndex(visitor.getCandidates());
        }

        /**
         * Adds factories and start the initial indexing
         *
         * @param factories
         *            factories to create items
         */
        public void addFactories(final List<? extends ItemFileLoaderFactory> factories) {
            for (final ItemFileLoaderFactory factory : factories) {
                this.factories.put(factory, new ArrayList<>());
            }
            // do a run only with the newly added factories by creating a visitor only with these
            final ItemIndexer visitor = new ItemIndexer(factories);
            logger.trace("Initial indexing of path: " + root.toString());
            try {
                Files.walkFileTree(root, visitor);
            } catch (final IOException e) {
                logger.error("Failed to walk path tree: " + e.getMessage());
            }
            afterIndex(visitor.getCandidates());
        }

        /**
         * Fire events after indexing files
         *
         * @param candidates
         *            changed files and loaders
         */
        private void afterIndex(final List<LoadCmd> candidates) {
            final List<ItemFileLoaderFactory.FileChangeListener> listenersWithInit = new ArrayList<>(candidates.size());

            // Update the state of deleted files
            final List<ChangeListenersAndState> listenersToDelete = new ArrayList<>();
            for (final ChangeListenersAndState l : indexedFiles.values()) {
                if (l.releaseIfFileNotExists()) {
                    listenersToDelete.add(l);
                }
            }
            for (final ChangeListenersAndState d : listenersToDelete) {
                indexedFiles.remove(d.getPath());
            }

            for (final LoadCmd candidate : candidates) {
                // Check event type for each candidate
                final ChangeListenersAndState changeListener = indexedFiles.get(candidate.getPath());
                if (changeListener != null) {
                    changeListener.triggerIfModified();
                } else {
                    // Create new listener
                    final ItemFileLoaderFactory.FileChangeListener newChangeListener = candidate.getLoader()
                            .load(candidate.getPath());
                    if (newChangeListener != null) {
                        this.indexedFiles.put(candidate.getPath(),
                                new ChangeListenersAndState(candidate.getPath(), newChangeListener));
                        listenersWithInit.add(newChangeListener);
                    }
                }
            }
            listenersWithInit.sort(Comparator.naturalOrder());
            for (final ItemFileLoaderFactory.FileChangeListener changeListener : listenersWithInit) {
                try {
                    changeListener.eventFileCreated();
                } catch (Exception e) {
                    ExcUtils.suppress(e);
                }
            }
        }

        public boolean removeFactoriesAndIsEmpty(final List<? extends ItemFileLoaderFactory> factories) {
            for (final ItemFileLoaderFactory factory : factories) {
                final List<Path> paths = this.factories.get(factory);
                if (paths != null) {
                    for (final Path path : paths) {
                        final ChangeListenersAndState listener = indexedFiles.get(path);
                        if (listener != null) {
                            listener.release();
                        }
                    }
                }
                this.factories.remove(factory);
            }
            return this.factories.isEmpty();
        }
    }

    private static class LoadCmd {
        private final ItemFileLoaderFactory loader;
        private final Path path;

        public ItemFileLoaderFactory getLoader() {
            return loader;
        }

        Path getPath() {
            return path;
        }

        private LoadCmd(final ItemFileLoaderFactory loader, final Path path) {
            this.loader = loader;
            this.path = path;
        }
    }

    /**
     * A Command that implements the FileVisitor interface to index files and return candidates
     */
    private static class ItemIndexer implements FileVisitor<Path> {

        private final Collection<? extends ItemFileLoaderFactory> factories;
        private final List<LoadCmd> candidates = new ArrayList<>();

        ItemIndexer(final Collection<? extends ItemFileLoaderFactory> factories) {
            this.factories = factories;
        }

        public List<LoadCmd> getCandidates() {
            return candidates;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            if (dir.getFileName().toString().startsWith(".")) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            if (!file.toString().startsWith(".")) {
                if (file.toString().endsWith(".zip")) {
                    // extract zip and terminate this run
                    final IFile zip = new IFile(file.toString());
                    final IFile extDir = new IFile(new IFile(file.toString()).getFilenameWithoutExt());
                    extDir.mkdir();
                    extDir.expectDirIsWritable();
                    logger.info("Extracting packaged types to {}", extDir.toString());
                    zip.unzipTo(extDir, pathname -> !pathname.toString().contains("META-INF"));
                    return FileVisitResult.TERMINATE;
                }
                for (final ItemFileLoaderFactory factory : factories) {
                    if (factory.couldHandle(file)) {
                        candidates.add(new LoadCmd(factory, file));
                    }
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }

    @Override
    public void register(final Path path, final List<? extends ItemFileLoaderFactory> factories) {
        final PathObserver observer = observers.get(path);
        if (observer == null) {
            final PathObserver newObserver = new PathObserver(path, factories);
            newObserver.addFactories(factories);
            DirWatcher.register(path, newObserver);
            observers.put(path, newObserver);
        } else {
            observer.addFactories(factories);
        }
    }

    @Override
    public void deregister(final List<? extends ItemFileLoaderFactory> factories) {
        final List<PathObserver> observersToRemove = observers.values().stream().filter(
                observer -> observer.removeFactoriesAndIsEmpty(factories)).collect(Collectors.toList());
        for (final PathObserver observer : observersToRemove) {
            DirWatcher.unregister(observer);
            this.observers.remove(observer);
        }
    }

    @Override
    public void release() {
        for (final PathObserver o : observers.values()) {
            DirWatcher.unregister(o);
        }
    }
}
