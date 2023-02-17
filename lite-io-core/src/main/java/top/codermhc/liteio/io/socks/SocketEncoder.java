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

package top.codermhc.liteio.io.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author yemh888
 */
public class SocketEncoder extends MessageToByteEncoder<Serializable> {

    private final int identity;

    public SocketEncoder(int identity) {
        this.identity = identity;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        out.writeInt(identity);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream out1 = new ObjectOutputStream(byteArrayOutputStream);) {
            out1.writeObject(msg);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        }
    }

}
