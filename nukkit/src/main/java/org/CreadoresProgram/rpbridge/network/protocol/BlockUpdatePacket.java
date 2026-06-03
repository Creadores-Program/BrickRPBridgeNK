package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class BlockUpdatePacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.BLOCK_UPDATE;
    public byte id;
    public short x, y, z;

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.id = this.getBuffer().readByte();
        this.x = this.getBuffer().readShort();
        this.y = this.getBuffer().readShort();
        this.z = this.getBuffer().readShort();
    }
    public void encode() throws IOException {
        this.reset();
        this.getBuffer().writeByte(this.id);
        this.getBuffer().writeShort(this.x);
        this.getBuffer().writeShort(this.y);
        this.getBuffer().writeShort(this.z);
    }
}