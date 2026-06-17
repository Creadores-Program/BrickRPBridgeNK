package org.CreadoresProgram.rpbridge.network.tunnel;
import com.jcraft.jsch.*;

import org.CreadoresProgram.rpbridge.Main;

public class TunnelManager{
    private Session session;
    private Main main;
    public TunnelManager(int localPort, String host, String user, String password, String subdomain){
        this.main = Main.getInstance();
        new Thread(()-> {
            try{}catch(Exception e){}
        }).start();
    }
    public void stop(){
        if(session != null && session.isConnected()){
            session.disconnect();
            main.getLogger().info("🛑 Tunnel Stopped!");
        }
    }
}