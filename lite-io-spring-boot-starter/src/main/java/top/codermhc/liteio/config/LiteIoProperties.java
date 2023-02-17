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

import java.util.Collections;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.codermhc.liteio.common.PropertyHolder;

/**
 * @author yemh888
 */
@Configuration
@ConfigurationProperties(prefix = "lite-io")
public class LiteIoProperties {

    private List<String> endpoints = Collections.emptyList();
    private int port = 9910;
    private String host = "localhost";
    private int magic = 0x0badc0de;

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public PropertyHolder copy() {
        return new PropertyHolder(port, host, magic, endpoints);
    }
}
