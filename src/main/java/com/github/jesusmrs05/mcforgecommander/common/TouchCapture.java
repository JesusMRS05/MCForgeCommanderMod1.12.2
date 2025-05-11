package com.github.jesusmrs05.mcforgecommander.common;

import java.io.Serializable;

public class TouchCapture implements Serializable {
    private int x;
    private int y;
    private int action;

    public TouchCapture(int x, int y, int action) {
        this.x = x;
        this.y = y;
        this.action = action;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAction() {
        return action;
    }
}
