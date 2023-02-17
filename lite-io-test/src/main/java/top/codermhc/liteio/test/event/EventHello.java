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

package top.codermhc.liteio.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import top.codermhc.liteio.annotation.Event;
import top.codermhc.liteio.annotation.EventCallback;
import top.codermhc.liteio.message.MessageWrapper;

/**
 * @author yemh888
 */
@Event
public class EventHello {

    @EventCallback
    public String hello(MessageWrapper message) throws JsonProcessingException {
        Object data = message.getData();
        System.out.println(data);
        System.out.println(data.getClass());
        return "hello";
    }

    @EventCallback("hello")
    public Future<String> hello1(MessageWrapper messageWrapper) {
        return CompletableFuture.completedFuture("test");
    }

}
