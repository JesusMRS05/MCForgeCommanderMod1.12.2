package com.github.jesusmrs05.mcforgecommander.server;

import com.github.jesusmrs05.mcforgecommander.Tags;
import com.github.jesusmrs05.mcforgecommander.common.Command;
import com.github.jesusmrs05.mcforgecommander.common.ServerPacket;
import com.github.jesusmrs05.mcforgecommander.mod.Action;
import com.github.jesusmrs05.mcforgecommander.mod.config.Config;
import com.github.jesusmrs05.mcforgecommander.server.threads.ConsumerThread;
import com.github.jesusmrs05.mcforgecommander.server.threads.ConverterThread;
import com.github.jesusmrs05.mcforgecommander.server.threads.ProducerThread;
import com.github.jesusmrs05.mcforgecommander.server.threads.StreamerThread;

import net.minecraft.client.Minecraft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static final int MAX_QUEUE_SIZE = 100;
    private static Server server;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private final BlockingQueue<Command> commands = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private final BlockingQueue<byte[]> imageQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private final BlockingQueue<FrameData> frameDataQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private ProducerThread producer;
    private ConsumerThread consumer;
    private StreamerThread streamer;
    private ConverterThread converter;
    private volatile boolean isOn;

    public static synchronized Server getInstance() {
        if (server == null) {
            server = new Server();
        }
        return server;
    }

    private Server() {
    }

    public void startServer(int port) {
        final int TIME_OUT = 3600000;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    isOn = true;
                    producer = new ProducerThread();
                    producer.setName("ProducerThread");
                    consumer = new ConsumerThread();
                    consumer.setName("ConsumerThread");
                    converter = new ConverterThread();
                    converter.setName("ConverterThread");
                    streamer = new StreamerThread();
                    streamer.setName("StreamerThread");

                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(port));

                    LOGGER.info("Waiting for client...");
                    clientSocket = serverSocket.accept();
                    LOGGER.info("Client connected with IP: {}", clientSocket.getInetAddress().getHostAddress());
                    clientSocket.setTcpNoDelay(true);

                    LOGGER.info("Creating output");
                    output = new ObjectOutputStream(clientSocket.getOutputStream());
                    output.flush();

                    LOGGER.info("Creating input");
                    input = new ObjectInputStream(clientSocket.getInputStream());

                    LOGGER.info("Going to read");
                    String password = input.readUTF();

                    while (!safeEquals(Config.key, password)) {
                        LOGGER.info("Incorrect Password");
                        output.writeObject("Incorrect Password");
                        output.flush();
                        clientSocket.close();
                        LOGGER.info("Waiting for client...");
                        clientSocket = serverSocket.accept();
                        LOGGER.info("Client connected");
                        output = new ObjectOutputStream(clientSocket.getOutputStream());
                        output.flush();
                        input = new ObjectInputStream(clientSocket.getInputStream());
                        password = input.readUTF();
                    }

                    synchronized (output) {
                        LOGGER.info("Sending Response");
                        output.writeUTF("Welcome");
                        output.flush();
                        LOGGER.info("Response Sent");
                        Minecraft mc = Minecraft.getMinecraft();
                        ServerPacket initialGUIStatusPacket;
                        if (mc.currentScreen == null) {
                            initialGUIStatusPacket = new ServerPacket(ServerPacket.GUIStatus.NONE, ServerPacket.Type.GUI_STATUS);
                        } else {
                            initialGUIStatusPacket = new ServerPacket(ServerPacket.GUIStatus.OTHER, ServerPacket.Type.GUI_STATUS);
                        }
                        output.writeObject(initialGUIStatusPacket);
                        output.flush();
                    }

                    Action.SET_FPS.accept(input.readInt());
                    Action.SET_JPEG_QUALITY.accept(input.readFloat());

                    producer.start();
                    consumer.start();
                    converter.start();
                    streamer.start();
                } catch (SocketException se) {
                    LOGGER.error("Error starting the server: {}", se.getMessage());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    se.printStackTrace(pw);
                    String stackTrace = sw.toString();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                    }
                    server.close(Config.enableServer);
                } catch (StreamCorruptedException sce) {
                    LOGGER.error("Error starting the server: {}", sce.getMessage());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    sce.printStackTrace(pw);
                    String stackTrace = sw.toString();
                    server.close(Config.enableServer);
                } catch (IOException e) {
                    LOGGER.error("Error starting the server: {}", e.getMessage());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String stackTrace = sw.toString();
                    LOGGER.error(stackTrace);
                    server.close(Config.enableServer);
                } catch (Exception e) {
                    LOGGER.error("Error starting the server: {}", e.getMessage());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String stackTrace = sw.toString();
                    LOGGER.error(stackTrace);
                    server.close(Config.enableServer);
                }
            }
        }).start();
    }

    public Command getCommand() throws IOException {
        Command command;
        try {
            command = (Command) input.readObject();
        } catch (ClassNotFoundException cnfe) {
            command = null;
            LOGGER.error("Error getting instruction: {}", cnfe.getMessage());
        }
        return command;
    }

    public void close(boolean restart) {
        isOn = false;
        if (producer != null) producer.interrupt();
        if (consumer != null) consumer.interrupt();
        if (converter != null) converter.interrupt();
        if (streamer != null) streamer.interrupt();
        commands.clear();
        imageQueue.clear();
        frameDataQueue.clear();
        try {
            if (output != null) output.reset();
        } catch (IOException ioe) {
            LOGGER.error("Error closing the server: {}", ioe.getMessage());
        }
        try {
            if (output != null) output.close();
        } catch (IOException ioe) {
            LOGGER.error("Error closing the server: {}", ioe.getMessage());
        }
        try {
            if (input != null) input.close();
        } catch (IOException ioe) {
            LOGGER.error("Error closing the server: {}", ioe.getMessage());
        }
        try {
            if (clientSocket != null) clientSocket.close();
        } catch (IOException ioe) {
            LOGGER.error("Error closing the server: {}", ioe.getMessage());
        }
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ioe) {
            LOGGER.error("Error closing the server: {}", ioe.getMessage());
        }
        if (restart) {
            startServer(Integer.parseInt(Config.port));
        } else {
            LOGGER.info("Server closed");
        }
    }

    public void add(Command command) throws InterruptedException {
        commands.put(command);
    }

    public Command get() throws InterruptedException {
        Command command = commands.take();
        /*if(commands.size() > MAX_QUEUE_SIZE){
            commands.clear();
        }*/
        return command;
    }

    public ObjectOutputStream getOutput() {
        return output;
    }

    public void enqueueImage(byte[] imageBytes) throws InterruptedException {
        imageQueue.put(imageBytes);
    }

    public byte[] takeImage() throws InterruptedException {
        byte[] bytes = imageQueue.take();
        /*if(imageQueue.size() > MAX_QUEUE_SIZE){
            imageQueue.clear();
        }*/
        return bytes;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void offerFrameData(FrameData frameData) {
        frameDataQueue.offer(frameData);
    }

    public FrameData takeFrameData() throws InterruptedException {
        FrameData frameData = frameDataQueue.take();
        /*if(frameDataQueue.size() > MAX_QUEUE_SIZE){
            frameDataQueue.clear();
        }*/
        return frameData;
    }

    public boolean isOn() {
        return isOn;
    }

    public static boolean safeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        int lenA = a.length();
        int lenB = b.length();
        int diff  = lenA ^ lenB;

        // Recorremos SIEMPRE toda la primera cadena
        for (int i = 0; i < lenA; i++) {
            char ca = a.charAt(i);
            char cb = (i < lenB) ? b.charAt(i) : 0;
            diff |= ca ^ cb;
        }

        return diff == 0;
    }
}