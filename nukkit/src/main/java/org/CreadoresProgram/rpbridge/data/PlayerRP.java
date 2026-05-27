package org.CreadoresProgram.rpbridge.data;

import cn.nukkit.Player;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.network.protocol.DataPacket;

import org.CreadoresProgram.rpbridge.network.ServerRP;
import org.CreadoresProgram.rpbridge.network.RPSourceInterface;

import java.net.InetSocketAddress;

public class PlayerRP extends Player{
    private ServerRP serverRp;
    protected final String rpId;
    protected final NetworkPlayerSession networkSessionRp;
    public PlayerRP(SourceInterface interfaz, String rpId, ServerRP serverRp){
        super(interfaz, new Random().nextLong(), new InetSocketAddress(0));
        this.networkSessionRp = ((RPSourceInterface) interfaz).getSession(rpId);
        this.serverRp = serverRp;
        this.rpId = rpId;
    }

    public ServerRP getServerRP(){
        return this.serverRp;
    }
    public void setServerRP(ServerRP serverRp){
        this.serverRp = serverRp;
    }

    @Override
    public NetworkPlayerSession getNetworkSession(){
        return this.networkSessionRp;
    }
    @Override
    public void sendCommandData() {}
    @Override
    public void setEnableClientCommand(boolean enable) {
        this.enableClientCommand = enable;
    }
    @Override
    public void sendChunk(int x, int z, DataPacket packet){}
    @Override
    public void sendChunk(int x, int z, int subChunkCount, byte[] payload, int dimension) {}
    @Override
    public boolean dataPacket(DataPacket packet) {
        return false;
    }
    @Override
    public void forceDataPacket(DataPacket packet, Runnable callback) {}
}