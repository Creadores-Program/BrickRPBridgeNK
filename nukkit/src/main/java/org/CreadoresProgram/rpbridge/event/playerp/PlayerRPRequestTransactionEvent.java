package org.CreadoresProgram.rpbridge.event.rp;

import org.CreadoresProgram.rpbridge.data.PlayerRP;

import cn.nukkit.event.HandlerList;
import cn.nukkit.event.Cancellable;
import cn.nukkit.Player;

public class PlayerRPRequestTransactionEvent extends PlayerRPEvent implements Cancellable{
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    protected String transactionId;
    public PlayerTransactionEvent(PlayerRP player, String transactionId){
        this.player = (Player) player;
        this.transactionId = transactionId;
    }
    @Override
    public Player getPlayer(){
        return this.player;
    }
    public String getTransactionId(){
        return this.transactionId;
    }
}