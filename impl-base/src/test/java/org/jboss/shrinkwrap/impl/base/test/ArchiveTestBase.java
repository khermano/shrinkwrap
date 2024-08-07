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
package org.jboss.shrinkwrap.impl.base.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchiveFormat;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.IllegalArchivePathException;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.TestIOUtil;
import org.jboss.shrinkwrap.impl.base.Validate;
import org.jboss.shrinkwrap.impl.base.io.IOUtil;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.jboss.shrinkwrap.impl.base.test.handler.ReplaceAssetHandler;
import org.jboss.shrinkwrap.impl.base.test.handler.SimpleHandler;
import org.jboss.shrinkwrap.spi.ArchiveFormatAssociable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;


/**
 * ArchiveTestBase
 *
 * Base test for all Archive service providers to help ensure consistency between implementations.
 *
 * @author <a href="mailto:baileyje@gmail.com">John Bailey</a>
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class ArchiveTestBase<T extends Archive<T>> {
    // -------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Name of a properties file upon the test CP
     */
    public static final String NAME_TEST_PROPERTIES = "org/jboss/shrinkwrap/impl/base/asset/Test.properties";

    /**
     * Name of another properties file upon the test CP
     */
    public static final String NAME_TEST_PROPERTIES_2 = "org/jboss/shrinkwrap/impl/base/asset/Test2.properties";

    // -------------------------------------------------------------------------------------||
    // Instance Members -------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Get the {@link Archive} to test.
     *
     * @return A Archive<T> instance.
     */
    protected abstract T getArchive();

    /**
     * Create a new {@link Archive} instance. <br/>
     * Used to test Archive.add(Archive) type addings.
     *
     * @return A new Archive<T> instance.
     */
    protected abstract Archive<T> createNewArchive();

    protected abstract ArchiveFormat getExpectedArchiveFormat();

    // -------------------------------------------------------------------------------------||
    // Tests ------------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    @Test
    public void testDefaultArchiveFormatIsSet() {
        Assert.assertEquals("Unexpected default archive format", getExpectedArchiveFormat(), getDefaultArchiveFormat());
    }

    private ArchiveFormat getDefaultArchiveFormat() {
        return ((ArchiveFormatAssociable) getArchive()).getArchiveFormat();
    }

    /**
     * Simple printout of the tested archive.
     */
    @After
    public void ls() {
        Archive<T> archive = getArchive();
        System.out.println("test@jboss:/$ ls -l " + archive.getName());
        System.out.println(archive.toString(true));
    }

    /**
     * Ensure adding an asset to the path results in successful storage.
     *
     */
    @Test
    public void testAddAssetToPath() {
        Archive<T> archive = getArchive();
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        ArchivePath location = new BasicPath("/", "test.properties");

        archive.add(asset, location);

        Assert.assertTrue("Asset should be placed on " + location.get(), archive.contains(location));
    }

    /**
     * Ensure adding an asset to the path requires path.
     *
     */
    @Test
    public void testAddRequiresPath() {
        Archive<T> archive = getArchive();
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);

        try {
            archive.add(asset, (ArchivePath) null);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    /**
     * Ensure adding an asset to the path requires an asset.
     *
     */
    @Test
    public void testAddRequiresAssets() {
        Archive<T> archive = getArchive();
        try {
            archive.add((Asset) null, new BasicPath("/", "Test.properties"));
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    /**
     * Ensure adding an asset to a string path results in successful storage.
     *
     */
    @Test
    public void testAddWithStringPath() {
        Archive<T> archive = getArchive();
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        ArchivePath location = new BasicPath("/", "test.properties");

        archive.add(asset, location.get());

        Assert.assertTrue("Asset should be placed on " + new BasicPath("/", "test.properties"),
            archive.contains(location));
    }

    /**
     * Ensure adding an asset to a string path requires path.
     *
     */
    @Test
    public void testAddWithStringPathRequiresPath() {
        Archive<T> archive = getArchive();
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);

        try {
            archive.add(asset, (String) null);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    /**
     * Ensure adding an asset to the path string requires an asset.
     *
     */
    @Test
    public void testAddWithStringPathRequiresAssets() {
        Archive<T> archive = getArchive();
        try {
            archive.add((Asset) null, "/Test.properties");
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    /**
     * Ensure adding an asset with a name under an {@link ArchivePath} context results in successful storage
     *
     */
    @Test
    public void testAddAssetWithArchivePathAndName() {
        Archive<T> archive = getArchive();
        final String name = "test.properties";
        final Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        ArchivePath location = ArchivePaths.root();

        archive.add(asset, location, name);

        ArchivePath expectedPath = new BasicPath("/", "test.properties");

        Assert.assertTrue("Asset should be placed on " + expectedPath.get(), archive.contains(expectedPath));
    }

    /**
     * Ensure adding an asset with a name under an {@link String} context results in successful storage
     *
     */
    @Test
    public void testAddAssetWithStringPathAndName() {
        Archive<T> archive = getArchive();
        final String name = "test.properties";
        final Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);

        archive.add(asset, "/", name);

        ArchivePath expectedPath = new BasicPath("/", "test.properties");

        Assert.assertTrue("Asset should be placed on " + expectedPath.get(), archive.contains(expectedPath));
    }

    /**
     * Ensure adding an asset with name requires the path attribute as an {@link ArchivePath}.
     *
     */
    @Test
    public void testAddAssetWithNameRequiresArchivePath() {
        Archive<T> archive = getArchive();
        final String name = "test.properties";
        final Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        try {
            archive.add(asset, (ArchivePath) null, name);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    /**
     * Ensure adding an asset with name requires the path attribute as a String
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddAssetWithNameRequiresStringPath() {
        final Archive<T> archive = getArchive();
        archive.add(EmptyAsset.INSTANCE, (String) null, "childPath");
    }

    /**
     * Ensure adding an asset with name requires the name attribute
     *
     */
    @Test
    public void testAddAssetWithNameRequiresName() {
        Archive<T> archive = getArchive();
        final ArchivePath path = new BasicPath("/", "Test.properties");
        final String resource = NAME_TEST_PROPERTIES;
        try {
            archive.add(new ClassLoaderAsset(resource), path, null);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    /**
     * Ensure adding an asset with name requires the asset attribute
     *
     */
    @Test
    public void testAddAssetWithNameRequiresAsset() {
        Archive<T> archive = getArchive();
        final String name = "test.properties";
        final ArchivePath path = new BasicPath("/", "Test.properties");
        try {
            archive.add(null, path, name);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    @Test
    public void testAddNamedAsset() {
        Archive<T> archive = getArchive();
        final String testName = "check.properties";
        final Asset testAsset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);

        final NamedAsset namedAsset = new NamedAsset() {

            @Override
            public String getName() {
                return testName;
            }

            @Override
            public InputStream openStream() {
                return testAsset.openStream();
            }

        };

        archive.add(namedAsset);

        Assert.assertTrue("Asset should be placed on " + testName, archive.contains(testName));

    }

    /**
     * Tests that empty directories may be added to the archive
     *
     */
    @Test
    public void testAddEmptyDirectories() {
        Archive<T> archive = getArchive();

        // Get Paths to add
        final ArchivePath path1 = ArchivePaths.create("path/to/dir");
        final ArchivePath path2 = ArchivePaths.create("path/to/dir2");
        final ArchivePath path3 = ArchivePaths.create("path/to");

        // Add
        archive.addAsDirectories(path1, path2, path3);

        // Test
        final String message = "Should be able to add directory: ";
        Assert.assertTrue(message + path1, archive.contains(path1));
        Assert.assertTrue(message + path2, archive.contains(path2));
        Assert.assertTrue(message + path3, archive.contains(path3));
    }

    @Test
    public void testHandlerIsCalledWhenAddingDirectoriesWithArchivePath() {
        final SimpleHandler simpleHandler1 = new SimpleHandler();
        final SimpleHandler simpleHandler2 = new SimpleHandler();
        getArchive().addHandlers(simpleHandler1, simpleHandler2);

        getArchive().addAsDirectories(ArchivePaths.create("/path/to/dir1"));

        Assert.assertTrue("Handler not called", simpleHandler1.called);
        Assert.assertTrue("Handler not called", simpleHandler2.called);
    }

    @Test
    public void testHandlerIsCalledWhenAddingDirectoriesWithStringPath() {
        final SimpleHandler simpleHandler1 = new SimpleHandler();
        final SimpleHandler simpleHandler2 = new SimpleHandler();
        getArchive().addHandlers(simpleHandler1, simpleHandler2);

        getArchive().addAsDirectories("/path/to/dir1");

        Assert.assertTrue("Handler not called", simpleHandler1.called);
        Assert.assertTrue("Handler not called", simpleHandler2.called);
    }

    @Test
    public void testHandlerIsCalledWhenAddingAssetWithArchivePath() throws Exception {
        final ReplaceAssetHandler handler1 = new ReplaceAssetHandler("unexpected");
        final ReplaceAssetHandler handler2 = new ReplaceAssetHandler("EXPECTED");
        getArchive().addHandlers(handler1, handler2);

        final ArchivePath path = ArchivePaths.create("/path/to/dir/test1.txt");
        final StringAsset asset = new StringAsset("Asset content");
        getArchive().add(asset, path);

        Assert.assertEquals("Handler not called", "EXPECTED", readStringAsset(path));
        Assert.assertEquals("Wrong asset received by handler", asset, handler1.savedAsset);
        Assert.assertEquals("Wrong asset received by handler", handler1.returnedAsset, handler2.savedAsset);
    }

    @Test
    public void testHandlerIsCalledWhenAddingAssetWithArchivePathAndName() throws Exception {
        final ReplaceAssetHandler handler1 = new ReplaceAssetHandler("unexpected");
        final ReplaceAssetHandler handler2 = new ReplaceAssetHandler("EXPECTED");
        getArchive().addHandlers(handler1, handler2);

        final ArchivePath path = ArchivePaths.create("/path/to/dir");
        final StringAsset asset = new StringAsset("Original");
        getArchive().add(asset, path, "asset.txt");

        String actual = readStringAsset(ArchivePaths.create(path, "/asset.txt"));
        Assert.assertEquals("Handler not called", "EXPECTED", actual);
        Assert.assertEquals("Wrong asset received by handler", asset, handler1.savedAsset);
        Assert.assertEquals("Wrong asset received by handler", handler1.returnedAsset, handler2.savedAsset);    }

    @Test
    public void testHandlerIsCalledWhenAddingAssetWithtStringPathAndName() throws Exception {
        final ReplaceAssetHandler handler1 = new ReplaceAssetHandler("unexpected");
        final ReplaceAssetHandler handler2 = new ReplaceAssetHandler("EXPECTED");
        getArchive().addHandlers(handler1, handler2);

        final ArchivePath path = ArchivePaths.create("/path/to/dir");
        final StringAsset asset = new StringAsset("Original");
        getArchive().add(asset, path.get(), "asset.txt");

        String actual = readStringAsset(ArchivePaths.create(path, "asset.txt"));
        Assert.assertEquals("Handler not called", "EXPECTED", actual);
        Assert.assertEquals("Wrong asset received by handler", asset, handler1.savedAsset);
        Assert.assertEquals("Wrong asset received by handler", handler1.returnedAsset, handler2.savedAsset);
    }

    @Test
    public void testHandlerIsCalledWhenAddingAssetWithStringPath() throws Exception {
        final ReplaceAssetHandler handler1 = new ReplaceAssetHandler("unexpected");
        final ReplaceAssetHandler handler2 = new ReplaceAssetHandler("EXPECTED");
        getArchive().addHandlers(handler1, handler2);

        final ArchivePath path = ArchivePaths.create("/path/to/dir/test1.txt");
        final StringAsset asset = new StringAsset("Original");
        getArchive().add(asset, path.get());

        Assert.assertEquals("Handler not called", "EXPECTED", readStringAsset(path));
        Assert.assertEquals("Wrong asset received by handler", asset, handler1.savedAsset);
        Assert.assertEquals("Wrong asset received by handler", handler1.returnedAsset, handler2.savedAsset);
    }

    @Test
    public void testHandlerIsCalledWhenAddingAssetWithArchivePathAndExporter() {
        final ReplaceAssetHandler handler1 = new ReplaceAssetHandler("unexpected");
        final ReplaceAssetHandler handler2 = new ReplaceAssetHandler("EXPECTED");
        getArchive().addHandlers(handler1, handler2);

        final ArchivePath path = ArchivePaths.create("/path/to/dir");
        final Archive<JavaArchive> asset = ShrinkWrap
              .create(JavaArchive.class, "asset.zip")
              .add(new StringAsset("asset content"), "content.txt");
        getArchive().add(asset, path, ZipExporter.class);

        Assert.assertTrue("Handler not called", handler1.called);
        Assert.assertEquals("Wrong asset received by handler", handler1.returnedAsset, handler2.savedAsset);
    }

    /**
     * Ensures that {@link Archive#contains(String)} works as expected
     */
    @Test
    public void testContainsPathAsString() {
        final Archive<T> archive = getArchive();
        final String path = "testpath";
        archive.add(EmptyAsset.INSTANCE, path);
        Assert.assertTrue("Archive should contain the path added", archive.contains(path));
    }

    /**
     * Ensures that {@link Archive#contains(ArchivePath)} works as expected
     */
    @Test
    public void testContainsPathAsArchivePath() {
        final Archive<T> archive = getArchive();
        final ArchivePath path = ArchivePaths.create("testpath");
        archive.add(EmptyAsset.INSTANCE, path);
        Assert.assertTrue("Archive should contain the path added", archive.contains(path));
    }

    /**
     * Ensure deleting an asset successfully removes asset from storage
     *
     */
    @Test
    public void testDeleteAssetWithArchivePath() {
        Archive<T> archive = getArchive();
        String resource = NAME_TEST_PROPERTIES;
        ArchivePath location = new BasicPath("/", "test.properties");
        final Asset asset = new ClassLoaderAsset(resource);
        archive.add(asset, location);
        Assert.assertTrue(archive.contains(location)); // Sanity check

        Assert.assertEquals("Successfully deleting an Asset should return the removed Node", asset,
            archive.delete(location).getAsset());

        Assert.assertFalse("There should no longer be an asset at: " + location.get() + " after deleted",
            archive.contains(location));
    }

    /**
     * Ensure deleting an asset successfully removes asset from storage
     *
     */
    @Test
    public void testDeleteAssetWithStringPath() {
        Archive<T> archive = getArchive();
        String resource = NAME_TEST_PROPERTIES;
        String location = "/test.properties";
        final Asset asset = new ClassLoaderAsset(resource);
        archive.add(asset, location);
        Assert.assertTrue(archive.contains(location)); // Sanity check

        Assert.assertEquals("Successfully deleting an Asset should return the removed Node", asset,
            archive.delete(location).getAsset());

        Assert.assertFalse("There should no longer be an asset at: " + location + " after deleted",
            archive.contains(location));
    }

    /**
     * Ensure deleting a missing asset returns correct status
     *
     */
    @Test
    public void testDeleteMissingAsset() {
        Archive<T> archive = getArchive();
        ArchivePath location = new BasicPath("/", "test.properties");

        Assert.assertNull("Deleting a non-existent Asset should return null", archive.delete(location));
    }

    /**
     * Ensure deleting a missing asset returns correct status
     *
     */
    @Test
    public void testDeleteMissingAssetWithStringPath() {
        Archive<T> archive = getArchive();
        String location = "/test.properties";

        Assert.assertNull("Deleting a non-existent Asset should return null", archive.delete(location));
    }

    /**
     * Ensure deleting an asset requires a path
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteAssetRequiresArchivePath() {
        Archive<T> archive = getArchive();
        archive.delete((ArchivePath) null);
        Assert.fail("Should have throw an IllegalArgumentException");
    }

    /**
     * Ensure deleting an asset requires a path
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteAssetRequiresStringPath() {
        Archive<T> archive = getArchive();
        archive.delete((String) null);
        Assert.fail("Should have throw an IllegalArgumentException");
    }

    /**
     * Delete directory which contains children.
     */
    @Test
    public void testDeletePathWithChildren() {
        // given
        final Archive<T> archive = getArchive();
        final String dirName = "dir";
        archive.addAsDirectories(dirName);
        archive.add(new StringAsset("asset"), dirName, "abc.txt").add(new StringAsset("asset"), dirName, "cde.txt");
        archive.add(new StringAsset("other"), "other.txt");

        // when
        archive.delete("dir");

        // then
        Assert.assertFalse(archive.contains(dirName));
        Assert.assertEquals(1, archive.getContent().size());
    }

    /**
     * Ensure an asset can be retrieved by its path
     *
     */
    @Test
    public void testGetAsset() {
        Archive<T> archive = getArchive();
        ArchivePath location = new BasicPath("/", "test.properties");
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        archive.add(asset, location);

        Node fetchedNode = archive.get(location);

        Assert.assertTrue("Asset should be returned from path: " + location.get(),
            compareAssets(asset, fetchedNode.getAsset()));
    }

    /**
     * Ensure get asset requires a path
     *
     */
    @Test
    public void testGetAssetRequiresPath() {
        Archive<T> archive = getArchive();
        try {
            archive.get((ArchivePath) null);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    /**
     * Ensure an asset can be retrieved by a string path
     *
     */
    @Test
    public void testGetAssetWithString() {
        Archive<T> archive = getArchive();
        ArchivePath location = new BasicPath("/", "test.properties");
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        archive.add(asset, location);

        Node fetchedNode = archive.get(location.get());

        Assert.assertTrue("Asset should be returned from path: " + location.get(),
            compareAssets(asset, fetchedNode.getAsset()));
    }

    /**
     * Ensure get asset by string requires a path
     *
     */
    @Test
    public void testGetAssetWithStringRequiresPath() {
        Archive<T> archive = getArchive();
        try {
            archive.get((String) null);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    @Test
    public void testImportArchiveAsTypeFromString() throws Exception {
        String resourcePath = "/test/cl-test.jar";
        GenericArchive archive = ShrinkWrap.create(GenericArchive.class).add(
            new FileAsset(TestIOUtil.createFileFromResourceName("cl-test.jar")), resourcePath);

        JavaArchive jar = archive.getAsType(JavaArchive.class, resourcePath, ArchiveFormat.ZIP).add(
            new StringAsset("test file content"), "test.txt");

        Assert.assertEquals("JAR imported with wrong name", resourcePath, jar.getName());
        Assert.assertNotNull("Class in JAR not imported", jar.get("test/classloader/DummyClass.class"));
        Assert.assertNotNull("Inner Class in JAR not imported",
            jar.get("test/classloader/DummyClass$DummyInnerClass.class"));
        Assert.assertNotNull("Should contain a new asset", ((ArchiveAsset) archive.get(resourcePath).getAsset())
            .getArchive().get("test.txt"));
    }

    @Test
    public void testImportArchiveAsTypeFromStringUsingDefaultFormat() throws Exception {
        String resourcePath = "/test/cl-test.jar";
        GenericArchive archive = ShrinkWrap.create(GenericArchive.class).add(
            new FileAsset(TestIOUtil.createFileFromResourceName("cl-test.jar")), resourcePath);

        JavaArchive jar = archive.getAsType(JavaArchive.class, resourcePath).add(new StringAsset("test file content"),
            "test.txt");

        Assert.assertEquals("JAR imported with wrong name", resourcePath, jar.getName());
        Assert.assertNotNull("Class in JAR not imported", jar.get("test/classloader/DummyClass.class"));
        Assert.assertNotNull("Inner Class in JAR not imported",
            jar.get("test/classloader/DummyClass$DummyInnerClass.class"));
        Assert.assertNotNull("Should contain a new asset", ((ArchiveAsset) archive.get(resourcePath).getAsset())
            .getArchive().get("test.txt"));
    }

    @Test
    public void testImportArchiveAsTypeFromArchivePath() throws Exception {
        String resourcePath = "/test/cl-test.jar";
        GenericArchive archive = ShrinkWrap.create(GenericArchive.class).add(
            new FileAsset(TestIOUtil.createFileFromResourceName("cl-test.jar")), resourcePath);

        JavaArchive jar = archive.getAsType(JavaArchive.class, ArchivePaths.create(resourcePath), ArchiveFormat.ZIP)
            .add(new StringAsset("test file content"), "test.txt");

        Assert.assertEquals("JAR imported with wrong name", resourcePath, jar.getName());
        Assert.assertNotNull("Class in JAR not imported", jar.get("test/classloader/DummyClass.class"));
        Assert.assertNotNull("Inner Class in JAR not imported",
            jar.get("test/classloader/DummyClass$DummyInnerClass.class"));
        Assert.assertNotNull("Should contain an archive asset", ((ArchiveAsset) archive.get(resourcePath).getAsset())
            .getArchive().get("test.txt"));
    }

    @Test
    public void testImportArchiveAsTypeFromArchivePathUsingDefaultFormat() throws Exception {
        String resourcePath = "/test/cl-test.jar";
        GenericArchive archive = ShrinkWrap.create(GenericArchive.class).add(
            new FileAsset(TestIOUtil.createFileFromResourceName("cl-test.jar")), resourcePath);

        JavaArchive jar = archive.getAsType(JavaArchive.class, ArchivePaths.create(resourcePath)).add(
            new StringAsset("test file content"), "test.txt");

        Assert.assertEquals("JAR imported with wrong name", resourcePath, jar.getName());
        Assert.assertNotNull("Class in JAR not imported", jar.get("test/classloader/DummyClass.class"));
        Assert.assertNotNull("Inner Class in JAR not imported",
            jar.get("test/classloader/DummyClass$DummyInnerClass.class"));
        Assert.assertNotNull("Should contain an archive asset", ((ArchiveAsset) archive.get(resourcePath).getAsset())
            .getArchive().get("test.txt"));
    }

    @Test
    public void testImportArchiveAsTypeFromFilter() throws Exception {
        String resourcePath = "/test/cl-test.jar";
        GenericArchive archive = ShrinkWrap.create(GenericArchive.class).add(
            new FileAsset(TestIOUtil.createFileFromResourceName("cl-test.jar")), resourcePath);

        Collection<JavaArchive> jars = archive
            .getAsType(JavaArchive.class, Filters.include(".*jar"), ArchiveFormat.ZIP);

        Assert.assertEquals("Unexpected result found", 1, jars.size());

        JavaArchive jar = jars.iterator().next().add(new StringAsset("test file content"), "test.txt");

        Assert.assertEquals("JAR imported with wrong name", resourcePath, jar.getName());
        Assert.assertNotNull("Class in JAR not imported", jar.get("test/classloader/DummyClass.class"));
        Assert.assertNotNull("Inner Class in JAR not imported",
            jar.get("test/classloader/DummyClass$DummyInnerClass.class"));
        Assert.assertNotNull("Should contain a new asset", ((ArchiveAsset) archive.get(resourcePath).getAsset())
            .getArchive().get("test.txt"));
    }

    @Test
    public void testImportArchiveAsTypeFromFilterUsingDefaultFormat() throws Exception {
        String resourcePath = "/test/cl-test.jar";
        GenericArchive archive = ShrinkWrap.create(GenericArchive.class).add(
            new FileAsset(TestIOUtil.createFileFromResourceName("cl-test.jar")), resourcePath);

        Collection<JavaArchive> jars = archive.getAsType(JavaArchive.class, Filters.include(".*jar"));

        Assert.assertEquals("Unexpected result found", 1, jars.size());

        JavaArchive jar = jars.iterator().next().add(new StringAsset("test file content"), "test.txt");

        Assert.assertEquals("JAR imported with wrong name", resourcePath, jar.getName());
        Assert.assertNotNull("Class in JAR not imported", jar.get("test/classloader/DummyClass.class"));
        Assert.assertNotNull("Inner Class in JAR not imported",
            jar.get("test/classloader/DummyClass$DummyInnerClass.class"));
        Assert.assertNotNull("Should contain a new asset", ((ArchiveAsset) archive.get(resourcePath).getAsset())
            .getArchive().get("test.txt"));
    }

    @Test
    public void testFilter() throws Exception {

        String resourcePath = "/test/cl-test.jar";
        GenericArchive archive = ShrinkWrap.create(ZipImporter.class).importFrom(
            TestIOUtil.createFileFromResourceName("cl-test.jar")).as(GenericArchive.class);

        GenericArchive filtered = archive.filter(Filters.include(".*MANIFEST\\.MF"));
        // Check that only META-INF/MANIFEST.MF exist in Archive
        Assert.assertEquals(2, filtered.getContent().size());
        Assert.assertTrue(filtered.contains(ArchivePaths.create("META-INF/MANIFEST.MF")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImportArchiveFromStringThrowExceptionIfClassIsNull() {
        ShrinkWrap.create(GenericArchive.class).getAsType((Class<GenericArchive>) null, "/path", ArchiveFormat.ZIP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImportArchiveFromStringThrowExceptionIfPathIsNull() {
        ShrinkWrap.create(GenericArchive.class).getAsType(JavaArchive.class, (String) null, ArchiveFormat.ZIP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImportArchiveFromStringThrowExceptionIfFormatIsNull() {
        ShrinkWrap.create(GenericArchive.class).getAsType(JavaArchive.class, "/path", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImportArchiveFromArchivePathThrowExceptionIfClassIsNull() {
        ShrinkWrap.create(GenericArchive.class).getAsType((Class<GenericArchive>) null, ArchivePaths.create("/path"),
            ArchiveFormat.ZIP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImportArchiveFromArchivePathThrowExceptionIfPathIsNull() {
        ShrinkWrap.create(GenericArchive.class).getAsType(JavaArchive.class, (ArchivePath) null, ArchiveFormat.ZIP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImportArchiveFromArchivePathThrowExceptionIfFormatIsNull() {
        ShrinkWrap.create(GenericArchive.class).getAsType(JavaArchive.class, ArchivePaths.create("/path"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImportArchiveFromFilterThrowExceptionIfClassIsNull() {
        ShrinkWrap.create(GenericArchive.class).getAsType((Class<JavaArchive>) null, Filters.includeAll(),
            ArchiveFormat.ZIP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImportArchiveFromFilterThrowExceptionIfPathIsNull() {
        ShrinkWrap.create(GenericArchive.class).getAsType(JavaArchive.class, (Filter<ArchivePath>) null,
            ArchiveFormat.ZIP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImportArchiveFromFilterThrowExceptionIfFormatIsNull() {
        ShrinkWrap.create(GenericArchive.class).getAsType(JavaArchive.class, Filters.includeAll(), null);
    }

    /**
     * Ensure we can get a added Archive as a specific type
     *
     */
    @Test
    public void testGetAsTypeString() {
        Archive<?> archive = getArchive();
        GenericArchive child = ShrinkWrap.create(GenericArchive.class);
        archive.add(child, "/", ZipExporter.class);

        GenericArchive found = archive.getAsType(GenericArchive.class, child.getName());

        Assert.assertNotNull(found);
    }

    /**
     * Ensure we can get a added Archive as a specific type
     *
     */
    @Test
    public void testGetAsTypeArchivePath() {
        Archive<?> archive = getArchive();
        GenericArchive child = ShrinkWrap.create(GenericArchive.class);
        archive.add(child, "/", ZipExporter.class);

        GenericArchive found = archive.getAsType(GenericArchive.class, ArchivePaths.create(child.getName()));

        Assert.assertNotNull(found);
    }

    /**
     * Ensure we can get a added Archive as a specific type
     *
     */
    @Test
    public void testGetAsTypeWithFilter() {
        GenericArchive child1 = ShrinkWrap.create(GenericArchive.class);
        GenericArchive child2 = ShrinkWrap.create(GenericArchive.class);
        // Create one not to be found by filter.
        GenericArchive child3 = ShrinkWrap.create(GenericArchive.class, "SHOULD_NOT_BE_FOUND.xxx");

        Archive<?> archive = getArchive().add(child1, "/", ZipExporter.class).add(child2, "/", ZipExporter.class)
            .add(child3, "/", ZipExporter.class);

        Collection<GenericArchive> matches = archive.getAsType(GenericArchive.class, Filters.include(".*\\.jar"));

        Assert.assertNotNull(matches);
        Assert.assertEquals("Two archives should be found", 2, matches.size());

        for (GenericArchive match : matches) {
            if (!match.getName().equals(child1.getName()) && !match.getName().equals(child2.getName())) {
                Assert.fail("Wrong archive found, " + match.getName() + ". Expected " + child1.getName() + " or "
                    + child2.getName());
            }
        }
    }

    /**
     * Ensure get content returns the correct map of content
     *
     */
    @Test
    public void testToGetContent() {
        Archive<T> archive = getArchive();
        ArchivePath location = new BasicPath("/", "test.properties");
        ArchivePath locationTwo = new BasicPath("/", "test2.properties");

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        Asset assetTwo = new ClassLoaderAsset(NAME_TEST_PROPERTIES_2);
        archive.add(asset, location).add(assetTwo, locationTwo);

        Map<ArchivePath, Node> content = archive.getContent();

        final Node node1 = content.get(location);
        final Node node2 = content.get(locationTwo);

        Assert.assertTrue("Asset should existing in content with key: " + location.get(),
            this.compareAssets(asset, node1.getAsset()));

        Assert.assertTrue("Asset should existing in content with key: " + locationTwo.get(),
            this.compareAssets(assetTwo, node2.getAsset()));
    }

    /**
     * Ensure get content returns the correct map of content based on the given filter
     *
     */
    @Test
    public void testToGetContentFiltered() {
        Archive<T> archive = getArchive();
        ArchivePath location = new BasicPath("/", "test.properties");
        ArchivePath locationTwo = new BasicPath("/", "test2.properties");

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        Asset assetTwo = new ClassLoaderAsset(NAME_TEST_PROPERTIES_2);
        archive.add(asset, location).add(assetTwo, locationTwo);

        Map<ArchivePath, Node> content = archive.getContent(Filters.include(".*test2.*"));

        final Node node1 = content.get(location);
        final Node node2 = content.get(locationTwo);

        Assert.assertEquals("Only 1 Asset should have been included", 1, content.size());
        Assert.assertNull("Should not be included in content", node1);

        Assert.assertNotNull("Should be included in content", node2);
    }

    /**
     * Ensure adding an archive to a path requires a path
     *
     */
    @Test
    public void testAddArchiveToPathRequireArchivePath() {
        Archive<T> archive = getArchive();
        try {
            archive.add(ShrinkWrap.create(JavaArchive.class), (ArchivePath) null, ZipExporter.class);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    /**
     * Ensure adding an archive to a path requires a path
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddArchiveToPathRequireStringPath() {
        Archive<T> archive = getArchive();
        archive.add(ShrinkWrap.create(JavaArchive.class), (String) null, ZipExporter.class);
    }

    /**
     * Ensure adding an archive to a path requires an archive
     *
     */
    @Test
    public void testAddArchiveToPathRequireArchive() {
        Archive<T> archive = getArchive();
        try {
            archive.add((Archive<?>) null, ArchivePaths.root(), ZipExporter.class);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    /**
     * Ensure that trying to add an asset on an illegal path throws an Exception
     *
     */
    @Test(expected = IllegalArchivePathException.class)
    public void shouldNotBeAbleToAddAssetOnIllegalPath() {
        Archive<T> archive = getArchive();

        // add an asset
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        ArchivePath location = new BasicPath("/", "test.properties");
        archive.add(asset, location);

        // try to add an asset on an illegal path
        Asset assetTwo = new ClassLoaderAsset(NAME_TEST_PROPERTIES_2);
        ArchivePath locationTwo = ArchivePaths.create("/test.properties/somewhere");
        archive.add(assetTwo, locationTwo);

    }

    /**
     * Ensure that trying to add a directory on an illegal path throws an Exception
     *
     */
    @Test(expected = IllegalArchivePathException.class)
    public void shouldNotBeAbleToAddDirectoryOnIllegalPath() {
        Archive<T> archive = getArchive();

        // add an asset
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        ArchivePath location = new BasicPath("/somewhere/test.properties");
        archive.add(asset, location);

        // try to add a directory on an illegal path
        archive.addAsDirectory("/somewhere/test.properties/test");

    }

    /**
     * Ensure merging content requires a source archive
     *
     */
    @Test
    public void testMergeRequiresSource() {
        Archive<T> archive = getArchive();
        try {
            archive.merge(null);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }

    /**
     * Ensure merging content from another archive successfully stores all assets
     *
     */
    @Test
    public void testMerge() {
        Archive<T> archive = getArchive();
        Archive<T> sourceArchive = createNewArchive();
        ArchivePath location = new BasicPath("/", "test.properties");
        ArchivePath locationTwo = new BasicPath("/", "test2.properties");

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        Asset assetTwo = new ClassLoaderAsset(NAME_TEST_PROPERTIES_2);
        sourceArchive.add(asset, location).add(assetTwo, locationTwo);

        archive.merge(sourceArchive);

        Node node1 = archive.get(location);
        Node node2 = archive.get(locationTwo);

        Assert.assertTrue("Asset should have been added to path: " + location.get(),
            this.compareAssets(node1.getAsset(), asset));

        Assert.assertTrue("Asset should have been added to path: " + location.get(),
            this.compareAssets(node2.getAsset(), assetTwo));
    }

    /**
     * Ensure merging content from another archive to a path successfully stores all assets to specific path
     *
     */
    @Test
    public void testMergeToPath() {
        Archive<T> archive = getArchive();
        Archive<T> sourceArchive = createNewArchive();
        ArchivePath location = new BasicPath("/", "test.properties");
        ArchivePath locationTwo = new BasicPath("/", "test2.properties");

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        Asset assetTwo = new ClassLoaderAsset(NAME_TEST_PROPERTIES_2);
        sourceArchive.add(asset, location).add(assetTwo, locationTwo);

        ArchivePath baseLocation = new BasicPath("somewhere");

        archive.merge(sourceArchive, baseLocation);

        ArchivePath expectedPath = new BasicPath(baseLocation, location);
        ArchivePath expectedPathTwo = new BasicPath(baseLocation, locationTwo);

        Node nodeOne = archive.get(expectedPath);
        Node nodeTwo = archive.get(expectedPathTwo);

        Assert.assertTrue("Asset should have been added to path: " + expectedPath.get(),
            this.compareAssets(nodeOne.getAsset(), asset));

        Assert.assertTrue("Asset should have been added to path: " + expectedPathTwo.getClass(),
            this.compareAssets(nodeTwo.getAsset(), assetTwo));
    }

    /**
     * Ensure merging content from another archive to a path successfully stores all assets to specific path
     *
     */
    @Test
    public void testMergeToStringPath() {
        Archive<T> archive = getArchive();
        Archive<T> sourceArchive = createNewArchive();
        ArchivePath location = new BasicPath("/", "test.properties");
        ArchivePath locationTwo = new BasicPath("/", "test2.properties");

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        Asset assetTwo = new ClassLoaderAsset(NAME_TEST_PROPERTIES_2);
        sourceArchive.add(asset, location).add(assetTwo, locationTwo);

        String baseLocation = "somewhere";

        archive.merge(sourceArchive, baseLocation);

        ArchivePath expectedPath = new BasicPath(baseLocation, location);
        ArchivePath expectedPathTwo = new BasicPath(baseLocation, locationTwo);

        Node nodeOne = archive.get(expectedPath);
        Node nodeTwo = archive.get(expectedPathTwo);

        Assert.assertTrue("Asset should have been added to path: " + expectedPath.get(),
            this.compareAssets(nodeOne.getAsset(), asset));

        Assert.assertTrue("Asset should have been added to path: " + expectedPathTwo.getClass(),
            this.compareAssets(nodeTwo.getAsset(), assetTwo));
    }

    /**
     * Ensure that the filter is used when merging.
     *
     */
    @Test
    public void testMergeToPathWithFilter() {
        Archive<?> archive = getArchive();
        Archive<T> sourceArchive = createNewArchive();
        ArchivePath location = new BasicPath("/", "test.properties");
        ArchivePath locationTwo = new BasicPath("/", "test2.properties");

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        Asset assetTwo = new ClassLoaderAsset(NAME_TEST_PROPERTIES_2);
        sourceArchive.add(asset, location).add(assetTwo, locationTwo);

        ArchivePath baseLocation = new BasicPath("somewhere");

        archive.merge(sourceArchive, baseLocation, Filters.include(".*test2.*"));

        Assert.assertEquals("Should only have merged 1", 1, numAssets(archive));

        ArchivePath expectedPath = new BasicPath(baseLocation, locationTwo);

        Assert.assertTrue("Asset should have been added to path: " + expectedPath.get(),
            this.compareAssets(archive.get(expectedPath).getAsset(), asset));
    }

    /**
     * Ensure that the filter is used when merging.
     *
     */
    @Test
    public void testMergeToStringPathWithFilter() {
        Archive<?> archive = getArchive();
        Archive<T> sourceArchive = createNewArchive();
        ArchivePath location = new BasicPath("/", "test.properties");
        ArchivePath locationTwo = new BasicPath("/", "test2.properties");

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        Asset assetTwo = new ClassLoaderAsset(NAME_TEST_PROPERTIES_2);
        sourceArchive.add(asset, location).add(assetTwo, locationTwo);

        String baseLocation = "somewhere";

        archive.merge(sourceArchive, baseLocation, Filters.include(".*test2.*"));

        Assert.assertEquals("Should only have merged 1", 1, numAssets(archive));

        ArchivePath expectedPath = new BasicPath(baseLocation, locationTwo);

        Assert.assertTrue("Asset should have been added to path: " + expectedPath.get(),
            this.compareAssets(archive.get(expectedPath).getAsset(), asset));
    }

    /**
     * Ensure that the filter is used when merging.
     *
     */
    @Test
    public void testMergeWithFilter() {
        Archive<?> archive = getArchive();
        Archive<T> sourceArchive = createNewArchive();
        ArchivePath location = new BasicPath("/", "test.properties");
        ArchivePath locationTwo = new BasicPath("/", "test2.properties");

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        Asset assetTwo = new ClassLoaderAsset(NAME_TEST_PROPERTIES_2);
        sourceArchive.add(asset, location).add(assetTwo, locationTwo);

        archive.merge(sourceArchive, Filters.include(".*test2.*"));

        Assert.assertEquals("Should only have merged 1", 1, numAssets(archive));

        Assert.assertTrue("Asset should have been added to path: " + locationTwo.get(),
            this.compareAssets(archive.get(locationTwo).getAsset(), asset));
    }

    /**
     * Ensure merging content from another archive requires a path
     *
     */
    @Test
    public void testMergeToPathRequiresPath() {
        Archive<T> archive = getArchive();
        try {
            archive.merge(createNewArchive(), (ArchivePath) null);
            Assert.fail("Should have throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // IAE is expected, passing test
        }
    }
    
    /**
     * Tests merging of two archives containing directories with same names.
     */
    @Test
    public void testMergeWithDirectories() {
        Archive<?> archive = getArchive();
        Archive<T> sourceArchive = createNewArchive();

        ArchivePath location = new BasicPath("/dir/", "test.properties");
        ArchivePath locationTwo = new BasicPath("/dir/", "test2.properties");

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        Asset assetTwo = new ClassLoaderAsset(NAME_TEST_PROPERTIES_2);

        sourceArchive.add(assetTwo, locationTwo);

        archive.add(asset, location);

        archive.merge(sourceArchive);

        Assert.assertTrue("Archive should contain given element", archive.contains("/dir/test.properties"));
        Assert.assertTrue("Archive should contain given element", archive.contains("/dir/test2.properties"));

        // content might be valid, but asset might not be in Node tree
        Assert.assertEquals("Archive children count is invalid", 2, countChildren(archive));
    }

    private int countChildren(final Archive<?> archive) {
        final Node node = archive.get("/");
        return countChildren(node);
    }

    private int countChildren(final Node node) {
        int count = 0;
        for (final Node child : node.getChildren()) {
            if (child.getAsset() != null) {
                count++;
            }
            count += countChildren(child);
        }
        return count;
    }
    
    /**
     * Ensure adding an archive to a path successfully stores all assets to specific path including the archive name
     *
     */
    @Test
    public void testAddArchiveToPath() {
        Archive<T> archive = getArchive();
        Archive<T> sourceArchive = createNewArchive();

        ArchivePath baseLocation = new BasicPath("somewhere");

        archive.add(sourceArchive, baseLocation, ZipExporter.class);

        ArchivePath expectedPath = new BasicPath(baseLocation, sourceArchive.getName());

        Node node = archive.get(expectedPath);
        Assert.assertNotNull("Asset should have been added to path: " + expectedPath.get(), node);
        Assert.assertTrue("An instance of ArchiveAsset should have been added to path: " + expectedPath.get(),
            node.getAsset() instanceof ArchiveAsset);
        ArchiveAsset archiveAsset = ArchiveAsset.class.cast(node.getAsset());

        Archive<?> nestedArchive = archiveAsset.getArchive();
        Assert.assertEquals("Nested Archive should be same archive that was added", sourceArchive, nestedArchive);

    }

    /**
     * Ensure an archive contains assets from nested archives.
     *
     */
    @Test
    public void testNestedArchiveContains() {
        Archive<T> archive = getArchive();

        Archive<T> sourceArchive = createNewArchive();

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);

        ArchivePath nestedAssetPath = new BasicPath("/", "test.properties");

        sourceArchive.add(asset, nestedAssetPath);

        ArchivePath baseLocation = new BasicPath("somewhere");

        archive.add(sourceArchive, baseLocation, ZipExporter.class);

        ArchivePath archivePath = new BasicPath(baseLocation, sourceArchive.getName());

        ArchivePath expectedPath = new BasicPath(archivePath, "test.properties");

        Assert.assertTrue("Nested archive assets should be verified through a fully qualified path",
            archive.contains(expectedPath));
    }

    /**
     * Ensure assets from a nested archive are accessible from parent archives.
     *
     */
    @Test
    public void testNestedArchiveGet() {
        Archive<T> archive = getArchive();

        Archive<T> nestedArchive = createNewArchive();

        ArchivePath baseLocation = new BasicPath("somewhere");

        archive.add(nestedArchive, baseLocation, ZipExporter.class);

        Archive<T> nestedNestedArchive = createNewArchive();

        nestedArchive.add(nestedNestedArchive, ArchivePaths.root(), ZipExporter.class);

        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);

        ArchivePath nestedAssetPath = new BasicPath("/", "test.properties");

        nestedNestedArchive.add(asset, nestedAssetPath);

        ArchivePath nestedArchivePath = new BasicPath(baseLocation, nestedArchive.getName());

        ArchivePath nestedNestedArchivePath = new BasicPath(nestedArchivePath, nestedNestedArchive.getName());

        ArchivePath expectedPath = new BasicPath(nestedNestedArchivePath, "test.properties");

        Node nestedNode = archive.get(expectedPath);

        Assert.assertNotNull(
            "Nested archive asset should be available through partent archive at " + expectedPath.get(),
            nestedNode.getAsset());
    }

    @Test
    public void shouldMoveAsset() {
       final Archive<JavaArchive> archive = ShrinkWrap.create(JavaArchive.class, "archive.jar");
       final String sourcePath = "path1";
       final String targetPath = "path2";
       archive.add(EmptyAsset.INSTANCE, sourcePath);
       archive.move(sourcePath, targetPath);

       Assert.assertEquals("The archive should have only one asset", 1, numAssets(archive));
       Assert.assertNotNull("The asset should be at the target path", archive.get(targetPath));
    }

    @Test
    public void shouldMoveDirectory() {
        final Archive<JavaArchive> archive = ShrinkWrap.create(JavaArchive.class, "archive.jar");
        final String sourcePath = "path1";
        final String targetPath = "path2";
        archive.addAsDirectory(sourcePath);
        archive.move(sourcePath, targetPath);

        Assert.assertTrue("Directory should be at the new path", archive.get(targetPath).getAsset() == null);
    }

    @Test
    public void shouldMoveNotEmptyDirectory() {
        final Archive<JavaArchive> archive = ShrinkWrap.create(JavaArchive.class, "archive.jar");
        final String sourcePath = "path1";
        final String targetPath = "path2";

        final String childDirName = "childDir";
        final String childDirPath = sourcePath + "/" + childDirName;
        final String childDirTargetPath = targetPath + "/" + childDirName;

        final String childFileName1 = "file1";
        final String childFilePath1 = childDirPath + "/" + childFileName1;
        final String childFileTargetPath1 = childDirTargetPath + "/" + childFileName1;
        final String childFileName2 = "file2";
        final String childFilePath2 = childDirPath + "/" + childFileName2;
        final String childFileTargetPath2 = childDirTargetPath + "/" + childFileName2;

        archive.addAsDirectory(sourcePath);
        archive.addAsDirectory(childDirPath);
        archive.add(EmptyAsset.INSTANCE, childFilePath1);
        archive.add(EmptyAsset.INSTANCE, childFilePath2);
        archive.move(sourcePath, targetPath);

        Assert.assertTrue("Directory should be at the new path", archive.get(targetPath).getAsset() == null);
        Assert.assertTrue("Child dir should be at the new path", archive.get(childDirTargetPath).getAsset() == null);
        Assert.assertTrue("Child asset1 should be at the new path", archive.get(childFileTargetPath1).getAsset() != null);
        Assert.assertTrue("Child asset2 should be at the new path", archive.get(childFileTargetPath2).getAsset() != null);
    }

    @Test(expected = IllegalArchivePathException.class)
    public void shouldNotMoveAssetBecauseOfInexistentPath() {
       final Archive<JavaArchive> archive = ShrinkWrap.create(JavaArchive.class, "archive.jar");
       final String sourcePath = "non-existent-path1";
       final String targetPath = "path2";
       archive.move(sourcePath, targetPath);
    }

    @Test
    public void ensureShallowCopyPreservesPointers() {
        Archive<T> archive = getArchive();
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        archive.add(asset, "location");

        Archive<T> copyArchive = archive.shallowCopy();

        Assert.assertTrue(copyArchive.contains("location"));
        Assert.assertSame(copyArchive.get("location").getAsset(), archive.get("location").getAsset());
    }

    @Test
    public void ensureShallowCopyHasASeparateCollectionOfTheSamePointers() {
        Archive<T> archive = getArchive();
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        archive.add(asset, "location");

        Archive<T> copyArchive = archive.shallowCopy();
        archive.delete("location");

        Assert.assertTrue(copyArchive.contains("location"));
    }
    
    @Test
    public void ensureShallowCopyOperatesOnNestedAssets() {
        Archive<T> archive = getArchive();
        Asset asset = new ClassLoaderAsset(NAME_TEST_PROPERTIES);
        archive.add(asset, "location/sublocation");

        Archive<T> copyArchive = archive.shallowCopy();

        Assert.assertTrue(copyArchive.contains("location"));
        Assert.assertTrue(copyArchive.contains("location/sublocation"));
        Assert.assertSame(copyArchive.get("location/sublocation").getAsset(), archive.get("location/sublocation")
            .getAsset());
    }

    @Test
    public void testId() {
        // Create two archives with same name and contents
        final JavaArchive one = ShrinkWrap.create(JavaArchive.class, "archive.jar");
        final JavaArchive two = ShrinkWrap.create(JavaArchive.class, "archive.jar");

        System.out.println("ALR: " + one.getId());
    }

    // -------------------------------------------------------------------------------------||
    // Internal Helper Methods -------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Compare two Asset with each other. <br/>
     * Does not check instances but content.
     *
     * @param one
     *            Asset to compare
     * @param two
     *            Asset to compare
     * @return true if they are equal
     * @throws IllegalArgumentException
     *             If either asset is not specified
     */
    private boolean compareAssets(final Asset one, final Asset two) throws IllegalArgumentException {
        // Precondition check
        Validate.notNull(one, "Asset one must be specified");
        Validate.notNull(two, "Asset two must be specified");

        byte[] oneData = IOUtil.asByteArray(one.openStream());
        byte[] twoData = IOUtil.asByteArray(two.openStream());

        return Arrays.equals(oneData, twoData);
    }

    /**
     * Returns the number of assets in a file.
     *
     * @param archive
     *            the Archive from which we want to retrieve the number of assets
     * @return the number of assets in the archive
     * @throws IllegalArgumentException
     *             If the archive is not specified
     */
    protected int numAssets(final Archive<?> archive) {
        // Precondition check
        Validate.notNull(archive, "Archive must be specified");

        int assets = 0;

        Map<ArchivePath, Node> content = archive.getContent();
        for (Map.Entry<ArchivePath, Node> entry : content.entrySet()) {
            if (entry.getValue().getAsset() != null) {
                assets++;
            }
        }

        return assets;
    }

    private String readStringAsset(final ArchivePath path) throws IOException {
        Asset addedAsset = getArchive().get(path).getAsset();
        return new BufferedReader(new InputStreamReader(addedAsset.openStream())).readLine();
    }
}
