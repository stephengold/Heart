/*
 Copyright (c) 2024 Stephen Gold
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
package jme3utilities.debug.test;

import com.jme3.scene.Node;
import java.util.logging.Logger;
import jme3utilities.debug.Describer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the {@code Describer} class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestDescriber {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestDescriber.class.getName());
    // *************************************************************************
    // new methods exposed

    /**
     * Test describing user data.
     */
    @Test
    public void testDescribeUserData() {
        Describer describer = new Describer();

        // String value containing a tab
        Node node1 = new Node("node1");
        node1.setUserData("key1", "\ttext");
        String description1 = describer.describeUserData(node1);
        Assert.assertEquals("key1=\"\\ttext\"", description1);

        // null value
        Node node2 = new Node("node2");
        node2.setUserData("key2", null);
        String description2 = describer.describeUserData(node2);
        Assert.assertEquals("", description2);

        // empty String value
        Node node3 = new Node("node3");
        node3.setUserData("key3", "");
        String description3 = describer.describeUserData(node3);
        Assert.assertEquals("key3=\"\"", description3);

        // NaN float value
        Node node4 = new Node("node4");
        node4.setUserData("key4", Float.NaN);
        String description4 = describer.describeUserData(node4);
        Assert.assertEquals("key4=NaN", description4);
    }
}
