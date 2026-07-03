package org.CreadoresProgram.rpbridge.data;

import cn.nukkit.Player;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.handler.FormResponseHandler;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector2;
import cn.nukkit.utils.DummyBossBar;
import cn.nukkit.utils.Binary;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.nbt.tag.CompoundTag;

import org.CreadoresProgram.rpbridge.network.ServerRP;
import org.CreadoresProgram.rpbridge.network.RPSourceInterface;
import org.CreadoresProgram.rpbridge.network.protocol.*;
import org.CreadoresProgram.rpbridge.event.playerp.*;

import java.net.InetSocketAddress;
import java.util.*;

public class PlayerRP extends Player{
    protected ServerRP serverRp;
    protected final String rpId;
    protected final NetworkPlayerSession networkSessionRp;
    protected Queue<Vector3> clientMovementsRP = new ArrayDeque<>();
    public static Skin defaulSkinR;
    public static Skin defaulSkinP;
    private boolean formOpen = false;
    public PlayerRP(SourceInterface interfaz, String rpId, ServerRP serverRp, String name){
        super(interfaz, new Random().nextLong(), new InetSocketAddress(0));
        this.networkSessionRp = ((RPSourceInterface) interfaz).getSession(rpId);
        this.serverRp = serverRp;
        this.rpId = rpId;
        if(this.serverRp.isRobloxServer()){
            this.skin = defaulSkinR;
        }else{
            this.skin = defaulSkinP;
        }
        this.username = name;
        this.displayName = name;
        this.iusername = this.username.toLowerCase();
        this.uuid = UUID.randomUUID();
        this.rawUUID = Binary.writeUUID(this.uuid);
        this.namedTag = new CompoundTag();
        this.namedTag.putInt("playerGameType", this.gamemode);
        this.namedTag.putLong("firstPlayed", System.currentTimeMillis());
        this.namedTag.putLong("lastPlayed", System.currentTimeMillis());
        this.initEntity();
        this.connected = true;
        this.completeLoginSequence();
        this.loggedIn = true;
        this.doFirstSpawn();
    }

