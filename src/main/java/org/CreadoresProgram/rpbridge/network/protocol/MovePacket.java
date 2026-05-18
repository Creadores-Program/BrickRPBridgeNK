package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

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
}