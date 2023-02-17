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

package top.codermhc.liteio.io.http;

import static io.netty.handler.codec.http.HttpUtil.is100ContinueExpected;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import java.util.HashMap;
import java.util.Map;
import top.codermhc.liteio.bus.EventBus;
import top.codermhc.liteio.message.MessagePack;
import top.codermhc.liteio.message.MessageType;
import top.codermhc.liteio.message.MessageWrapper;

/**
 * @author yemh888
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private EventBus bus;

    public HttpRequestHandler(EventBus bus) {
        this.bus = bus;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        //100 Continue
        if (is100ContinueExpected(req)) {
            ctx.write(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.CONTINUE));
        }
        // 获取请求的uri
        String uri = req.uri();
        Map<String,String> resMap = new HashMap<>();
        resMap.put("method",req.method().name());
        resMap.put("uri",uri);
        ByteBuf content = req.content();

        // call message hub
        bus.accept(new MessagePack(uri.substring(1), new MessageWrapper(MessageType.LPC, content)));
        // bus.publish(req.method().name(), new BroadcastMessage(content.toString()));

        System.out.println(content.toString(CharsetUtil.UTF_8));
        String msg = "<html><head><title>test</title></head><body>你请求uri为：" + uri+"</body></html>";
        // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.NOT_FOUND,
            Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
        // 设置头信息
        // response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        // 将html write到客户端
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