    public ServerRP getServerRP(){
        return this.serverRp;
    }
    public void setServerRP(ServerRP serverRp){
        this.serverRp = serverRp;
    }
    public boolean isRobloxPlayer(){
        return this.serverRp.isRobloxServer();
    }
    public boolean isPolytoriaPlayer(){
        return this.serverRp.isPolytoriaServer();
    }
    public String getRpId(){
        return this.rpId;
    }
    public void requestTransaction(String transactionId){
        PlayerRPRequestTransactionEvent ev = new PlayerRPRequestTransactionEvent(this, transactionId);
        this.server.getPluginManager().callEvent(ev);
        if(ev.isCancelled()){
            return;
        }
        TransactionManagerPacket pk = new TransactionManagerPacket();
        pk.playerIdRP = this.rpId;
        pk.transactionId = ev.getTransactionId();
        this.serverRp.sendPacket(pk);
    }
    public void setNewPosition(Vector3 v){
        this.newPosition = v;
    }
    public Queue<Vector3> getClientMovements(){
        return this.clientMovementsRP;
    }
    public Vector3 getSleeping(){
        return this.sleeping;
    }
    public boolean canInteractEntity(Vector3 pos, double maxDistanceSquared) {
        if (this.distanceSquared(pos) > maxDistanceSquared) {
            return false;
        }

        Vector2 dV = this.getDirectionPlane();
        return (dV.dot(new Vector2(pos.x, pos.z)) - dV.dot(new Vector2(this.x, this.z))) >= -0.87;
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

    @Override
    public void sendPosition(Vector3 pos, double yaw, double pitch, int mode, Player[] targets) {
        super.sendPosition(pos, yaw, pitch, mode, targets);
        if (targets == null) {
            this.clientMovementsRP.clear();
        }
    }
    @Override
    public void close(TextContainer message, String reason, boolean notify){
        super.close(message, reason, notify);
        this.clientMovementsRP = null;
    }
    @Override
    public boolean onUpdate(int currentTick) {
        if (!this.loggedIn) {
            return false;
        }

        int tickDiff = currentTick - this.lastUpdate;

        if (tickDiff <= 0) {
            return true;
        }
        if (this.spawned) {
            while (!this.clientMovementsRP.isEmpty()) {
                this.handleMovement(this.clientMovementsRP.poll());
            }
        }
        return super.onUpdate(currentTick);
    }
    @Override
    public void sendMessage(String message, boolean isLocalized) {
        ChatPacket pk = new ChatPacket();
        pk.type = ChatPacket.Type.RAW;
        pk.playerIdRP = this.rpId;
        pk.message = this.server.getLanguage().translateString(message);
        this.serverRp.sendPacket(pk);
    }
    @Override
    public void sendTranslation(String message, String[] parameters) {
        ChatPacket pk = new ChatPacket();
        pk.type = ChatPacket.Type.RAW;
        pk.playerIdRP = this.rpId;
        pk.message = this.server.getLanguage().translateString(message, parameters);
        this.serverRp.sendPacket(pk);
    }
    @Override
    public void sendChat(String source, String message) {
        this.sendMessage(message);
    }
    @Override
    public void sendPopup(String message) {
        ChatPacket pk = new ChatPacket();
        pk.type = ChatPacket.Type.POPUP;
        pk.playerIdRP = this.rpId;
        pk.message = this.server.getLanguage().translateString(message);
        this.serverRp.sendPacket(pk);
    }
    @Override
    public void sendTip(String message) {
        ChatPacket pk = new ChatPacket();
        pk.type = ChatPacket.Type.TIP;
        pk.playerIdRP = this.rpId;
        pk.message = this.server.getLanguage().translateString(message);
        this.serverRp.sendPacket(pk);
    }
    @Override
    public void clearTitle() {
        TitlePacket pk = new TitlePacket();
        pk.playerIdRP = this.rpId;
        pk.type = TitlePacket.Type.CLEAR;
        pk.message = "";
        this.serverRp.sendPacket(pk);
    }
    @Override
    public void resetTitleSettings() {}
    @Override
    public void setSubtitle(String subtitle) {
        TitlePacket pk = new TitlePacket();
        pk.playerIdRP = this.rpId;
        pk.type = TitlePacket.Type.SUB_TITLE;
        pk.message = subtitle;
        this.serverRp.sendPacket(pk);
    }
    @Override
    public void setTitleAnimationTimes(int fadein, int duration, int fadeout) {}
    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        TitlePacket pk = new TitlePacket();
        pk.playerIdRP = this.rpId;
        pk.type = TitlePacket.Type.TITLE;
        pk.message = title;
        this.serverRp.sendPacket(pk);
        this.setSubtitle(subtitle);
    }
    @Override
    public void sendActionBar(String title, int fadein, int duration, int fadeout) {}
    @Override
    public void sendToast(String title, String content) {
        ToastPacket pk = new ToastPacket();
        pk.playerIdRP = this.rpId;
        pk.title = title;
        pk.content = content;
        this.serverRp.sendPacket(pk);
    }
    @Override
    public void setHealth(float health){
        super.setHealth(health);
        UpdateHealthPacket pk = new UpdateHealthPacket();
        pk.playerIdRP = this.rpId;
        pk.health = health;
        this.serverRp.sendPacket(pk);
    }
    @Override
    public void setMaxHealth(int maxHealth) {
        super.setMaxHealth(maxHealth);
        SetMaxHealthPacket pk = new SetMaxHealthPacket();
        pk.playerIdRP = this.rpId;
        pk.health = maxHealth;
        this.serverRp.sendPacket(pk);
    }
    @Override
    public int showFormWindow(FormWindow window, int id) {
        if(formOpen) return -1;
        FormPacket pk = new FormPacket();
        pk.playerIdRP = this.rpId;
        pk.id = id;
        pk.close = false;
        pk.window = window;
        this.formWindows.put(pk.id, window);
        this.serverRp.sendPacket(pk);
        this.formOpen = true;
        return id;
    }
    public void processFormResponse(int id, String response){
        this.formOpen = false;
        if (!this.spawned || !this.isAlive()) {
            return;
        }
        if (!formWindows.containsKey(id)) {
            return;
        }
        FormWindow window = formWindows.remove(id);
        window.setResponse(response);
        for (FormResponseHandler handler : window.getHandlers()) {
            handler.handle(this, id);
        }
        PlayerFormRespondedEvent event = new PlayerFormRespondedEvent(this, id, window);
        getServer().getPluginManager().callEvent(event);
    }
    @Override
    public long createBossBar(DummyBossBar dummyBossBar) {
        this.dummyBossBars.put(dummyBossBar.getBossBarId(), dummyBossBar);
        dummyBossBar.create();
        BossBarPacket pk = new BossBarPacket();
        pk.playerIdRP = this.rpId;
        pk.bossbar = dummyBossBar;
        pk.type = BossBarPacket.Type.CREATE;
        this.serverRp.sendPacket(pk);
        return dummyBossBar.getBossBarId();
    }
    @Override
    public void setDisplayName(String displayName) {
        UpdateNamePacket pk = new UpdateNamePacket();
        pk.eid = this.getClientId();
        pk.playerIdRP = this.rpId;
        pk.name = displayName;
        super.setDisplayName(displayName);
    }
    @Override
    public void updateBossBar(String text, int length, long bossBarId) {
        super.updateBossBar(text, length, bossBarId);
        if (!this.dummyBossBars.containsKey(bossBarId)) {
            return;
        }
        DummyBossBar bossb = this.getDummyBossBar(bossBarId);
        BossBarPacket pk = new BossBarPacket();
        pk.playerIdRP = this.rpId;
        pk.bossbar = bossb;
        pk.type = BossBarPacket.Type.UPDATE;
        this.serverRp.sendPacket(pk);
    }
    @Override
    public void removeBossBar(long bossBarId) {
        DummyBossBar bossb = this.getDummyBossBar(bossBarId);
        super.removeBossBar(bossBarId);
        BossBarPacket pk = new BossBarPacket();
        pk.playerIdRP = this.rpId;
        pk.bossbar = bossb;
        pk.type = BossBarPacket.Type.REMOVE;
        this.serverRp.sendPacket(pk);
    }
    @Override
    public void transfer(String hostName, int port) {}
    @Override
    public void closeFormWindows() {
        this.formWindows.clear();
        this.formOpen = false;
        FormPacket pk = new FormPacket();
        pk.playerIdRP = this.rpId;
        pk.id = 0;
        pk.close = true;
        this.serverRp.sendPacket(pk);
    }
}