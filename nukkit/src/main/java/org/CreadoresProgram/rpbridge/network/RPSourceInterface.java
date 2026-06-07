package org.CreadoresProgram.rpbridge.network;

import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.Player;
import cn.nukkit.Server;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import spark.Service;
import spark.Request;
import spark.Response;
import spark.Route;

import org.CreadoresProgram.rpbridge.data.PlayerRP;
import org.CreadoresProgram.rpbridge.network.protocol.*;
import org.CreadoresProgram.rpbridge.Main;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBufInputStream;

public class RPSourceInterface implements SourceInterface, Route, Listener {
    
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
    private Server server;
    protected byte[] password;
    private static final int timeout = 1600;

    public RPSourceInterface(int port, String password, Server server){
        this.password = password.getBytes(StandardCharsets.UTF_8);
        this.sparkServer = Service.ignate();
        this.server = server;
        this.sparkServer.port(port);
        String serverRPprUrl = "/ServerRPprotocol";
        final String resOK = "OK";
        this.sparkServer.post(serverRPprUrl, this);
        this.sparkServer.options(serverRPprUrl, (req, res)->{
            res.header(AccessCtrlAllOrin, AccessOrinVal);
            res.header(AccessCtrlAllMeth, AccessMethVal);
            res.header(AccessCtrlAllHead, AccessHeadVal);
            res.status(200);
            return resOK;
        });
        String serverWorldRPprUrl = "/RqServerWorld";
        this.sparkServer.get(serverWorldRPprUrl, this::handleWorld);
        this.sparkServer.options(serverWorldRPprUrl, (req, res)->{
            res.header(AccessCtrlAllOrin, AccessOrinVal);
            res.header(AccessCtrlAllMeth, AccessMethWVal);
            res.header(AccessCtrlAllHead, AccessHeadVal);
            res.status(200);
            return resOK;
        });
        this.sparkServer.before((req, res)->{
            res.header(AccessCtrlAllOrin, AccessOrinVal);
            res.header(AccessCtrlAllHead, AccessHeadVal);
        });
        server.getPluginManager().registerEvents(this, Main.getInstance());
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
        RPNetworkPlayerSession ps = this.sessions.get(player.getRpId());
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
        return (int) ((PlayerRP) player).getServerRP().getPing();
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
        RPNetworkPlayerSession ps = this.getSession(p.getRpId());
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

    public void removeServerRP(String rpId){
        this.serversRP.remove(rpId);
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
            byte id = packets.getByte(packets.readerIndex());
            if(!this.isAutenticated(request) && id != RPprotocolInfo.LOGIN_SERVER){
                response.status(401);
                return null;
            }
            ServerRP serverRp = null;
            if(request.headers(serverIdPrefix) != null && this.serversRP.get(request.headers(serverIdPrefix)) != null){
                serverRp = this.serverRp.get(request.headers(serverIdPrefix));
            }
            try{
                this.processDatapacks(packets, serverRp);
            }catch(Exception e){
                this.server.getLogger().error("error in process datapacks in RP", e);
                response.status(500);
                return null;
            }
            CompositeByteBuf composite = Unpooled.compositeBuffer();
            if(serverRp != null){
                composite.addComponents(true, serverRp.getRawDataPacks());
            }
            if(serverRp != null && serverRp.getIp() == null){
                serverRp.setIp(request.ip());
            }
            response.type(contTypVal);
            response.status(200);
            try(ByteBufInputStream stream = new ByteBufInputStream(composite)){
                return stream;
            }finally{
                composite.release();
                if(serverRp != null){
                    serverRp.clearRawDatapacks();
                }
            }
        }finally{
            if (packets.refCnt() > 0) {
                packets.release();
            }
        }
    }

