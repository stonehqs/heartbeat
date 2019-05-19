package com.hqs.heartbeat.server;

import com.hqs.heartbeat.common.CustomeHeartbeatHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author huangqingshi
 * @Date 2019-05-11
 */
public class ServerHandler extends CustomeHeartbeatHandler {

    public ServerHandler() {
        super("server");
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext,
                              ByteBuf byteBuf) {
        byte[] data = new byte[byteBuf.readableBytes() - 3];
        ByteBuf responseBuf = Unpooled.copiedBuffer(byteBuf);
        byteBuf.skipBytes(3);
        byteBuf.readBytes(data);
        String content = new String(data);
        System.out.println(name + " get content : " + content);
        channelHandlerContext.writeAndFlush(responseBuf);
    }

    @Override
    protected void handlReadIdle(ChannelHandlerContext channelHandlerContext) {
        super.handlReadIdle(channelHandlerContext);
        System.out.println(" client " + channelHandlerContext.channel().remoteAddress() + " reader timeout close it --");
        channelHandlerContext.close();
    }
}
