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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.interactive_instruments.etf.EtfXpathEvaluator;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.StreamWriteDao;
import de.interactive_instruments.etf.dal.dto.translation.TranslationTemplateBundleDto;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;

/**
 * A loader for Translation Template Bundles stored in XML files
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class TranslationTemplateBundleXmlFileLoader
        extends AbstractItemFileLoaderFactory<TranslationTemplateBundleDto> {

    private final StreamWriteDao<TranslationTemplateBundleDto> writeDao;
    private final static Logger logger = LoggerFactory.getLogger(TranslationTemplateBundleXmlFileLoader.class);
    private static final String TRANSLATION_TEMPLATE_BUNDLE_PREFIX = "TranslationTemplateBundle-";
    private static final String TRANSLATION_TEMPLATE_BUNDLE_SUFFIX = ".xml";
    private static final int priority = 200;

    TranslationTemplateBundleXmlFileLoader(final Dao<TranslationTemplateBundleDto> writeDao) {
        this.writeDao = (StreamWriteDao<TranslationTemplateBundleDto>) writeDao;
    }

    private static class TranslationTemplateBundleLoadCmd extends AbstractItemFileLoader<TranslationTemplateBundleDto> {

        private final StreamWriteDao<TranslationTemplateBundleDto> writeDao;

        TranslationTemplateBundleLoadCmd(final ItemFileLoaderResultListener<TranslationTemplateBundleDto> itemListener,
                final Path path, final StreamWriteDao<TranslationTemplateBundleDto> writeDao) {
            super(itemListener, priority, path.toFile());
            this.writeDao = writeDao;
        }

        @Override
        protected boolean doPrepare() {
            try {
                dependsOn(EtfXpathEvaluator.evalEidOrNull(
                        "/etf:TranslationTemplateBundle[1]/etf:parent/@ref", file));
            } catch (final IOException | XPathExpressionException e) {
                logger.error("Error preparing Translation Template Bundle from file {}", file, e);
                return false;
            }
            return true;
        }

        @Override
        protected TranslationTemplateBundleDto doBuild() {
            try {
                return writeDao.add(new FileInputStream(file), Optional.empty(), dto -> {
                    dto.setSource(file.toURI());
                    return dto;
                });
            } catch (IOException e) {
                logger.error("Error creating Translation Template Bundle from file {}", file, e);
            }
            return null;
        }

        @Override
        protected void doRelease() {
            if (getResult() != null) {
                try {
                    writeDao.delete(getResult().getId());
                } catch (StorageException | ObjectWithIdNotFoundException e) {
                    ExcUtils.suppress(e);
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean couldHandle(final Path path) {
        final String fName = path.getFileName().toString();
        return fName.startsWith(TRANSLATION_TEMPLATE_BUNDLE_PREFIX) &&
                fName.endsWith(TRANSLATION_TEMPLATE_BUNDLE_SUFFIX);
    }

    @Override
    public FileChangeListener load(final Path path) {
        if (couldHandle(path)) {
            return new TranslationTemplateBundleLoadCmd(
                    this, path, writeDao).setItemRegistry(getItemRegistry());
        }
        return null;
    }
}
