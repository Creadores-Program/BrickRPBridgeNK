package org.CreadoresProgram.rpbridge;

import cn.nukkit.plugin.PluginBase;

import org.CreadoresProgram.rpbridge.network.RPSourceInterface;
import org.CreadoresProgram.rpbridge.event.fastrespawn.FRListener;
import org.CreadoresProgram.rpbridge.data.PlayerRP;

public class Main extends PluginBase{
    private static Main instance;
    public static Main getInstance(){
        return instance;
    }

    @Override
    public void onLoad(){
        instance = this;
        this.getLogger().info("§eLoading...");
        PlayerRP.defaulSkinR = new Skin();
        PlayerRP.defaulSkinP = new Skin();
        String pathS = "ewogICAiZ2VvbWV0cnkiIDogewogICAgICAiZGVmYXVsdCIgOiAiZ2VvbWV0cnkuaHVtYW5vaWQuY3VzdG9tIgogICB9Cn0K";
        PlayerRP.defaulSkinP.setSkinResourcePatch(pathS);
        PlayerRP.defaulSkinR.setSkinResourcePatch(pathS);
        String nameG = "";
        PlayerRP.defaulSkinP.setGeometryName(nameG);
        PlayerRP.defaulSkinR.setGeometryName(nameG);
        PlayerRP.defaulSkinP.setPersona(true);
        PlayerRP.defaulSkinR.setPersona(true);
        PlayerRP.defaulSkinP.setTrusted(true);
        PlayerRP.defaulSkinR.setTrusted(true);
        try {
            InputStream streamG = this.getResource("skins/skin_geometry.json");
            
            if (streamG == null) {
                this.getLogger().error("Skin geometry not found");
                return;
            }
            String contentG;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(streamG))) {
                contentG = reader.lines().collect(Collectors.joining("\n"));
            }
            PlayerRP.defaulSkinP.setGeometryData(contentG);
            PlayerRP.defaulSkinR.setGeometryData(contentG);


            InputStream streamR = this.getResource("skins/roblox_data.txt");
            
            if (streamR == null) {
                this.getLogger().error("Skin Roblox not found");
                return;
            }
            String contentBase64;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(streamR))) {
                contentBase64 = reader.lines().collect(Collectors.joining("\n"));
            }

            contentBase64 = contentBase64.trim().replace("\r", "").replace("\n", "");

            byte[] bytesR = Base64.getDecoder().decode(contentBase64);
            PlayerRP.defaulSkinR.setSkinData(bytesR);
            PlayerRP.defaulSkinR.generateSkinId("Roblox");

            InputStream streamP = this.getResource("skins/polytoria_data.txt");
            
            if (streamP == null) {
                this.getLogger().error("Skin Polytoria not found");
                return;
            }
            String contentBase64P;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(streamP))) {
                contentBase64P = reader.lines().collect(Collectors.joining("\n"));
            }

            contentBase64P = contentBase64P.trim().replace("\r", "").replace("\n", "");

            byte[] bytesP = Base64.getDecoder().decode(contentBase64P);
            PlayerRP.defaulSkinP.setSkinData(bytesP);
            PlayerRP.defaulSkinP.generateSkinId("Polytoria");
        } catch (IllegalArgumentException e) {
            this.getLogger().error("Skins File Damage.");
            e.printStackTrace();
            return;
        } catch (Exception e) {
            this.getLogger().error("Fail to load Skins.");
            e.printStackTrace();
            return;
        }
    }
    @Override
    public void onEnable(){
        this.getLogger().info("§eLoadig Server Http...");
        this.getServer().getNetwork().registerInterface(new RPSourceInterface(this.getConfig().getInt("port"), this.getConfig().getString("password"), this.getServer()));
        this.getLogger().info("RPServerNK open in 0.0.0.0:" + this.getConfig().getInt("port"));
        if(this.getServer().getPluginManager().getPlugin("FastRespawn") == null){
            this.getServer().getPluginManager().registerEvents(new FRListener(), this);
        }
        if(this.getConfig().getBoolean("tunnel.enabled")){
            this.getLogger().info("§eLoading tunnel...");
            //Tunnel manager
        }
        this.getLogger().info("§aDone!");
    }
}