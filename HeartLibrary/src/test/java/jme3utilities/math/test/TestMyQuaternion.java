/*
 Copyright (c) 2023, Stephen Gold
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
import jme3utilities.test.HeartTest;
import org.junit.Test;

/**
 * Test the MyQuaternion class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestMyQuaternion {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestMyQuaternion.class.getName());
    // *************************************************************************
    // new methods exposed

    /**
     * Test rotate().
     */
    @Test
    public void testRotate() {
        Vector3f[] testVectors = {
            new Vector3f(1f, 2f, 3f),
            new Vector3f(0f, -1f, -2f),
            new Vector3f(7f, 0.7f, 0f)
        };
        Quaternion[] testQuaternions = {
            new Quaternion(1f, 7f, 1f, -7f),
            new Quaternion(3f, -4f, 0f, 0f),
            new Quaternion(0f, 0f, 0f, 1f),
            new Quaternion(0f, 0f, -1f, 1f),
            new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f)
        };

        float tol = 1e-6f;

        for (Vector3f v : testVectors) {
            for (Quaternion q : testQuaternions) {
                Quaternion qn = q.clone();
                MyQuaternion.normalizeLocal(qn);
                Vector3f expected = qn.mult(v);

                Quaternion q1 = q.clone();
                Vector3f v1 = v.clone();

                Vector3f actual1 = MyQuaternion.rotate(q1, v1, null);
                HeartTest.assertEquals(expected, actual1, tol);
                HeartTest.assertEquals(q, q1, 0f); // unaffected
                HeartTest.assertEquals(v, v1, 0f); // unaffected

                Vector3f actual2 = MyQuaternion.rotate(q1, v1, v1);
                HeartTest.assertEquals(expected, actual2, tol);
                HeartTest.assertEquals(q, q1, 0f); // unaffected
                HeartTest.assertEquals(expected, v1, tol);
            }
        }
    }
}
