package org.CreadoresProgram.rpbridge.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import cn.nukkit.Server;

import org.CreadoresProgram.rpbridge.data.PlayerRP;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerRP{
    private String rpId;
    private String uuidPass;
    private List<ByteBuf> dataPacks = new ObjectArrayList<>();
    private Map<String, PlayerRP> players = new ConcurrentHashMap<>();

    public ServerRP(String rpId, String uuidPass){
        this.rpId = rpId;
        this.uuidPass = uuidPass;
    }
    public String getRPId(){
        return this.rpId;
    }
    public List<ByteBuf> getRawDataPacks(){
        return this.dataPacks;
    }
    public String getUuidPass(){
        return this.uuidPass;
    }
    public void sendPacket(RPpacket packet){
        try{
            packet.tryEncode();
            synchronized(this.dataPacks){
                this.dataPacks.add(packet.getBuffer());
            }
        }catch(Exception e){
            Server.getIntance().getLogger().error("Failed encode RPpacket: ", e);
        }
    }
}