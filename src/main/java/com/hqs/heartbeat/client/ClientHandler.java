package com.hqs.heartbeat.client;

import com.hqs.heartbeat.common.CustomeHeartbeatHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author huangqingshi
 * @Date 2019-05-11
 */
public class ClientHandler extends CustomeHeartbeatHandler {

    private Client client;

    public ClientHandler(Client client) {
        super("client");
        this.client = client;
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        byte[] data = new byte[byteBuf.readableBytes() - 3];
        byteBuf.skipBytes(3);
        byteBuf.readBytes(data);
        String content = new String(data);
        System.out.println(name + " get content:" + content);
    }

    @Override
    protected void handlALLIdle(ChannelHandlerContext channelHandlerContext) {
        super.handlALLIdle(channelHandlerContext);
        sendPing(channelHandlerContext);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        client.doConnect();
    }
}
