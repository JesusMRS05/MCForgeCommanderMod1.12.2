package com.example.modid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class ExampleMod {

	public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
	private static final int PORT = 6000;
	private static boolean moveForward = false;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOGGER.info("Hello From {}!", Tags.MOD_NAME);
		MinecraftForge.EVENT_BUS.register(this); // Registra el evento de movimiento
		startServer();
	}

	private void startServer() {
		new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(PORT)) {
				LOGGER.info("Servidor escuchando en el puerto " + PORT);

				while (true) {
					try (Socket clientSocket = serverSocket.accept();
					     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

						String inputLine;
						while ((inputLine = in.readLine()) != null) {
							LOGGER.info("Recibido: " + inputLine);

							boolean tieneGUI = Minecraft.getMinecraft().currentScreen != null;
							LOGGER.info("¿Interfaz activa?: " + (tieneGUI ? "Sí" : "No"));

							if ("delante".equalsIgnoreCase(inputLine)) {
								movePlayerForward();
							} else {
								sendMessageAsPlayer(inputLine);
							}
						}
					} catch (Exception e) {
						LOGGER.error("Error al procesar la conexión del cliente", e);
					}
				}
			} catch (Exception e) {
				LOGGER.error("Error al iniciar el servidor", e);
			}
		}).start();
	}


	private void movePlayerForward() {
		LOGGER.info("Moviendo al jugador hacia adelante...");
		moveForward = true;

		new Thread(() -> {
			try {
				Thread.sleep(3000); // Mantiene el movimiento por 3 segundos
			} catch (InterruptedException e) {
				LOGGER.error("Error en el movimiento del jugador", e);
			}
			moveForward = false;
			LOGGER.info("Movimiento detenido.");
		}).start();
	}

	private void sendMessageAsPlayer(String message) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.player;

		if (player != null) {
			// Enviar el mensaje como si fuera el jugador escribiendo en el chat
			mc.getConnection().sendPacket(new CPacketChatMessage(message)); // Simula el envío de un mensaje
			LOGGER.info("Mensaje enviado como si el jugador lo escribiera: " + message);
		}
	}

	@SubscribeEvent
	public void onInputUpdate(InputUpdateEvent event) {
		if (moveForward) {
			event.getMovementInput().moveForward = 1.0F; // Simula la tecla W presionada
		}
	}
}
