/*
 Copyright (c) 2017-2023, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.test;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.export.binary.BinaryExporter;
import java.util.logging.Logger;
import jme3utilities.NameGenerator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the NameGenerator class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestNameGenerator {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestNameGenerator.class.getName());
    // *************************************************************************
    // fields

    /**
     * AssetManager required by the BinaryImporter
     */
    final private static AssetManager assetManager = new DesktopAssetManager();
    // *************************************************************************
    // new methods exposed

    /**
     * Test the NameGenerator class.
     */
    @Test
    public void testNameGenerator() {
        NameGenerator example = new NameGenerator();

        String apple1 = example.unique("apple");
        Assert.assertTrue(NameGenerator.isFrom(apple1, "apple"));
        Assert.assertEquals("apple", NameGenerator.getPrefix(apple1));

        String apple2 = example.unique("apple");
        Assert.assertTrue(NameGenerator.isFrom(apple2, "apple"));
        Assert.assertEquals("apple", NameGenerator.getPrefix(apple2));
        Assert.assertNotEquals(apple1, apple2);

        String pear1 = example.unique("pear");
        Assert.assertTrue(NameGenerator.isFrom(pear1, "pear"));
        Assert.assertEquals("pear", NameGenerator.getPrefix(pear1));
        Assert.assertNotEquals(apple1, pear1);
        Assert.assertNotEquals(apple2, pear1);

        String apple3 = example.unique("apple");
        Assert.assertTrue(NameGenerator.isFrom(apple3, "apple"));
        Assert.assertEquals("apple", NameGenerator.getPrefix(apple3));
        Assert.assertNotEquals(apple1, apple3);
        Assert.assertNotEquals(apple2, apple3);
        Assert.assertNotEquals(pear1, apple3);

        String exampleString = example.toString();
        NameGenerator copy = BinaryExporter.saveAndLoad(assetManager, example);
        String copyString = copy.toString();
        Assert.assertEquals(copyString, exampleString);
    }
}
