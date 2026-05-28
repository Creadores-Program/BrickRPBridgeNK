package org.CreadoresProgram.rpbridge.data;

import cn.nukkit.Player;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.session.NetworkPlayerSession;
import cn.nukkit.network.protocol.DataPacket;

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
        //clearTitle
    }
    @Override
    public void resetTitleSettings() {}
    @Override
    public void setSubtitle(String subtitle) {
        //setSubtitle
    }
    @Override
    public void setTitleAnimationTimes(int fadein, int duration, int fadeout) {}
    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        //sendTitle
    }
    @Override
    public void sendActionBar(String title, int fadein, int duration, int fadeout) {}
    @Override
    public void sendToast(String title, String content) {
        //sendToast
    }
    @Override
    public void close(TextContainer message, String reason, boolean notify) {
        if (this.connected && !this.closed) {

            this.connected = false;

            this.resetCraftingGridType();
            this.removeAllWindows(true);

            if (this.fishing != null) {
                this.stopFishing(false);
            }

            PlayerQuitEvent ev = null;
            if (this.username != null && !this.username.isEmpty()) {
                this.server.getPluginManager().callEvent(ev = new PlayerQuitEvent(this, message, true, reason));
                if (this.loggedIn && ev.getAutoSave()) {
                    this.save();
                }
            }

            if (this.getUniqueId() != null) {
                for (Player player : this.server.getOnlinePlayers().values()) {
                    if (!player.canSee(this)) {
                        player.showPlayer(this);
                    }
                }
            }

            this.hiddenPlayers.clear();

            this.unloadChunks(false);

            super.close();

            this.interfaz.close(this, notify ? reason : "");

            this.server.removeOnlinePlayer(this);

            if (this.loggedIn) {
                this.loggedIn = false;
            }

            if (ev != null && !Objects.equals(this.username, "") && this.spawned && !Objects.equals(ev.getQuitMessage().toString(), "")) {
                this.server.broadcastMessage(ev.getQuitMessage());
            }

            this.server.getPluginManager().unsubscribeFromPermission(Server.BROADCAST_CHANNEL_USERS, this);
            this.spawned = false;
            this.server.getLogger().info(this.getServer().getLanguage().translateString("nukkit.player.logOut",
                    TextFormat.AQUA + this.username + TextFormat.WHITE,
                    this.getAddress(),
                    String.valueOf(this.getPort()),
                    this.getServer().getLanguage().translateString(reason)));

            this.windows.clear();
            this.hasSpawned.clear();

            if (this.riding instanceof EntityRideable) {
                this.riding.passengers.remove(this);
            }

            this.riding = null;
        }

        if (this.perm != null) {
            this.perm.clearPermissions();
            this.perm = null;
        }

        this.inventory = null;
        this.chunk = null;
        this.clientMovements = null;
        this.resourceChunksRequested = null;

        this.server.removePlayer(this);

        if (this.loggedIn) {
            this.server.getLogger().warning("Player is still logged in: " + this.username);
            this.interfaz.close(this, notify ? reason : "");
            this.server.removeOnlinePlayer(this);
            this.loggedIn = false;
        }
    }
}