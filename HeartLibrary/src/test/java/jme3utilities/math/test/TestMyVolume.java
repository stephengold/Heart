/*
 Copyright (c) 2022, Stephen Gold
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
package jme3utilities.math.test;

import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.math.MyVolume;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the MyVolume class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestMyVolume {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestMyVolume.class.getName());
    // *************************************************************************
    // new methods exposed

    @Test
    public void test1() {
        Vector3f halfHeight1 = new Vector3f(1f, 5f, 3f);
        float volume1 = MyVolume.boxVolume(halfHeight1);
        Assert.assertEquals(120f, volume1, 1e-4f);

        float radius2 = 1.5f;
        float height2 = 2.5f;
        float volume2 = MyVolume.capsuleVolume(radius2, height2);
        Assert.assertEquals(31.8086f, volume2, 1e-4f);

        float radius3 = 1.2f;
        float height3 = 2.1f;
        float volume3 = MyVolume.coneVolume(radius3, height3);
        Assert.assertEquals(3.16673f, volume3, 1e-5f);

        Vector3f halfHeight4 = new Vector3f(2f, 3f, 7f);
        float volume4 = MyVolume.cylinderVolume(halfHeight4);
        Assert.assertEquals(263.894f, volume4, 1e-3f);

        float volume5 = MyVolume.sphereVolume(4.6f);
        Assert.assertEquals(407.72f, volume5, 1e-3f);

        Vector3f v1 = new Vector3f(1f, -1f, 0f);
        Vector3f v2 = new Vector3f(8f, 2f, 7.5f);
        Vector3f v3 = new Vector3f(3f, 1.2f, 0.4f);
        Vector3f v4 = new Vector3f(4f, -3f, -2f);
        double volume6 = MyVolume.tetrahedronVolume(v1, v2, v3, v4);
        Assert.assertEquals(14.85, volume6, 1e-3);
    }
}
