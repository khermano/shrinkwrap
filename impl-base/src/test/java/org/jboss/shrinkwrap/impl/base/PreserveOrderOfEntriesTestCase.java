package org.jboss.shrinkwrap.impl.base;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

/**
 * Ensures that entries added to the archives preserve the order they are
 * added in.
 * <p>
 * SHRINKWRAP-480
 *
 * @author <a href="mailto:hiram@hiramchirino.com">Hiram Chirino</a>
 * @version $Revision: $
 */
public class PreserveOrderOfEntriesTestCase {

    // -------------------------------------------------------------------------------------||
    // Tests ------------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    @Test
    public void canPreserveOrder() throws Exception {
        File target = new File("target");
        // this returns false if target exists
        target.mkdirs();
        File testJar = new File(target, "test.jar");
        // this returns false if testJar in target does not exist
        testJar.delete();

        if( testJar.exists() ) {
            throw new Exception("Test setup failed");
        }

        ArrayList<String> expectedOrder = new ArrayList<>();

        // Create an archive with resources
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, testJar.getName());
        Random random = new Random(0);
        for (int i = 0; i < 5; i++) {
            String name = "f" + Long.toHexString(random.nextLong());
            expectedOrder.add(name);
            archive.addAsResource(new StringAsset("content"), name);
        }

        Assertions.assertEquals(expectedOrder, getPaths(archive));

        archive.as(ZipExporter.class).exportTo(testJar, true);

        Assertions.assertTrue(testJar.exists());

        // Verify Zip entries are in the right order.
        ArrayList<String> actualOrder = new ArrayList<>();
        try (final FileInputStream fileInputStream = new FileInputStream(testJar);
             final ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            while( nextEntry!=null ) {
                actualOrder.add(nextEntry.getName());
                nextEntry = zipInputStream.getNextEntry();
            }
        }
        Assertions.assertEquals(expectedOrder, actualOrder);

        // Verify imported archive stays in the right order.
        JavaArchive archive2 = ShrinkWrap.create(JavaArchive.class, testJar.getName());
        archive2.as(ZipImporter.class).importFrom(testJar);
        Assertions.assertEquals(expectedOrder, getPaths(archive2));

        Assertions.assertTrue(testJar.delete(), "There is a problem removing testJar.");
    }

    private ArrayList<String> getPaths(JavaArchive archive2) {
        ArrayList<String> rc = new ArrayList<>();
        for (ArchivePath path : archive2.getContent().keySet()) {
            String file = path.get();
            file = file.substring(1); // to strip the leading /
            rc.add(file);
        }
        return rc;
    }


}
