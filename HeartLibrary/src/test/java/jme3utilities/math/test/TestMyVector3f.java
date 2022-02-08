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
import jme3utilities.math.MyVector3f;
import jme3utilities.test.HeartTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the MyVector3f class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TestMyVector3f {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TestMyVector3f.class.getName());
    // *************************************************************************
    // new methods exposed

    /**
     * Test dot(), lengthSquared(), projection(), rejection(), and
     * scalarProjection().
     */
    @Test
    public void test1() {
        Vector3f v1 = new Vector3f(1f, 2f, 3f);
        Vector3f saveV1 = v1.clone();
        Vector3f v2 = new Vector3f(0f, -1f, -1f);
        Vector3f saveV2 = v2.clone();

        {
            double d12 = MyVector3f.dot(v1, v2);
            Assert.assertEquals(-5.0, d12, 1e-15);
            HeartTest.assertEquals(saveV1, v1, 0f);
            HeartTest.assertEquals(saveV2, v2, 0f);
        }

        {
            double d21 = MyVector3f.dot(v2, v1);
            Assert.assertEquals(-5.0, d21, 1e-15);
            HeartTest.assertEquals(saveV1, v1, 0f);
            HeartTest.assertEquals(saveV2, v2, 0f);
        }

        {
            double ls1 = MyVector3f.lengthSquared(v1);
            Assert.assertEquals(14.0, ls1, 1e-15);
            HeartTest.assertEquals(saveV1, v1, 0f);
        }

        {
            double ls2 = MyVector3f.lengthSquared(v2);
            Assert.assertEquals(2.0, ls2, 1e-15);
            HeartTest.assertEquals(saveV2, v2, 0f);
        }

        {
            Vector3f p12 = MyVector3f.projection(v1, v2, null);
            HeartTest.assertEquals(0f, 2.5f, 2.5f, p12, 1e-6f);
            HeartTest.assertEquals(saveV1, v1, 0f);
            HeartTest.assertEquals(saveV2, v2, 0f);
        }

        {
            Vector3f p21 = MyVector3f.projection(v2, v1, null);
            HeartTest.assertEquals(-0.357143f, -0.714286f, -1.071429f,
                    p21, 1e-6f);
            HeartTest.assertEquals(saveV1, v1, 0f);
            HeartTest.assertEquals(saveV2, v2, 0f);
        }

        {
            Vector3f r12 = MyVector3f.rejection(v1, v2, null);
            HeartTest.assertEquals(1f, -0.5f, 0.5f, r12, 1e-6f);
            HeartTest.assertEquals(saveV1, v1, 0f);
            HeartTest.assertEquals(saveV2, v2, 0f);
        }

        {
            float s12 = MyVector3f.scalarProjection(v1, v2);
            Assert.assertEquals(-3.535534f, s12, 1e-6f);
            HeartTest.assertEquals(saveV1, v1, 0f);
            HeartTest.assertEquals(saveV2, v2, 0f);
        }

        {
            float s21 = MyVector3f.scalarProjection(v2, v1);
            Assert.assertEquals(-1.336306f, s21, 1e-6f);
            HeartTest.assertEquals(saveV1, v1, 0f);
            HeartTest.assertEquals(saveV2, v2, 0f);
        }

        {
            Vector3f r122 = MyVector3f.rejection(v1, v2, v2); // trashes v2
            HeartTest.assertEquals(1f, -0.5f, 0.5f, r122, 1e-6f);
            HeartTest.assertEquals(saveV1, v1, 0f);
            Assert.assertSame(v2, r122);
        }

        {
            Vector3f r111 = MyVector3f.rejection(v1, v1, v1); // trashes v1
            HeartTest.assertEquals(0f, 0f, 0f, r111, 1e-6f);
            Assert.assertSame(v1, r111);
        }
    }
}
