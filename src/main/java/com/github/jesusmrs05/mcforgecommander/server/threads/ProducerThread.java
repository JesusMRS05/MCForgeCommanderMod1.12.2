package com.github.jesusmrs05.mcforgecommander.server.threads;

import com.github.jesusmrs05.mcforgecommander.common.Command;
import com.github.jesusmrs05.mcforgecommander.mod.config.Config;
import com.github.jesusmrs05.mcforgecommander.server.Server;

import java.io.IOException;
import java.util.logging.Logger;

public class ProducerThread extends Thread {
    @Override
    public void run() {
        Server server = Server.getInstance();
        try {
            while (!isInterrupted()) {
                Command command = server.getCommand();
                if (command != null) {
                    server.add(command);
                }
            }
        } catch (InterruptedException ie){
            interrupt();
        } catch (IOException ioe) {
            server.close(Config.enableServer);
        } catch (NullPointerException npe) {
            Logger.getLogger("MCForgeCommander").info("ProducerThread NullPointerException");
        }
    }
}
