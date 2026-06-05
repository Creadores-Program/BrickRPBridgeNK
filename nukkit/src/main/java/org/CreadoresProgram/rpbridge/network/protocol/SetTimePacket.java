package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class TransferWorldPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.TRANSFER_WORLD;
    public int time;

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.time = this.getBuffer().readInt();
    }
    public void encode() throws IOException {
        this.reset();
        this.getBuffer().writeInt(this.time);
    }
}