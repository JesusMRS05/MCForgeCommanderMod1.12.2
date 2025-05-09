package com.github.jesusmrs05.mcforgecommander.server;

import com.github.jesusmrs05.mcforgecommander.common.Command;

import java.util.logging.Logger;

public class ProducerThread extends Thread {
    @Override
    public void run() {
        Server server = Server.getInstance();
        try {
            while (!isInterrupted()) {
                Command command = server.getCommand();
                server.add(command);
            }
        } catch (InterruptedException ie){
            Thread.currentThread().interrupt();
        } catch (NullPointerException npe) {
            Logger.getLogger("MCForgeCommander").info("ProducerThread NullPointerException");
        }
    }
}
