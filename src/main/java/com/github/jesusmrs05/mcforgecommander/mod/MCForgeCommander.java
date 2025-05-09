package com.github.jesusmrs05.mcforgecommander.mod;

import com.github.jesusmrs05.mcforgecommander.Tags;
import com.github.jesusmrs05.mcforgecommander.mod.config.Config;
import com.github.jesusmrs05.mcforgecommander.server.FrameData;
import com.github.jesusmrs05.mcforgecommander.server.Server;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

@Mod(
        modid = Tags.MOD_ID,
        name = Tags.MOD_NAME,
        version = Tags.VERSION,
        guiFactory = "com.github.jesusmrs05.mcforgecommander.mod.config.ModGuiFactory"
)
public class MCForgeCommander {

    private long lastCaptureTime = 0;
    private static final long FRAME_CAPTURE_INTERVAL_MS = 33; // fps = 1000/FRAME_CAPTURE_INTERVAL_MS, for example, 10 fps = 100ms
    private Server server;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.init(event.getSuggestedConfigurationFile());
        server = Server.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastCaptureTime) >= FRAME_CAPTURE_INTERVAL_MS && server.getOutput() != null && server.getClientSocket().isConnected()) {
                try {
                    Minecraft mc = Minecraft.getMinecraft();
                    int width = mc.displayWidth;
                    int height = mc.displayHeight;

                    ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
                    GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                    buffer.rewind();

                    FrameData frameData = new FrameData(buffer, width, height);
                    server.enqueueFrameData(frameData);

                    lastCaptureTime = currentTime;
                } catch (Exception e) {
                    Logger.getLogger("MCForgeCommander").severe("Error capturing frame: " + e.getMessage());
                }
            }
        }
    }

}