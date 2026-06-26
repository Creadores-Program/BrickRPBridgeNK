package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

public abstract class RPpacket implements Cloneable{
    private ByteBuf buffer;
    public volatile boolean isEncoded = false;

    public final void tryEncode() throws Exception {
        if (!this.isEncoded) {
            if(this.buffer == null){
                this.buffer = Unpooled.buffer();
            }
            this.isEncoded = true;
            this.encode();
        }
    }
    public final void tryDecode(ByteBuf buffer) throws Exception {
        if(this.isEncoded){
            throw new Exception("The packet has already been encoded");
        }
        this.buffer = buffer;
        this.decode();
        this.buffer = null;
    }

    public abstract byte pid();
    public abstract void encode()throws IOException;
    public abstract void decode() throws IOException;

    public void reset(){
        buffer.clear();
        buffer.writeByte(this.pid());
    }

    @Override
    public RPpacket clone(){
        try{
            RPpacket pk = (RPpacket) super.clone();
            pk.setBuffer(this.buffer);
            return pk;
        }catch(CloneNotSupportedException e){
            return null;
        }
    }
    public ByteBuf getBuffer(){
        return this.buffer;
    }
    public void setBuffer(ByteBuf buffer){
        this.buffer = buffer;
    }
}