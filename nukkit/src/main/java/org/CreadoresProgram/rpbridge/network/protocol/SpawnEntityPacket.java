package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class SpawnEntityPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.SPAWN_ENTITY;
    public long eid;
    public int rid;
    public String playerIdRP;
    public String gameId = ENTITY_ID;
    public String displayName;
    public float x, y, z;

    public static final String ENTITY_ID = "ENTY";

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.eid = this.getBuffer().readLong();
        this.ride = this.getBuffer().readInt();
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
        this.gameId = ByteBufProvider.readString(this.getBuffer());
        this.displayName = ByteBufProvider.readString(this.getBuffer());
        this.x = this.getBuffer().readFloat();
        this.y = this.getBuffer().readFloat();
        this.z = this.getBuffer().readFloat();
    }
    public void encode() throws IOException {
        this.reset();
        this.getBuffer().writeLong(this.eid);
        this.getBuffer().writeInt(this.rid);
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
        ByteBufProvider.writeString(this.getBuffer(), this.gameId);
        ByteBufProvider.writeString(this.getBuffer(), this.displayName);
        this.getBuffer().writeFloat(this.x);
        this.getBuffer().writeFloat(this.y);
        this.getBuffer().writeFloat(this.z);
    }
}