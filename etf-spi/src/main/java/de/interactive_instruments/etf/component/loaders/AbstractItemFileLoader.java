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

import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.interactive_instruments.Releasable;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.*;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;

/**
 *
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class AbstractItemFileLoader<T extends Dto>
        implements ItemFileLoaderFactory.FileChangeListener, ItemRegistry.DependencyChangeListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final EidHolderMap<Dto> resolvedDependencies = new DefaultEidHolderMap();
    private final Set<EID> unresolvedDependencies = new HashSet<>();
    private ItemRegistry registry = NullItemRegistry.instance();
    private final ClassLoader contextClassloader;
    private final ItemFileLoaderResultListener<T> itemListener;
    // The item this builder built
    private T item;
    private final int priority;
    private boolean prepared;
    protected final File file;

    protected AbstractItemFileLoader(final ItemFileLoaderResultListener<T> itemListener, final int priority, final File file) {
        this.priority = priority;
        this.file = file;
        this.contextClassloader = Thread.currentThread().getContextClassLoader();
        this.itemListener = itemListener;
        this.prepared = false;
    }

    /**
     * Reset the ClassLoader to that one, that was used during the creation of this object. Otherwise classes may not be
     * found during build calls.
     */
    protected final void ensureContextClassLoader() {
        Thread.currentThread().setContextClassLoader(contextClassloader);
    }

    private void addResolvedAndUnresolved(
            final EidHolderMap<? extends Dto> resolvedDependencies, final Collection<EID> allDependencies) {
        this.resolvedDependencies.addAll((Collection<Dto>) resolvedDependencies.values());

        allDependencies.stream().filter(id -> !resolvedDependencies.containsKey(id)).forEach(this.unresolvedDependencies::add);
    }

    private void changeResolvedToUnresolved(final EID id) {
        this.unresolvedDependencies.add(id);
        this.resolvedDependencies.remove(id);
    }

    private void changeUnresolvedToResolved(final Dto item) {
        this.unresolvedDependencies.remove(item.getId());
        this.resolvedDependencies.add(item);
        // re-lookup required
        final EidHolderMap<? extends Dto> resolvedDeps = this.registry.lookupDependency(
            this.resolvedDependencies.keySet(), this);
        this.unresolvedDependencies.removeAll(resolvedDeps.keySet());
        this.resolvedDependencies.putAll(resolvedDeps);
    }

    /**
     * Called to set a dependency on other items
     *
     * @param id
     *            dependency
     */
    protected final void dependsOn(final EID id) {
        if (id != null) {
            final Set<EID> ids = Collections.singleton(id);
            addResolvedAndUnresolved(registry.lookupDependency(ids, this), ids);
        }
    }

    protected final Dto getDependency(final EID id) throws ObjectWithIdNotFoundException {
        final Dto dto = this.resolvedDependencies.get(id);
        if (dto != null) {
            return dto;
        }
        return registry.lookup(Collections.singleton(id)).values().iterator().next();
    }

    /**
     * Called to set a dependency on other items
     *
     * @param ids
     *            dependencies
     */
    protected final void dependsOn(final Collection<EID> ids) {
        if (ids != null && !ids.isEmpty()) {
            addResolvedAndUnresolved(registry.lookupDependency(ids, this), ids);
        }
    }

    protected final Set<EID> getUnresolvedDependencies() {
        return Collections.unmodifiableSet(this.unresolvedDependencies);
    }

    /**
     * Prepare Loader, by parsing and setting dependencies, etc.
     *
     * Prerequisite for building the Item. If the method returns false, {@link #doBuild()} is not called.
     *
     * @return false if the preparation failed, true otherwise
     */
    protected abstract boolean doPrepare();

    /**
     * Prepare Loader, by parsing and setting dependencies, etc.
     *
     * Prerequisite for building the Item. If the method returns false, {@link #doBuild()} is not called.
     *
     * @return false if the preparation failed, true otherwise
     */
    private final boolean prepare() {
        this.resolvedDependencies.clear();
        this.unresolvedDependencies.clear();
        ensureContextClassLoader();
        return doPrepare();
    }

    /**
     * Set the ItemRegistry and prepare this Loader by resolving dependencies with the new registry.
     *
     * @param registry
     *            ItemRegistry to resolve dependencies
     * @return this object
     */
    public final AbstractItemFileLoader<T> setItemRegistry(final ItemRegistry registry) {
        this.registry = registry;
        this.prepared = prepare();
        return this;
    }

    @Override
    public final void fireEventDependencyResolved(final Dto resolvedItem) {
        changeUnresolvedToResolved(resolvedItem);
        if (this.item == null) {
            build();
        }
    }

    @Override
    public final void fireEventDependencyUpdated(final Dto resolvedItem) {
        changeUnresolvedToResolved(resolvedItem);
        eventFileUpdated();
    }

    @Override
    public final void fireEventDependencyDeregistered(final Class<? extends Dto> item, final EID eid) {
        changeResolvedToUnresolved(eid);
        destroyAndDeregisterItem();
    }

    boolean canBuild() {
        ensureContextClassLoader();
        if (logger.isInfoEnabled() && !this.unresolvedDependencies.isEmpty()) {
            logger.info("Item in file '{}' is still waiting for the resolution of {} dependencies: {} ",
                    file.getName(), this.unresolvedDependencies.size(),
                    SUtils.concatStr(",", this.unresolvedDependencies));
        }
        return this.prepared && this.unresolvedDependencies.isEmpty();
    }

    /**
     * Try to build the item.
     *
     * If the item has been successfully built, it will be registered in the dependency registry
     */
    final void build() {
        if (canBuild()) {
            this.item = doBuild();
            if (this.item != null) {
                itemListener.eventItemBuilt(this.item);
                this.registry.register(Collections.singleton(this.item.createCopy()));
            }
        }
    }

    /**
     * Build the item
     *
     * @return the result
     */
    protected abstract T doBuild();

    /**
     * Destroy the item and de-register it from the dependency registry
     */
    protected final void destroyAndDeregisterItem() {
        if (item != null) {
            ensureContextClassLoader();
            logger.trace("Releasing item '{}' created from file '{}' ", this.item.getId(), this.file.getName());
            this.registry.deregister(Collections.singleton(item.createCopy()));
            itemListener.eventItemDestroyed(this.item.getId());
            doRelease();
            if (this.item instanceof Releasable) {
                ((Releasable) this.item).release();
            }
            this.item = null;
        }
    }

    /**
     * Release before this object de-registers the DependencyChangeListeners
     */
    protected abstract void doRelease();

    /**
     * Try to build the item.
     *
     * If the item has been successfully built, it will be updated or registered in the dependency registry
     */
    @Override
    public void eventFileCreated() {
        // Check if this item has already
        // been prepared after setting the item registry
        if (!this.prepared) {
            this.prepared = prepare();
        }
        build();
    }

    /**
     * Try to re-build the item.
     *
     * If the item has been successfully built, it will be updated or registered in the dependency registry
     */
    @Override
    public final void eventFileUpdated() {
        this.prepared = prepare();
        if (canBuild()) {
            this.item = doBuild();
            if (this.item != null) {
                itemListener.eventItemUpdated(this.item);
                try {
                    this.registry.update(Collections.singleton(this.item.createCopy()));
                } catch (ObjectWithIdNotFoundException e) {
                    // This may indicate a state issue
                    this.registry.register(Collections.singleton(this.item.createCopy()));
                }
            }
        } else {
            destroyAndDeregisterItem();
        }
    }

    /**
     * Release the builder
     *
     * Note: this also de-registers this builder form the dependency registry
     */
    @Override
    public final void eventFileDeleted() {
        release();
    }

    @Override
    public void release() {
        ensureContextClassLoader();
        destroyAndDeregisterItem();
        doRelease();
        this.registry.deregisterCallback(this);
    }

    @Override
    public int compareTo(final ItemFileLoaderFactory.FileChangeListener o) {
        final AbstractItemFileLoader<?> otherFileLoader = (AbstractItemFileLoader<?>) o;
        // 0 if objects are identical
        if(this==o) {
            return 0;
        }
        // Compare priorities
        final int prioCmp = Integer.compare(this.priority, otherFileLoader.priority);
        if (prioCmp == 0) {
            final int depsCmp = Integer.compare(
                unresolvedDependencies.size(),
                otherFileLoader.unresolvedDependencies.size());
            if(depsCmp!=0) {
                return depsCmp;
            }else{
                // Compare target files
                final int f = this.file.compareTo(otherFileLoader.file);
                return Integer.compare(f, 0);
            }
        }
        return prioCmp;
    }

    protected final T getResult() {
        return this.item;
    }
}
