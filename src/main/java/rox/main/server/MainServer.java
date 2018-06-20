package rox.main.server;

import rox.main.Main;
import rox.main.event.events.MainServerStartingEvent;
import rox.main.event.events.MainServerStoppingEvent;
import rox.main.server.command.*;
import rox.main.server.database.MainDatabase;
import rox.main.server.permission.PermissionManager;
import rox.main.server.permission.Rank;
import rox.main.util.BaseServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MainServer implements BaseServer {

    private ServerSocket serverSocket;

    private int port;

    private boolean waitingConnection = true, isActive = false;

    private ConcurrentHashMap<UUID, Object[]> clients = new ConcurrentHashMap<>();

    private MainDatabase database;

    private Thread acceptThread;

    private ServerCommandLoader serverCommandLoader;

    private StaticManager staticManager;

    private PermissionManager permissionManager;


    /**
     * Sets port for clients listening.
     *
     * @param port The port for the server
     */

    public MainServer(int port){
        this.port = port;
    }


    /**
     * Connect to Database and create server socket. Open a new thread for listening new clients are connecting.
     */
    public boolean start(){

        MainServerStartingEvent event = new MainServerStartingEvent();
        Main.getEventManager().callEvent(event);
        if (event.isCancelled()) return false;

        long startTime = System.currentTimeMillis(); // Load Time
        try {
            database = new MainDatabase("localhost", 3306, "root", "", "rox"); // Connecting to database
            if (!database.isConnected()) {
                Main.getLogger().err("MainServer", "Could not start MainServer."); // If can not connect to database
                return false;
            }
            serverSocket = new ServerSocket(port); // Create server socket
            (acceptThread = new ClientAcceptHandler()).start(); // Open new thread for new clients input
            serverCommandLoader = new ServerCommandLoader(); // Command handler if client send a message
            staticManager = new StaticManager(); // Methods where i don't know where to write them.
            permissionManager = new PermissionManager(); // Permissions System for clients, Admins, Mods, Members,..
            loadCommands(); // Loading all commands for the clients
            Main.getLogger().log("MainServer", "Started.");
            isActive = true; // Global boolean to check if server is active
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Main.getLogger().time("MainServerLoad", startTime); // Write to console how long it took to start the server
        return false;
    }

    /**
     * Closing everything and clear lists.
     */
    public boolean stop(){
        try {

            MainServerStoppingEvent event = new MainServerStoppingEvent();
            Main.getEventManager().callEvent(event);
            if (event.isCancelled()) return false;

            serverSocket.close();
            acceptThread.interrupt();
            clients.forEach(((s, objects) -> ((Thread) objects[2]).interrupt()));
            clients.clear();
            Main.getLogger().log("MainServer", "Stopped.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ConcurrentHashMap<UUID, Object[]> getClients() {
        return clients;
    }

    @Override
    public boolean isConnected() {
        return serverSocket.isClosed();
    }

    public void setClients(ConcurrentHashMap<UUID, Object[]> clients) {
        if (this.clients != null) this.clients.clear();
        this.clients = clients;
    }

    public MainDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MainDatabase database) {
        if (this.database != null) this.database.disconnect();
        this.database = database;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public boolean isActive() {
        return isActive;
    }

    void setActive(boolean bool) {
        isActive = bool;
    }

    void setWaitingConnection(boolean bool) {
        waitingConnection = bool;
    }

    public boolean isWaitingConnection(){
        return waitingConnection;
    }

    private void loadCommands() {
        serverCommandLoader.addCommand("§DISCONNECT", new DisconnectCommand());
        serverCommandLoader.addCommand("§MSG", new MsgCommand());
        serverCommandLoader.addCommand("§INFO", new InfoCommand());
        serverCommandLoader.addCommand("§BAN", new BanCommand());
        serverCommandLoader.addCommand("§RANK", new RankCommand());
    }

    public ServerCommandLoader getServerCommandLoader() {
        return serverCommandLoader;
    }

    public StaticManager getStaticManager() {
        return staticManager;
    }

    public void saveUser(UUID uuid) {
        getDatabase().Update("UPDATE users SET rank='" + getClients().get(uuid)[5].toString().toUpperCase() + "'");
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }


    void createUser(String username, String password) {
        getDatabase().Update("INSERT INTO users(username, uuid, password, points, rank) VALUES ('" + username + "','" + UUID.randomUUID() + "','" + password + "','0','" + Rank.USER + "')");
    }

    public boolean isMaintenance() {
        return (Boolean) Main.getFileConfiguration().getValue("maintenance");
    }

    public void setMaintenance(String key, Object value) {
        Main.getFileConfiguration().saveKey(key, value);
    }

}
