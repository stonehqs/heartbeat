package com.hqs.heartbeat.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author huangqingshi
 * @Date 2019-05-11
 */
public abstract class CustomeHeartbeatHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static final byte PING = 1;
    public static final byte PONG = 2;
    public static final byte CUSTOM_MSG = 3;

    protected String name;
    private AtomicInteger heartbeatCount = new AtomicInteger(0);

    public CustomeHeartbeatHandler(String name) {
        this.name = name;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        if(byteBuf.getByte(2) == PING) {
            sendPong(channelHandlerContext);
        } else if(byteBuf.getByte(2) == PONG) {
            System.out.println("get pong msg from " + channelHandlerContext
                    .channel().remoteAddress());
        } else {
            handleData(channelHandlerContext, byteBuf);
        }
    }

    protected abstract void handleData(ChannelHandlerContext channelHandlerContext,
                                       ByteBuf byteBuf);


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channel read : " + msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println(byteBuf.getByte(2));
        super.channelRead(ctx, msg);
    }

    protected void sendPong(ChannelHandlerContext channelHandlerContext) {
        ByteBuf buf = channelHandlerContext.alloc().buffer(3);
        buf.writeShort(3);
        buf.writeByte(PONG);
        channelHandlerContext.writeAndFlush(buf);
        heartbeatCount.incrementAndGet();
        System.out.println("send pong message to " + channelHandlerContext.channel().remoteAddress());
    }

    protected void sendPing(ChannelHandlerContext channelHandlerContext) {
        ByteBuf buf = channelHandlerContext.alloc().buffer(3);
        buf.writeShort(3);
        buf.writeByte(PING);
        channelHandlerContext.writeAndFlush(buf);
        heartbeatCount.incrementAndGet();
        System.out.println("send ping message to " + channelHandlerContext.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case ALL_IDLE:
                    handlALLIdle(ctx);
                    break;
                case READER_IDLE:
                    handlReadIdle(ctx);
                    break;
                case WRITER_IDLE:
                    handlWriteIdle(ctx);
                    break;
                 default:
                     break;
            }
        }
    }

    protected void handlReadIdle(ChannelHandlerContext channelHandlerContext) {
        System.out.println("READ_IDLE---");
    }

    protected void handlWriteIdle(ChannelHandlerContext channelHandlerContext) {
        System.out.println("WRITE_IDLE---");
    }

    protected void handlALLIdle(ChannelHandlerContext channelHandlerContext) {
        System.out.println("ALL_IDLE---");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel:" + ctx.channel().remoteAddress() + " is active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel:" + ctx.channel().remoteAddress() + " is inactive");
    }
}
