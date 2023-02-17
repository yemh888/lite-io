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

import top.codermhc.liteio.message.MessagePack;

/**
 * @author yemh888
 */
public class LocalEndpoint extends Endpoint {

    public LocalEndpoint(EventBus bus) {
        this.bus = bus;
    }

    private final EventBus bus;

    @Override
    public void transport(MessagePack messagePack) {
        bus.accept(messagePack);
    }

    @Override
    public void close() throws Exception {
        // NOP
    }
}
