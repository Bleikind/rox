package rox.main.command;

import rox.main.Main;
import rox.main.MainCommandExecutor;

public class MCSCommand implements MainCommandExecutor {
    @Override
    public void command(String name, String[] args) {

        // mcs (start, stop)

        if (args.length == 2) {
            switch (args[1]) {
                case "start":
                    Main.getMinecraftServer().start();
                    break;
                case "stop":
                    Main.getMinecraftServer().stop();
                    break;
                default:
                    System.out.println("mcs (start, stop)");
            }
        } else {
            System.out.println("mcs (start, stop)");
        }
    }
}