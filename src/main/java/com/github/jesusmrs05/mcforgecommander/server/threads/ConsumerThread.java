package com.github.jesusmrs05.mcforgecommander.server.threads;

import com.github.jesusmrs05.mcforgecommander.common.Command;
import com.github.jesusmrs05.mcforgecommander.common.Instruction;
import com.github.jesusmrs05.mcforgecommander.mod.Action;
import com.github.jesusmrs05.mcforgecommander.server.Server;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

// TODO The "movement" and "chat" distinction should be renamed to something more descriptive. They served a different purpose originally
public class ConsumerThread extends Thread {
    private BlockingQueue<Command> movementCommands = new LinkedBlockingQueue<>(Server.MAX_QUEUE_SIZE);
    private BlockingQueue<Command> chatCommands = new LinkedBlockingQueue<>(Server.MAX_QUEUE_SIZE);
    private BlockingQueue<Command> clickCommands = new LinkedBlockingQueue<>(Server.MAX_QUEUE_SIZE);

    private Runnable movementThreadRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Command command = getMovement();
                    Serializable params = command.getParams();
                    Instruction instruction = command.getInstruction();
                    Action action = Action.valueOf(instruction.name());
                    try {
                        action.accept(params);
                    } catch (Exception e) {
                        Logger.getLogger("MCForgeCommander").info("ConsumerThreadMovement Exception while executing " + instruction.name());
                    }
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
                    try {
                        action.accept(params);
                    } catch (Exception e) {
                        Logger.getLogger("MCForgeCommander").info("ConsumerThreadChat Exception while executing " + instruction.name());
                    }
                }
            } catch (NullPointerException npe) {
                Logger.getLogger("MCForgeCommander").info("ConsumerThreadChat NullPointerException");
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    };

    private Runnable clickThreadRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Command command = getClick();
                    Serializable params = command.getParams();
                    Instruction instruction = command.getInstruction();
                    Action action = Action.valueOf(instruction.name());
                    try {
                        action.accept(params);
                    } catch (Exception e) {
                        Logger.getLogger("MCForgeCommander").info("ConsumerThreadChat Exception while executing " + instruction.name());
                    }
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
        movementThread.setName("MovementThread");
        Thread chatThread = new Thread(chatThreadRunnable);
        chatThread.setName("ChatThread");
        Thread clickThread = new Thread(clickThreadRunnable);
        clickThread.setName("ClickThread");
        movementThread.start();
        chatThread.start();
        clickThread.start();
        try {
            while (!isInterrupted()) {
                Command command = server.get();
                Instruction instruction = command.getInstruction();
                switch (instruction) {
                    case TOGGLE_MOVE_BACKWARD:
                    case TOGGLE_MOVE_FORWARD:
                    case TOGGLE_MOVE_LEFT:
                    case TOGGLE_MOVE_RIGHT:
                    case SCREEN_TOUCH:
                    case PRESS_JUMP_KEY:
                        addMovement(command);
                        break;
                    case LEFT_CLICK:
                    case RIGHT_CLICK:
                        addClick(command);
                        break;
                    case SEND_MESSAGE_TO_CHAT:
                    case PRESS_CHAT_KEY:
                    case PRESS_INVENTORY_KEY:
                    case PRESS_MENU_KEY:
                    case PRESS_CERTAIN_HOTBAR_KEY:
                        addChat(command);
                        break;
                }
            }
        } catch (NullPointerException npe) {
            Logger.getLogger("MCForgeCommander").info("ConsumerThread NullPointerException");
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        movementThread.interrupt();
        chatThread.interrupt();
        clickThread.interrupt();
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

    public Command getClick() throws InterruptedException {
        return clickCommands.take();
    }

    public void addClick(Command command) throws InterruptedException {
        clickCommands.put(command);
    }
}
