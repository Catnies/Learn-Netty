package top.catnies.learn_netty.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {

    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap(); // 创建服务器的 Bootstrap .
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // 第一个是 bossGroup，用于接收连接；
        EventLoopGroup workerGroup = new NioEventLoopGroup();  // 第二个是 workerGroup，用于处理每个连接的数据读写。
        serverBootstrap.group(bossGroup, workerGroup); // 为 Bootstrap 分配 Boss组 和 Worker组 .
        serverBootstrap.channel(NioServerSocketChannel.class); // 指定 Netty 服务端使用的通道类型. NioServerSocketChannel 是对 NIO 原生 ServerSocketChannel 的封装.
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {  // 设置每一个新连接（子通道）的初始化逻辑，添加入站和出站处理器
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline(); // 获取管道

                // 入站处理器
                pipeline.addLast(new LineBasedFrameDecoder(1024)); // 按行切分，最多 1024 字节.
                pipeline.addLast(new StringDecoder()); // Buffer解码成字符串
                pipeline.addLast(new StatusHandler()); // 自定义状态处理器(日志)
                pipeline.addLast(new ResponseHandler()); // 自定义回复消息

                // 出站处理器
                pipeline.addLast(new StringEncoder()); // 字符串编码成Buffer
            }
        });

        ChannelFuture bindFuture = serverBootstrap.bind(8987); // 让Bootstrap监听端口, 返回的是一个future
        bindFuture.addListener( future -> {     // 可以通过添加监听器, 来检查监听端口是否成功和失败, 然后执行相关的操作.
            if (bindFuture.isSuccess()) System.out.println("服务端监听端口 8987 成功!");
            else System.out.println("服务器监听端口失败, 请检查端口是否已经被占用!");
        });
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
            ctx.channel().writeAndFlush("回复消息 -> " + msg + "\n");
        }
    }

}



