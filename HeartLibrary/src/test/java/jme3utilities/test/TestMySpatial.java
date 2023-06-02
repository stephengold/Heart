/*
 Copyright (c) 2013-2022, Stephen Gold
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

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.logging.Logger;
import jme3utilities.MySpatial;
import jme3utilities.math.MyQuaternion;
import org.junit.Test;

/**
 * Test the MySpatial class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestMySpatial {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestMySpatial.class.getName());
    // *************************************************************************
    // new methods exposed

    /**
     * Test the MySpatial class.
     */
    @Test
    public void testMySpatial() {
        Node parent = new Node("parent");
        parent.setLocalScale(new Vector3f(6f, 5f, 4f));
        parent.setLocalRotation(new Quaternion(3f, 5f, 2f, 4f)); // not normal
        parent.setLocalTranslation(new Vector3f(-1f, 2f, 3f));
        Node child = new Node("child");
        parent.attachChild(child);

        Vector3f loc = new Vector3f(9f, 7f, 8f);
        MySpatial.setWorldLocation(child, loc);
        Vector3f loc2 = MySpatial.worldLocation(child, null);
        HeartTest.assertEquals(loc2, loc, 0.00001f);

        parent.setLocalScale(2f); // make uniform scale

        Quaternion rot = new Quaternion(3f, 1f, 4f, 15f).normalizeLocal();
        MySpatial.setWorldOrientation(child, rot);
        Quaternion rot2 = MySpatial.worldOrientation(child, null);
        MyQuaternion.normalizeLocal(rot2);
        HeartTest.assertEquals(rot2, rot, 0.00001f);
    }
}
