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

package com.hazelcast.spi.discovery;

/**
 * <p>The <tt>DiscoveryMode</tt> describes how the {@link DiscoveryStrategy} is going
 * to behave on discovery requests. Depending on the current environment it will
 * either be setup to run in client or server mode.</p>
 * <p>Implementors of {@link DiscoveryStrategy}s are free to change behavior as necessary.
 * One possible use case is to prevent to start a multicast service on clients when only
 * discovery is necessary.</p>
 */
public enum DiscoveryMode {
    /**
     * The current runtime environment is a Hazelcast member node
     */
    Member,

    /**
     * The current runtime environment is a Hazelcast client
     */
    Client
}
