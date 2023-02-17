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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import top.codermhc.liteio.common.PropertyHolder;
import top.codermhc.liteio.io.socks.SocketDecoder;
import top.codermhc.liteio.io.socks.SocketEncoder;
import top.codermhc.liteio.message.MessagePack;

/**
 * @author yemh888
 */
public class RpcEndpoint extends Endpoint {

    private Channel channel;

    public RpcEndpoint(InetSocketAddress inetSocketAddress, PropertyHolder propertyHolder) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 4, 4));
                    pipeline.addLast(new SocketEncoder(propertyHolder.getMagicNumber()));
                    pipeline.addLast(new SocketDecoder(propertyHolder.getMagicNumber()));
                    pipeline.addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS));
                }
            });
        try {
            channel = bootstrap.connect(inetSocketAddress).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void transport(MessagePack messagePack) {
        channel.writeAndFlush(messagePack);
    }

    @Override
    public void close() throws Exception {
        if (channel != null) {
            channel.close();
        }
    }

    public boolean ping() {
        return channel.isActive();
    }
}
