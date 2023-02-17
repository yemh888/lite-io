/*
 * Copyright (c) 2023, yemh888 (mh_c_y@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package top.codermhc.liteio.bus;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import top.codermhc.liteio.action.Callback;
import top.codermhc.liteio.common.PropertyHolder;
import top.codermhc.liteio.exception.DuplicateEventException;
import top.codermhc.liteio.io.ServerStub;
import top.codermhc.liteio.message.Message;
import top.codermhc.liteio.message.MessagePack;
import top.codermhc.liteio.message.MessageType;

/**
 * @author yemh888
 */
public class EventBus {

    private final Map<String, Callback<?>> eventHandles = new ConcurrentHashMap<>();
    private final ServerStub stub;

    @Deprecated
    public Map<String, Callback<?>> getEventHandles() {
        return eventHandles;
    }

    public EventBus() {
        this.stub = new ServerStub(this);
    }

    public void register(String eventName, Callback<?> callback) {
        if (eventName == null || callback == null || "".equals(eventName)) {
            throw new IllegalArgumentException("non-null");
        }
        Callback<?> cb;
        if ((cb = eventHandles.putIfAbsent(eventName, callback)) != callback && cb != null) {
            throw new DuplicateEventException(eventName);
        }
    }

    public void unregister(String eventName) {
        eventHandles.remove(eventName);
    }

    public void publish(String event, Message message) {
        stub.trigger(event, message);
    }

    public Future<?> accept(MessagePack messagePack) {
        Message message = messagePack.getMessage();
        if (MessageType.LPC.equals(message.getMessageType())
            || MessageType.BROADCAST.equals(message.getMessageType())) {
            String event = messagePack.getEvent();
            return Optional.ofNullable(eventHandles.get(event)).map(c -> c.call(message)).orElse(null);
        }
        return null;
    }

    public void refreshProperties(PropertyHolder properties) {
        stub.refreshProperties(properties);
    }

}
