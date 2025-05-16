package com.github.jesusmrs05.mcforgecommander.server.threads;

import com.github.jesusmrs05.mcforgecommander.server.Server;

import java.io.ObjectOutputStream;
import java.util.logging.Logger;

public class StreamerThread extends Thread {
    @Override
    public void run() {
        Server server = Server.getInstance();
        ObjectOutputStream output = server.getOutput();

        try {
            while (!isInterrupted()) {
                byte[] imageBytes = server.takeImage();
                synchronized (output) {
                    output.writeInt(imageBytes.length);
                    output.write(imageBytes);
                    output.flush();
                }
            }
        } catch (NullPointerException npe) {
            Logger.getLogger("MCForgeCommander").info("StreamerThread NullPointerException");
        } catch (Exception e) {
            Logger.getLogger("MCForgeCommander").severe("Error streaming: " + e.getMessage());
        }
    }
}
