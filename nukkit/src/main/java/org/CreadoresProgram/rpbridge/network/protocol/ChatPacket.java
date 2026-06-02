package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class ChatPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.CHAT;
    public String message;
    public String playerIdRP;
    public byte type;

    public static class Type{
        public static final byte RAW = 0x01;
        public static final byte COMMAND = 0x02;
        public static final byte POPUP = 0x03;
        public static final byte TIP = 0x04;
    }

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.message = ByteBufProvider.readString(this.getBuffer());
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
        this.type = this.getBuffer().readByte();
    }
    public void encode() throws IOException {
        this.reset();
        ByteBufProvider.writeString(this.getBuffer(), this.message);
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
        this.getBuffer().writeByte(this.type);
    }
}