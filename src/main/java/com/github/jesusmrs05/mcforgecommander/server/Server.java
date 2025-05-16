package com.github.jesusmrs05.mcforgecommander.server;

import com.github.jesusmrs05.mcforgecommander.Tags;
import com.github.jesusmrs05.mcforgecommander.common.Command;
import com.github.jesusmrs05.mcforgecommander.mod.config.Config;
import com.github.jesusmrs05.mcforgecommander.server.threads.ConsumerThread;
import com.github.jesusmrs05.mcforgecommander.server.threads.ConverterThread;
import com.github.jesusmrs05.mcforgecommander.server.threads.ProducerThread;
import com.github.jesusmrs05.mcforgecommander.server.threads.StreamerThread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    private static final int MAX_QUEUE_SIZE = 120;
    private static Server server;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private final BlockingQueue<Command> commands = new LinkedBlockingQueue<>();
    private final BlockingQueue<byte[]> imageQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<FrameData> frameDataQueue = new LinkedBlockingQueue<>();
    private ProducerThread producer;
    private ConsumerThread consumer;
    private StreamerThread streamer;
    private ConverterThread converter;
    private boolean isOn;

    public static synchronized Server getInstance() {
        if (server == null) {
            server = new Server();
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
                    isOn = true;
                    producer = new ProducerThread();
                    consumer = new ConsumerThread();
                    converter = new ConverterThread();
                    streamer = new StreamerThread();
                    serverSocket = new ServerSocket();
                    serverSocket.setSoTimeout(TIME_OUT);
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(port));
                    LOGGER.info("Waiting for client...");
                    clientSocket = serverSocket.accept();
                    LOGGER.info("Client connected");
                    output = new ObjectOutputStream(clientSocket.getOutputStream());
                    input = new ObjectInputStream(clientSocket.getInputStream());
                    String password = input.readUTF();
                    while(!password.equals(Config.key)){
                        close(Config.enableServer);
                        output.writeUTF("Incorrect Password");
                        clientSocket = serverSocket.accept();
                        output = new ObjectOutputStream(clientSocket.getOutputStream());
                        input = new ObjectInputStream(clientSocket.getInputStream());
                        password = input.readUTF();
                    }
                    output.writeUTF("Welcome");
                    producer.start();
                    consumer.start();
                    converter.start();
                    streamer.start();
                } catch (IOException e) {
                    LOGGER.error("Error starting the server: {}", e.getMessage());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String stackTrace = sw.toString();
                    LOGGER.error(stackTrace);
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
        try {
            producer.interrupt();
            consumer.interrupt();
            converter.interrupt();
            streamer.interrupt();
            output.close();
            input.close();
            clientSocket.close();
            serverSocket.close();
            commands.clear();
            imageQueue.clear();
            frameDataQueue.clear();
        } catch (IOException e) {
            LOGGER.error("Error closing the server: {}", e.getMessage());
        }
        if (restart) {
            startServer(Integer.parseInt(Config.port));
        }
    }

    public void add(Command command) throws InterruptedException{
        commands.put(command);
    }

    public Command get() throws InterruptedException {
        Command command = commands.take();
        if(commands.size() > MAX_QUEUE_SIZE){
            commands.clear();
        }
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
        if(imageQueue.size() > MAX_QUEUE_SIZE){
            imageQueue.clear();
        }
        return bytes;
    }

    public Socket getClientSocket(){
        return clientSocket;
    }

    public void enqueueFrameData(FrameData frameData) throws InterruptedException {
        frameDataQueue.put(frameData);
    }

    public FrameData takeFrameData() throws InterruptedException {
        FrameData frameData = frameDataQueue.take();
        if(frameDataQueue.size() > MAX_QUEUE_SIZE){
            frameDataQueue.clear();
        }
        return frameData;
    }

    public boolean isOn(){
        return isOn;
    }
}