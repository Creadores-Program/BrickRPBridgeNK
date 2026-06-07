package org.CreadoresProgram.rpbridge.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.Task;

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
    private volatile long ping;
    private volatile int timeOutTaskId;
    private PingTask pingTask;
    private String ip;
    private int maxPosX, maxPosZ, minPosX, minPosZ;

    public ServerRP(String rpId, String uuidPass, Level level, String gameId){
        this.rpId = rpId;
        this.uuidPass = uuidPass.getBytes(StandardCharsets.UTF_8);
        this.level = level;
        this.maxPosX = (int) Math.round(level.getProvider().getSpawn().getX() + 149);
        this.maxPosZ = (int) Math.round(level.getProvider().getSpawn().getZ() + 149);
        this.minPosX = (int) Math.round(level.getProvider(),getSpawn().getX() - 149);
        this.minPosZ = (int) Math.round(level.getProvider(),getSpawn().getZ() - 149);
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
    public void setIp(String ip){
        this.ip = ip;
    }
    public String getIp(){
        return this.ip;
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
    public int getMaxPosX(){
        return this.maxPosX;
    }
    public int getMaxPosZ(){
        return this.maxPosZ;
    }
    public int getMinPosX(){
        return this.minPosX;
    }
    public int getMinPosZ(){
        return this.minPosZ;
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
    public void setPing(long ping){
        this.ping = ping;
    }
    public long getPing(){
        return this.ping;
    }
    public void setTimeOutTaskId(int id){
        this.timeOutTaskId = id;
    }
    public int getTimeOutTaskId(){
        return this.timeOutTaskId;
    }
    public void setPingTask(PingTask pingTask){
        this.pingTask = pingTask;
    }
    public PingTask getPingTask(){
        return this.pingTask;
    }
    public static class PingTask extends Task{
        private ServerRP serverRp;
        private RPSourceInterface interfaz;
        public PingTask(ServerRP serverRp, RPSourceInterface interfaz){
            this.serverRp = serverRp;
            this.interfaz = interfaz;
        }
        @Override
        public void onRun(int currentTik){
            this.serverRp.getPlayers().values().forEach((player)-> player.close());
            this.interfaz.removeServerRP(this.serverRp.getRPId());
        }
    }
}