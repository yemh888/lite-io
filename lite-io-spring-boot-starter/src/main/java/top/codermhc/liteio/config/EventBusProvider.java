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

package top.codermhc.liteio.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import top.codermhc.liteio.action.Callback;
import top.codermhc.liteio.annotation.Event;
import top.codermhc.liteio.annotation.EventCallback;
import top.codermhc.liteio.bus.EventBus;
import top.codermhc.liteio.message.Message;

/**
 * @author yemh888
 */
@Configuration
public class EventBusProvider implements ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(EventBus.class);

    @ConditionalOnMissingBean(EventBus.class)
    @Bean
    public EventBus eventBus() {
        EventBus eventBus = new EventBus();
        applicationContext.getBeansWithAnnotation(Event.class).forEach((k,bean) -> {
            Arrays.stream(bean.getClass().getDeclaredMethods()).forEach(method -> {
                EventCallback annotation = AnnotationUtils.getAnnotation(method, EventCallback.class);
                boolean needWrap;
                if (annotation != null) {
                    try {
                        Method template = Callback.class.getMethod("call", Message.class);
                        needWrap = !method.getReturnType().isAssignableFrom(template.getReturnType());
                        if (method.getParameterCount() != template.getParameterCount()) {
                            throw new IllegalArgumentException();
                        }
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Class<?>[] templateParameterTypes = template.getParameterTypes();
                        for (int i = 0; i < parameterTypes.length; i++) {
                            if (!templateParameterTypes[i].isAssignableFrom(parameterTypes[i])) {
                                throw new IllegalArgumentException();
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }

                    eventBus.register("".equals(annotation.value()) ? method.getName() : annotation.value(), (Callback<?>) (msg) -> {
                        try {
                            if (needWrap) {
                                return CompletableFuture.completedFuture(method.invoke(bean, msg));
                            } else {
                                return (Future) method.invoke(bean, msg);
                            }
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            logger.error("call event handler failed.", e);
                            throw new RuntimeException(e);
                        }
                    });
                }
            });
        });
        LiteIoProperties properties = applicationContext.getBean(LiteIoProperties.class);
        eventBus.refreshProperties(properties.copy());
        return eventBus;
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
