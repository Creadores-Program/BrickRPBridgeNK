package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class PingPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.PING;
    public long timeMilis;

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.timeMilis = this.getBuffer().readLong();
    }
    public void encode() throws IOException {
        this.reset();
        this.getBuffer().writeLong(System.currentTimeMillis());
    }
}