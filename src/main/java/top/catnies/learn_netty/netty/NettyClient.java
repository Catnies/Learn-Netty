package top.catnies.learn_netty.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.TimeUnit;

public class NettyClient {

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();  // 创建客户端的 Bootstrap
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group);   // 客户端只需要一个线程组（EventLoopGroup），负责连接和处理读写。
        bootstrap.channel(NioSocketChannel.class);  // 指定 Netty 客户端使用的通道类型. NioSocketChannel 是对 NIO 原生 SocketChannel 的封装.
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {  // 为 ServerBootstrap 添加一个子处理器
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline(); // 获取管道

                // 出站处理器
                pipeline.addLast(new StringEncoder()); // 字符串编码成Buffer

                // 入站处理器
                pipeline.addLast(new LineBasedFrameDecoder(1024)); // 按行切分，最多 1024 字节.
                pipeline.addLast(new StringDecoder()); // Buffer解码成字符串
                pipeline.addLast(new PrintHandler());   // 自定义打印解码处理器
            }
        });


        ChannelFuture connectFuture = bootstrap.connect("127.0.0.1", 8987); // 指定Bootstrap的目标端口.
        connectFuture.addListener(connect -> {
            if (connect.isSuccess()) {
                System.out.println("客户端成功链接了 8987 端口.");
                EventLoop eventLoop = connectFuture.channel().eventLoop();  // 获取channel的eventloop, 因为客户端和服务端是1对1的
                eventLoop.scheduleAtFixedRate(() -> {   // 每隔一秒定时向服务端发送消息
                    connectFuture.channel().writeAndFlush("hello server! at " + System.currentTimeMillis() + "\n");
                }, 0, 1, TimeUnit.SECONDS);
            } else { System.out.println("客户端链接失败, 请检查服务端是否在线!"); }
        });
    }


    static class PrintHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("[日志] 收到来自服务器: " + ctx.channel().remoteAddress() + " 的消息, 内容是: " + msg + "\n");
        }
    }
}
