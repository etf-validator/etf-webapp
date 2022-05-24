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

import java.util.Collection;

import de.interactive_instruments.etf.dal.dto.Dto;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidHolder;
import de.interactive_instruments.etf.model.EidHolderMap;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;

/**
 * A registry interface to search for specific objects that are created at runtime and to inform requesting objects
 * about the state changes of their dependencies.
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface ItemRegistry {

    /**
     * Inform observers about the item states on which they depend.
     */
    interface DependencyChangeListener {

        /**
         * Fired when a item has been resolved
         *
         * @param resolvedItem
         *            the item that has been resolved
         */
        void fireEventDependencyResolved(final Dto resolvedItem);

        /**
         * Fired when a item has been de-registered
         *
         * @param item
         *            the class of the item that has been de-registered
         * @param item
         *            the item that has been de-registered
         */
        void fireEventDependencyDeregistered(final Class<? extends Dto> item, final EID eid);

        /**
         * Fired when a item has been updated
         *
         * @param updatedItem
         *            the item that has been updated
         */
        void fireEventDependencyUpdated(final Dto updatedItem);
    }

    /**
     * Register items
     *
     * Objects that are dependent on these elements are informed about the registration.
     *
     * @throws IllegalStateException
     *             if the item is already registered
     *
     * @param items
     *            items to register
     */
    void register(Collection<? extends Dto> items);

    /**
     * Deregister items
     *
     * Clients that are dependent on these elements are informed about the de-registration.
     *
     * Note: does not remove the {@link DependencyChangeListener}, that may have been created when
     * {@link #lookupDependency(Collection, DependencyChangeListener)} has been called. See
     * {@link #deregisterCallback(DependencyChangeListener)} for that case.
     *
     * Items that are not found are silently ignored.
     *
     * @param items
     *            items to deregister
     */
    void deregister(Collection<? extends EidHolder> items);

    /**
     * Update items
     *
     * Clients that are dependent on these elements are informed about the update.
     *
     * If the dependency was previously de-registered, an {@link DependencyChangeListener#fireEventDependencyResolved(Dto)}
     * is fired, otherwise an {@link DependencyChangeListener#fireEventDependencyUpdated(Dto)} event.
     *
     * @throws ObjectWithIdNotFoundException
     *             if one of the items were not found
     *
     * @param items
     *            items to deregister
     */
    void update(Collection<? extends Dto> items) throws ObjectWithIdNotFoundException;

    /**
     * Look up specific objects that are created at run time.
     *
     * If the dependencies can be resolved, they are returned directly. The listener is informed when an object on which he
     * depends disappears ( {@link DependencyChangeListener#fireEventDependencyDeregistered(Class, EID)} callback).
     *
     * If a dependency is unknown, the listener is informed (
     * {@link DependencyChangeListener#fireEventDependencyResolved(Dto)} callback) as soon as the object is available.
     *
     * @param dependencies
     *            dependencies the requesting object depends on
     * @param callbackListener
     *            the callback interface of the requesting object
     * @return known dependencies
     */
    EidHolderMap<? extends Dto> lookupDependency(
            Collection<EID> dependencies, DependencyChangeListener callbackListener);

    /**
     * Deregister a listener
     *
     * Note: if the client has registered items, it should have called the method {@link #deregister(Collection)} before.
     *
     * @param listener
     *            the listener to remove
     */
    void deregisterCallback(final DependencyChangeListener listener);

    /**
     * Look up specific objects that are created at run time.
     *
     * If one of the requested objects was not found, an {@link ObjectWithIdNotFoundException } is thrown.
     *
     * See {@link #lookupDependency(Collection, DependencyChangeListener)} for a callback interface.
     *
     * @param ids
     *            dependencies the requesting object depends on
     * @return known dependencies
     */
    EidHolderMap<? extends Dto> lookup(final Collection<EID> ids)
            throws ObjectWithIdNotFoundException;
}
