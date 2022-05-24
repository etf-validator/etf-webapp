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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.interactive_instruments.CLUtils;
import de.interactive_instruments.IFile;
import de.interactive_instruments.UriUtils;
import de.interactive_instruments.exceptions.ExcUtils;

/**
 * A class loader used for test components.
 *
 * This CL does not search for the loaded class by calling the parent class loader (default Java RT ClassLoader
 * behaviour) but tries to load the class as Jar from its children URL ClassLoader first.
 *
 * Detailed search order: - Check if parent already loaded Class - Try loading from Jars - Try loading from dependency
 * ClassLoaders - Try loading from parent
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
final class ComponentClassLoader extends ClassLoader implements Closeable {
    private final static Logger logger = LoggerFactory.getLogger(ComponentClassLoader.class);
    private final ChildURLClassLoader childClassLoader;

    /**
     * Delegation to parent Classloader
     */
    private static class ParentDelegationClassClassLoader extends ClassLoader {
        private final Logger logger;

        ParentDelegationClassClassLoader(final ClassLoader parent, final Logger logger) {
            super(parent);
            this.logger = logger;
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            logger.trace("ParentCL: Loading of class {} failed, trying parent class loader {}",
                    name, super.getClass().getSimpleName());
            return super.findClass(name);
        }

        @Override
        public URL getResource(String name) {
            logger.trace("ParentCL: Loading of resource {} failed, trying parent class loader {}",
                    name, super.getClass().getSimpleName());
            return super.getResource(name);
        }

        @Override
        public InputStream getResourceAsStream(final String name) {
            logger.trace("ParentCL: Loading of resource {} failed, trying parent class loader {}",
                    name, super.getClass().getSimpleName());
            return super.getResourceAsStream(name);
        }
    }

    /**
     * Delegation to child URL Classloader
     */
    private static class ChildURLClassLoader extends URLClassLoader {
        private final ParentDelegationClassClassLoader realParent;
        private final Logger logger;
        private Collection<ClassLoader> dependencyClassLoaders;
        private IFile jarExtractionDir;
        private final Set<String> disallowedJarNames;

        ChildURLClassLoader(final URL[] urls, final ParentDelegationClassClassLoader realParent,
                final Set<String> disallowedJarNames, final Logger logger) {
            super(urls, null);
            this.logger = logger;
            this.realParent = realParent;
            this.disallowedJarNames = disallowedJarNames;
            for (int i = 0; i < urls.length; i++) {
                addSubPackagedLibJars(urls[i]);
            }
        }

        private void ensureExtractionDir() throws IOException {
            if (jarExtractionDir == null) {
                jarExtractionDir = IFile.createTempDir("etf_CL" + hashCode());
                logger.trace("Created temporary directory {}", jarExtractionDir);
            }
        }

        @Override
        public Class<?> findClass(final String name) throws ClassNotFoundException {
            try {
                logger.trace("ChildCL: Trying to load class {}", name);
                final Class<?> loaded = super.findLoadedClass(name);
                if (loaded != null) {
                    // Load already defined Class
                    return loaded;
                }
                return super.findClass(name);
            } catch (final ClassNotFoundException ignore) {
                if (dependencyClassLoaders != null) {
                    for (final ClassLoader dependencyClassLoader : dependencyClassLoaders) {
                        try {
                            return dependencyClassLoader.loadClass(name);
                        } catch (ClassNotFoundException | NoClassDefFoundError ignore2) {
                            ExcUtils.suppress(ignore2);
                        }
                    }
                }
                return realParent.loadClass(name);
            }
        }

        @Override
        public URL getResource(final String name) {
            logger.trace("ChildCL:  Trying to load resource {}", name);
            final URL resource = super.getResource(name);
            if (resource != null) {
                return resource;
            } else {
                if (dependencyClassLoaders != null) {
                    for (final ClassLoader dependencyClassLoader : dependencyClassLoaders) {
                        final URL resource2 = dependencyClassLoader.getResource(name);
                        if (resource2 != null) {
                            return resource2;
                        }
                    }
                }
                return realParent.getResource(name);
            }
        }

        @Override
        public InputStream getResourceAsStream(final String name) {
            logger.trace("ChildCL:  Trying to load resource {}", name);
            final InputStream stream = super.getResourceAsStream(name);
            if (stream != null) {
                return stream;
            } else {
                if (dependencyClassLoaders != null) {
                    for (final ClassLoader dependencyClassLoader : dependencyClassLoaders) {
                        final InputStream stream2 = dependencyClassLoader.getResourceAsStream(name);
                        if (stream2 != null) {
                            return stream2;
                        }
                    }
                }
                return realParent.getResourceAsStream(name);
            }
        }

        /**
         * Load a JAR from an URL
         *
         * @param url
         *            JAR URL
         */
        void addUrl(final URL url) {
            if (disallowedJarNames == null || !disallowedJarNames.contains(
                    IFile.getFilenameWithoutExtAndVersion(UriUtils.lastSegment(url.getPath())))) {
                logger.trace("Adding jar {}", url);
                addURL(url);
                addSubPackagedLibJars(url);
            } else if (logger.isTraceEnabled()) {
                logger.trace("Disallowed jar {}", url);
            }
        }

        /**
         * Add dependency ClassLoaders
         *
         * @param classLoaders
         */
        void addDependencyClassLoaders(final Collection<ClassLoader> classLoaders) {
            dependencyClassLoaders = classLoaders;
        }

        /**
         * Add one dependency ClassLoader
         *
         * @param classLoader
         */
        void addDependencyClassLoader(final ClassLoader classLoader) {
            if (dependencyClassLoaders == null) {
                dependencyClassLoaders = new ArrayList<>(2);
            }
            dependencyClassLoaders.add(classLoader);
        }

        /**
         * Add JAR files located in a lib folder of a JAR path
         *
         * @param jarUrl
         */
        private void addSubPackagedLibJars(final URL jarUrl) {
            try {
                final JarFile jar = new JarFile(UriUtils.download(jarUrl.toURI()));
                final Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();
                    if (entry.getName().startsWith("lib/") && entry.getName().endsWith(".jar")) {
                        ensureExtractionDir();
                        final IFile libFile = new IFile(jarExtractionDir, entry.getName().substring(4));
                        libFile.write(jar.getInputStream(entry));
                        logger.trace("Adding sub packaged library {}", libFile);
                        addURL(libFile.toURI().toURL());
                    }
                }
            } catch (URISyntaxException | IOException e) {
                throw new IllegalArgumentException("Cannot add sub packaged JARs from JAR " + jarUrl, e);
            }
        }

    }

    /**
     * Component Classloader
     *
     * @param jar
     *            jar path
     */
    ComponentClassLoader(final URL jar) {
        super(Thread.currentThread().getContextClassLoader());
        childClassLoader = new ChildURLClassLoader(new URL[]{jar},
                new ParentDelegationClassClassLoader(this.getParent(), logger), null, logger);
    }

    /**
     * Component Classloader
     *
     * @param jar
     *            jar path
     * @param disallowedJarNames
     *            names of jar files that will not be loaded
     */
    ComponentClassLoader(final URL jar, final Set<String> disallowedJarNames) {
        super(Thread.currentThread().getContextClassLoader());
        childClassLoader = new ChildURLClassLoader(new URL[]{jar},
                new ParentDelegationClassClassLoader(this.getParent(), logger), disallowedJarNames, logger);
    }

    /**
     * Component Classloader
     *
     * @param jars
     *            jar files
     */
    ComponentClassLoader(final Collection<URL> jars) {
        super(Thread.currentThread().getContextClassLoader());
        childClassLoader = new ChildURLClassLoader(jars.toArray(new URL[jars.size()]),
                new ParentDelegationClassClassLoader(this.getParent(), logger), null, logger);
    }

    /**
     * Component Classloader
     *
     * @param jars
     *            jar files
     * @param disallowedJarNames
     *            names of jar files that will not be loaded
     */
    ComponentClassLoader(final Collection<URL> jars, final Set<String> disallowedJarNames) {
        super(Thread.currentThread().getContextClassLoader());
        childClassLoader = new ChildURLClassLoader(jars.toArray(new URL[jars.size()]),
                new ParentDelegationClassClassLoader(this.getParent(), logger), disallowedJarNames, logger);
    }

    @Override
    protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        try {
            logger.trace("ComponentCL: Trying to load class {}", name);
            return childClassLoader.findClass(name);
        } catch (ClassNotFoundException e) {
            logger.trace("ComponentCL: Loading of class {} failed, trying class loader {}",
                    name, super.getClass().getSimpleName());
            return super.loadClass(name, resolve);
        }
    }

    @Override
    public URL getResource(final String name) {
        logger.trace("ComponentCL: Trying to load resource {}", name);
        final URL resource = childClassLoader.getResource(name);
        if (resource != null) {
            return resource;
        } else {
            logger.trace("ComponentCL: Loading of resource {} failed, trying class loader {}",
                    name, super.getClass().getSimpleName());
            return super.getResource(name);
        }
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        logger.trace("ComponentCL: Trying to load resource {}", name);
        final InputStream stream = childClassLoader.getResourceAsStream(name);
        if (stream != null) {
            return stream;
        } else {
            logger.trace("ComponentCL: Loading of resource {} failed, trying class loader {}",
                    name, super.getClass().getSimpleName());
            return super.getResourceAsStream(name);
        }
    }

    @Override
    public void close() {
        CLUtils.forceCloseUcp(this.childClassLoader);
    }

    /**
     * Load a JAR from an URL
     *
     * @param url
     *            url to JAR
     */
    public void addUrl(final URL url) {
        childClassLoader.addUrl(url);
    }

    /**
     * Load a JAR path
     *
     * @param file
     *            JAR path
     * @throws IOException
     */
    public void addJar(final IFile file) throws IOException {
        file.expectFileIsReadable();
        childClassLoader.addUrl(file.toURI().toURL());
    }

    /**
     * Load a directory with Jar files
     *
     * @param dir
     *            directory containing JARs
     * @throws IOException
     */
    public void addJars(final IFile dir) throws IOException {
        dir.expectDirIsReadable();
        final File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                final File file = files[i];
                if (file.getName().toLowerCase().endsWith(".jar")) {
                    addUrl(file.toURI().toURL());
                }
            }
        }
    }

    /**
     * Dependency ClassLoaders are invoked before the parent is used for loading a Class.
     *
     * @param classLoader
     */
    public void addDependencyClassLoader(final ClassLoader classLoader) {
        childClassLoader.addDependencyClassLoader(classLoader);
    }

    /**
     * Dependency ClassLoaders are invoked before the parent is used for loading a Class.
     *
     * @param classLoaders
     */
    public void addDependencyClassLoaders(final Collection<ClassLoader> classLoaders) {
        childClassLoader.addDependencyClassLoaders(classLoaders);
    }
}
