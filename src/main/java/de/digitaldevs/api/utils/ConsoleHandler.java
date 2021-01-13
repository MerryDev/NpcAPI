package de.digitaldevs.api.utils;

import lombok.AllArgsConstructor;
import org.bukkit.command.ConsoleCommandSender;

@AllArgsConstructor
public class ConsoleHandler {

  private final ConsoleCommandSender consoleCommandSender;

  public void sendPluginName() {
    consoleCommandSender.sendMessage("§3  _   _   _____     _____              _____    _____ ");
    consoleCommandSender.sendMessage("§3 | \\ | | |  __ \\   / ____|     /\\     |  __ \\  |_   _|");
    consoleCommandSender.sendMessage("§3 |  \\| | | |__) | | |         /  \\    | |__) |   | |  ");
    consoleCommandSender.sendMessage("§3 | . ` | |  ___/  | |        / /\\ \\   |  ___/    | |  ");
    consoleCommandSender.sendMessage("§3 | |\\  | | |      | |____   / ____ \\  | |       _| |_ ");
    consoleCommandSender.sendMessage("§3 |_| \\_| |_|       \\_____| /_/    \\_\\ |_|      |_____|");
    consoleCommandSender.sendMessage("§3                                                     ");
  }

}
