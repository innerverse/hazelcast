/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.client.spi;

import com.hazelcast.client.impl.client.BaseClientRemoveListenerRequest;
import com.hazelcast.client.impl.client.ClientRequest;

/**
 * Client service to add/remove remote listeners.
 */
public interface ClientListenerService {

    String startListening(ClientRequest request, Object key, EventHandler handler);

    String startListeningOnPartition(ClientRequest request, int partitionId, EventHandler handler);

    boolean stopListening(BaseClientRemoveListenerRequest request, String registrationId);

    boolean stopListeningOnPartition(BaseClientRemoveListenerRequest request, String registrationId, int partitionId);

    void registerListener(String uuid, Integer callId);

    String deRegisterListener(String uuid);
}
