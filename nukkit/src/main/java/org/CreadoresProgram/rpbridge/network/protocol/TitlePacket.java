package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class TitlePacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.TITLE;
    public String message;
    public String playerIdRP;
    public byte type;

    public static class Type{
        public byte TITLE = 0x01;
        public byte SUB_TITLE = 0x02;
        public byte CLEAR = 0x03;
    }

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.message = ByteBufProvider.readString(this.getBuffer());
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
        this.type = this.getBuffer().getByte();
    }
    public void encode() throws IOException {
        this.reset();
        ByteBufProvider.writeString(this.getBuffer(), this.message);
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
        this.getBuffer().writeByte(this.type);
    }
}