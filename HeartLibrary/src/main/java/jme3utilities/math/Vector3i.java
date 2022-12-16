/*
 Copyright (c) 2021-2022, Stephen Gold
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
package jme3utilities.math;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Integer vector with 3 components, used to identify a chunk in a 3-D world or
 * represent the offset between 2 chunks. Immutable except for
 * {@link #read(com.jme3.export.JmeImporter)}.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Vector3i implements Savable {
    // *************************************************************************
    // constants and loggers

    /**
     * instance with all components =0
     */
    final public static Vector3i zero = new Vector3i(0, 0, 0);
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Vector3i.class.getName());
    // *************************************************************************
    // fields

    /**
     * X component
     */
    private int x;
    /**
     * Y component
     */
    private int y;
    /**
     * Z component
     */
    private int z;
    // *************************************************************************
    // constructors

    /**
     * No-argument constructor needed by SavableClassUtil.
     */
    protected Vector3i() {
        // do nothing
    }

    /**
     * Instantiate a vector.
     *
     * @param x the desired X component
     * @param y the desired Y component
     * @param z the desired Z component
     */
    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add to (translate) this vector.
     *
     * @param deltaX the number of units in the +X direction
     * @param deltaY the number of units in the +Y direction
     * @param deltaZ the number of units in the +Z direction
     * @return a new vector
     */
    public Vector3i add(int deltaX, int deltaY, int deltaZ) {
        Vector3i result = new Vector3i(x + deltaX, y + deltaY, z + deltaZ);
        return result;
    }

    /**
     * Subtract from (inverse translate) this vector.
     *
     * @param deltaX the number of units in the -X direction
     * @param deltaY the number of units in the -Y direction
     * @param deltaZ the number of units in the -Z direction
     * @return a new vector
     */
    public Vector3i subtract(int deltaX, int deltaY, int deltaZ) {
        Vector3i result = new Vector3i(x - deltaX, y - deltaY, z - deltaZ);
        return result;
    }

    /**
     * Determine the X component.
     *
     * @return the component value
     */
    public int x() {
        return x;
    }

    /**
     * Compare the X component to that of another vector.
     *
     * @param other (not null)
     * @return the difference (this minus other)
     */
    public int xDiff(Vector3i other) {
        int result = x - other.x();
        return result;
    }

    /**
     * Determine the Y component.
     *
     * @return the component value
     */
    public int y() {
        return y;
    }

    /**
     * Compare the Y component to that of another chunk.
     *
     * @param other (not null)
     * @return the difference (this minus other)
     */
    public int yDiff(Vector3i other) {
        int result = y - other.y();
        return result;
    }

    /**
     * Determine the Z component.
     *
     * @return the component value
     */
    public int z() {
        return z;
    }

    /**
     * Compare the Z component to that of another chunk.
     *
     * @param other (not null)
     * @return the difference (this minus other)
     */
    public int zDiff(Vector3i other) {
        int result = z - other.z();
        return result;
    }
    // *************************************************************************
    // Object methods

    /**
     * Test for exact equivalence with another Object.
     *
     * @param otherObject the object to compare to (may be null, unaffected)
     * @return true if the objects are equivalent, otherwise false
     */
    @Override
    public boolean equals(Object otherObject) {
        boolean result;
        if (otherObject == this) {
            result = true;
        } else if (otherObject != null
                && otherObject.getClass() == getClass()) {
            Vector3i otherVector = (Vector3i) otherObject;
            result = (otherVector.x() == x)
                    && (otherVector.y() == y)
                    && (otherVector.z() == z);
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Generate the hash code for this vector.
     *
     * @return the value to use for hashing
     */
    @Override
    public int hashCode() {
        int result = 707;
        result = 29 * result + x;
        result = 29 * result + y;
        result = 29 * result + z;

        return result;
    }

    /**
     * Represent this vector as a text string. The format is: (X,Y,Z)
     *
     * @return a descriptive string of text (not null)
     */
    @Override
    public String toString() {
        String result = String.format("(%d,%d,%d)", x, y, z);
        return result;
    }
    // *************************************************************************
    // Savable methods

    /**
     * De-serialize this vector, for example when loading from a J3O file.
     *
     * @param importer (not null)
     * @throws IOException from importer
     */
    @Override
    public void read(JmeImporter importer) throws IOException {
        InputCapsule capsule = importer.getCapsule(this);

        this.x = capsule.readInt("x", 0);
        this.y = capsule.readInt("y", 0);
        this.z = capsule.readInt("z", 0);
    }

    /**
     * Serialize this vector, for example when saving to a J3O file.
     *
     * @param exporter (not null)
     * @throws IOException from exporter
     */
    @Override
    public void write(JmeExporter exporter) throws IOException {
        OutputCapsule capsule = exporter.getCapsule(this);

        capsule.write(x, "x", 0);
        capsule.write(y, "y", 0);
        capsule.write(z, "z", 0);
    }
}
