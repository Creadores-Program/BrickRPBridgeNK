package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class InteractPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.INTERACT;
    public byte type;
    public long eidTarjet;
    public String playerIdRPTarjet;
    public String playerIdRP;

    public static class Type {
        public static final byte DAMAGE = 0x01;
        public static final byte INTERACT = 0x02;
    }

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.type = this.getBuffer().readByte();
        this.eidTarjet = this.getBuffer().readLong();
        this.playerIdRPTarjet = ByteBufProvider.readString(this.getBuffer());
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
    }
    public void encode() throws IOException {
        this.reset();
        this.getBuffer().writeByte(this.type);
        this.getBuffer().writeLong(this.eidTarjet);
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRPTarjet);
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
    }
}