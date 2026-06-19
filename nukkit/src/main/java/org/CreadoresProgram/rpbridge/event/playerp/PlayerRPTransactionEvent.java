package org.CreadoresProgram.rpbridge.event.rp;

import org.CreadoresProgram.rpbridge.data.PlayerRP;

import cn.nukkit.event.HandlerList;
import cn.nukkit.Player;

public class PlayerRPTransactionEvent extends PlayerRPEvent{
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    protected String transactionId;
    protected boolean accept;
    public PlayerRPTransactionEvent(PlayerRP player, String transactionId, boolean accept){
        this.player = (Player) player;
        this.transactionId = transactionId;
        this.accept = accept;
    }
    @Override
    public Player getPlayer(){
        return this.player;
    }
    public String getTransactionId(){
        return this.transactionId;
    }
    public boolean isAccept(){
        return this.accept;
    }
}