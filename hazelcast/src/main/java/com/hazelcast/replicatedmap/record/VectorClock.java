/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.replicatedmap.record;

import com.hazelcast.core.Member;
import com.hazelcast.instance.MemberImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.replicatedmap.operation.ReplicatedMapDataSerializerHook;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * A vector clock implementation based on hashcodes of the Hazelcast members UUID to solve conflicts on
 * replication updates
 */
public final class VectorClock
        implements IdentifiedDataSerializable {

    private Map<Member, Integer> clocks;

    public VectorClock() {
        this.clocks = Collections.emptyMap();
    }

    private VectorClock(Map<Member, Integer> clocks) {
        this.clocks = Collections.unmodifiableMap(clocks);
    }

    VectorClock incrementClock0(Member localMember) {
        Map<Member, Integer> copy = new HashMap<Member, Integer>(clocks);
        Integer clock = copy.get(localMember);
        if (clock == null) {
            clock = 0;
        }

        copy.put(localMember, ++clock);
        return new VectorClock(copy);
    }

    VectorClock applyVector0(VectorClock update) {
        Map<Member, Integer> copy = new HashMap<Member, Integer>(clocks);
        for (Member m : update.clocks.keySet()) {
            final Integer currentClock = copy.get(m);
            final Integer updateClock = update.clocks.get(m);
            if (smaller(currentClock, updateClock)) {
                copy.put(m, updateClock);
            }
        }
        return new VectorClock(copy);
    }

    @Override
    public void writeData(ObjectDataOutput dataOutput)
            throws IOException {

        Map<Member, Integer> clocks = this.clocks;
        dataOutput.writeInt(clocks.size());
        for (Entry<Member, Integer> entry : clocks.entrySet()) {
            entry.getKey().writeData(dataOutput);
            dataOutput.writeInt(entry.getValue());
        }
    }

    @Override
    public void readData(ObjectDataInput dataInput)
            throws IOException {

        int size = dataInput.readInt();
        Map<Member, Integer> data = new HashMap<Member, Integer>();
        for (int i = 0; i < size; i++) {
            Member m = new MemberImpl();
            m.readData(dataInput);
            int clock = dataInput.readInt();
            data.put(m, clock);
        }
        this.clocks = Collections.unmodifiableMap(data);
    }

    @Override
    public int getId() {
        return ReplicatedMapDataSerializerHook.VECTOR;
    }

    @Override
    public int getFactoryId() {
        return ReplicatedMapDataSerializerHook.F_ID;
    }

    @Override
    public String toString() {
        return "Vector{" + "clocks=" + clocks + '}';
    }

    private boolean smaller(Integer int1, Integer int2) {
        int i1 = int1 == null ? 0 : int1;
        int i2 = int2 == null ? 0 : int2;
        return i1 < i2;
    }

    static VectorClock copyVector(VectorClock vectorClock) {
        Map<Member, Integer> clocks = new HashMap<Member, Integer>();
        for (Entry<Member, Integer> entry : vectorClock.clocks.entrySet()) {
            MemberImpl member = new MemberImpl((MemberImpl) entry.getKey());
            Integer value = entry.getValue();
            clocks.put(member, value);
        }
        return new VectorClock(clocks);
    }

    static boolean happenedBefore(VectorClock x, VectorClock y) {
        Set<Member> members = new HashSet<Member>(x.clocks.keySet());
        members.addAll(y.clocks.keySet());

        boolean hasLesser = false;
        for (Member m : members) {
            int xi = x.clocks.get(m) != null ? x.clocks.get(m) : 0;
            int yi = y.clocks.get(m) != null ? y.clocks.get(m) : 0;
            if (xi > yi) {
                return false;
            }
            if (xi < yi) {
                hasLesser = true;
            }
        }
        return hasLesser;
    }
}
