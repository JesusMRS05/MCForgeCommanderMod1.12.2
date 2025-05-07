package com.github.jesusmrs05.mcforgecommander.mod;

import com.example.modid.Tags;
import com.github.jesusmrs05.mcforgecommander.common.Command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    private static Server server;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectInputStream input;

    public static synchronized Server getInstance() {
        if (Server.server == null) {
            Server.server = new Server();
        }
        return server;
    }

    private Server() {
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            LOGGER.error("Error starting the server: {}", e.getMessage());
        }
    }

    public Command getCommand() {
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
            input.close();
            clientSocket.close();
            serverSocket.close();
            server = null;
        } catch (IOException e) {
            LOGGER.error("Error closing the server: {}", e.getMessage());
        }
    }
}