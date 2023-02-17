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

package top.codermhc.liteio.common;

import java.util.List;

/**
 * @author yemh888
 */
public class PropertyHolder {

    private final int port;
    private final int magicNumber;
    private final String host;
    private List<String> endpoints;

    public PropertyHolder(int port, String host, int magicNumber, List<String> endpoints) {
        this.port = port;
        this.host = host;
        this.magicNumber = magicNumber;
        this.endpoints = endpoints;
    }

    public int getPort() {
        return port;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public String getHost() {
        return host;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }
}
