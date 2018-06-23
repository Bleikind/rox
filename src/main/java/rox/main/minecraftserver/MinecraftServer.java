package rox.main.minecraftserver;

import rox.main.Main;
import rox.main.event.events.MinecraftServerStartingEvent;
import rox.main.event.events.MinecraftServerStoppingEvent;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MinecraftServer {

    private ConcurrentHashMap<UUID, Object[]> serverMap;

    private ServerSocket serverSocket;

    private MCI mci;

    private int port;

    private boolean active = false;

    private Thread mcAcceptHandlerThread;

    public MinecraftServer(int port) {
        this.port = port;
    }

    public void start() {
        long startTime = System.currentTimeMillis();
        MinecraftServerStartingEvent event = new MinecraftServerStartingEvent();
        Main.getEventManager().callEvent(event);
        if (event.isCancelled()) return;


        if (!Main.getDatabase().isConnected()) {
            Main.getLogger().err("MinecraftSysetm", "Could not start MinecraftSystem.");
            return;
        }

        try {
            serverMap = new ConcurrentHashMap<>();
            serverSocket = new ServerSocket(port);
            mci = new MCI();
            (mcAcceptHandlerThread = new MCAcceptHandler()).start();
            active = true;
            Main.getLogger().log("MinecraftSystem", "Started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Main.getLogger().time("MinecraftSystemLoad", startTime);
    }

    public void stop() {
        try {

            MinecraftServerStoppingEvent event = new MinecraftServerStoppingEvent();
            Main.getEventManager().callEvent(event);
            if (event.isCancelled()) return;

            serverMap.forEach((name, objects) -> {
                try {
                    ((Thread) objects[1]).interrupt();
                    ((Socket) objects[0]).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverSocket.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void registerServer(String serverName, UUID uuid, String password){
        Main.getDatabase().Update("INSERT INTO mc_servers(servername, uuid, password) VALUES ('" + serverName + "','" + uuid.toString() + "','" + password + "')");
    }

    public void unregisterServer(UUID uuid){
        Main.getDatabase().Update("DELETE FROM mc_servers WHERE uuid='" + uuid.toString() + "'");
    }

    public boolean isActive() {
        return this.active;
    }

    public void clear() {
        this.getServerMap().clear();
    }

    public Thread getMcAcceptHandlerThread() {
        return this.mcAcceptHandlerThread;
    }

    public void addServer(UUID uuid, Object[] objects) {
        serverMap.put(uuid, objects);
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public ConcurrentHashMap<UUID, Object[]> getServerMap() {
        return serverMap;
    }

    public MCI getMCI() {
        return this.mci;
    }

    public void removeServer(UUID uuid) {
        serverMap.remove(uuid);
    }
}
