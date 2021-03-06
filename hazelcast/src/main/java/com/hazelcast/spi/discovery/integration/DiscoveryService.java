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

package com.hazelcast.spi.discovery.integration;

import com.hazelcast.spi.discovery.DiscoveredNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.NodeFilter;

/**
 * The <tt>DiscoveryService</tt> interface defines the basic entry point
 * into the Discovery SPI implementation. If not overridden explicitly the Hazelcast
 * internal {@link com.hazelcast.spi.discovery.impl.DefaultDiscoveryService}
 * implementation is used. A <tt>DiscoveryService</tt> somehow finds available
 * {@link DiscoveryStrategy}s inside the classpath and manages their activation
 * or deactivation status.
 * <p/>
 * This interface is used by system integrators, integrating Hazelcast into their own
 * frameworks or environments, are free to extend or exchange the default implementation
 * based on their needs and requirements.
 * <p/>
 * Only enabled providers are expected to discover nodes but, depending on the
 * <tt>DiscoveryService</tt> implementation, multiple {@link DiscoveryStrategy}s
 * might be enabled at the same time (e.g. TCP-IP Joiner with well known addresses
 * and Cloud discovery).
 */
public interface DiscoveryService {

    /**
     * The <tt>start</tt> method is called on system startup to implement simple
     * lifecycle management. This method is expected to call
     * {@link DiscoveryStrategy#start(com.hazelcast.spi.discovery.DiscoveryMode)} on all
     * discovered and activated strategies.
     */
    void start();

    /**
     * Returns a discovered and filtered, if a {@link NodeFilter} is setup, set of
     * discovered nodes to connect to.
     *
     * @return a set of discovered and filtered nodes
     */
    Iterable<DiscoveredNode> discoverNodes();

    /**
     * The <tt>start</tt> method is called on system startup to implement simple
     * lifecycle management. This method is expected to call
     * {@link DiscoveryStrategy#destroy()} on all discovered and activated strategies
     * before the service itself will shutdown.
     */
    void destroy();
}
