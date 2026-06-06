package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

import cn.nukkit.utils.DummyBossBar;
import cn.nukkit.utils.BossBarColor;

public class BossBarPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.BOSSBAR;
    public String playerIdRP;
    public DummyBossBar bossbar;
    public byte type;
    public static class Type {
        public static final byte CREATE = 0x01;
        public static final byte UPDATE = 0x02;
        public static final byte REMOVE = 0x03;
    }

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
        //this.bossbar no Build
        this.type = this.getBuffer().readByte();
    }
    public void encode() throws IOException {
        this.reset();
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
        this.getBuffer().writeLong(this.bossbar.getBossBarId());
        this.getBuffer().writeFloat(this.bossbar.getLength());
        ByteBufProvider.writeString(this.getBuffer(), this.bossbar.getText());
        this.getBuffer().writeByte((byte) this.bossbar.getColor().ordinal());
    }
}