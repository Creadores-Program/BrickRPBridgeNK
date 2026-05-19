package org.CreadoresProgram.rpbridge;

import cn.nukkit.plugin.PluginBase;

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
    public void onEnable(){}
}