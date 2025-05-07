package com.github.jesusmrs05.mcforgecommander.mod.config;

import com.github.jesusmrs05.mcforgecommander.Tags;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {}

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new ModConfigGui(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    public static class ModConfigGui extends GuiConfig {
        public ModConfigGui(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), Tags.MOD_ID, false, false, "Configuración de MCForgeCommander");
        }

        private static List<IConfigElement> getConfigElements() {
            return new ArrayList<>();
        }
    }
}