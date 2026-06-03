package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

public class TransactionManagerPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.TRANSACTION_MANAGER;
    public String playerIdRP;
    public String transactionId;
    public boolean accept = false;

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
        this.transactionId = ByteBufProvider.readString(this.getBuffer());
        this.accept = this.getBuffer().readBoolean();
    }
    public void encode() throws IOException {
        this.reset();
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
        ByteBufProvider.writeString(this.getBuffer(), this.transactionId);
        this.getBuffer().writeBoolean(this.accept);
    }
}