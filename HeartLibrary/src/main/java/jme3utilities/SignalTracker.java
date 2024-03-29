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
package jme3utilities;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Track the active/inactive status of named signals. A signal may originate
 * from multiple sources such as buttons or hotkeys. A signal is active as long
 * as any of its sources is active.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class SignalTracker {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(SignalTracker.class.getName());
    // *************************************************************************
    // fields

    /**
     * map signal names to statuses
     */
    final private Map<String, Set<Integer>> statusMap = new TreeMap<>();
    // *************************************************************************
    // constructors

    /**
     * A no-arg constructor to avoid javadoc warnings from JDK 18.
     */
    public SignalTracker() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a new signal with all of its sources inactive. If the signal name is
     * already in use, this has no effect.
     *
     * @param name the name for the signal (not null)
     */
    public void add(String name) {
        Validate.nonNull(name, "signal name");

        Set<Integer> status = statusMap.get(name);
        if (status == null) {
            status = new TreeSet<>();
            statusMap.put(name, status);
        }
    }

    /**
     * Update whether a named signal source is active.
     *
     * @param signalName the signal's name (not null)
     * @param sourceIndex the index of the signal source (key or button) which
     * is being updated
     * @param newState true if the source has gone active; false if the source
     * has gone inactive
     */
    public void setActive(String signalName, int sourceIndex,
            boolean newState) {
        Validate.nonNull(signalName, "signal name");

        Set<Integer> status = statusMap.get(signalName);
        if (status == null) {
            logger.log(Level.WARNING, "Unknown signal: {0}",
                    MyString.quote(signalName));
            return;
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "name = {0}, newState = {1}", new Object[]{
                MyString.quote(signalName), newState
            });
        }

        if (newState) {
            status.add(sourceIndex);
        } else {
            status.remove(sourceIndex);
        }
    }

    /**
     * Test whether the named signal is active.
     *
     * @param signalName the name of the signal (not null)
     * @return true if any of the signal's sources is active, otherwise false
     */
    public boolean test(String signalName) {
        Validate.nonNull(signalName, "signal name");

        Set<Integer> status = statusMap.get(signalName);
        if (status == null) {
            logger.log(Level.WARNING,
                    "Testing a signal which has not yet been added: {0}.",
                    MyString.quote(signalName));
            status = new TreeSet<>();
            statusMap.put(signalName, status);
        }
        boolean result = !status.isEmpty();

        return result;
    }
}
