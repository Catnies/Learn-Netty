package top.catnies.learn_netty.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class NettyServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // 第一个是 bossGroup，用于接收连接；
        EventLoopGroup workerGroup = new NioEventLoopGroup();  // 第二个是 workerGroup，用于处理每个连接的数据读写。
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap(); // 创建服务器的 Bootstrap .
            serverBootstrap.group(bossGroup, workerGroup) // 为 Bootstrap 分配 Boss组 和 Worker组 .
                .channel(NioServerSocketChannel.class) // 指定 Netty 服务端使用的通道类型. NioServerSocketChannel 是对 NIO 原生 ServerSocketChannel 的封装.
                .childHandler(new ChannelInitializer<SocketChannel>() {  // 设置每一个新连接（子通道）的初始化逻辑，添加入站和出站处理器
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                // 入站处理器
                                .addLast(new NettyClient.MyLengthDecoder()) // 入站之后, 用处理器检查当前的消息是否完整
                                .addLast(new StringDecoder())       // Buffer解码成字符串
                                .addLast(new StatusHandler())       // 自定义状态处理器(日志)
                                .addLast(new ResponseHandler())    // 自定义回复消息

                                // 出站处理器
                                .addLast(new NettyClient.MyLengthEncoder()); // 将字符串添加长度信息
                    }
                })
                    // 当客户端发起连接请求时，如果服务器还没有来得及 accept 这个连接，这个连接就会被暂存在 backlog 队列中。
                    // 如果队列已满，新连接请求就会被丢弃（或返回 RST）。
                .option(ChannelOption.SO_BACKLOG, 128)
                    // 这是用于 启用 TCP 保活机制 的参数。当连接在一段时间内没有数据传输时，TCP 保活机制会自动发送探测包，以确认连接是否仍然有效。
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture bindFuture = serverBootstrap.bind(8987); // 让Bootstrap监听端口, 返回的是一个future
            bindFuture.addListener( future -> {     // 可以通过添加监听器, 来检查监听端口是否成功和失败, 然后执行相关的操作.
                if (bindFuture.isSuccess()) System.out.println("服务端监听端口 8987 成功!");
                else System.out.println("服务器监听端口失败, 请检查端口是否已经被占用!");
            });

            // 阻塞主线程，直到服务器通道关闭
            bindFuture.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    static class StatusHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("[日志] 收到来自客户端: " + ctx.channel().remoteAddress() + " 的消息, 内容是: " + msg);
            ctx.fireChannelRead(msg); // 直接传递给下一层处理
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println(ctx.channel() + " 注册了");
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println(ctx.channel() + " 解除注册了");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println(ctx.channel() + " 可以使用了");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println(ctx.channel() + " 不可使用了");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println(ctx.channel() + " 出现了异常");
            cause.printStackTrace();
        }
    }


    static class ResponseHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            if (!msg.startsWith("bt:")) ctx.channel().writeAndFlush("re: " + msg);
        }
    }

}



