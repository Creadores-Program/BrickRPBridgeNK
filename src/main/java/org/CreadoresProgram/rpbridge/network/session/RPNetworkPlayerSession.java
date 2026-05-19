package org.CreadoresProgram.rpbridge.network.session;

import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.Player;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.CreadoresProgram.rpbridge.data.PlayerRP;
import org.CreadoresProgram.rpbridge.network.protocol.RPpacket;
import org.CreadoresProgram.rpbridge.network.protocol.DisconnectPlayerPacket;
import org.CreadoresProgram.rpbridge.network.ServerRP;

import java.util.List;

public class RPNetworkPlayerSession implements NetworkPlayerSession{
    private PlayerRP player;
    private ServerRP serverRP;

    @Override
    public void sendPacket(DataPacket packet){
        this.traducePacket(packet);
    }
    public void sendPacket(RPpacket packet){
        this.serverRP.sendPacket(this.player, packet);
    }

    @Override
    public void sendImmediatePacket(DataPacket packet, Runnable callback){
        this.traducePacket(packet);
        callback.run();
    }
    public void sendImmediatePacket(RPpacket packet, Runnable callback){
        this.sendPacket(packet);
        callback.run();
    }

    @Override
    public void disconnect(String reason){
        DisconnectPlayerPacket pk = new DisconnectPlayerPacket();
        pk.playerId = this.player.getRPId();
        pk.reason = reason;
        this.sendPacket(pk);
    }
}