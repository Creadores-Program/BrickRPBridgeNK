package org.CreadoresProgram.rpbridge.network;

import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.level.Location;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMace;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.mace.EnchantmentMace;
import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.*;
import cn.nukkit.event.entity.*;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.entity.EntityDamageEvent.DamageModifier;
import cn.nukkit.math.Vector3;
import cn.nukkit.entity.Entity;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.Nukkit;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import spark.Service;
import spark.Request;
import spark.Response;
import spark.Route;

import org.CreadoresProgram.rpbridge.data.*;
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
                    Vector3 clientPosition = new Vector3(pk.x, pk.y, pk.z).substract(0, play.riding == null ? play.getBaseOffset() : play.riding.getMountedOffset(play).getY(), 0).asVector3();
                    double distSqrt = clientPosition.distanceSquared(play);
                    if (distSqrt > 100) { // Notice: This is the distance to player's position on server side. There are likely still unhandled previous movements when next move packet is received.
                        play.sendPosition(play, pk.yaw, pk.pitch, MovePlayerPacket.MODE_RESET);
                        server.getLogger().debug(play.getName() + ": move " + distSqrt + " > 100");
                        return;
                    }
                    boolean revertMotion = false;
                    if (!play.isAlive() || !play.spawned) {
                        revertMotion = true;
                    }
                    if (revertMotion || clientPosition.distanceSquared(play) > 0.1) {
                        play.sendPosition(play, pk.yaw, pk.pitch, MovePlayerPacket.MODE_RESET);
                    } else {
                        float yaw = pk.yaw % 360;
                        float pitch = pk.pitch % 360;
                        if (yaw < 0) {
                            yaw += 360;
                        }
                        play.setRotation(yaw, pitch);
                        play.setNewPosition(clientPosition);
                        play.getClientMovements().offer(clientPosition);
                    }
                    break;
                case RPprotocolInfo.FORM:
                    FormPacket pk = new FormPacket();
                    pk.tryDecode(packets);
                    PlayerRP play = serverRp.getPlayers().get(pk.playerIdRP);
                    play.processFormResponse(pk.id, pk.response);
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
                    Entity target = play.getLevel().getEntity(pk.eidTarjet);
                    if (target == null) {
                        break;
                    }
                    Item item = play.getInventory().getItemInHand();
                    Level level = ply.getLevel();
                    switch(pk.type){
                        case InteractPacket.Type.INTERACT:
                            if (play.distanceSquared(target) > 256) { // TODO: Note entity scale
                                server.getLogger().debug(play.getname() + ": target entity is too far away");
                                break;
                            }
                            play.breakingBlock = null;

                            play.setUsingItem(false);
                            Vector3 clickPos = new Vector3(0, 0, 0);
                            PlayerInteractEntityEvent playerInteractEntityEvent = new PlayerInteractEntityEvent(play, target, item, clickPos);
                            if (play.isSpectator()) playerInteractEntityEvent.setCancelled();
                            server.getPluginManager().callEvent(playerInteractEntityEvent);

                            if (playerInteractEntityEvent.isCancelled()) {
                                break;
                            }
                            if (target.onInteract(play, item, clickPos) && (play.isSurvival() || play.isAdventure())) {
                                if (item.isTool()) {
                                    if (item.useOn(target) && item.getDamage() >= item.getMaxDurability()) {
                                        level.addSound(play, Sound.RANDOM_BREAK);
                                        level.addParticle(new ItemBreakParticle(play, item));
                                        item = Item.get(Item.AIR);
                                    }
                                } else {
                                    if (item.count > 1) {
                                        item.count--;
                                    } else {
                                        item = Item.get(Item.AIR);
                                    }
                                }

                                if (item.getId() == 0 || play.getInventory().getItemInHandFast().getId() == item.getId()) {
                                    play.getInventory().setItemInHand(item);
                                } else if (Nukkit.DEBUG > 1) {
                                    server.getLogger().debug("Tried to set item " + item.getId() + " but " + play.getName() + " had item " + play.getInventory().getItemInHandFast().getId() + " in their hand slot");
                                }
                            }
                            break;
                        case InteractPacket.Type.DAMAGE:
                            if (target.getId() == play.getId()) {
                                play.kick(PlayerKickEvent.Reason.INVALID_PVP, "Tried to attack invalid player");
                                break;
                            }

                            if (!play.canInteractEntity(target, play.isCreative() ? 64 : 25)) { // 8 : 5
                                break;
                            } else if (target instanceof Player) {
                                if ((((Player) target).gamemode & 0x01) > 0) {
                                    break;
                                } else if (!this.server.pvpEnabled) {
                                    break;
                                }
                            }

                            play.breakingBlock = null;

                            play.setUsingItem(false);

                            if (play.getSleeping() != null) {
                                server.getLogger().debug(play.getName() + ": USE_ITEM_ON_ENTITY_ACTION_ATTACK while sleeping");
                                break;
                            }
                            /*if (play.inventoryOpen) {
                                server.getLogger().debug(play.getName() + ": USE_ITEM_ON_ENTITY_ACTION_ATTACK while viewing inventory");
                                break;
                            }*/

                            play.setShieldBlockingDelay(5);

                            if (server.attackStopSprint) {
                                play.setSprinting(false);
                            }

                            Enchantment[] enchantments = item.getEnchantments();

                            float itemDamage = item.getAttackDamage();
                            for (Enchantment enchantment : enchantments) {
                                itemDamage += enchantment.getDamageBonus(target);
                            }

                            Map<DamageModifier, Float> damage = new EnumMap<>(DamageModifier.class);
                            damage.put(DamageModifier.BASE, itemDamage);

                            float knockBack = 0.3f;
                            Enchantment knockBackEnchantment = item.getEnchantment(Enchantment.ID_KNOCKBACK);
                            if (knockBackEnchantment != null) {
                                knockBack += knockBackEnchantment.getLevel() * 0.1f;
                            }

                            EntityDamageByEntityEvent entityDamageByEntityEvent = new EntityDamageByEntityEvent(play, target, DamageCause.ENTITY_ATTACK, damage, knockBack, enchantments);
                            level.addLevelSoundEvent(play, LevelSoundEventPacket.SOUND_BLOCK_SMITHING_TABLE_USE);

                            boolean smashAttack = false;

                            if (item instanceof ItemMace && !play.isGliding()) {
                                double height = play.highestPosition - target.y;

                                if (height >= 1.5) {
                                    smashAttack = true;

                                    int smashDamage = 6; // normal damage
                                    for (int i = 0; i <= height; i++) {
                                        if (i < 3) { // 4 extra damage for each of the first 3 blocks fallen
                                            smashDamage += 4;
                                        } else if (i < 8) { // 2 extra damage for each of the next 5 blocks fallen
                                            smashDamage += 2;
                                        } else { // 1 extra damage for each block fallen after that
                                            smashDamage += 1;
                                        }
                                    }

                                    int density = item.getEnchantmentLevel(EnchantmentMace.ID_DENSITY);
                                    if (density > 0) { // 0.5 per block fallen per level of enchantment
                                        smashDamage += (int) (0.5 * height * density);
                                    }

                                    entityDamageByEntityEvent.setDamage(smashDamage);
                                }
                            }

                            if (play.isSpectator()) {
                                entityDamageByEntityEvent.setCancelled();
                            }
                            if ((target instanceof Player) && !level.getGameRules().getBoolean(GameRule.PVP)) {
                                entityDamageByEntityEvent.setCancelled();
                            }

                            if (!target.attack(entityDamageByEntityEvent)) {
                                if (item.isTool() && !play.isCreative()) {
                                    play.needSendHeldItem = true;
                                }
                                break;
                            }

                            for (Enchantment enchantment : item.getEnchantments()) {
                                enchantment.doPostAttack(play, target);
                            }

                            if (smashAttack) {
                                int windBurst = item.getEnchantmentLevel(Enchantment.ID_WIND_BURST);
                                if (windBurst > 0) {
                                    Vector3 knockback = new Vector3(play.motionX, play.motionY, play.motionZ);

                                    knockback.x /= 2d;
                                    knockback.y /= 2d;
                                    knockback.z /= 2d;

                                    knockback.y += windBurst == 1 ? 1.2 : windBurst == 2 ? 1.75 : (1.15 + 0.35 * windBurst);

                                    play.resetFallDistance();

                                    play.setMotion(knockback);

                                    play.riptideTicks = 40 * windBurst;

                                    target.getLevel().addParticle(new GenericParticle(target, Particle.TYPE_WIND_EXPLOSION));
                                }

                                target.getLevel().addLevelEvent(target, LevelEventPacket.EVENT_PARTICLE_SMASH_ATTACK_GROUND_DUST);
                                target.getLevel().addLevelSoundEvent(target, LevelSoundEventPacket.SOUND_MACE_SMASH_AIR);
                            }

                            if (item.isTool() && !play.isCreative()) {
                                if (item.useOn(target) && item.getDamage() >= item.getMaxDurability()) {
                                    level.addSound(play, Sound.RANDOM_BREAK);
                                    level.addParticle(new ItemBreakParticle(play, item));
                                    play.getInventory().clear(play.getInventory().getHeldItemIndex(), true);
                                } else {
                                    if (item.getId() == 0 || play.getInventory().getItemInHandFast().getId() == item.getId()) {
                                        play.getInventory().setItemInHand(item);
                                    } else if (Nukkit.DEBUG > 1) {
                                        server.getLogger().debug("Tried to set item " + item.getId() + " but " + play.getName() + " had item " + play.getInventory().getItemInHandFast().getId() + " in their hand slot");
                                    }
                                }
                            }
                            break;
                    }
                    break;
                case RPprotocolInfo.RESPAWN:
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
                }else if(event.getFrom().getLevel() == serverRp.getLevel()){
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
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Level level = player.getLevel();
        for(ServerRP serverRp : this.serversRP.values()){
            if(level != serverRp.getLevel() || player.getX() > serverRp.getMaxPosX() || player.getZ() > serverRp.getMaxPosZ() || player.getX() < serverRp.getMinPosX() || player.getZ() < serverRp.getMinPosZ()){
                continue;
            }
            SpawnEntityPacket pk = new SpawnEntityPacket();
            pk.eid = player.getClientId();
            pk.rid = 0;
            if(player instanceof PlayerRP){
                pk.playerIdRP = ((PlayerRP) player).getRpId();
                pk.gameId = ((PlayerRP) player).getServerRP().getGameId();
            }else{
                pk.playerIdRP = player.getUniqueId().toString();
                pk.gameId = GameIds.MC;
            }
            pk.displayName = player.getDisplayName();
            pk.x = player.getX();
            pk.y = player.getY();
            pk.z = player.getZ();
            serverRp.sendPacket(pk);
        }
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        Level level = player.getLevel();
        for(ServerRP serverRp : this.serversRP.values()){
            UnSpawnEntityPacket pk = new UnSpawnEntityPacket();
            pk.eid = player.getClientId();
            if(player instanceof PlayerRP){
                pk.playerIdRP = ((PlayerRP) player).getRpId();
            }else{
                pk.playerIdRP = player.getUniqueId().toString();
            }
            serverRp.sendPacket(pk);
        }
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event){}
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDespawn(EntityDespawnEvent event){}
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event){}
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTp(EntityTeleportEvent event){}
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void oEntityMotion(EntityMotionEvent event){}
}
