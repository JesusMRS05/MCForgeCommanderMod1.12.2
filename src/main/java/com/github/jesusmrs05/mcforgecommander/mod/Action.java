package com.github.jesusmrs05.mcforgecommander.mod;

import com.github.jesusmrs05.mcforgecommander.common.TouchCapture;
import com.github.jesusmrs05.mcforgecommander.server.threads.ConverterThread;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public enum Action implements Consumer<Serializable> {
    TOGGLE_MOVE_FORWARD {
        private boolean isMoving = false;
        private final Object movementListener = new Object() {
            @SubscribeEvent
            public void onInputUpdate(InputUpdateEvent event) {
                if (isMoving) {
                    event.getMovementInput().moveForward = 1.0F;
                }
            }
        };

        @Override
        public void accept(Serializable params) {
            boolean shouldMove = false;
            if (params instanceof Boolean) {
                shouldMove = (Boolean) params;
            }
            Logger logger = LogManager.getLogger("MCForgeCommander");

            if (shouldMove && !isMoving) {
                MinecraftForge.EVENT_BUS.register(movementListener);
                logger.info("Move forward ENABLED");
                isMoving = true;
            } else if (!shouldMove && isMoving) {
                MinecraftForge.EVENT_BUS.unregister(movementListener);
                logger.info("Move forward DISABLED");
                isMoving = false;
            }
        }
    },

    TOGGLE_MOVE_BACKWARD {
        private boolean isMoving = false;
        private final Object movementListener = new Object() {
            @SubscribeEvent
            public void onInputUpdate(InputUpdateEvent event) {
                if (isMoving) {
                    event.getMovementInput().moveForward = -1.0F;
                }
            }
        };

        @Override
        public void accept(Serializable params) {
            boolean shouldMove = false;
            if (params instanceof Boolean) {
                shouldMove = (Boolean) params;
            }
            Logger logger = LogManager.getLogger("MCForgeCommander");

            if (shouldMove && !isMoving) {
                MinecraftForge.EVENT_BUS.register(movementListener);
                logger.info("Move backward ENABLED");
                isMoving = true;
            } else if (!shouldMove && isMoving) {
                MinecraftForge.EVENT_BUS.unregister(movementListener);
                logger.info("Move backward DISABLED");
                isMoving = false;
            }
        }
    },

    TOGGLE_MOVE_LEFT {
        private boolean isMoving = false;
        private final Object movementListener = new Object() {
            @SubscribeEvent
            public void onInputUpdate(InputUpdateEvent event) {
                if (isMoving) {
                    event.getMovementInput().moveStrafe = 1.0F;
                }
            }
        };

        @Override
        public void accept(Serializable params) {
            boolean shouldMove = false;
            if (params instanceof Boolean) {
                shouldMove = (Boolean) params;
            }
            Logger logger = LogManager.getLogger("MCForgeCommander");

            if (shouldMove && !isMoving) {
                MinecraftForge.EVENT_BUS.register(movementListener);
                logger.info("Move left ENABLED");
                isMoving = true;
            } else if (!shouldMove && isMoving) {
                MinecraftForge.EVENT_BUS.unregister(movementListener);
                logger.info("Move left DISABLED");
                isMoving = false;
            }
        }
    },

    TOGGLE_MOVE_RIGHT {
        private boolean isMoving = false;
        private final Object movementListener = new Object() {
            @SubscribeEvent
            public void onInputUpdate(InputUpdateEvent event) {
                if (isMoving) {
                    event.getMovementInput().moveStrafe = -1.0F;
                }
            }
        };

        @Override
        public void accept(Serializable params) {
            boolean shouldMove = false;
            if (params instanceof Boolean) {
                shouldMove = (Boolean) params;
            }
            Logger logger = LogManager.getLogger("MCForgeCommander");

            if (shouldMove && !isMoving) {
                MinecraftForge.EVENT_BUS.register(movementListener);
                logger.info("Move right ENABLED");
                isMoving = true;
            } else if (!shouldMove && isMoving) {
                MinecraftForge.EVENT_BUS.unregister(movementListener);
                logger.info("Move right DISABLED");
                isMoving = false;
            }
        }
    },
    SEND_MESSAGE_TO_CHAT {
        @Override
        public void accept(Serializable params) {
            Logger logger = LogManager.getLogger("MCForgeCommander");

            if (params instanceof String) {
                try {
                    String message = (String) params;
                    Minecraft mc = Minecraft.getMinecraft();
                    EntityPlayerSP player = mc.player;

                    if (player != null) {
                        try {
                            mc.getConnection().sendPacket(new CPacketChatMessage(message));
                            logger.info("Message sent");
                        } catch (NullPointerException npe) {
                            logger.error("The connection is null.");
                        }
                    } else {
                        logger.error("Player is null.");
                    }
                } catch (ClassCastException cce) {
                    logger.error("The parameter to send the message is not a string.");
                }
            }
        }
    },
    SCREEN_TOUCH {
        @Override
        public void accept(Serializable params) {
            if (!(params instanceof TouchCapture)) return;

            TouchCapture capture = (TouchCapture) params;
            if (capture == null) return;

            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.player;

            if (mc.currentScreen != null) {
                int inputWidth = capture.getInputWidth();
                int inputHeight = capture.getInputHeight();

                int screenWidth = mc.displayWidth;
                int screenHeight = mc.displayHeight;

                if (inputWidth <= 0 || inputHeight <= 0) {
                    LogManager.getLogger("MCForgeCommander").error("Resolución de entrada inválida");
                    return;
                }

                int scaledX = capture.getX() * screenWidth / inputWidth;
                int rawY = capture.getY() * screenHeight / inputHeight;
                int scaledY = screenHeight - rawY;

                Mouse.setCursorPosition(scaledX, scaledY);

                ScaledResolution scaled = new ScaledResolution(mc);
                int guiX = scaledX * scaled.getScaledWidth() / screenWidth;
                int guiY = rawY * scaled.getScaledHeight() / screenHeight;

                if (capture.isClick()) {
                    mc.addScheduledTask(() -> {
                        try {
                            Method clickMethod = GuiScreen.class.getDeclaredMethod("mouseClicked", int.class, int.class, int.class);
                            clickMethod.setAccessible(true);
                            clickMethod.invoke(mc.currentScreen, guiX, guiY, 0);

                            Method releaseMethod = GuiScreen.class.getDeclaredMethod("mouseReleased", int.class, int.class, int.class);
                            releaseMethod.setAccessible(true);
                            releaseMethod.invoke(mc.currentScreen, guiX, guiY, 0);
                        } catch (Exception e) {
                        }
                    });
                }
            } else {
                if (player != null && capture.isMovement()) {
                    player.rotationYaw += capture.getDeltaYaw();
                    player.rotationPitch -= capture.getDeltaPitch();

                    if (player.rotationPitch > 90.0F) player.rotationPitch = 90.0F;
                    if (player.rotationPitch < -90.0F) player.rotationPitch = -90.0F;
                }
            }
        }
    },
    PRESS_CHAT_KEY {
        @Override
        public void accept(Serializable params) {
            if (params instanceof Boolean) {
                boolean open = (Boolean) params;
                if (open) {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiChat());
                } else {
                    Minecraft.getMinecraft().displayGuiScreen(null); // Cierra cualquier GUI
                }
            }
        }
    },

    PRESS_INVENTORY_KEY {
        @Override
        public void accept(Serializable params) {
            if (params instanceof Boolean) {
                boolean open = (Boolean) params;
                if (open) {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(Minecraft.getMinecraft().player));
                } else {
                    Minecraft.getMinecraft().displayGuiScreen(null);
                }
            }
        }
    },
    PRESS_MENU_KEY {
        @Override
        public void accept(Serializable params) {
            Minecraft.getMinecraft().displayInGameMenu();
        }
    },
    //Goes from 0 to 8
    PRESS_CERTAIN_HOTBAR_KEY {
        @Override
        public void accept(Serializable params) {
            if (params instanceof Integer) {
                int hotbarSlot = (Integer) params;
                Minecraft.getMinecraft().player.inventory.currentItem = hotbarSlot;
            }
        }
    },
    PRESS_JUMP_KEY {
        @Override
        public void accept(Serializable params) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.player;

            if (player != null && player.onGround) {
                player.jump();
            }
        }
    },
    SET_FPS {
        @Override
        public void accept(Serializable params) {
            if (params instanceof Integer) {
                int fps = (Integer) params;
                MCForgeCommander.setFPS(fps);
                Minecraft minecraft = Minecraft.getMinecraft();
                minecraft.gameSettings.limitFramerate = fps;
            }
        }
    },
    SET_JPEG_QUALITY {
        Logger logger = LogManager.getLogger("MCForgeCommander");
        @Override
        public void accept(Serializable params) {
            if (params instanceof Float) {
                float quality = (Float) params;
                ConverterThread.setJpegQuality(quality);
                logger.info("JPEG Quality set to: " + quality);
            }
        }
    };
}