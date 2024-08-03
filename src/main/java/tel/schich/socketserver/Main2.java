package tel.schich.socketserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Main2 {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Missing arguments! Usage: <bind address> <port> <buffer size>");
            System.exit(1);
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(Integer.parseInt(args[2]));

        final Selector selector = Selector.open();
        final ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(InetAddress.getByName(args[0]), Integer.parseInt(args[1])), 50);
        serverChannel.configureBlocking(false);
        final SelectionKey acceptKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        int i = 0;
        while (true) {
            final int selectedAmount = selector.select();
            if (selectedAmount == 0) {
                continue;
            }
            final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                final SelectionKey key = iterator.next();
                if (acceptKey == key) {
                    final SocketChannel clientChannel = serverChannel.accept();
                    clientChannel.configureBlocking(false);
                    System.out.println("Accepted via selector: " + (i++));
                    clientChannel.register(selector, SelectionKey.OP_READ);
                } else {
                    final SocketChannel channel = (SocketChannel) key.channel();
                    if (channel.read(buffer) == -1) {
                        channel.close();
                        System.out.println("Disconnected: " + (--i));
                    } else {
                        buffer.clear();
//                        System.out.println("Read with selector!");
                    }
                }
                iterator.remove();
            }
        }
    }
}
