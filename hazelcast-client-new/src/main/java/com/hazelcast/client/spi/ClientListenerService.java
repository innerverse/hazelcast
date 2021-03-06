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

import com.hazelcast.client.impl.ClientMessageDecoder;
import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.spi.impl.ListenerRemoveCodec;

/**
 * Client service to add/remove remote listeners.
 */
public interface ClientListenerService {

    String startListening(ClientMessage clientMessage, Object key, EventHandler handler,
                          ClientMessageDecoder responseDecoder);

    String startListeningOnPartition(ClientMessage clientMessage, int partitionId, EventHandler handler,
                                            ClientMessageDecoder responseDecoder);

    boolean stopListening(String registrationId, ListenerRemoveCodec listenerRemoveCodec);

    boolean stopListeningOnPartition(String registrationId, ListenerRemoveCodec listenerRemoveCodec, int partitionId);

    void registerListener(String uuid, Integer callId);

    String deRegisterListener(String uuid);
}
