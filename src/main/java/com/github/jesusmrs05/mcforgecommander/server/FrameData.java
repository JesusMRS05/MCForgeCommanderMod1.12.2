package com.github.jesusmrs05.mcforgecommander.server;

import java.nio.ByteBuffer;

public class FrameData {
    public final ByteBuffer buffer;
    public final int width;
    public final int height;

    public FrameData(ByteBuffer buffer, int width, int height) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }
}
