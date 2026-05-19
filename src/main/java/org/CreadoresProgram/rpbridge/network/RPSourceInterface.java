package org.CreadoresProgram.rpbridge.network;

import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.Player;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import spark.Service;

import org.CreadoresProgram.rpbridge.data.PlayerRP;
import org.CreadoresProgram.rpbridge.network.protocol.RPpacket;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RPSourceInterface implements SourceInterface{
    
    private static final String NO_REASON = "no reason";
    private static final String SHUTDOWN_REASON = "Shutdown";
    private static final String NO_PLAYERRP = "player not is PlayerRP instance";

    private Map<String, ServerRP> serversRP = new ConcurrentHashMap<>();
    private boolean isRun;
    private Map<String, RPNetworkPlayerSession> sessions = new ConcurrentHashMap<>();
    private Service sparkServer;

    @Override
    public Integer putPacket(Player player, DataPacket packet){
        RPNetworkPlayerSession ps = player.getNetworkSession();
        if(ps != null){
            ps.sendPacket(packet);
        }
        return null;
    }
    @Override
    public Integer putPacket(Player player, DataPacket packet, boolean needACK) {
        return this.putPacket(player, packet);
    }
    @Override
    public Integer putPacket(Player player, DataPacket packet, boolean needACK, boolean immediate) {
        return this.putPacket(player, packet);
    }

    public void putPacket(PlayerRP player, RPpacket packet){
        RPNetworkPlayerSession ps = this.sessions.get(player.getRPId());
        if(ps != null){
            ps.sendPacket(packet);
        }
    }

    @Override
    public NetworkPlayerSession getSession(InetSocketAddress address){
        return null;
    }
    public NetworkPlayerSession getSession(String rpId){
        return this.sessions.get(rpId);
    }

    @Override
    public int getNetworkLatency(Player player){
        return (int) player.getNetworkSession().getPing();
    }

    @Override
    public void close(Player player){
        this.close(player, NO_REASON);
    }
    public void close(Player player, String reason){
        if(!(player instanceof PlayerRP)){
            throw new RuntimeException(NO_PLAYERRP);
            return;
        }
        PlayerRP p = (PlayerRP) player;
        RPNetworkPlayerSession ps = this.getSession(p.getRPId());
        if(ps != null){
            ps.disconnect(reason);
        }
    }

    @Override
    public void setName(String name){}

    @Override
    public boolean process(){
        return this.isRun;
    }

    @Override
    public void shutdown(){
        this.sessions.values().forEach(session -> session.disconnect(SHUTDOWN_REASON));
        this.isRun = false;
    }
    @Override
    public void emergencyShutdown(){
        this.shutdown();
    }
}