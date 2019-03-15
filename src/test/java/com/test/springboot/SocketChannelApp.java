package com.test.springboot;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketChannelApp {

    public static void main(String[] args) throws Exception {
        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 8989);
        SocketChannel sc = SocketChannel.open();
        sc.connect(addr);
        sc.configureBlocking(false);

        while (!sc.finishConnect()) {
            doSomethings();
        }

        //Do something with the connected socket
        ByteBuffer buffer = ByteBuffer.wrap(new String("Hello server!").getBytes());
        sc.write(buffer);
        sc.close();

    }

    private static void doSomethings() {
        System.out.println("do something useless!");
    }
}
