package tel.schich.socketserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static class Client implements Runnable {
        private final Socket clientSocket;
        private final byte[] buffer;
        private final AtomicInteger i;

        public Client(Socket clientSocket, int bufferSize, AtomicInteger i) {
            this.clientSocket = clientSocket;
            buffer = new byte[bufferSize];
            this.i = i;
        }

        @Override
        public void run() {
            try {
                final InputStream input = clientSocket.getInputStream();
                while (true) {
                    if (input.read(buffer) == -1) {
                        clientSocket.close();
                        System.out.println("Disconnected: " + i.getAndDecrement());
                        break;
                    }
                    // System.out.println("Read!");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Missing arguments! Usage: <bind address> <port> <buffer size>");
            System.exit(1);
        }
        final ServerSocket socket = new ServerSocket(Integer.parseInt(args[1]), 50, InetAddress.getByName(args[0]));
        int bufferSize = Integer.parseInt(args[2]);
        AtomicInteger i = new AtomicInteger(0);
        while (true) {
            final Socket clientSocket = socket.accept();
            System.out.println("Accepted: " + i.incrementAndGet());
            new Thread(new Client(clientSocket, bufferSize, i)).start();
        }
    }
}
