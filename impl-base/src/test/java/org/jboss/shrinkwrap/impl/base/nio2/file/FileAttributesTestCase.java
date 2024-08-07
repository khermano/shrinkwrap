/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.shrinkwrap.impl.base.nio2.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Assert;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.nio2.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases to assert the ShrinkWrap implementation of the NIO.2 {@link BasicFileAttributes} is working as contracted.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class FileAttributesTestCase {

    private FileSystem fs;

    private JavaArchive archive;

    @Before
    public void createStore() throws IOException {
        // Setup
        final String name = "test.jar";
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, name);
        final FileSystem fs = ShrinkWrapFileSystems.newFileSystem(archive);

        // Set
        this.archive = archive;
        this.fs = fs;
    }

    @After
    public void closeFs() throws IOException {
        this.fs.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getLastModifiedTime() {
        this.getAttributes("path", true).lastModifiedTime();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void lastAccessTime() {
        this.getAttributes("path", true).lastAccessTime();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void creationTime() {
        this.getAttributes("path", true).creationTime();
    }

    @Test
    public void isRegularFile() {
        Assert.assertTrue(this.getAttributes("path", true).isRegularFile());
    }

    @Test
    public void isRegularFileFalse() {
        Assert.assertFalse(this.getAttributes("path/", true).isRegularFile());
    }

    @Test
    public void isDirectory() {
        Assert.assertTrue(this.getAttributes("path/", true).isDirectory());
    }

    @Test
    public void isDirectoryFalse() {
        Assert.assertFalse(this.getAttributes("path", true).isDirectory());
    }

    @Test
    public void isOther() {
        Assert.assertFalse(this.getAttributes("alwaysFalse", true).isOther());
    }

    @Test
    public void isSymbolicLink() {
        Assert.assertFalse(this.getAttributes("alwaysFalse", true).isSymbolicLink());
    }

    @Test
    public void size() {

        final int size = 1024;
        final Asset kiloAsset = () -> new ByteArrayInputStream(new byte[size]);
        final String path = "path";
        this.archive.add(kiloAsset, path);
        Assert.assertEquals("Size not reported as expected", size, this.getAttributes(path, false).size());
    }

    @Test
    public void fileKey() {
        final String path = "path";
        final String expected = this.archive.getId() + "/" + path;
        Assert.assertEquals("Filekey not as expected", expected, this.getAttributes(path, true).fileKey());
    }

    private ShrinkWrapFileAttributes getAttributes(final String pathName, boolean create) {
        String attributesFor = pathName;
        if (create) {
            if (pathName.endsWith("/")) {
                attributesFor = pathName.substring(0, pathName.length() - 1);
                archive.addAsDirectory(attributesFor);
            } else {
                archive.add(EmptyAsset.INSTANCE, pathName);
            }
        }
        try {
            return Files.readAttributes(this.fs.getPath(attributesFor), ShrinkWrapFileAttributes.class, (LinkOption) null);
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
