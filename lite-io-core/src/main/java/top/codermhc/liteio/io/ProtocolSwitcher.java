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

package top.codermhc.liteio.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.List;
import java.util.concurrent.TimeUnit;
import top.codermhc.liteio.bus.EventBus;
import top.codermhc.liteio.common.PropertyHolder;
import top.codermhc.liteio.io.http.HttpRequestHandler;
import top.codermhc.liteio.io.socks.SocketDecoder;
import top.codermhc.liteio.io.socks.SocketEncoder;
import top.codermhc.liteio.io.socks.SocksRequestHandler;

/**
 * @author yemh888
 */
public class ProtocolSwitcher extends ByteToMessageDecoder {

    private EventBus bus;
    private final int MAGIC_NUMBER;

    public ProtocolSwitcher(EventBus bus, PropertyHolder propertyHolder) {
        this.bus = bus;
        this.MAGIC_NUMBER = propertyHolder.getMagicNumber();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        if (in.getInt(0) == MAGIC_NUMBER) {
            // 当成TCP请求处理
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 4, 4));
            pipeline.addLast(new SocketEncoder(MAGIC_NUMBER));
            pipeline.addLast(new SocketDecoder(MAGIC_NUMBER));
            pipeline.addLast(new IdleStateHandler(100, 100, 100, TimeUnit.MICROSECONDS));
            pipeline.addLast(new SocksRequestHandler(bus));

            // 将自身移除掉
            pipeline.remove(this);
        } else {
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.addLast(new HttpServerCodec());// http 编解码
            pipeline.addLast("httpAggregator",new HttpObjectAggregator(512*1024)); // http 消息聚合器                                                                     512*1024为接收的最大contentlength
            pipeline.addLast(new HttpRequestHandler(bus));// 请求处理器

            pipeline.remove(this);
        }
    }
}
