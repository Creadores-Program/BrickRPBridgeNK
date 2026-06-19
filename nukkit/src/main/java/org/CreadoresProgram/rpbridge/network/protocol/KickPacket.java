package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class KickPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.KICK;
    public String reason;
    public String playerIdRP;

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.message = ByteBufProvider.readString(this.getBuffer());
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
    }
    public void encode() throws IOException {
        this.reset();
        ByteBufProvider.writeString(this.getBuffer(), this.message);
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
    }
}