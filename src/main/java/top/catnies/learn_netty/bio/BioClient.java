package top.catnies.learn_netty.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BioClient {
    public static void main(String[] args) throws IOException {
        // 创建一个客户端链接并尝试链接.
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", 2580));

        // 获取客户端链接的输出流, 写入数据后关闭链接.
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("woc 666".getBytes());
        outputStream.flush();
        socket.close();
    }
}
