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

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.regex.Pattern;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.exceptions.ExcUtils;

/**
 * Loads a EidFactory service provider
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
class EidFactoryLoader {

    public static final String ETF_EID_FACTORY = "etf.eid.EidFactory";

    private static final class InstanceHolder {
        static final EidFactory findEidFactoy() {
            final ServiceLoader<EidFactory> factories = ServiceLoader.load(EidFactory.class);
            final String factoryClassname = System.getProperty(ETF_EID_FACTORY);
            if (!SUtils.isNullOrEmpty(factoryClassname) && factories.iterator().hasNext()) {
                for (final EidFactory factory : factories) {
                    if (factoryClassname.equals(factory.getClass().getName())) {
                        return factory;
                    }
                }
            }
            return new DefaultEidFactory();
        }

        private static class DefaultEidFactory implements EidFactory {
            private static Pattern pattern = Pattern.compile(
                    "EID[0-9A-F]{8}-[0-9A-F]{4}-[1-5][0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}", Pattern.CASE_INSENSITIVE);

            // TODO rename to createNewId(). Include a timestamp:
            // https://engineering.instagram.com/sharding-ids-at-instagram-1cf5a71e5a5c#.fgealvsj9
            @Override
            public EID createRandomId() {
                return new DefaultEid(UUID.randomUUID().toString());
            }

            @Override
            public EID createAndPreserveStr(final String str) {
                return new DefaultEid(str);
            }

            @Override
            public EID createUUID(final String uuid) {
                Objects.requireNonNull(uuid, "Cannot generate EID from null String!");
                try {
                    if (uuid.length() == 36) {
                        return new DefaultEid(UUID.fromString(uuid).toString());
                    } else if (uuid.length() == 39 && uuid.startsWith("EID")) {
                        return new DefaultEid(UUID.fromString(uuid.substring(3)).toString());
                    }
                } catch (IllegalArgumentException e) {
                    ExcUtils.suppress(e);
                }
                return new DefaultEid(UUID.nameUUIDFromBytes(uuid.getBytes()).toString());
            }

            @Override
            public EID createAndPreserveUUID(final UUID uuid) {
                return new DefaultEid(uuid.toString());
            }

            @Override
            public Pattern getPattern() {
                return pattern;
            }
        }

        static final EidFactory INSTANCE = findEidFactoy();
    }

    public static EidFactory instance() {
        return EidFactoryLoader.InstanceHolder.INSTANCE;
    }
}
