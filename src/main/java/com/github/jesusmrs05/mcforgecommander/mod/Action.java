package com.github.jesusmrs05.mcforgecommander.mod;

import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

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
	};
}
