package com.github.jesusmrs05.mcforgecommander.server;

import com.github.jesusmrs05.mcforgecommander.Tags;
import com.github.jesusmrs05.mcforgecommander.common.Command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    private static Server server;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectInputStream input;
    private boolean occupied;
    private BlockingQueue<Command> commands = new LinkedBlockingQueue<>();
    private ProducerThread producer;
    private ConsumerThread consumer;

    public static synchronized Server getInstance() {
        if (Server.server == null) {
            Server.server = new Server();
        }
        return server;
    }

    private Server() {
    }

    public void startServer(int port) {
        final int TIME_OUT = 3600000; //milliseconds, 1 hour (this should be changed later)
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setSoTimeout(TIME_OUT);
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(port));
                    clientSocket = serverSocket.accept();
                    input = new ObjectInputStream(clientSocket.getInputStream());
                    producer = new ProducerThread();
                    consumer = new ConsumerThread();
                    producer.start();
                    consumer.start();
                } catch (IOException e) {
                    LOGGER.error("Error starting the server: {}", e.getMessage());
                }
            }
        }).start();
    }

    protected Command getCommand() {
        Command command;
        try {
            command = (Command) input.readObject();
        } catch (ClassNotFoundException | IOException e) {
            command = null;
            LOGGER.error("Error getting instruction: {}", e.getMessage());
        }
        return command;
    }

    public void close() {
        try {
            producer.interrupt();
            consumer.interrupt();
            input.close();
            clientSocket.close();
            serverSocket.close();
            server = null;
        } catch (IOException e) {
            LOGGER.error("Error closing the server: {}", e.getMessage());
        }
    }

    protected void add(Command command) throws InterruptedException{
        commands.put(command);
    }

    protected Command get() throws InterruptedException {
        return commands.take();
    }
}