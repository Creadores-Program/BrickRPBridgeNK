package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class LoginServerPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.LOGIN_SERVER;

    public String password;
    public String serverId;
    public String level;
    public String uuidPass;
    public byte version = RPprotocolInfo.VERSION;

    public byte pid(){
        return NETWORK_ID;
    }

    public void decode() throws IOException {
        this.password = ByteBufProvider.readString(this.getBuffer());
        this.serverId = ByteBufProvider.readString(this.getBuffer());
        this.level = ByteBufProvider.readString(this.getBuffer());
        this.uuidPass = ByteBufProvider.readString(this.getBuffer());
        this.version = this.getBuffer().getByte();
    }
    public void encode() throws IOException {
        this.reset();
        ByteBufProvider.writeString(this.getBuffer(), this.password);
        ByteBufProvider.writeString(this.getBuffer(), this.serverId);
        ByteBufProvider.writeString(this.getBuffer(), this.level);
        ByteBufProvider.writeString(this.getBuffer(), this.uuidPass);
        this.getBuffer().writeByte(this.version);
    }
}