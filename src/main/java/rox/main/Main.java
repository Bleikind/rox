package rox.main;

import rox.main.discord.DiscordBot;
import rox.main.event.EventManager;
import rox.main.event.events.MainStartedEvent;
import rox.main.httpserver.HTTPServer;
import rox.main.logger.Logger;
import rox.main.minecraftserver.MinecraftServer;
import rox.main.news.NewsSystem;
import rox.main.pluginsystem.JavaScriptEngine;
import rox.main.pluginsystem.PluginManager;
import rox.main.server.MainServer;
public class Main {

    private static MainServer mainServer;

    private static DiscordBot discordBot;

    private static Object[] informatics = new Object[16];

    private static Thread[] threads = new Thread[16];

    private static MainCommandLoader mainCommandLoader;

    private static FileConfiguration fileConfiguration;

    private static MinecraftServer minecraftServer;

    private static HTTPServer httpServer;

    private static JavaScriptEngine javaScriptEngine;

    private static PluginManager pluginManager;

    private static NewsSystem newsSystem;

    private static Logger logger;

    private static EventManager eventManager;

    /*
     * This class is the main class.
     * It will setup all servers in a own thread.
     *
     * Informations Object Array ->
     *
     * [0] - EMPTY
     * [1] - DISCORD TOKEN
     * [2] - EMPTY
     * [3] - EMPTY
     *
     * Threads Array ->
     * [0] - MAIN SERVER THREAD
     * [1] - DISCORD BOT THREAD
     * [2] - CONSOLE SCANNER THREAD
     * [3] - MINECRAFT SERVER THREAD
     * [4] - HTTP SERVER THREAD
     * [5] - NEWS SYSTEM THREAD
     * [6] - PLUGIN LOADER THREAD
     * [7] - SCRIPT ENGINE THREAD
     */

    /**
     * The start up function to load everything.
     *
     * @param args  All arguments given to the program
     */


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis(); //Boot Start Time
        (eventManager = new EventManager()).loadEvents();
        logger = new Logger(); // Init logger
        logger.log("ROX", "Starting ROX.");
        fileConfiguration = new FileConfiguration(); // Load files for information
        (mainCommandLoader = new MainCommandLoader()).loadCommands(); // Loading all system commands
        computeArgs(args); // calculate args
        loadThreads();
        logger.time("MainLoad", startTime); // writing to console how long it take to startup everything
        eventManager.callEvent(new MainStartedEvent());
    }

    private static void loadThreads() { // starts all servers and some system functions in a own thread
        long startTime = System.currentTimeMillis(); //Loading Time
        (threads[0] = new Thread(() -> mainServer = new MainServer(8981))).start(); // main server
        (threads[1] = new Thread(() -> discordBot = new DiscordBot((String) informatics[1]))).start(); // discord bot
        (threads[2] = new Thread(() -> mainCommandLoader.initCommandHandle())).start(); // system command handler
        (threads[3] = new Thread(() -> minecraftServer = new MinecraftServer(8982))).start(); // minecraft server
        (threads[4] = new Thread(() -> httpServer = new HTTPServer(8081))).start(); // http server
        (threads[5] = new Thread(() -> newsSystem = new NewsSystem())).start(); // news system
        (threads[6] = new Thread(() -> pluginManager = new PluginManager())).start(); // plugin system
        (threads[7] = new Thread(() -> javaScriptEngine = new JavaScriptEngine())).start(); // javascript engine
        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown)); // Function if system exit
        logger.time("ThreadLoad", startTime); // writing to console how long it take to init threads
    }

    /**
     * Calculate arguments and save them
     *
     * @param args  Arguments from main method.
     * @see         Main#main
     */

    private static void computeArgs(String[] args) {
        try {
            informatics[1] = args[0];
            discordBot.setToken(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log("ROX", "No arguments given.");
        }
    }

    /**
     * Event if system exit
     *
     * stops everything and disconnect all systems from clients or servers
     */


    private static void shutdown() {

        for (Thread thread : threads) {
            if (thread != null) {
                thread.interrupt();
            }
            pluginManager.stop();
            discordBot.disconnect();
            minecraftServer.stop();
            httpServer.getServer().stop(0);
            mainServer.stop();
        }
    }

    public static MainCommandLoader getMainCommandLoader() {
        return mainCommandLoader;
    }

    public static Thread getThread(int i) {
        return threads[i];
    }

    public static void setThread(int i, Thread thread) {
        if (threads[i] == null) {
            threads[threads.length + 1] = thread;
        } else {
            threads[i] = thread;
        }
    }

    public static Object[] getInformatics() {
        return informatics;
    }

    public static void setInformatics(int i, Object obj) {
        informatics[i] = obj;
    }

    public static MainServer getMainServer() {
        return mainServer;
    }

    public static DiscordBot getDiscordBot() {
        return discordBot;
    }

    public static FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public static MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }

    public static HTTPServer getHttpServer() {
        return httpServer;
    }

    public static boolean isDebug() {

        //return (Boolean) fileConfiguration.getValue("debug");
        return true;
    }

    public JavaScriptEngine getJavaScriptEngine() {
        return javaScriptEngine;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public static NewsSystem getNewsSystem() {
        return newsSystem;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static EventManager getEventManager() {
        return eventManager;
    }
}
