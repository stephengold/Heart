/*
 Copyright (c) 2022-2023, Stephen Gold
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

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.ReadXZ;
import jme3utilities.math.VectorXZ;
import jme3utilities.test.HeartTest;
import org.junit.Test;

/**
 * Test the VectorXZ class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestVectorXZ {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestVectorXZ.class.getName());
    // *************************************************************************
    // new methods exposed

    /**
     * Test the VectorXZ class.
     */
    @Test
    public void test1() {
        VectorXZ[] cases = new VectorXZ[4];
        cases[0] = VectorXZ.east;
        cases[1] = new VectorXZ(1f, 1f);
        cases[2] = VectorXZ.west;
        cases[3] = VectorXZ.zero;

        for (VectorXZ vIn : cases) {
            // reconstruct a VectorXZ from polar coordinates
            float a = vIn.azimuth();
            float length = vIn.length();
            ReadXZ vOut = new VectorXZ(a).mult(length);
            HeartTest.assertEquals(vIn, vOut, 1e-5f);

            if (!vIn.isZero()) {
                // 3 different ways to combine rotations
                Vector3f v3 = new Vector3f(1f, 2f, 3f);
                VectorXZ vxz = new VectorXZ(v3);
                ReadXZ r1 = vIn.normalize().mult(vxz);

                Quaternion q1 = vIn.toQuaternion();
                Vector3f v31 = MyQuaternion.rotate(q1, v3, null);
                VectorXZ r2 = new VectorXZ(v31);

                Quaternion q2 = new Quaternion();
                q2.fromAngleNormalAxis(-a, new Vector3f(0f, 1f, 0f));
                Vector3f v32 = MyQuaternion.rotate(q2, v3, null);
                VectorXZ r3 = new VectorXZ(v32);

                HeartTest.assertEquals(r1, r2, 1e-5f);
                HeartTest.assertEquals(r2, r3, 1e-5f);
            }
        }
    }
}
