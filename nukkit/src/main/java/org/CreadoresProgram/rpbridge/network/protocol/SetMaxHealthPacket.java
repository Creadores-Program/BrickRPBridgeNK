package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class SetMaxHealthPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.SET_MAX_HEALTH;
    public String playerIdRP;
    public int health;

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.health = this.getBuffer().readInt();
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
    }
    public void encode() throws IOException {
        this.reset();
        this.getBuffer().writeInt(this.health);
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
    }
}