    private void processDatapacks(ByteBuf packets, ServerRP serverRp) throws Exception{
        while(packets.readableBytes() > 0){
            switch(packets.readByte()){
                case RPprotocolInfo.LOGIN_SERVER:
                    LoginServerPacket pk = new LoginServerPacket();
                    pk.tryDecode(packets);
                    if(!this.autenticateServerRP(pk)){
                        return;
                    }
                    ServerRP servRp = this.serversRP.get(pk.serverId);
                    servRp.setPingTask(new ServerRP.PingTask(servRp, this));
                    servRp.setTimeOutTaskId(this.server.getScheduler().scheduleDelayedTask(servRp.getPingTask(), timeout).getTaskId());
                    break;
                case RPprotocolInfo.CHAT:
                    ChatPacket pk = new ChatPacket();
                    pk.tryDecode(packets);
                    PlayerRP play = serverRp.getPlayers().get(pk.playerIdRP);
                    if(play == null){
                        break;
                    }
                    if (!play.spawned || !play.isAlive()) {
                        break;
                    }
                    if(pk.type == ChatPacket.Type.RAW){
                        if(pk.message.length() > 512){
                            break;
                        }
                        String chatMessage = pk.message;
                        int breakLine = chatMessage.indexOf('\n');
                        if(breakLine != -1){
                            chatMessage = chatMessage.substring(0, breakLine);
                        }
                        play.chat(chatMessage);
                        break;
                    }
                    PlayerCommandPreprocessEvent playerCommandPreprocessEvent = new PlayerCommandPreprocessEvent(play, pk.message + ' ');
                    this.server.getPluginManager().callEvent(playerCommandPreprocessEvent);
                    if(playerCommandPreprocessEvent.isCancelled()){
                        break;
                    }
                    this.server.dispatchCommand(playerCommandPreprocessEvent.getPlayer(), playerCommandPreprocessEvent.getMessage().substring(1));
                    break;
                case RPprotocolInfo.MOVE:
                    MovePacket pk = new MovePacket();
                    pk.tryDecode(packets);
                    PlayerRP play = serverRp.getPlayers().get(pk.playerIdRP);
                    if(play == null){
                        break;
                    }
                    //move
                    break;
                case RPprotocolInfo.PING:
                    PingPacket pk = new PingPacket();
                    pk.tryDecode(packets);
                    serverRp.setPing(System.currentTimeMillis() - pk.timeMilis);
                    PingPacket pkres = new PingPacket();
                    serverRp.sendPacket(pkres);
                    this.server.getScheduler().cancelTask(serverRp.getTimeOutTaskId());
                    serverRp.setTimeOutTaskId(this.server.getScheduler().scheduleDelayedTask(servRp.getPingTask(), timeout).getTaskId());
                    break;
                case RPprotocolInfo.INTERACT:
                    InteractPacket pk = new InteractPacket();
                    pk.tryDecode(packets);
                    PlayerRP play = serverRp.getPlayers().get(pk.playerIdRP);
                    //damage or interact
                    break;
                default:
                    this.server.getLogger().error("Unknown RP packet!");
                    packets.skipBytes(packets.readableBytes());
                    break;
            }
        }
    }

    private boolean autenticateServerRP(LoginServerPacket pk){
        if((!MessageDigest.isEqual(pk.password.getBytes(StandardCharsets.UTF_8), this.password)) || (!this.server.isLevelGenerated(pk.level)) || this.serversRP.get(pk.serverId) != null){
            return false;
        }
        Level levelServer = this.server.getLevelByName(pk.level);
        if(levelServer == null){
            return false;
        }
        LoginServerPacket res = new LoginServerPacket();
        String uuidPass = UUID.nameUUIDFromBytes(this.password).toString() + UUID.nameUUIDFromBytes(pk.serverId.getBytes(StandardCharsets.UTF_8)).toString();
        res.password = pk.password;
        res.serverId = pk.serverId;
        res.level = pk.level;
        res.uuidPass = uuidPass;
        ServerRP serverRp = new ServerRP(pk.serverId, uuidPass, levelServer, pk.gameId);
        serverRp.sendPacket(res);
        this.serversRP.put(pk.serverId, serverRp);
        return true;
    }

    private Object handleWorld(Request request, Response response) throws Exception{
        if(!this.isAutenticated(request)){
            response.status(401);
            return null;
        }
    }

    private static String uuidPre = "UUID";
    private static String serverIdPrefix = "IdServer";
    private boolean isAutenticated(Request req){
        if(req.headers(uuidPre) == null || req.headers(uuidPre).isEmpty() || req.headers(serverIdPrefix) == null || req.headers(serverIdPrefix).isEmpty()){
            return false;
        }
        ServerRP serverRp = this.serversRP.get(req.headers(serverIdPrefix));
        if(serverRp == null){
            return false;
        }
        if(serverRp.getIp() != null && !(serverRp.getIp().equals(req.ip()))){
            return false;
        }
        return MessageDigest.isEqual(serverRp.getUuidPass(), req.headers(uuidPre).getBytes(StandardCharsets.UTF_8));
    }

