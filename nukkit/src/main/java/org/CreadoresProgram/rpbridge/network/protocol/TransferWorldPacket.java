package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class TransferWorldPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.TRANSFER_WORLD;
    public String playerIdRP;
    public String level;

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
        this.level = ByteBufProvider.readString(this.getBuffer());
    }
    public void encode() throws IOException {
        this.reset();
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
        ByteBufProvider.writeString(this.getBuffer(), this.level);
    }
}