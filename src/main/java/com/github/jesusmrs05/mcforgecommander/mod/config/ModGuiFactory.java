package com.github.jesusmrs05.mcforgecommander.mod.config;

import com.github.jesusmrs05.mcforgecommander.Tags;
import com.github.jesusmrs05.mcforgecommander.server.Server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ModGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {
    }

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
            super(parentScreen, getConfigElements(), Tags.MOD_ID, false, false, "MCForgeCommander");
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> list = new ArrayList<>();
            ConfigElement generalCategory = new ConfigElement(Config.config.getCategory(Configuration.CATEGORY_GENERAL));
            list.addAll(generalCategory.getChildElements());
            list.add(new KeyConfigElement());
            return list;
        }

        @Override
        public void onGuiClosed() {
            Config.port = getConfigElements().get(1).get().toString();
            Config.enableServer = Boolean.parseBoolean(getConfigElements().get(0).get().toString());
            Config.save();
            if (Config.enableServer) {
                Server server = Server.getInstance();
                if(!server.isOn()){
                    server.startServer(Integer.parseInt(Config.port));
                }
            } else {
                try {
                    Server.getInstance().close(false);
                } catch (NullPointerException npe) {
                }
            }
        }
    }

    public static class KeyConfigElement implements IConfigElement {
        @Override
        public boolean isProperty() {
            return false;
        }

        @Override
        public Class<? extends GuiConfigEntries.IConfigEntry> getConfigEntryClass() {
            return KeyConfigEntry.class;
        }

        @Override
        public Class<? extends GuiEditArrayEntries.IArrayEntry> getArrayEntryClass() {
            return null;
        }

        @Override
        public String getName() {
            return "Key";
        }

        @Override
        public String getQualifiedName() {
            return "Key";
        }

        @Override
        public String getLanguageKey() {
            return "";
        }

        @Override
        public String getComment() {
            return "Click refresh (↻) to generate new key";
        }

        @Override
        public List<IConfigElement> getChildElements() {
            return null;
        }

        @Override
        public ConfigGuiType getType() {
            return ConfigGuiType.STRING;
        }

        @Override
        public boolean isList() {
            return false;
        }

        @Override
        public boolean isListLengthFixed() {
            return false;
        }

        @Override
        public int getMaxListLength() {
            return 0;
        }

        @Override
        public boolean isDefault() {
            return false;
        }

        @Override
        public boolean showInGui() {
            return true;
        }

        @Override
        public boolean requiresWorldRestart() {
            return false;
        }

        @Override
        public boolean requiresMcRestart() {
            return false;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object[] getList() {
            return new Object[0];
        }

        @Override
        public Object getDefault() {
            return null;
        }

        @Override
        public Object[] getDefaults() {
            return new Object[0];
        }

        @Override
        public void set(Object value) {
        }

        @Override
        public void set(Object[] aVal) {
        }

        @Override
        public String[] getValidValues() {
            return new String[0];
        }

        @Override
        public Object getMinValue() {
            return null;
        }

        @Override
        public Object getMaxValue() {
            return null;
        }

        @Override
        public Pattern getValidationPattern() {
            return null;
        }

        @Override
        public void setToDefault() {
        }
    }

    public static class KeyConfigEntry extends GuiConfigEntries.ButtonEntry {
        private static final int BUTTON_WIDTH = 20;
        private static final int BUTTON_HEIGHT = 20;
        private static final int KEY_LENGTH = 16;
        private String btnText = "";
        private int refreshX;
        private int refreshY;
        private int eyeX;
        private int eyeY;

        public KeyConfigEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
            super(owningScreen, owningEntryList, configElement);
            if (Config.key.equals("")) {
                Config.key = KeyGenerator.generateKey(KEY_LENGTH);
            }
            String asterisks = "";
            for (int i = 0; i < Config.key.length(); i++) {
                asterisks = asterisks + "*";
            }
            btnText = asterisks;
            updateValueButtonText();
        }

        @Override
        public void updateValueButtonText() {
            this.btnValue.displayString = btnText;
            this.btnValue.enabled = false;
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);

            refreshX = x + listWidth - BUTTON_WIDTH * 2 - 4;
            refreshY = y + (slotHeight - BUTTON_HEIGHT) / 2;
            eyeX = x + listWidth - BUTTON_WIDTH - 2;
            eyeY = y + (slotHeight - BUTTON_HEIGHT) / 2;

            GuiButton refreshBtn = new GuiButton(0, refreshX, refreshY, BUTTON_WIDTH, BUTTON_HEIGHT, "↻");
            refreshBtn.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);

            GuiButton eyeBtn = new GuiButton(1, eyeX, eyeY, BUTTON_WIDTH, BUTTON_HEIGHT, "◉");
            eyeBtn.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseEvent) {
            if (isMouseOver(refreshX, refreshY, BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY)) {
                Config.key = KeyGenerator.generateKey(KEY_LENGTH);
                String asterisks = "";
                for (int i = 0; i < Config.key.length(); i++) {
                    asterisks = asterisks + "*";
                }
                btnText = asterisks;
                updateValueButtonText();
                owningScreen.initGui();
            }

            if (isMouseOver(eyeX, eyeY, BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY)) {
                if (Config.key.equals("")) {
                    btnText = "Click ↻ to generate a key";
                } else if (btnText.matches("[\\*]{" + KEY_LENGTH + "}")) {
                    btnText = Config.key;
                } else {
                    String asterisks = "";
                    for (int i = 0; i < Config.key.length(); i++) {
                        asterisks = asterisks + "*";
                    }
                    btnText = asterisks;
                }
                updateValueButtonText();
                owningScreen.initGui();
            }
            super.mouseClicked(mouseX, mouseY, mouseEvent);
        }

        private boolean isMouseOver(int x, int y, int width, int height, int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        @Override
        public void valueButtonPressed(int slotIndex) {
        }

        @Override
        public boolean isDefault() {
            return false;
        }

        @Override
        public void setToDefault() {
        }

        @Override
        public boolean isChanged() {
            return false;
        }

        @Override
        public void undoChanges() {
        }

        @Override
        public boolean saveConfigElement() {
            return false;
        }

        @Override
        public Object getCurrentValue() {
            return null;
        }

        @Override
        public Object[] getCurrentValues() {
            return new Object[0];
        }
    }
}