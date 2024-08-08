/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.shrinkwrap.impl.base.path;

import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Base support for tests of {@link ArchivePath} implementations and factories
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class PathsTestBase {
    // -------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(PathsTestBase.class.getName());

    // -------------------------------------------------------------------------------------||
    // Contracts --------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Creates and returns a new {@link ArchivePath} with the specified context
     *
     * @param context
     *            Context to assign the Path
     * @throws IllegalArgumentException
     *             If the context is not specified
     */
    abstract ArchivePath createPath(final String context);

    /**
     * Creates and returns a new {@link ArchivePath} with the specified context and base
     *
     * @param base
     *            Parent context
     * @param context
     *            Context to assign the Path
     * @throws IllegalArgumentException
     *             If the context or base is not specified
     */
    abstract ArchivePath createPath(final ArchivePath base, final ArchivePath context);

    /**
     * Creates and returns a new {@link ArchivePath} with the specified context and base
     *
     * @param base
     *            Parent context
     * @param context
     *            Context to assign the Path
     * @throws IllegalArgumentException
     *             If the context or base is not specified
     */
    abstract ArchivePath createPath(final ArchivePath base, final String context);

    /**
     * Creates and returns a new {@link ArchivePath} with the specified context and base
     *
     * @param base
     *            Parent context
     * @param context
     *            Context to assign the Path
     * @throws IllegalArgumentException
     *             If the context or base is not specified
     */
    abstract ArchivePath createPath(final String base, final String context);

    /**
     * Creates and returns a new {@link ArchivePath} with the specified context and base
     *
     * @param base
     *            Parent context
     * @param context
     *            Context to assign the Path
     * @throws IllegalArgumentException
     *             If the context or base is not specified
     */
    abstract ArchivePath createPath(final String base, final ArchivePath context);

    // -------------------------------------------------------------------------------------||
    // Tests ------------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Ensures that a null context results in a root Path
     */
    @Test
    public void testNullDefaultsToRoot() {
        // Log
        log.info("testNullDefaultsToRoot");

        // Create a path with null context
        final ArchivePath path = this.createPath(null);

        // Ensure expected
        final String resolved = path.get();
        Assertions.assertEquals(String.valueOf(ArchivePath.SEPARATOR), resolved,
                "Null context should resolve to root path");
        log.info("null argument resolves to: " + path);
    }

    /**
     * Ensures that a relative path resolves to absoulte form in the single-arg ctor
     */
    @Test
    public void testRelativeResolvedToAbsolute() {
        // Log
        log.info("testRelativeResolvedToAbsolute");

        // Create a relative path
        final String relative = "relative";
        final ArchivePath path = this.createPath(relative);

        // Ensure expected
        final String resolved = path.get();
        final String expected = ArchivePath.SEPARATOR + relative;
        Assertions.assertEquals(expected, resolved, "Relative paths should resolve to absolute");
        log.info("\"" + relative + "\" resolves to: " + path);
    }

    /**
     * Ensures that an absolute directory path is preserved as-is
     */
    @Test
    public void testAbsoluteDirectoryContextPreserved() {
        // Log
        log.info("testAbsoluteDirectoryContextPreserved");

        // Create an absolute dir path
        final String absoluteDir = "/absoluteDir/";
        final ArchivePath path = this.createPath(absoluteDir);

        // Ensure expected
        Assertions.assertEquals(absoluteDir, path.get(), "Absolute directory contexts should be preserved");
        log.info("\"" + absoluteDir + "\" resolves to: " + path);
    }

    /**
     * Ensures that a new path may be created from a context under a specified base path
     */
    @Test
    public void testBasePathAndRelativeContext() {
        // Log
        log.info("testBasePathAndRelativeContext");

        // Create a base path
        final String base = "base";
        final ArchivePath basePath = this.createPath(base);

        // Create a new path using a relative context to the base
        final String context = "context";
        final ArchivePath contextPath = this.createPath(context);
        final ArchivePath path = this.createPath(basePath, contextPath);

        // Ensure expected
        Assertions.assertEquals(ArchivePath.SEPARATOR + base + ArchivePath.SEPARATOR + context, path.get(),
                "Context under base should resolve to relative");
        log.info("\"" + context + "\" under base " + basePath + " resolves to: " + path);
    }

    /**
     * Ensures that a new path may be created from a context (as String) under a specified base path
     */
    @Test
    public void testBasePathAndRelativeContextAsString() {
        // Log
        log.info("testBasePathAndRelativeContextAsString");

        // Create a base path
        final String base = "base";
        final ArchivePath basePath = this.createPath(base);

        // Create a new path using a relative context to the base
        final String context = "context";
        final ArchivePath path = this.createPath(basePath, context);

        // Ensure expected
        Assertions.assertEquals(ArchivePath.SEPARATOR + base + ArchivePath.SEPARATOR + context, path.get(),
                "Context under base should resolve to relative");
        log.info("\"" + context + "\" under base " + basePath + " resolves to: " + path);
    }

    /**
     * Ensures that a new path may be created from a context (as {@link ArchivePath}) under a specified base path (as
     * String)
     */
    @Test
    public void testBasePathAsStringAndRelativeContext() {
        // Log
        log.info("testBasePathAsStringAndRelativeContext");

        // Create a base path
        final String base = "base";

        // Create a new path using a relative context to the base
        final ArchivePath context = this.createPath("context");
        final ArchivePath path = this.createPath(base, context);

        // Ensure expected
        Assertions.assertEquals(ArchivePath.SEPARATOR + base + context.get(), path.get(),
                "Context under base should resolve to relative");
        log.info("\"" + context + "\" under base " + base + " resolves to: " + path);
    }

    /**
     * Ensures that a new path may be created from a context (as String) under a specified base path (which is
     * represented as a String)
     */
    @Test
    public void testBasePathAsStringAndRelativeContextAsString() {
        // Log
        log.info("testBasePathAsStringAndRelativeContextAsString");

        // Create a base path
        final String base = "base";

        // Create a new path using a relative context to the base
        final String context = "context";
        final ArchivePath path = this.createPath(base, context);

        // Ensure expected
        Assertions.assertEquals(ArchivePath.SEPARATOR + base + ArchivePath.SEPARATOR + context, path.get(),
                "Context under base should resolve to relative");
        log.info("\"" + context + "\" under base \"" + base + "\" resolves to: " + path);
    }

    /**
     * Ensures that Paths with equal contexts have equal hash codes
     */
    @Test
    public void testHashCode() {
        // Log
        log.info("testHashCode");

        // Create new paths
        final String context = "context";
        final ArchivePath path1 = this.createPath(context);
        final ArchivePath path2 = this.createPath(context);

        // Obtain hash
        final int hash1 = path1.hashCode();
        final int hash2 = path2.hashCode();

        // Ensure expected
        Assertions.assertEquals(hash1, hash2, "Paths with the same context should have equal hash codes");
        log.info("Both " + path1 + " and " + path2 + " have hashCode: " + hash1);
    }

    /**
     * Ensures that Paths with equal contexts are equal by value
     */
    @Test
    public void testEquals() {
        // Log
        log.info("testEquals");

        // Create new paths
        final String context = "context";
        final String contextWithFollowingSlash = context + ArchivePath.SEPARATOR;
        final ArchivePath path1 = this.createPath(context);
        final ArchivePath path2 = this.createPath(context);
        final ArchivePath pathWithFollowingSlash = this.createPath(contextWithFollowingSlash);

        // Ensure expected
        Assertions.assertEquals(path1, path2, "Paths with same context should be equal by value");
        Assertions.assertEquals(path1, pathWithFollowingSlash,
                "Paths with same context (regardless of following slash) should be equal by value");
        log.info(path1 + " equal by value to " + path2);
    }

    /**
     * Ensures that Paths with inequal contexts are equal by value
     */
    @Test
    public void testNotEqual() {
        // Log
        log.info("testEquals");

        // Create new paths
        final String context1 = "context1";
        final String context2 = "context2";
        final ArchivePath path1 = this.createPath(context1);
        final ArchivePath path2 = this.createPath(context2);

        // Ensure expected
        Assertions.assertNotEquals(path1, path2, "Paths with different contexts should not be equal by value");
        log.info(path1 + " not equal by value to " + path2);
    }
}
