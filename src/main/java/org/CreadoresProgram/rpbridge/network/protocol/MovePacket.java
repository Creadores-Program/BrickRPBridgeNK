package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

import cn.nukkit.network.protocol.MovePlayerPacket;

public class MovePacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.MOVE;
    public float x;
    public float y;
    public float z;
    public float yaw;
    public float pitch;
    public long eid;

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.x = this.getBuffer().readFloat();
        this.y = this.getBuffer().readFloat();
        this.z = this.getBuffer().readFloat();
        this.yaw = this.getBuffer().readFloat();
        this.pitch = this.getBuffer().readFloat();
        this.eid = this.getBuffer().readLong();
    }
    public void encode() throws IOException {
        this.reset();
        this.getBuffer().writeFloat(this.x);
        this.getBuffer().writeFloat(this.y);
        this.getBuffer().writeFloat(this.z);
        this.getBuffer().writeFloat(this.yaw);
        this.getBuffer().writeFloat(this.pitch);
        this.getBuffer().writeLong(this.eid);
    }
}