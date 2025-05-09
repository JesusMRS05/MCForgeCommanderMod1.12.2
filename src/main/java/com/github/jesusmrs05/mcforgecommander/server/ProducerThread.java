package com.github.jesusmrs05.mcforgecommander.server;

import com.github.jesusmrs05.mcforgecommander.common.Command;
import com.github.jesusmrs05.mcforgecommander.common.Instruction;
import com.github.jesusmrs05.mcforgecommander.mod.Action;

import java.io.Serializable;

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
        }
    }
}
