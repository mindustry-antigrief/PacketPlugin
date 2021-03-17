package com.github.mindustryantigrief;

import mindustry.net.Packet;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class ClientNetworkPacket implements Packet {
    public byte[] content;
    public int sender = -1;

    public ClientNetworkPacket() {}

    public ClientNetworkPacket(byte[] content) {
        this.content = content;
    }

    @Override
    public void read(ByteBuffer buffer) {
        this.sender = buffer.getInt();
        int size = buffer.getInt();
        if (size > 10240) {
            throw new BufferOverflowException();
        }
        content = new byte[size];
        buffer.get(content);
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.putInt(sender);
        buffer.putInt(content.length);
        buffer.put(content);
    }
}
