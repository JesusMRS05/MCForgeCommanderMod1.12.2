package com.github.jesusmrs05.mcforgecommander.server.threads;

import com.github.jesusmrs05.mcforgecommander.common.ServerPacket;
import com.github.jesusmrs05.mcforgecommander.server.Server;

import java.io.ObjectOutputStream;
import java.util.logging.Logger;

public class StreamerThread extends Thread {
    @Override
    public void run() {
        Server server = Server.getInstance();
        ObjectOutputStream output = server.getOutput();

        int sent = 0;

        try {
            while (!isInterrupted()) {
                byte[] imageBytes = server.takeImage();
                synchronized (output) {
                    ServerPacket packet = new ServerPacket(imageBytes, ServerPacket.Type.IMAGE);
                    output.writeObject(packet);
                    if(++sent % 240 == 0){
                        output.reset();
                    }
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
