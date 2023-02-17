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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import top.codermhc.liteio.bus.Endpoint;
import top.codermhc.liteio.bus.EventBus;
import top.codermhc.liteio.bus.LocalEndpoint;
import top.codermhc.liteio.bus.RpcEndpoint;
import top.codermhc.liteio.common.LiteIoConstant;
import top.codermhc.liteio.common.PropertyHolder;
import top.codermhc.liteio.exception.LiteIOException;
import top.codermhc.liteio.io.socks.SocketDecoder;
import top.codermhc.liteio.io.socks.SocketEncoder;
import top.codermhc.liteio.io.socks.SocksRequestHandler;
import top.codermhc.liteio.message.Message;
import top.codermhc.liteio.message.MessagePack;
import top.codermhc.liteio.message.MessageType;
import top.codermhc.liteio.message.MessageWrapper;
import top.codermhc.liteio.util.NetTools;

/**
 * @author yemh888
 */
public class ServerStub {

    private final Map<String,Endpoint> remoteEndpoints = new ConcurrentHashMap<>();
    private final Endpoint localEndpoint;
    // private final Map<MessageType, List<Endpoint>> endpoints = new ConcurrentHashMap<>();
    // private final Map<String, MessageType> serverTypes = new ConcurrentHashMap<>();
    private final List<String> addresses = new CopyOnWriteArrayList<>();
    private final EventBus bus;
    private PropertyHolder propertyHolder;
    private Future servicing;

    public ServerStub(EventBus bus) {
        this.bus = bus;
        this.localEndpoint = new LocalEndpoint(bus);
    }

    public void trigger(String event, Message message) {
        CompletableFuture.runAsync(()->{
            MessagePack pack = new MessagePack(event, message);
            if (MessageType.LPC.equals(message.getMessageType())) {
                localEndpoint.transport(pack);
            } else {
                switch (message.getMessageType()) {
                    case RPC:
                        String session = pack.getSession();

                        break;
                    case BROADCAST:
                        remoteEndpoints.values().forEach(endpoint -> endpoint.transport(pack));
                        System.out.println("send broadcast message." + remoteEndpoints.size());
                        break;
                }
            }
        });
    }

    private void service(PropertyHolder properties) {
        if (servicing != null) {
            throw new LiteIOException("Already running.");
        }

        addresses.addAll(properties.getEndpoints());

        bus.unregister(LiteIoConstant.CLUSTER_NOTICE_EVENT);
        bus.register(LiteIoConstant.CLUSTER_NOTICE_EVENT, (msg)->{
            RpcInfo data = new RpcInfo();
            data.setHost(properties.getHost());
            data.setPort(properties.getPort());
            MessageWrapper messageWrapper = new MessageWrapper(MessageType.RPC, data);
            return CompletableFuture.completedFuture(messageWrapper);
        });

        servicing = CompletableFuture.runAsync(
            () -> {
                NioEventLoopGroup boss = new NioEventLoopGroup(1);
                NioEventLoopGroup work = new NioEventLoopGroup();

                work.scheduleAtFixedRate(this::scan, 5, 10, TimeUnit.SECONDS);
                work.scheduleAtFixedRate(this::prune, 10, 100, TimeUnit.SECONDS);

                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(boss, work)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.SO_REUSEADDR, true)
                        .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                socketChannel.pipeline().addLast(new ProtocolSwitcher(bus, properties));
                            }
                        })
                        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                    bootstrap.bind(properties.getPort()).sync().channel().closeFuture().sync();
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                } finally {
                    boss.shutdownGracefully();
                    work.shutdownGracefully();
                }
            }
        );
    }

    private void scan() {
        for (String endpoint : addresses) {
            String[] hostPort = endpoint.split(":");
            InetSocketAddress inetSocketAddress = new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1]));
            if (NetTools.isLocalHost(inetSocketAddress.getAddress()) && inetSocketAddress.getPort() == propertyHolder.getPort()) {
                // skip
            } else {
                // atomic operation
                remoteEndpoints.computeIfAbsent(endpoint, s -> {
                    RpcEndpoint rpcEndpoint = new RpcEndpoint(inetSocketAddress, propertyHolder);
                    System.out.println(remoteEndpoints.size());
                    return rpcEndpoint;
                });
            }
        }
    }

    private void prune() {
        Iterator<Entry<String, Endpoint>> iterator = remoteEndpoints.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Endpoint> next = iterator.next();
            if (!next.getValue().ping()) {
                try {
                    next.getValue().close();
                    iterator.remove();
                } catch (Exception e) {
                    // NOP
                }
            }
        }
    }

    public synchronized void shutdown() {
        if (servicing == null) {
            return;
        }
        // 阻塞等待
        servicing.cancel(true);
        addresses.clear();
        remoteEndpoints.forEach((address,endpoint)->{
            try {
                endpoint.close();
            } catch (Exception e) {
                // NOP
            }
        });
        remoteEndpoints.clear();
        while (!servicing.isDone()) {
            try {
                wait(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        servicing = null;
    }

    public synchronized void refreshProperties(PropertyHolder properties) {
        if (this.propertyHolder != properties) {
            this.propertyHolder = properties;
            shutdown();
            service(properties);
        }
    }

}
