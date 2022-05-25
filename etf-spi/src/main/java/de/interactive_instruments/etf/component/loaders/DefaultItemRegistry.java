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

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.DefaultEidHolderMap;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidHolder;
import de.interactive_instruments.etf.model.EidHolderMap;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;

/**
 * A registry to search for specific objects that are created at runtime and to inform requesting objects about the
 * state changes of your dependencies.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class DefaultItemRegistry implements ItemRegistry {

    private final static Logger logger = LoggerFactory.getLogger(DefaultItemRegistry.class);

    /**
     * Represents the state of a dependency and the associated listeners
     */
    private static abstract class DependencyState {
        protected Set<DependencyChangeListener> listeners;

        protected final void addListener(final DependencyChangeListener listener) {
            if (listener != null) {
                if (listeners == null) {
                    listeners = new ConcurrentSkipListSet<DependencyChangeListener>() {
                        {
                            add(listener);
                        }
                    };
                } else {
                    listeners.add(listener);
                }
            }
        }

        final void deregister(final DependencyChangeListener listener) {
            if (this.listeners != null) {
                this.listeners.remove(listener);
            }
        }

        abstract Dto registerListenerAndGetTarget(final DependencyChangeListener listener);

        abstract DependencyState resolve(final Dto dto);

        abstract DependencyState deregister();

        abstract DependencyState update(final Dto dto);
    }

    /**
     * A dependency that as been resolved
     */
    private static class ResolvedDependencyState extends DependencyState {
        /**
         * the resolved object
         */
        private final Dto entry;

        private ResolvedDependencyState(final Dto entry) {
            this.entry = entry;
        }

        private ResolvedDependencyState(
                final Set<DependencyChangeListener> listeners,
                final Dto entry) {
            this.listeners = listeners;
            this.entry = entry;
        }

        public Dto registerListenerAndGetTarget(final DependencyChangeListener listener) {
            addListener(listener);
            return entry;
        }

        @Override
        public DependencyState resolve(final Dto dto) {
            throw new IllegalStateException("Item '" + dto.getId()
                    + "' already registered. The ETS Developer or the Administrator should check for duplicate IDs (in duplicated files).");
        }

        @Override
        public DependencyState deregister() {
            if (this.listeners != null) {
                for (final DependencyChangeListener listener : this.listeners) {
                    try {
                        if (listener != null) {
                            listener.fireEventDependencyDeregistered(
                                    this.entry.getClass(), this.entry.getId());
                        }
                    } catch (Exception ign) {
                        ExcUtils.suppress(ign);
                    }
                }
            }
            return new UnknownDependencyState(this.listeners);
        }

        @Override
        DependencyState update(final Dto dto) {
            if (this.listeners != null) {
                for (final DependencyChangeListener listener : this.listeners) {
                    try {
                        if (listener != null) {
                            listener.fireEventDependencyUpdated(dto);
                        }
                    } catch (Exception ign) {
                        ExcUtils.suppress(ign);
                    }
                }
            }
            return this;
        }
    }

    /**
     * Represents an unknown dependency
     */
    private static class UnknownDependencyState extends DependencyState {
        private UnknownDependencyState(final DependencyChangeListener listener) {
            addListener(listener);
        }

        private UnknownDependencyState(final Set<DependencyChangeListener> listeners) {
            this.listeners = listeners;
        }

        public Dto registerListenerAndGetTarget(final DependencyChangeListener listener) {
            addListener(listener);
            return null;
        }

        @Override
        public DependencyState resolve(final Dto dto) {
            if (this.listeners != null) {
                for (final DependencyChangeListener listener : this.listeners) {
                    try {
                        if (listener != null) {
                            // Use a copy
                            listener.fireEventDependencyResolved(dto);
                        }
                    } catch (Exception ign) {
                        ExcUtils.suppress(ign);
                    }
                }
            }
            return new ResolvedDependencyState(this.listeners, dto);
        }

        @Override
        public DependencyState deregister() {
            return this;
        }

        @Override
        public DependencyState update(final Dto dto) {
            return resolve(dto);
        }
    }

    /**
     * Container class for dependency states
     */
    private static class DependencyEntry {
        private DependencyState entry;

        synchronized void resolve(final Dto dto) {
            entry = entry.resolve(dto);
        }

        synchronized void deregister() {
            entry = entry.deregister();
        }

        synchronized void update(final Dto dto) {
            entry = entry.update(dto);
        }

        synchronized Dto registerListenerDependencyAndGetTarget(DependencyChangeListener listener) {
            return entry.registerListenerAndGetTarget(listener);
        }

        synchronized void deregister(final DependencyChangeListener listener) {
            entry.deregister(listener);
        }

        private DependencyEntry(final DependencyState entry) {
            this.entry = entry;
        }

        static DependencyEntry createUnknown(final DependencyChangeListener listener, EID eid) {
            return new DependencyEntry(new UnknownDependencyState(listener));
        }

        static DependencyEntry createResolved(final Dto dto) {
            return new DependencyEntry(new ResolvedDependencyState(dto));
        }

        boolean isResolved() {
            return entry instanceof ResolvedDependencyState;
        }
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<EID, DependencyEntry> entries = new HashMap<>();
    private final Map<DependencyChangeListener, List<DependencyEntry>> listeners = new HashMap<>();

    @Override
    public void register(final Collection<? extends Dto> items) {
        for (final Dto dto : items) {
            final DependencyEntry dependency = entries.get(dto.getId());
            lock.lock();
            if (dependency == null) {
                entries.put(dto.getId(), DependencyEntry.createResolved(dto));
                logger.debug("Adding '{}'", dto.getId());
                lock.unlock();
            } else {
                lock.unlock();
                logger.debug("Adding '{}' and informing dependent listeners", dto.getId());
                dependency.resolve(dto);
            }
        }
    }

    @Override
    public void deregister(final Collection<? extends EidHolder> items) {
        for (final EidHolder eidHolder : items) {
            final DependencyEntry dependency = entries.get(eidHolder.getId());
            // ignore items that are not registered
            if (dependency != null) {
                dependency.deregister();
                logger.debug("Unregistered '{}'", eidHolder.getId());
            }
        }
    }

    @Override
    public void update(final Collection<? extends Dto> items) throws ObjectWithIdNotFoundException {
        for (final Dto dto : items) {
            final DependencyEntry dependency = entries.get(dto.getId());
            if (dependency == null) {
                throw new ObjectWithIdNotFoundException(dto.getId().toString());
            } else {
                dependency.update(dto);
                logger.debug("Updated '{}'", dto.getId());
            }
        }
    }

    @Override
    public void deregisterCallback(final DependencyChangeListener listener) {
        final List<DependencyEntry> dependencyEntries = listeners.get(listener);
        lock.lock();
        if (dependencyEntries != null) {
            dependencyEntries.forEach(d -> d.deregister(listener));
        }
        lock.unlock();
    }

    @Override
    public EidHolderMap<? extends Dto> lookupDependency(
            final Collection<EID> dependencies, final DependencyChangeListener callbackListener) {
        final EidHolderMap<Dto> results = new DefaultEidHolderMap<>();
        final List<DependencyEntry> listenerEntries;
        lock.lock();
        if (listeners.containsKey(Objects.requireNonNull(callbackListener,
                "DependencyChangeListener is null"))) {
            listenerEntries = listeners.get(callbackListener);
        } else {
            listenerEntries = new ArrayList<>(dependencies.size());
            listeners.put(callbackListener, listenerEntries);
        }
        for (final EID dependency : dependencies) {
            final DependencyEntry result = entries.get(dependency);
            if (result != null) {
                final Dto dto = result.registerListenerDependencyAndGetTarget(callbackListener);
                if (dto != null) {
                    results.add(dto);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Registering listener for unresolved dependency '{}' ", dependency);
                }
                listenerEntries.add(result);
            } else {
                final DependencyEntry unknownEntry = DependencyEntry.createUnknown(callbackListener, dependency);
                logger.debug("Registering listener for unresolved dependency '{}' ", dependency);
                entries.put(dependency, unknownEntry);
                listenerEntries.add(unknownEntry);
            }
        }
        lock.unlock();
        return results;
    }

    @Override
    public EidHolderMap<? extends Dto> lookup(final Collection<EID> ids) throws ObjectWithIdNotFoundException {
        final EidHolderMap<Dto> results = new DefaultEidHolderMap<>();
        for (final EID id : ids) {
            final DependencyEntry result = this.entries.get(id);
            if (result == null) {
                throw new ObjectWithIdNotFoundException(id.toString());
            }
            final Dto resolved = result.registerListenerDependencyAndGetTarget(null);
            if (resolved == null) {
                throw new ObjectWithIdNotFoundException(id.toString());
            }
            results.add(resolved);
        }
        return results;
    }

    SortedSet<EID> unresolvedIds() {
        final SortedSet<EID> unresolvedIds = new TreeSet<>();
        for (final Map.Entry<EID, DependencyEntry> dep : entries.entrySet()) {
            if (!dep.getValue().isResolved()) {
                unresolvedIds.add(dep.getKey());
            }
        }
        return unresolvedIds;
    }
}
