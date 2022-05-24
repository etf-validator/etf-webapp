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
package de.interactive_instruments.etf.testdriver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Vector;
import java.util.jar.JarFile;

import org.apache.commons.lang3.SystemUtils;

import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.exceptions.ExcUtils;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class TestTaskClassLoader extends URLClassLoader {
    private static final URL[] NO_URLS = new URL[0];

    public TestTaskClassLoader(final EID id, final ClassLoader classLoader) {
        super("TestTaskClassLoader-" + id.getId(), NO_URLS, classLoader);
        // avoid class loader leak
        if (SystemUtils.IS_OS_WINDOWS) {
            URLConnection.setDefaultUseCaches("JAR", false);
        }
    }

    @Override
    public String getName() {
        return "TestTaskClassLoader";
    }

    @Override
    public void close() throws IOException {
        try {
            final Class<? extends URLClassLoader> clazz = URLClassLoader.class;
            final Field ucp = clazz.getDeclaredField("ucp");
            ucp.setAccessible(true);
            final Object sunMiscURLClassPath = ucp.get(this);
            final Field loaders = sunMiscURLClassPath.getClass().getDeclaredField("loaders");
            loaders.setAccessible(true);
            final Collection<?> collection = (Collection<?>) loaders.get(sunMiscURLClassPath);
            for (final Object sunMiscURLClassPathJarLoader : collection.toArray()) {
                try {
                    final Field loader = sunMiscURLClassPathJarLoader.getClass().getDeclaredField("jar");
                    loader.setAccessible(true);
                    final Object jarFile = loader.get(sunMiscURLClassPathJarLoader);
                    ((JarFile) jarFile).close();
                } catch (Throwable ignore) {
                    ExcUtils.suppress(ignore);
                }
            }
        } catch (Throwable ignore) {
            ExcUtils.suppress(ignore);
        }

        try {
            final Class<? extends ClassLoader> clazz = ClassLoader.class;
            final Field classesProperty = clazz.getDeclaredField("classes");
            classesProperty.setAccessible(true);
            final Vector<Class<?>> classes = (Vector<Class<?>>) classesProperty.get(this);
            classes.clear();
        } catch (Exception e) {}
    }
}