    private static final String exitReasonByMove = "You can't be in those orders!";

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        Level level = player.getLevel();
        for(ServerRP serverRp : this.serversRP.values()){
            if(serverRp.getLevel() != level){
                if(player instanceof PlayerRP && serverRp.getPlayers().get(((PlayerRP) player).getRpId()) != null){
                    TranferWorldPacket pk = new TranferWorldPacket();
                    pk.playerIdRP = ((PlayerRP) player).getRpId();
                    pk.level = level.getName();
                    serverRp.sendPacket(pk);
                }
                continue;
            }
            if(player instanceof PlayerRP){
                if(player.getX() > serverRp.getMaxPosX() || player.getZ() > serverRp.getMaxPosZ() || player.getX() < serverRp.getMinPosX() || player.getZ() < serverRp.getMinPosZ()){
                    player.close(exitReasonByMove);
                    continue;
                }
                MovePacket pk = new MovePacket();
                pk.x = player.getX();
                pk.y = player.getY();
                pk.z = player.getZ();
                pk.yaw = player.getYaw();
                pk.pitch = player.getPitch();
                pk.playerIdRP = ((PlayerRP) player).getRpId();
                pk.eid = player.getClientId();
            }else{
                if(player.getX() > serverRp.getMaxPosX() || player.getZ() > serverRp.getMaxPosZ() || player.getX() < serverRp.getMinPosX() || player.getZ() < serverRp.getMinPosZ()){
                    UnSpawnEntityPacket pk = new UnSpawnEntityPacket();
                    pk.eid = player.getClientId();
                    pk.playerIdRP = player.getUniqueId().toString();
                    serverRp.sendPacket(pk);
                    continue;
                }
                MovePacket pk = new MovePacket();
                pk.x = player.getX();
                pk.y = player.getY();
                pk.z = player.getZ();
                pk.yaw = player.getYaw();
                pk.pitch = player.getPitch();
                pk.playerIdRP = player.getUniqueId().toString();
                pk.eid = player.getClientId();
            }
        }
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTp(PlayerTeleportEvent event){
        Player player = event.getPlayer();
        Level level = player.getLevel();
        for(ServerRP serverRp : this.serversRP.values()){
            if(serverRp.getLevel() != level){
                if(player instanceof PlayerRP && serverRp.getPlayers().get(((PlayerRP) player).getRpId()) != null){
                    TranferWorldPacket pk = new TranferWorldPacket();
                    pk.playerIdRP = ((PlayerRP) player).getRpId();
                    pk.level = level.getName();
                    serverRp.sendPacket(pk);
                }else{
                    UnSpawnEntityPacket pk = new UnSpawnEntityPacket();
                    pk.playerIdRP = player.getUniqueId().toString();
                    pk.eid = player.getClientId();
                    serverRp.sendPacket(pk);
                }
                continue;
            }
            if(player instanceof PlayerRP){
                if(player.getX() > serverRp.getMaxPosX() || player.getZ() > serverRp.getMaxPosZ() || player.getX() < serverRp.getMinPosX() || player.getZ() < serverRp.getMinPosZ()){
                    player.close(exitReasonByMove);
                    continue;
                }
                MovePacket pk = new MovePacket();
                pk.x = player.getX();
                pk.y = player.getY();
                pk.z = player.getZ();
                pk.yaw = player.getYaw();
                pk.pitch = player.getPitch();
                pk.playerIdRP = ((PlayerRP) player).getRpId();
                pk.eid = player.getClientId();
            }else{
                if(player.getX() > serverRp.getMaxPosX() || player.getZ() > serverRp.getMaxPosZ() || player.getX() < serverRp.getMinPosX() || player.getZ() < serverRp.getMinPosZ()){
                    UnSpawnEntityPacket pk = new UnSpawnEntityPacket();
                    pk.eid = player.getClientId();
                    pk.playerIdRP = player.getUniqueId().toString();
                    serverRp.sendPacket(pk);
                    continue;
                }
                MovePacket pk = new MovePacket();
                pk.x = player.getX();
                pk.y = player.getY();
                pk.z = player.getZ();
                pk.yaw = player.getYaw();
                pk.pitch = player.getPitch();
                pk.playerIdRP = player.getUniqueId().toString();
                pk.eid = player.getClientId();
            }
        }
    }
}
