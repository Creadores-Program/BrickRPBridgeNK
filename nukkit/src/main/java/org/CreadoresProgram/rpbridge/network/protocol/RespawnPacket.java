package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class RespawnPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.RESPAWN;
    public long eid;
    public String playerIdRP;

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.eid = this.getBuffer().readLong();
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
    }
    public void encode() throws IOException {
        this.reset();
        this.getBuffer().writeLong(this.eid);
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
    }
}