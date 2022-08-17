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

import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;

import de.interactive_instruments.IFile;
import de.interactive_instruments.SUtils;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.config.ConfigurationException;
import de.interactive_instruments.properties.PropertyUtils;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class DsUtils {

    private DsUtils() {

    }

    static String valueOfOrDefault(final Object obj, final String defaultVal) {
        if (obj != null) {
            final String val = String.valueOf(obj);
            if (!SUtils.isNullOrEmpty(val)) {
                return val;
            }
        }
        return defaultVal;
    }

    static XsltOutputTransformer loadReportTransformer(final Dao dao) throws IOException, ConfigurationException,
            InvalidStateTransitionException, InitializationException, TransformerConfigurationException {
        final XsltOutputTransformer reportTransformer;
        final String resultStylePath = PropertyUtils.getenvOrProperty("etf.result.style.file", null);
        if (resultStylePath == null) {
            reportTransformer = new XsltOutputTransformer(
                    dao, "html", "text/html", "xslt/default/TestRun2DefaultReport.xsl", "xslt/default/");
        } else {
            final IFile resultStyleFile = new IFile(resultStylePath, "Report Stylesheet File");
            resultStyleFile.expectFileIsReadable();
            reportTransformer = new XsltOutputTransformer(
                    dao, "html", "text/html", resultStyleFile);
        }
        reportTransformer.getConfigurationProperties().setPropertiesFrom(dao.getConfigurationProperties(), true);
        reportTransformer.init();
        return reportTransformer;
    }
}
