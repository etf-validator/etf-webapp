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
package de.interactive_instruments.etf;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import de.interactive_instruments.SUtils;

/**
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public abstract class LocalizableError extends Error {
    // Extending Throwable does not work with Spring, extending
    // Exception does not work with SoapUIs Groovy script engine.

    protected final String id;
    protected final Map<String, Object> arguments;

    @Override
    public String getMessage() {
        if (arguments != null && !arguments.isEmpty()) {
            return this.id + "[" + SUtils.concatStr(",", arguments.values()) + "]";
        }
        return this.id;
    }

    private static Map<String, Object> toMap(final Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            return Collections.EMPTY_MAP;
        } else if (arguments.length == 1) {
            return Collections.singletonMap("0", arguments[0]);
        }
        final Map<String, Object> map = new TreeMap<>();
        for (int i = 0, argumentsLength = arguments.length; i < argumentsLength; i++) {
            map.put(String.valueOf(i), arguments[i]);
        }
        return map;
    }

    protected LocalizableError(final String id) {
        super();
        this.id = id;
        arguments = null;
    }

    protected LocalizableError(final String id, final Exception e) {
        super(e);
        this.id = id;
        arguments = Collections.singletonMap("0", e.getMessage());
    }

    protected LocalizableError(final String id, final Exception e, final Object... arguments) {
        super(e);
        this.id = id;
        this.arguments = toMap(arguments);
    }

    protected LocalizableError(final String id, final Object... arguments) {
        super();
        this.id = id;
        this.arguments = toMap(arguments);
    }

    protected LocalizableError(final String id, final String... arguments) {
        super();
        this.id = id;
        this.arguments = (Map) SUtils.toStrMap(arguments);
    }

    protected LocalizableError(final Exception e) {
        super(e);
        this.id = null;
        arguments = Collections.singletonMap("0", e.getMessage());
    }

    public final String getId() {
        return id;
    }

    public final Object[] getArgumentValueArr() {
        if (arguments != null) {
            final Collection<Object> vals = arguments.values();
            return vals.toArray(new Object[vals.size()]);
        }
        return null;
    }

    public final Collection<Object> getArgumentValues() {
        return arguments != null ? Collections.unmodifiableCollection(arguments.values()) : null;
    }

    public Map<String, Object> getArguments() {
        return Collections.unmodifiableMap(arguments);
    }
}
