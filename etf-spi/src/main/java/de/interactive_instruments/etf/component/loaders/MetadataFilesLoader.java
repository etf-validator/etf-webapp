/**
 * Copyright 2010-2022 interactive instruments GmbH
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

import java.util.ArrayList;

import de.interactive_instruments.Configurable;
import de.interactive_instruments.Releasable;
import de.interactive_instruments.etf.EtfConstants;
import de.interactive_instruments.etf.dal.dao.DataStorage;
import de.interactive_instruments.etf.dal.dto.capabilities.TagDto;
import de.interactive_instruments.etf.dal.dto.capabilities.TestRunTemplateDto;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.properties.ConfigProperties;
import de.interactive_instruments.properties.ConfigPropertyHolder;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class MetadataFilesLoader implements Configurable, Releasable {
    private final ArrayList<AbstractItemFileLoaderFactory> factories;
    private final ConfigProperties configProperties;
    private final LoadingContext loadingContext;
    private boolean initialized;

    public MetadataFilesLoader(final DataStorage dataStorageCallback, final LoadingContext contextLoader) {
        factories = new ArrayList<AbstractItemFileLoaderFactory>() {
            {
                add(new TranslationTemplateBundleXmlFileLoader(dataStorageCallback.getDao(TranslationTemplateBundleDto.class)));
                add(new TagXmlFileLoader(dataStorageCallback.getDao(TagDto.class)));
                add(new TestRunTemplateXmlFileLoader(dataStorageCallback.getDao(TestRunTemplateDto.class)));
            }
        };
        this.configProperties = new ConfigProperties(EtfConstants.ETF_PROJECTS_DIR);
        this.loadingContext = contextLoader;
        for (final AbstractItemFileLoaderFactory factory : factories) {
            factory.setContextLoader(this.loadingContext);
        }
    }

    @Override
    public void init() throws ConfigurationException, InitializationException {
        this.configProperties.expectAllRequiredPropertiesSet();
        this.loadingContext.getItemFileObserverRegistry().register(
                this.configProperties.getPropertyAsFile(EtfConstants.ETF_PROJECTS_DIR).toPath(), factories);
        this.initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public ConfigPropertyHolder getConfigurationProperties() {
        return this.configProperties;
    }

    @Override
    public void release() {
        this.loadingContext.getItemFileObserverRegistry().deregister(factories);
    }
}
