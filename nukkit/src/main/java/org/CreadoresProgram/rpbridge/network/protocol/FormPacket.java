package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

import cn.nukkit.form.window.*;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.element.*;

public class FormPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.FORM;
    public int id;
    public boolean close;
    public String playerIdRP;
    public FormWindow window;
    public FormResponse response;

    private static class Type {
        public static byte SIMPLE = 0x01;
        public static byte MODAL = 0x02;
        public static byte CUSTOM = 0x03;
    }
    private static class TypeElements {
        public static byte BUTTON = 0x01;
        public static byte HEADER = 0x02;
    }

    public byte pid(){
        return NETWORK_ID;
    }
    public void decode() throws IOException {
        this.id = this.getBuffer().readInt();
        this.close = this.getBuffer().readBoolean();
        this.playerIdRP = ByteBufProvider.readString(this.getBuffer());
        if(this.close){ 
            return;
        }
    }
    public void encode() throws IOException {
        this.reset();
        this.getBuffer().writeInt(this.id);
        this.getBuffer().writeBoolean(this.close);
        ByteBufProvider.writeString(this.getBuffer(), this.playerIdRP);
        if(this.close){
            return;
        }
        if(this.window instanceof FormWindowSimple){
            FormWindowSimple swin = (FormWindowSimple) this.window;
            this.getBuffer().writeByte(Type.SIMPLE);
            ByteBufProvider.writeString(this.getBuffer(), swin.getTitle());
            ByteBufProvider.writeString(this.getBuffer(), swin.getContent());
            this.getBuffer().writeShort(swin.getElements().size());
            for(SimpleElement elem : swin.getElements()){
                if(elem instanceof ElementButton){
                    ElementButton belem = (ElementButton) elem;
                    this.writeButton(belem);
                    continue;
                }
                if(elem instanceof ElementHeader){
                    ElementHeader helem = (ElementHeader) elem;
                    this.writeHeader(helem);
                }
            }
            return;
        }
        if(this.window instanceof FormWindowModal){
            FormWindowModal mwin = (FormWindowModal) this.window;
            this.getBuffer().writeByte(Type.MODAL);
            ByteBufProvider.writeString(this.getBuffer(), mwin.getTitle());
            ByteBufProvider.writeString(this.getBuffer(), mwin.getContent());
            ByteBufProvider.writeString(this.getBuffer(), mwin.getButton1());
            ByteBufProvider.writeString(this.getBuffer(), mwin.getButton2());
            return;
        }
        if(this.window instanceof FormWindowCustom){
            FormWindowCustom cwin = (FormWindowCustom) this.window;
            this.getBuffer().writeByte(Type.CUSTOM);
            ByteBufProvider.writeString(this.getBuffer(), cwin.getTitle());
            if(cwin.getSubmitButtonText() == null){
                this.getBuffer().writeBoolean(false);
            }else{
                this.getBuffer().writeBoolean(true);
                ByteBufProvider.writeString(this.getBuffer(), cwin.getSubmitButtonText());
            }
            if(cwin.getIcon() == null || cwin.getIcon().getType().equals(ElementButtonImageData.IMAGE_DATA_TYPE_PATH)){
                this.getBuffer().writeBoolean(false);
            }else{
                ElementButtonImageData icon = cwin.getIcon();
                this.getBuffer().writeBoolean(true);
                ByteBufProvider.writeString(this.getBuffer(), icon.getData());
            }
            this.getBuffer().writeShort(cwin.getElements().size());
            for(Element elem : cwin.getElements()){
                //escribir elementos
            }
            return;
        }
        throw new RuntimeException("Window not compatible witch RP");
    }
    private void writeButton(ElementButton button) throws IOException {
        this.getBuffer().writeByte(TypeElements.BUTTON);
        ByteBufProvider.writeString(this.getBuffer(), button.getText());
        if(button.getImage() == null){
            this.getBuffer().writeBoolean(false);
            return;
        }
        ElementButtonImageData img = button.getImage();
        if(img.getType().equals(ElementButtonImageData.IMAGE_DATA_TYPE_PATH)){
            this.getBuffer().writeBoolean(false);
            return;
        }
        this.getBuffer().writeBoolean(true);
        ByteBufProvider.writeString(this.getBuffer(), img.getData());
    }
    private void writeHeader(ElementHeader header) throws IOException {
        this.getBuffer().writeByte(TypeElements.HEADER);
        ByteBufProvider.writeString(this.getBuffer(), header.getText());
        if(header.getImage() == null){
            this.getBuffer().writeBoolean(false);
            return;
        }
        ElementButtonImageData img = header.getImage();
        if(img.getType().equals(ElementButtonImageData.IMAGE_DATA_TYPE_PATH)){
            this.getBuffer().writeBoolean(false);
            return;
        }
        this.getBuffer().writeBoolean(true);
        ByteBufProvider.writeString(this.getBuffer(), img.getData());
    }
}