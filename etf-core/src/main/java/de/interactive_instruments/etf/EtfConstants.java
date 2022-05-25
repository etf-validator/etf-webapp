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
package de.interactive_instruments.etf;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import de.interactive_instruments.CLUtils;

/**
 * Constants which are used in the whole framework
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class EtfConstants {

    private EtfConstants() {

    }

    /**
     * PROPERTY KEYS
     */
    public static final String ETF_PK_PREFIX = "etf.";
    public static final String ETF_TESTDOMAIN_PK = ETF_PK_PREFIX + "test.domain";
    public static final String ETF_AUTHOR_PK = ETF_PK_PREFIX + "author";
    public static final String ETF_LAST_EDITOR_PK = ETF_PK_PREFIX + "last.editor";
    public static final String ETF_CREATION_DATE_PK = ETF_PK_PREFIX + "creation.date";
    public static final String ETF_LAST_UPDATE_DATE_PK = ETF_PK_PREFIX + "last.update.date";
    public static final String ETF_DESCRIPTION_PK = ETF_PK_PREFIX + "description";
    public static final String ETF_VERSION_PK = ETF_PK_PREFIX + "version";
    public static final String ETF_IMPL_STATUS_PK = ETF_PK_PREFIX + "status";
    public static final String ETF_REFERENCE_PK = ETF_PK_PREFIX + "reference";
    public static final String ETF_SUPPORTED_TESTOBJECT_TYPE_IDS_PK = ETF_PK_PREFIX + "supported.test.object.type.ids";
    public static final String ETF_TRANSLATION_TEMPLATE_BUNDLE_ID_PK = ETF_PK_PREFIX + "translation.template.bundle.id";
    public static final String ETF_DEPENDENCY_IDS_PK = ETF_PK_PREFIX + "dependency.ids";

    public static final String ETF_IGNORE_PROPERTIES_PK = ETF_PK_PREFIX + "ignore.properties";

    public static Set<String> ETF_PROPERTY_KEYS = Collections.unmodifiableSet(
            new TreeSet<String>() {
                {
                    add(ETF_TESTDOMAIN_PK);
                    add(ETF_AUTHOR_PK);
                    add(ETF_LAST_EDITOR_PK);
                    add(ETF_CREATION_DATE_PK);
                    add(ETF_LAST_UPDATE_DATE_PK);
                    add(ETF_DESCRIPTION_PK);
                    add(ETF_VERSION_PK);
                    add(ETF_IGNORE_PROPERTIES_PK);
                    add(ETF_IMPL_STATUS_PK);
                    add(ETF_REFERENCE_PK);
                    add(ETF_SUPPORTED_TESTOBJECT_TYPE_IDS_PK);
                    add(ETF_TRANSLATION_TEMPLATE_BUNDLE_ID_PK);
                    add(ETF_DEPENDENCY_IDS_PK);
                }
            });

    /**
     * DIRECTORY KEYS
     */
    public static final String ETF_DIR_PREFIX = "etf.";

    @Deprecated
    public static final String ETF_DATASOURCE_DIR = ETF_DIR_PREFIX + "datasource.dir";

    public static final String ETF_INTERNAL_DATABASE_DIR = ETF_DIR_PREFIX + "internal.database.dir";
    public static final String ETF_ATTACHMENT_DIR = ETF_DIR_PREFIX + "attachments.dir";
    public static final String ETF_TESTDRIVERS_DIR = ETF_DIR_PREFIX + "testdrivers.dir";
    public static final String ETF_TESTDRIVERS_STORAGE_DIR = ETF_DIR_PREFIX + "testdrivers.storage.dir";
    public static final String ETF_PROJECTS_DIR = ETF_DIR_PREFIX + "projects.dir";
    public static final String ETF_REPORTSTYLES_DIR = ETF_DIR_PREFIX + "reportstyles.dir";
    public static final String ETF_BACKUP_DIR = ETF_DIR_PREFIX + "backup.dir";
    public static final String ETF_CURRENT_MODEL_VERSION = CLUtils.getImplVersionOrDefault(EtfConstants.class, "unknown");

    /**
     * ETF NAMESPACE
     */
    public static final String ETF_XMLNS = "http://www.interactive-instruments.de/etf/2.0";

    /**
     * ETF test project property path
     */
    public static final String ETF_TESTPROJECT_PROPERTY_FILE_SUFFIX = ".etftp.properties";

    /**
     * Other properties
     */
    public static final String ETF_DATA_STORAGE_NAME = ETF_PK_PREFIX + "datastorage.name";

    // Slant font by Glenn Chappell
    public static final String ETF_ASCII = "\n" +
            "    __________________\n" +
            "   / ____/_  __/ ____/\n" +
            "  / __/   / / / /_    \n" +
            " / /___  / / / __/    \n" +
            "/_____/ /_/ /_/       \n" +
            "                      \n" +
            "ETF core " + ETF_CURRENT_MODEL_VERSION + "\n";
}
