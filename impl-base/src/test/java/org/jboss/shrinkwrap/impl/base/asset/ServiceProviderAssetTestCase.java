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
package org.jboss.shrinkwrap.impl.base.asset;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.impl.base.io.IOUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * ServiceProviderAssetTestCase
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServiceProviderAssetTestCase {

    @Test
    public void shouldCreateServiceProviderFile() {
        Asset asset = new ServiceProviderAsset(TestImpl1.class, TestImpl2.class);

        byte[] expectedContent = (TestImpl1.class.getName() + "\n" + TestImpl2.class.getName() + "\n").getBytes();
        byte[] content = IOUtil.asByteArray(asset.openStream());

        Assertions.assertArrayEquals(expectedContent, content);
    }

    @Test
    public void shouldThrowExceptionOnNullArgumnet() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ServiceProviderAsset((Class<?>[]) null));
    }

    @Test
    public void shouldThrowExceptionOnNullArgumnetValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ServiceProviderAsset((Class<?>) null));
    }

    private interface TestIF {

    }

    private class TestImpl1 implements TestIF {

    }

    private class TestImpl2 implements TestIF {

    }
}
