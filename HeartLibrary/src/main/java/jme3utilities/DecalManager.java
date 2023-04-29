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
package jme3utilities;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Manage decal geometries, "aging them out" on a first-in, first-out (FIFO)
 * basis.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DecalManager {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DecalManager.class.getName());
    // *************************************************************************
    // fields

    /**
     * queue of retained decals, from oldest to newest
     */
    final private Deque<Geometry> fifo = new LinkedList<>();
    /**
     * maximum number of triangles to retain
     */
    private int maxTriangles = 9_999;
    /**
     * assign unique names to inactive decals
     */
    private int nextId = 0;
    /**
     * total triangles across all retained decals
     */
    private int totalTriangles = 0;
    /**
     * parent the decals
     */
    final private Node decalNode = new Node("Decal Node");
    // *************************************************************************
    // constructors

    /**
     * A no-arg constructor to avoid javadoc warnings from JDK 18.
     */
    public DecalManager() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a decal based on the specified template.
     *
     * @param active the template (not null, not empty, unaffected)
     */
    public void addCloneOf(Geometry active) {
        Geometry decal = (Geometry) active.deepClone();
        addDecal(decal);
    }

    /**
     * Add the specified decal to the queue.
     *
     * @param decal the decal to add (not null, not empty, alias created)
     */
    public void addDecal(Geometry decal) {
        int triangleCount = decal.getTriangleCount();
        assert triangleCount > 0 : triangleCount;

        decal.setName("decal #" + nextId);
        ++nextId;

        fifo.addLast(decal);
        decalNode.attachChild(decal);
        this.totalTriangles += triangleCount;

        purge();
    }

    /**
     * Return the maximum number of decal triangles to retain.
     *
     * @return the limit value (&gt;0)
     */
    public int getMaxTriangles() {
        return maxTriangles;
    }

    /**
     * Access the scene-graph node which parents all the decals.
     *
     * @return the pre-existing instance (not null)
     */
    public Node getNode() {
        return decalNode;
    }

    /**
     * Remove just enough of the old decals to conform to the triangle limit.
     */
    public void purge() {
        while (totalTriangles > maxTriangles) {
            Geometry oldest = fifo.removeFirst();
            oldest.removeFromParent();

            int count = oldest.getTriangleCount();
            this.totalTriangles -= count;
        }
    }

    /**
     * Remove all decals.
     */
    public void removeAll() {
        fifo.clear();
        decalNode.detachAllChildren();
        this.totalTriangles = 0;
    }

    /**
     * Alter the maximum number of decal triangles to retain.
     *
     * @param newLimit the desired limit value (&gt;0, default=9999)
     */
    public void setMaxTriangles(int newLimit) {
        Validate.positive(newLimit, "new limit");

        this.maxTriangles = newLimit;
        purge();
    }

    /**
     * Translate all decals by the specified offset.
     *
     * @param offset the desired offset (in world coordinates, not null, finite,
     * unaffected)
     */
    public void translateAll(Vector3f offset) {
        Validate.finite(offset, "offset");

        for (Geometry geometry : fifo) {
            geometry.move(offset);
        }
    }
}
