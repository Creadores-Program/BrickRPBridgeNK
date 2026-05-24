package org.CreadoresProgram.rpbridge.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import cn.nukkit.Server;
import cn.nukkit.level.Level;

import org.CreadoresProgram.rpbridge.data.PlayerRP;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;

public class ServerRP{
    private String rpId;
    private byte[] uuidPass;
    private Level level;
    private List<ByteBuf> dataPacks = new ObjectArrayList<>();
    private Map<String, PlayerRP> players = new ConcurrentHashMap<>();

    public ServerRP(String rpId, String uuidPass, Level level){
        this.rpId = rpId;
        this.uuidPass = uuidPass.getBytes(StandardCharsets.UTF_8);
        this.level = level;
    }
    public String getRPId(){
        return this.rpId;
    }
    public List<ByteBuf> getRawDataPacks(){
        return this.dataPacks;
    }
    public byte[] getUuidPass(){
        return this.uuidPass;
    }
    public Level getLevel(){
        return this.level;
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