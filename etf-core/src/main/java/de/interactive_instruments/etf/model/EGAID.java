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
package de.interactive_instruments.etf.model;

import java.util.regex.Pattern;

import de.interactive_instruments.ImmutableVersion;
import de.interactive_instruments.Versionable;

/**
 * The ETF Group and Artifact ID class is intended to provide identifiers for a group of objects with different versions
 * in the ETF environment.
 *
 * TODO move VERSION into EGAVID
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public interface EGAID extends Versionable, Comparable {

    Pattern EGAID = Pattern.compile("egaid\\.([a-z][a-z1-9\\-.]*)\\.([a-z][a-z1-9\\-]*)");
    Pattern EGAID_WP = Pattern.compile("([a-z][a-z1-9\\-.]*)\\.([a-z][a-z1-9\\-]*)");
    Pattern EGAID_REF = Pattern
            .compile("egaid\\.([a-z][a-z1-9\\-.]*)\\.([a-z][a-z1-9\\-]*)(:([0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?|latest))?");

    String getArtifactId();

    String getGroupId();

    String getEgaId();

    default String getEgaIdRef() {
        final ImmutableVersion v = this.getVersion();
        if (v != null) {
            return getEgaId() + ":" + v;
        }
        return getEgaId();
    }

    @Override
    default int compareTo(final Object o) {
        if (o instanceof EGAID) {
            return ((EGAID) o).getEgaIdRef().compareToIgnoreCase(getEgaIdRef());
        } else if (o instanceof EgaidHolder) {
            final EGAID h = ((EgaidHolder) o).getEgaId();
            return h.getEgaIdRef().compareToIgnoreCase(getEgaIdRef());
        }
        return -1;
    }
}
