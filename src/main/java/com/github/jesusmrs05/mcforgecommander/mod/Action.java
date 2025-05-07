package com.github.jesusmrs05.mcforgecommander.mod;

import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum Action implements Runnable {
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
		public void run() {
			isMoving = !isMoving;
			Logger logger = LogManager.getLogger("MCForgeCommander");

			if (isMoving) {
				MinecraftForge.EVENT_BUS.register(movementListener);
				logger.info("Movimiento hacia adelante ACTIVADO");
			} else {
				MinecraftForge.EVENT_BUS.unregister(movementListener);
				logger.info("Movimiento hacia adelante DESACTIVADO");
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
		public void run() {
			isMoving = !isMoving;
			Logger logger = LogManager.getLogger("MCForgeCommander");

			if (isMoving) {
				MinecraftForge.EVENT_BUS.register(movementListener);
				logger.info("Movimiento hacia atrás ACTIVADO");
			} else {
				MinecraftForge.EVENT_BUS.unregister(movementListener);
				logger.info("Movimiento hacia atrás DESACTIVADO");
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
		public void run() {
			isMoving = !isMoving;
			Logger logger = LogManager.getLogger("MCForgeCommander");

			if (isMoving) {
				MinecraftForge.EVENT_BUS.register(movementListener);
				logger.info("Movimiento a la izquierda ACTIVADO");
			} else {
				MinecraftForge.EVENT_BUS.unregister(movementListener);
				logger.info("Movimiento a la izquierda DESACTIVADO");
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
		public void run() {
			isMoving = !isMoving;
			Logger logger = LogManager.getLogger("MCForgeCommander");

			if (isMoving) {
				MinecraftForge.EVENT_BUS.register(movementListener);
				logger.info("Movimiento a la derecha ACTIVADO");
			} else {
				MinecraftForge.EVENT_BUS.unregister(movementListener);
				logger.info("Movimiento a la derecha DESACTIVADO");
			}
		}
	};
}
