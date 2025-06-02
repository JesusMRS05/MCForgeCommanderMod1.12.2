package com.github.jesusmrs05.mcforgecommander.mod;

import com.github.jesusmrs05.mcforgecommander.Tags;
import com.github.jesusmrs05.mcforgecommander.common.ServerPacket;
import com.github.jesusmrs05.mcforgecommander.mod.config.Config;
import com.github.jesusmrs05.mcforgecommander.server.FrameData;
import com.github.jesusmrs05.mcforgecommander.server.Server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Logger;

@Mod(
        modid = Tags.MOD_ID,
        name = Tags.MOD_NAME,
        version = Tags.VERSION,
        guiFactory = "com.github.jesusmrs05.mcforgecommander.mod.config.ModGuiFactory"
)
public class MCForgeCommander {

    //TODO add Minecraft.getMinecraft().displayGuiScreen(new GuiAlertDialog("Your Alert Message")); to Server.startServer when something goes wrong

    private long lastCaptureTime = 0;
    private static int frameCaptureIntervalMS = 40;//40 = 25fps; // fps = 1000/frameCaptureIntervalMS, for example, 10 fps = 100ms
    private static int startingFrameLimit = 120;
    private Server server;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.init(event.getSuggestedConfigurationFile());
        server = Server.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
        startingFrameLimit = Minecraft.getMinecraft().gameSettings.limitFramerate;
        Minecraft mc = Minecraft.getMinecraft();
        startingFrameLimit = mc.gameSettings.limitFramerate;

        int frameCaptureInterval = 1000 / frameCaptureIntervalMS;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                File optionsFile = new File(mc.gameDir, "options.txt");
                List<String> lines = Files.readAllLines(optionsFile.toPath());

                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).startsWith("maxFps:")) {
                        int currentLimit = Integer.parseInt(lines.get(i).split(":")[1].trim());
                        if (currentLimit == frameCaptureInterval) {
                            lines.set(i, "maxFps:" + startingFrameLimit);
                            break;
                        }
                    }
                }

                Files.write(optionsFile.toPath(), lines);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (server.isOn() && event.phase == TickEvent.Phase.END) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastCaptureTime) >= frameCaptureIntervalMS && server.getOutput() != null && server.getClientSocket().isConnected()) {
                try {
                    Minecraft mc = Minecraft.getMinecraft();
                    int width = mc.displayWidth;
                    int height = mc.displayHeight;

                    ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
                    GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                    buffer.rewind();

                    FrameData frameData = new FrameData(buffer, width, height);
                    server.offerFrameData(frameData);

                    lastCaptureTime = currentTime;
                } catch (Exception e) {
                    Logger.getLogger("MCForgeCommander").severe("Error capturing frame: " + e.getMessage());
                }
            }
        }
    }

    public static void setFPS(int fps) {
        if (fps <= 0 || fps > 60) {
            throw new IllegalArgumentException("FPS must be between 1 and 60");
        } else {
            frameCaptureIntervalMS = 1000 / fps;
        }
    }

    public static int getStartingFrameLimit() {
        return startingFrameLimit;
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen gui = event.getGui();
        if (server != null && server.getClientSocket() != null && server.getClientSocket().isConnected()) {
            ObjectOutputStream output = server.getOutput();
            try {
                if (gui == null) {
                    ServerPacket packet = new ServerPacket(ServerPacket.GUIStatus.NONE, ServerPacket.Type.GUI_STATUS);
                    output.writeObject(packet);
                    output.flush();
                } else {
                    ServerPacket packet = new ServerPacket(ServerPacket.GUIStatus.OTHER, ServerPacket.Type.GUI_STATUS);
                    synchronized (output) {
                        output.writeObject(packet);
                        output.flush();
                    }
                }
            } catch (IOException e) {
            }
        }
    }
}