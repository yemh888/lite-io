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

package top.codermhc.liteio.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.codermhc.liteio.bus.EventBus;
import top.codermhc.liteio.message.MessageType;
import top.codermhc.liteio.message.MessageWrapper;

/**
 * @author yemh888
 */
@RestController
public class TestController {

    @Autowired
    EventBus eventBus;

    @GetMapping("hello")
    public String hello() {
        eventBus.publish("hello",new MessageWrapper(MessageType.BROADCAST, "hello"));
        return "ok";
    }

}
