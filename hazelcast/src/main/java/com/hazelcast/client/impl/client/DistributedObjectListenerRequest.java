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

package com.hazelcast.client.impl.client;

import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.spi.ProxyService;
import com.hazelcast.spi.impl.PortableDistributedObjectEvent;
import com.hazelcast.spi.impl.proxyservice.impl.ProxyServiceImpl;

import java.security.Permission;
import java.util.concurrent.Callable;

public class DistributedObjectListenerRequest extends CallableClientRequest implements RetryableRequest
        , DistributedObjectListener {

    public DistributedObjectListenerRequest() {
    }

    @Override
    public Object call() throws Exception {
        final ProxyService proxyService = clientEngine.getProxyService();
        final String registrationId = proxyService.addProxyListener(this);
        endpoint.addDestroyAction(registrationId, new Callable() {
            @Override
            public Boolean call() {
                return proxyService.removeProxyListener(registrationId);
            }
        });
        return registrationId;
    }

    @Override
    public String getServiceName() {
        return ProxyServiceImpl.SERVICE_NAME;
    }

    @Override
    public int getFactoryId() {
        return ClientPortableHook.ID;
    }

    @Override
    public int getClassId() {
        return ClientPortableHook.LISTENER;
    }

    @Override
    public void distributedObjectCreated(DistributedObjectEvent event) {
        send(event);
    }

    @Override
    public void distributedObjectDestroyed(DistributedObjectEvent event) {
        send(event);
    }

    private void send(DistributedObjectEvent event) {
        if (endpoint.isAlive()) {
            PortableDistributedObjectEvent portableEvent = new PortableDistributedObjectEvent(
                    event.getEventType(), event.getDistributedObject().getName(), event.getServiceName());
            endpoint.sendEvent(null, portableEvent, getCallId());
        }
    }

    @Override
    public Permission getRequiredPermission() {
        return null;
    }

    @Override
    public String getMethodName() {
        return "addDistributedObjectListener";
    }
}
