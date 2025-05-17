package com.github.jesusmrs05.mcforgecommander.common;

import java.io.Serializable;

public class ServerPacket implements Serializable {
    private final Serializable serializable;
    public ServerPacket(Serializable serializable) {
        this.serializable = serializable;
    }
    public Serializable getSerializable() {
        return serializable;
    }
}