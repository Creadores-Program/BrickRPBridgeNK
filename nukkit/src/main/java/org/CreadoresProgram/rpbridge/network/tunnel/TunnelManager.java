package org.CreadoresProgram.rpbridge.network.tunnel;
import com.jcraft.jsch.*;

import org.CreadoresProgram.rpbridge.Main;

import java.util.Properties;

public class TunnelManager{
    private Session session;
    private Main main;
    public TunnelManager(int localPort, String host, String user, String password, String subdomain){
        this.main = Main.getInstance();
        new Thread(()-> {
            try{
                JSch jsch = new JSch();
                session = jsch.getSession(user, host, 22);
                if(password != null && !password.isEmpty()){
                    session.setPassword(password);
                }
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();
                session.setPortForwardingR(80, "localhost", localPort);
                main.getLogger().info("§aNetwork Tunnel successfully established via " + host + "!");
                if(!subdomain.isEmpty()){
                    main.getLogger().info("Fixed HTTPS domain should be: https://"+subdomain+"."+host);
                }else{
                    main.getLogger().info("Check the console or the provider's logs to see your random HTTPS URL.");
                }
            }catch(Exception e){
                main.getLogger().error("Error connecting the tunnel", e);
            }
        }).start();
    }
    public void stop(){
        if(session != null && session.isConnected()){
            session.disconnect();
            main.getLogger().info("§cTunnel Stopped!");
        }
    }
}