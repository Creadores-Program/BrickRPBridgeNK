package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class SetWeatherPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.SET_WEATHER;
    public boolean weather;

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.weather = this.getBuffer().readBoolean();
    }
    public void encode() throws IOException {
        this.reset();
        this.getBuffer().writeBoolean(this.weather);
    }
}