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
package de.interactive_instruments.etf.dal.dao.basex;

import java.io.*;
import java.util.Collection;
import java.util.Map;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.interactive_instruments.Configurable;
import de.interactive_instruments.IFile;
import de.interactive_instruments.MediaType;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.OutputFormat;
import de.interactive_instruments.exceptions.ExcUtils;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.properties.ConfigProperties;
import de.interactive_instruments.properties.ConfigPropertyHolder;
import de.interactive_instruments.properties.PropertyHolder;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class XsltOutputTransformer implements OutputFormat, Configurable {

    /**
     * Identifier for the transformer
     */
    private final String label;

    private final EID id;

    /**
     * Thread safe!
     */
    private Templates cachedXSLT;

    private ConfigProperties configProperties = new ConfigProperties(
            "etf.webapp.base.url", "etf.api.base.url");
    private IFile stylesheetFile;
    private long stylesheetLastModified = 0;
    private final TransformerFactory transFact = TransformerFactory.newInstance(
            "net.sf.saxon.BasicTransformerFactory", null);
    private final Logger logger = LoggerFactory.getLogger(XsltOutputTransformer.class);
    private boolean initialized = false;
    private final String mimeTypeStr;

    private final MediaType mimeType = new MediaType() {
        @Override
        public MediaType getBaseType() {
            return null;
        }

        @Override
        public String getType() {
            return mimeTypeStr;
        }

        @Override
        public String getSubtype() {
            return null;
        }

        @Override
        public Map<String, String> getParameters() {
            return null;
        }
    };

    private final static class ResourceResolver implements URIResolver {
        private final String xsltBase;

        ResourceResolver(final String xsltBase) {
            this.xsltBase = xsltBase.substring(0, xsltBase.lastIndexOf("/"));
        }

        public Source resolve(final String ref, final String base) {
            if (SUtils.isNullOrEmpty(base)) {
                final IFile file = new IFile(ref);
                if (file.isAbsolute()) {
                    try {
                        return new StreamSource(new FileInputStream(file), file.getPath());
                    } catch (final FileNotFoundException e) {
                        ExcUtils.suppress(e);
                    }
                }
                // can not be resolved without base or failed to create file stream
                return null;
            } else {
                final ClassLoader cL = getClass().getClassLoader();
                final InputStream is = cL.getResourceAsStream(
                        this.xsltBase + "/" + ref);
                return new StreamSource(is, this.xsltBase + "/" + ref);
            }
        }
    }

    /**
     * Create a new XSL Output Transformer and load stylesheet files from the jar
     *
     * Use the other Ctor for XSLTs which are using imports.
     *
     * @param label
     *            Transformer label
     * @param stylesheetJarPath
     *            path to XSLT in JAR
     * @throws IOException
     *             if stylesheet is not readable
     * @throws TransformerConfigurationException
     *             if stylesheet contains errors
     */
    public XsltOutputTransformer(final Dao dao, final String label, final String mimeTypeStr, final String stylesheetJarPath)
            throws IOException, TransformerConfigurationException {
        this(dao, label, mimeTypeStr, stylesheetJarPath, null);
    }

    /**
     * Create a new XSL Output Transformer and load stylesheet files from the jar
     *
     * @param label
     *            Transformer label
     * @param stylesheetJarPath
     *            path to XSLT in JAR
     * @param jarImportPath
     *            base path in JAR for imports
     * @throws IOException
     *             if stylesheet is not readable
     * @throws TransformerConfigurationException
     *             if stylesheet contains errors
     */
    public XsltOutputTransformer(final Dao dao, final String label, final String mimeTypeStr, final String stylesheetJarPath,
            final String jarImportPath)
            throws IOException, TransformerConfigurationException {
        this.id = EidFactory.getDefault().createUUID(dao.getDtoType().getSimpleName() + label);
        this.label = label;
        this.mimeTypeStr = mimeTypeStr;
        this.stylesheetFile = null;
        final ClassLoader cL = getClass().getClassLoader();
        // important to set systemId!
        final Source xsltSource = new StreamSource(cL.getResourceAsStream(stylesheetJarPath), stylesheetJarPath);
        if (jarImportPath != null) {
            transFact.setURIResolver(new ResourceResolver(stylesheetJarPath));
        }
        this.cachedXSLT = transFact.newTemplates(xsltSource);
    }

    /**
     * Create a new XSL Output Transformer and load stylesheet files from a file
     *
     * @param label
     * @param stylesheetFile
     *            XSL stylesheet file
     * @throws IOException
     *             if stylesheet is not readable
     * @throws TransformerConfigurationException
     *             if stylesheet contains errors
     */
    public XsltOutputTransformer(final Dao dao, final String label, final String mimeTypeStr, final IFile stylesheetFile)
            throws IOException, TransformerConfigurationException {
        this.id = EidFactory.getDefault().createUUID(dao.getDtoType().getSimpleName());
        this.mimeTypeStr = mimeTypeStr;
        this.label = label;
        this.stylesheetFile = stylesheetFile;
        stylesheetFile.expectFileIsReadable();
        newTransformerFromCurrentStyle();
    }

    private Transformer newTransformerFromCurrentStyle() throws TransformerConfigurationException {
        if (stylesheetFile != null && stylesheetFile.lastModified() != stylesheetLastModified) {
            synchronized (this) {
                logger.info(this.label + " : caching stylesheet " + stylesheetFile.getAbsolutePath());
                final Source xsltSource = new StreamSource(stylesheetFile);
                this.cachedXSLT = transFact.newTemplates(xsltSource);
                this.stylesheetLastModified = stylesheetFile.lastModified();
                return this.cachedXSLT.newTransformer();
            }
        }
        return this.cachedXSLT.newTransformer();
    }

    @Override
    public EID getId() {
        return this.id;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public MediaType getMediaTypeType() {
        return mimeType;
    }

    @Override
    public void streamTo(final PropertyHolder arguments, final InputStream inputStream, final OutputStream outputStreamStream)
            throws IOException {
        try {
            final Transformer transformer = newTransformerFromCurrentStyle();
            if (arguments != null) {
                arguments.forEach(a -> transformer.setParameter(a.getKey(), a.getValue()));
            }
            if (configProperties != null) {
                configProperties.forEach(c -> transformer.setParameter(c.getKey(), c.getValue()));

                // Basic properties
                transformer.setParameter("stylePath", configProperties.getProperty("etf.webapp.base.url") + "/css");
                transformer.setParameter("baseUrl", configProperties.getProperty("etf.webapp.base.url"));
                transformer.setParameter("serviceUrl", configProperties.getProperty("etf.api.base.url"));
            }

            transformer.transform(
                    new StreamSource(inputStream), new StreamResult(outputStreamStream));
        } catch (final TransformerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String getParamTypeName() {
        return null;
    }

    @Override
    public Collection<Parameter> getParameters() {
        return null;
    }

    @Override
    public Parameter getParameter(final String s) {
        return null;
    }

    @Override
    public ConfigPropertyHolder getConfigurationProperties() {
        return configProperties;
    }

    @Override
    public void init() throws ConfigurationException, InitializationException, InvalidStateTransitionException {
        configProperties.expectAllRequiredPropertiesSet();
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }
}
