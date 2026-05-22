package org.CreadoresProgram.rpbridge.network;

import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.Player;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import spark.Service;
import spark.Request;
import spark.Response;
import spark.Route;

import org.CreadoresProgram.rpbridge.data.PlayerRP;
import org.CreadoresProgram.rpbridge.network.protocol.RPpacket;
import org.CreadoresProgram.rpbridge.network.protocol.RPprotocolInfo;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBufInputStream;

public class RPSourceInterface implements SourceInterface, Route {
    
    private static final String NO_REASON = "no reason";
    private static final String SHUTDOWN_REASON = "Shutdown";
    private static final String NO_PLAYERRP = "player not is PlayerRP instance";
    private static final String AccessCtrlAllOrin = "Access-Control-Allow-Origin";
    private static final String AccessOrinVal = "*";
    private static final String AccessCtrlAllMeth = "Access-Control-Allow-Methods";
    private static final String AccessMethVal = "POST";
    private static final String AccessCtrlAllHead = "Access-Control-Allow-Headers";
    private static final String AccessHeadVal = "Content-Type, IdServer, UUID, World";
    private static final String AccessMethWVal = "GET";

    private Map<String, ServerRP> serversRP = new ConcurrentHashMap<>();
    private boolean isRun;
    private Map<String, RPNetworkPlayerSession> sessions = new ConcurrentHashMap<>();
    private Service sparkServer;

    public RPSourceInterface(int port){
        this.sparkServer = Service.ignate();
        this.sparkServer.port(port);
        String serverRPprUrl = "/ServerRPprotocol";
        this.sparkServer.post(serverRPprUrl, this);
        this.sparkServer.options(serverRPprUrl, (req, res)->{
            res.header(AccessCtrlAllOrin, AccessOrinVal);
            res.header(AccessCtrlAllMeth, AccessMethVal);
            res.header(AccessCtrlAllHead, AccessHeadVal);
            res.status(200);
            return "OK";
        });
        String serverWorldRPprUrl = "/RqServerWorld";
        this.sparkServer.get(serverWorldRPprUrl, this::handleWorld);
        this.sparkServer.options(serverWorldRPprUrl, (req, res)->{
            res.header(AccessCtrlAllOrin, AccessOrinVal);
            res.header(AccessCtrlAllMeth, AccessMethWVal);
            res.header(AccessCtrlAllHead, AccessHeadVal);
            res.status(200);
            return "OK";
        });
        this.sparkServer.before((req, res)->{
            res.header(AccessCtrlAllOrin, AccessOrinVal);
            res.header(AccessCtrlAllHead, AccessHeadVal);
        });
    }

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
        if(this.sparkServer != null){
            this.sparkServer.stop();
            this.sparkServer.awaitStop();
        }
        this.isRun = false;
    }
    @Override
    public void emergencyShutdown(){
        this.shutdown();
    }

    private static String contTypPre = "Content-Type";
    private static String contTypVal = "application/octet-stream";
    @Override
    public Object handle(Request request, Response response) throws Exception{
        if(request.headers(contTypPre) == null || request.headers(contTypPre).isEmpty() || !request.headers(contTypPre).equals(contTypVal)){
            response.status(415);
            return null;
        }
        byte[] bytesBody = request.bodyAsBytes();
        if(bytesBody == null || bytesBody.length == 0){
            response.status(400);
            return null;
        }
        ByteBuf packets = Unpooled.wrappedBuffer(bytesBody);
        try{
            byte id = packets.readByte();
            if(!this.isAuntenticated(request) && id != RPprotocolInfo.LOGIN_SERVER){
                response.status(401);
                return null;
            }
            this.processDatapacks(packets);
            CompositeByteBuf composite = Unpooled.compositeBuffer();
            if(this.serversRP.get(request.headers(serverIdPrefix)) != null){
                composite.addComponents(true, this.serversRP.get(request.headers(serverIdPrefix)).getRawDataPacks());
            }
            response.type(contTypVal);
            try(ByteBufInputStream stream = new ByteBufInputStream(composite)){
                return stream;
            }finally{
                composite.release();
            }
        }finally{
            if (packets.refCnt() > 0) {
                packets.release();
            }
        }
    }

    private void processDatapacks(ByteBuf packets){}

    private Object handleWorld(Request request, Response response) throws Exception{
        if(!this.isAuntenticated(request)){
            response.status(401);
            return null;
        }
    }

    private static String uuidPre = "UUID";
    private static String serverIdPrefix = "IdServer";
    private boolean isAuntenticated(Request req){
        if(req.headers(uuidPre) == null || req.headers(uuidPre).isEmpty() || req.headers(serverIdPrefix) == null || req.headers(serverIdPrefix).isEmpty()){
            return false;
        }
        ServerRP server = this.serversRP.get(req.headers(serverIdPrefix));
        if(server == null){
            return false;
        }
        return server.getUuidPass().equals(req.headers(uuidPre));
    }
}