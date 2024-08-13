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
package org.jboss.shrinkwrap.impl.base;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * SecurityActions
 * <p>
 * A set of privileged actions that are not to leak out of this package
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
final class SecurityActions {

    // -------------------------------------------------------------------------------||
    // Constructor ------------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    /**
     * No instantiation
     */
    private SecurityActions() {
        throw new UnsupportedOperationException("No instantiation");
    }

    // -------------------------------------------------------------------------------||
    // Utility Methods --------------------------------------------------------------||
    // -------------------------------------------------------------------------------||

    static Constructor<?>[] getConstructors(final Class<?> clazz) {
        return AccessController.doPrivileged((PrivilegedAction<Constructor<?>[]>) clazz::getConstructors);
    }

    /**
     * Obtains the Constructor specified from the given Class and argument types
     *
     * @param clazz
     *         The class from which to obtain the constructor
     * @param argumentTypes
     *         The types of the constructor arguments
     * @return The {@link Constructor} object corresponding to the requested constructor
     * @throws NoSuchMethodException
     */
    static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... argumentTypes)
        throws NoSuchMethodException {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<Constructor<?>>) () -> clazz.getConstructor(argumentTypes));
        }
        // Unwrap
        catch (final PrivilegedActionException pae) {
            final Throwable t = pae.getCause();
            // Rethrow
            if (t instanceof NoSuchMethodException) {
                throw (NoSuchMethodException) t;
            } else {
                // No other checked Exception thrown by Class.getConstructor
                try {
                    throw (RuntimeException) t;
                }
                // Just in case we've really messed up
                catch (final ClassCastException cce) {
                    throw new RuntimeException("Obtained unchecked Exception; this code should never be reached", t);
                }
            }
        }
    }
}
