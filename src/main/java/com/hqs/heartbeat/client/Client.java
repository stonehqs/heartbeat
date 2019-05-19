package com.hqs.heartbeat.client;

import com.hqs.heartbeat.common.CustomeHeartbeatHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author huangqingshi
 * @Date 2019-05-11
 */
public class Client {


    private NioEventLoopGroup workGroup = new NioEventLoopGroup(4);
    private Channel channel;
    private Bootstrap bootstrap;

    public static void main(String[] args) throws InterruptedException {
        Client client = new Client();
        client.start();
        client.sendData();
    }

    public void start() {

        try {
            bootstrap = new Bootstrap();

            bootstrap.group(workGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline channelPipeline = ch.pipeline()
                                    .addLast(new IdleStateHandler(0,0,5))
                                    .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, -2, 0))
                                    .addLast(new ClientHandler(Client.this));
                        }
                    });
            doConnect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendData() throws InterruptedException {
        Random random = new Random(System.currentTimeMillis());
        for(int i = 0; i < 10000; i++) {
            if(channel != null && channel.isActive()) {
                String content = "client msg " + i;
                ByteBuf byteBuf = channel.alloc().buffer(3 + content.getBytes().length);
                byteBuf.writeShort(3 + content.getBytes().length);
                byteBuf.writeByte(CustomeHeartbeatHandler.CUSTOM_MSG);
                byteBuf.writeBytes(content.getBytes());
                channel.writeAndFlush(byteBuf);
            }

            Thread.sleep(random.nextInt(20000));

        }

    }

    public void doConnect() {
        if(channel != null && channel.isActive()) {
            return;
        }

        ChannelFuture future = bootstrap
                .connect("127.0.0.1", 9999);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()) {
                    channel = future.channel();
                    System.out.println("connect to server successfully");
                } else {
                    System.out.println("Failed to connect to server, try after 10s");

                    future.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 10, TimeUnit.SECONDS);
                }
            }
        });
    }

}
