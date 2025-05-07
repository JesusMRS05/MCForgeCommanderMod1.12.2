package com.github.jesusmrs05.mcforgecommander.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
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
			isMoving = !isMoving;
			Logger logger = LogManager.getLogger("MCForgeCommander");

			if (isMoving) {
				MinecraftForge.EVENT_BUS.register(movementListener);
				logger.info("Move forward ENABLED");
			} else {
				MinecraftForge.EVENT_BUS.unregister(movementListener);
				logger.info("Move forward DISABLED");
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
			isMoving = !isMoving;
			Logger logger = LogManager.getLogger("MCForgeCommander");

			if (isMoving) {
				MinecraftForge.EVENT_BUS.register(movementListener);
				logger.info("Move backward ENABLED");
			} else {
				MinecraftForge.EVENT_BUS.unregister(movementListener);
				logger.info("Move backward DISABLED");
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
			isMoving = !isMoving;
			Logger logger = LogManager.getLogger("MCForgeCommander");

			if (isMoving) {
				MinecraftForge.EVENT_BUS.register(movementListener);
				logger.info("Move left ENABLED");
			} else {
				MinecraftForge.EVENT_BUS.unregister(movementListener);
				logger.info("Move left DISABLED");
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
			isMoving = !isMoving;
			Logger logger = LogManager.getLogger("MCForgeCommander");

			if (isMoving) {
				MinecraftForge.EVENT_BUS.register(movementListener);
				logger.info("Move right ENABLED");
			} else {
				MinecraftForge.EVENT_BUS.unregister(movementListener);
				logger.info("Move right DISABLED");
			}
		}
	},

	SEND_MESSAGE_TO_CHAT {
		@Override
		public void accept(Serializable params) {
			Logger logger = LogManager.getLogger("MCForgeCommander");

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
	};
}