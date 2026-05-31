package org.CreadoresProgram.rpbridge.data;

import cn.nukkit.Player;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.utils.DummyBossBar;

import org.CreadoresProgram.rpbridge.network.ServerRP;
import org.CreadoresProgram.rpbridge.network.RPSourceInterface;
import org.CreadoresProgram.rpbridge.network.protocol.*;

import java.net.InetSocketAddress;

public class PlayerRP extends Player{
    protected ServerRP serverRp;
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
    public boolean isRobloxPlayer(){
        return this.serverRp.isRobloxServer();
    }
    public boolean isPolytoriaPlayer(){
        return this.serverRp.isPolytoriaServer();
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
        //sendToast
    }
    @Override
    public void setHealth(float health){
        super.setHealth(health);
        //set to RP
    }
    @Override
    public void setMaxHealth(int maxHealth) {
        super.setMaxHealth(maxHealth);
        //set to RP
    }
    @Override
    public int showFormWindow(FormWindow window, int id) {
        FormPacket pk = new FormPacket();
        pk.playerIdRP = this.rpId;
        pk.id = id;
        pk.close = false;
        pk.window = window;
        this.serverRp.sendPacket(pk);
    }
    @Override
    public long createBossBar(DummyBossBar dummyBossBar) {
        //createBossBar
    }
    @Override
    public void transfer(String hostName, int port) {}
    @Override
    public void closeFormWindows() {
        FormPacket pk = new FormPacket();
        pk.playerIdRP = this.rpId;
        pk.id = 0;
        pk.close = true;
        this.serverRp.sendPacket(pk);
    }
}