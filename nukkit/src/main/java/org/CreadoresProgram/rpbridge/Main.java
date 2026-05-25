package org.CreadoresProgram.rpbridge;

import cn.nukkit.plugin.PluginBase;

import org.CreadoresProgram.rpbridge.network.RPSourceInterface;

public class Main extends PluginBase{
    private static Main instance;
    public static Main getInstance(){
        return instance;
    }

    @Override
    public void onLoad(){
        instance = this;
        this.getLogger().info("§eLoading...");
    }
    @Override
    public void onEnable(){
        this.getLogger().info("§eLoadig Server Http...");
        this.getServer().getNetwork().registerInterface(new RPSourceInterface(this.getConfig().getInt("port"), this.getConfig().getString("password")));
        this.getLogger().info("RPServerNK open in 0.0.0.0:" + this.getConfig().getInt("port"));
        if(this.getConfig().getBoolean("tunnel.enabled")){
            this.getLogger().info("§eLoading tunnel...");
            //Tunnel manager
        }
        this.getLogger().info("§aDone!");
    }
}