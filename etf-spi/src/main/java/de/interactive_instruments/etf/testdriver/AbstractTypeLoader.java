/**
 * Copyright 2010-2022 interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package de.interactive_instruments.etf.testdriver;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
/*
 * public class AbstractTypeLoader implements TypeLoader {
 *
 * protected final DataStorage dataStorageCallback; protected final Logger logger = LoggerFactory.getLogger(getClass());
 *
 * private final ClassLoader contextClassloader; private boolean initialized = false; private final EidHolderMap
 * cachedTypes = new DefaultEidHolderMap();
 *
 * protected AbstractTypeLoader(final DataStorage dataStorageCallback) { this.dataStorageCallback = dataStorageCallback;
 * this.contextClassloader = Thread.currentThread().getContextClassLoader(); }
 *
 * protected void ensureContextClassLoader() { Thread.currentThread().setContextClassLoader(contextClassloader); }
 *
 * protected abstract void doBeforeDeregister(final EidSet<? extends Dto> dtos);
 *
 * protected final void deregisterTypes(final EidSet<Dto> values) { cachedTypes.removeAll(values);
 * doBeforeDeregister(values); try { ((WriteDao) dataStorageCallback.getDao(dto.getClass())).delete(dto.getId()); }
 * catch (ObjectWithIdNotFoundException | StorageException e) { logger.error("Could not deregister {} : ",
 * dto.getDescriptiveLabel(), e); } }); }
 *
 * protected abstract void doBeforeRegister(final EidSet<? extends Dto> dtos);
 *
 * protected abstract void doAfterRegister(final EidSet<? extends Dto> dtos);
 *
 * protected void registerTypes(final EidSet<Dto> dtos) { // Register IDs doBeforeRegister(dtos);
 * cachedTypes.addAll(dtos); doAfterRegister(dtos); }
 *
 * @Override public final EidSet<? extends Dto> getTypes() { return cachedTypes.toSet(); }
 *
 * protected abstract void doInit() throws ConfigurationException, InitializationException,
 * InvalidStateTransitionException;
 *
 * @Override public final void init() throws InitializationException, InvalidStateTransitionException,
 * ConfigurationException { if (initialized == true) { throw new InvalidStateTransitionException("Already initialized");
 * }
 *
 * doInit();
 *
 * this.initialized = true; } }
 */
