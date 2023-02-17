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
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;


/**
 * @author yemh888
 */
public class SocketDecoder extends ByteToMessageDecoder {

    private final int identity;

    public SocketDecoder(int identity) {
        this.identity = identity;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes1 = new byte[10];
        in.getBytes(0, bytes1);
        int flag = in.getInt(0);
        if (flag == identity) {
            int size = in.getInt(4);
            in.skipBytes(8);
            byte[] bytes = new byte[size];
            in.readBytes(bytes, 0, size);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Object o = new ObjectInputStream(byteArrayInputStream).readObject();
            out.add(o);
        }
    }


}
