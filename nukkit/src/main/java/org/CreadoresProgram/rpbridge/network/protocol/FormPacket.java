package org.CreadoresProgram.rpbridge.network.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.CreadoresProgram.rpbridge.utils.ByteBufProvider;

import cn.nukkit.form.window.*;
import cn.nukkit.form.response.*;
import cn.nukkit.form.element.*;

public class FormPacket extends RPpacket{
    public static final byte NETWORK_ID = RPprotocolInfo.FORM;
    public int id;
    public boolean close;
    public String playerIdRP;
    public FormWindow window;
    public String response;

    protected static class Type {
        public static byte SIMPLE = 0x01;
        public static byte MODAL = 0x02;
        public static byte CUSTOM = 0x03;
    }
    protected static class TypeElements {
        public static byte BUTTON = 0x01;
        public static byte HEADER = 0x02;
        public static byte DIVIDER = 0x03;
        public static byte DROPDOWN = 0x04;
        public static byte INPUT = 0x05;
        public static byte LABEL = 0x06;
        public static byte SLIDER = 0x07;
        public static byte STEP_SLIDER = 0x08;
        public static byte TOGGLE = 0x09;
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
        this.getBuffer().readByte();
        this.response = ByteBufProvider.readString(this.getBuffer());
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
                }else if(elem instanceof ElementHeader){
                    ElementHeader helem = (ElementHeader) elem;
                    this.writeHeader(helem);
                    continue;
                }else if(elem instanceof ElementDivider){
                    ElementDivider delem = (ElementDivider) elem;
                    this.writeDivider(delem);
                    continue;
                }else if(elem instanceof ElementLabel){
                    ElementLabel lelem = (ElementLabel) elem;
                    this.writeLabel(lelem);
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
                if(elem instanceof ElementButton){
                    ElementButton belem = (ElementButton) elem;
                    this.writeButton(belem);
                    continue;
                }else if(elem instanceof ElementHeader){
                    ElementHeader helem = (ElementHeader) elem;
                    this.writeHeader(helem);
                    continue;
                }else if(elem instanceof ElementDivider){
                    ElementDivider delem = (ElementDivider) elem;
                    this.writeDivider(delem);
                    continue;
                }else if(elem instanceof ElementDropdown){
                    ElementDropdown drelem = (ElementDropdown) elem;
                    this.writeDropdown(drelem);
                    continue;
                }else if(elem instanceof ElementInput){
                    ElementInput ielem = (ElementInput) elem;
                    this.writeInput(ielem);
                    continue;
                }else if(elem instanceof ElementLabel){
                    ElementLabel lelem = (ElementLabel) elem;
                    this.writeLabel(lelem);
                    continue;
                }else if(elem instanceof ElementSlider){
                    ElementSlider selem = (ElementSlider) elem;
                    this.writeSlider(selem);
                    continue;
                }else if(elem instanceof ElementStepSlider){
                    ElementStepSlider sselem = (ElementStepSlider) elem;
                    this.writeStepSlider(sselem);
                    continue;
                }else if(elem instanceof ElementToggle){
                    ElementToggle telem = (ElementToggle) elem;
                    this.writeToggle(telem);
                }
            }
            return;
        }
        throw new RuntimeException("Window not compatible witch RP");
    }
    protected void writeButton(ElementButton button) throws IOException {
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
    protected void writeHeader(ElementHeader header) throws IOException {
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
    protected void writeDivider(ElementDivider divider){
        this.getBuffer().writeByte(TypeElements.DIVIDER);
        ByteBufProvider.writeString(divider.getText());
    }
    protected void writeDropdown(ElementDropdown dropdown){
        this.getBuffer().writeByte(TypeElements.DROPDOWN);
        ByteBufProvider.writeString(this.getBuffer(), dropdown.getText());
        if(dropdown.getTooltip() == null){
            this.getBuffer().writeBoolean(false);
        }else{
            this.getBuffer().writeBoolean(true);
            ByteBufProvider.writeString(this.getBuffer(), dropdown.getTooltip());
        }
        this.getBuffer().writeInt(dropdown.getDefaultOptionIndex());
        this.getBuffer().writeShort(dropdown.getOptions().size());
        for(String option : dropdown.getOptions()){
            ByteBufProvider.writeString(this.getBuffer(), option);
        }
    }
    protected void writeInput(ElementInput input){
        this.getBuffer().writeByte(TypeElements.INPUT);
        ByteBufProvider.writeString(this.getBuffer(), input.getText());
        if(input.getTooltip() == null){
            this.getBuffer().writeBoolean(false);
        }else{
            this.getBuffer().writeBoolean(true);
            ByteBufProvider.writeString(this.getBuffer(), input.getTooltip());
        }
        ByteBufProvider.writeString(this.getBuffer(), input.getDefaultText());
        ByteBufProvider.writeString(this.getBuffer(), input.getPlaceHolder());
    }
    protected void writeLabel(ElementLabel label){
        this.getBuffer().writeByte(TypeElements.LABEL);
        ByteBufProvider.writeString(this.getBuffer(), label.getText());
    }
    protected void writeSlider(ElementSlider slider){
        this.getBuffer().writeByte(TypeElements.SLIDER);
        ByteBufProvider.writeString(this.getBuffer(), slider.getText());
        if(slider.getTooltip() == null){
            this.getBuffer().writeBoolean(false);
        }else{
            this.getBuffer().writeBoolean(true);
            ByteBufProvider.writeString(this.getBuffer(), slider.getTooltip());
        }
        this.getBuffer().writeFloat(slider.getDefaultValue());
        this.getBuffer().writeFloat(slider.getMin());
        this.getBuffer().writeFloat(slider.getMax());
        this.getBuffer().writeInt(slider.getStep());
    }
    protected void writeStepSlider(ElementStepSlider stepSlider){
        this.getBuffer().writeByte(TypeElements.STEP_SLIDER);
        ByteBufProvider.writeString(this.getBuffer(), stepSlider.getText());
        if(stepSlider.getTooltip() == null){
            this.getBuffer().writeBoolean(false);
        }else{
            this.getBuffer().writeBoolean(true);
            ByteBufProvider.writeString(this.getBuffer(), stepSlider.getTooltip());
        }
        this.getBuffer().writeInt(stepSlider.getDefaultOptionIndex());
        this.getBuffer().writeShort(stepSlider.getSteps().size());
        for(String step : stepSlider.getSteps()){
            ByteBufProvider.writeString(this.getBuffer(), step);
        }
    }
    protected void writeToggle(ElementToggle toggle){
        this.getBuffer().writeByte(TypeElements.TOGGLE);
        ByteBufProvider.writeString(toggle.getText());
        if(toggle.getTooltip() == null){
            this.getBuffer().writeBoolean(false);
        }else{
            this.getBuffer().writeBoolean(true);
            ByteBufProvider.writeString(this.getBuffer(), toggle.getTooltip());
        }
        this.getBuffer().writeBoolean(toggle.isDefaultValue());
    }
}