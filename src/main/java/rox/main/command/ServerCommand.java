package rox.main.command;

import rox.main.Main;
import rox.main.MainCommandExecutor;

public class ServerCommand implements MainCommandExecutor {
    @Override
    public void command(String name, String[] args) {

        if (args.length == 2) {
            switch (args[1]) {
                case "start":
                    if (Main.getMainServer().isActive()) {
                        System.out.println("Server is already running.");
                    } else {
                        System.out.println("Starting Main Server...");
                        Main.getMainServer().start();
                    }
                    break;

                case "stop":
                    if (Main.getMainServer().isActive()) {
                        System.out.println("Main Server stopping...");
                        Main.getMainServer().stop();
                    } else {
                        System.out.println("Main server isn't running.");
                    }
                    break;

                default:
                    System.out.println("server (start,stop)");
                    break;
            }
        } else {
            System.out.println("server (start,stop)");
        }

    }
}