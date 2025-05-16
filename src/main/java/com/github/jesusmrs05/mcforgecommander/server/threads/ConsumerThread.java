package com.github.jesusmrs05.mcforgecommander.server.threads;

import com.github.jesusmrs05.mcforgecommander.common.Command;
import com.github.jesusmrs05.mcforgecommander.common.Instruction;
import com.github.jesusmrs05.mcforgecommander.mod.Action;
import com.github.jesusmrs05.mcforgecommander.server.Server;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ConsumerThread extends Thread {
    private BlockingQueue<Command> movementCommands = new LinkedBlockingQueue<>();
    private BlockingQueue<Command> chatCommands = new LinkedBlockingQueue<>();

    private Runnable movementThreadRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Command command = getMovement();
                    Serializable params = command.getParams();
                    Instruction instruction = command.getInstruction();
                    Action action = Action.valueOf(instruction.name());
                    action.accept(params);
                }
            } catch (NullPointerException npe) {
                Logger.getLogger("MCForgeCommander").info("ConsumerThreadMovement NullPointerException");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    };

    private Runnable chatThreadRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Command command = getChat();
                    Serializable params = command.getParams();
                    Instruction instruction = command.getInstruction();
                    Action action = Action.valueOf(instruction.name());
                    action.accept(params);
                }
            } catch (NullPointerException npe) {
                Logger.getLogger("MCForgeCommander").info("ConsumerThreadChat NullPointerException");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    };

    @Override
    public void run() {
        Server server = Server.getInstance();
        Thread movementThread = new Thread(movementThreadRunnable);
        Thread chatThread = new Thread(chatThreadRunnable);
        movementThread.start();
        chatThread.start();
        try {
            while (!isInterrupted()) {
                Command command = server.get();
                if (command.getInstruction().name().matches(Instruction.TOGGLE_MOVE_FORWARD.name() + "|" +
                        Instruction.TOGGLE_MOVE_BACKWARD.name() + "|" +
                        Instruction.TOGGLE_MOVE_LEFT.name() + "|" +
                        Instruction.TOGGLE_MOVE_RIGHT.name() + "|" +
                        Instruction.SCREEN_TOUCH.name())) {
                    addMovement(command);
                } else if (command.getInstruction().name().matches(Instruction.SEND_MESSAGE_TO_CHAT.name() + "|" +
                        Instruction.PRESS_CHAT_KEY.name())) {
                    addChat(command);
                }
            }
        } catch (NullPointerException npe) {
            Logger.getLogger("MCForgeCommander").info("ConsumerThread NullPointerException");
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            movementThread.interrupt();
            chatThread.interrupt();
        }
    }

    public void addMovement(Command command) throws InterruptedException {
        movementCommands.put(command);
    }

    public void addChat(Command command) throws InterruptedException {
        chatCommands.put(command);
    }

    public Command getMovement() throws InterruptedException {
        return movementCommands.take();
    }

    public Command getChat() throws InterruptedException {
        return chatCommands.take();
    }
}
