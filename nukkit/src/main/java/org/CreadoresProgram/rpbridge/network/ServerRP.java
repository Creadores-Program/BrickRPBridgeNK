package org.CreadoresProgram.rpbridge.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import cn.nukkit.Server;
import cn.nukkit.level.Level;

import org.CreadoresProgram.rpbridge.data.PlayerRP;
import org.CreadoresProgram.rpbridge.data.GameIds;

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
    private String gameId;

    public ServerRP(String rpId, String uuidPass, Level level, String gameId){
        this.rpId = rpId;
        this.uuidPass = uuidPass.getBytes(StandardCharsets.UTF_8);
        this.level = level;
        this.gameId = gameId;
    }
    public boolean isRobloxServer(){
        return GameIds.RB.equals(this.gameId);
    }
    public boolean isPolytoriaServer(){
        return GameIds.PL.equals(this.gameId);
    }
    public String getRPId(){
        return this.rpId;
    }
    public List<ByteBuf> getRawDataPacks(){
        return this.dataPacks;
    }
    public void clearRawDatapacks(){
        synchronized(this.dataPacks){
            this.dataPacks.clear();
        }
    }
    public Map<String, PlayerRP> getPlayers(){
        return this.players;
    }
    public void addPlayer(String rpId, PlayerRP player){
        this.players.put(rpId, player);
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