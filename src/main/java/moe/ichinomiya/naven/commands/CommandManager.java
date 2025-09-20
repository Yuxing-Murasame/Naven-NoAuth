package moe.ichinomiya.naven.commands;

import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.commands.impl.CommandBind;
import moe.ichinomiya.naven.commands.impl.CommandHacker;
import moe.ichinomiya.naven.commands.impl.CommandToggle;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventClientChat;
import moe.ichinomiya.naven.utils.ChatUtils;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    public final static String PREFIX = ".";

    public final Map<String, Command> aliasMap = new HashMap<>();

    public CommandManager() {
        try {
            initCommands();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Naven.getInstance().getEventManager().register(this);
    }

    private void initCommands() {
        registerCommand(new CommandBind());
        registerCommand(new CommandToggle());
        registerCommand(new CommandHacker());
    }

    private void registerCommand(Command command) {
        command.initCommand();

        aliasMap.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            aliasMap.put(alias.toLowerCase(), command);
        }
    }

    @EventTarget
    public void onChat(EventClientChat e) {
        if (e.getMessage().startsWith(PREFIX)) {
            e.setCancelled(true);

            String chatMessage = e.getMessage().substring(PREFIX.length());

            String[] arguments = chatMessage.split(" ");

            if (arguments.length < 1) {
                ChatUtils.addChatMessage("Invalid command.");
                return;
            }

            String alias = arguments[0].toLowerCase();
            Command command = aliasMap.get(alias);

            if (command == null) {
                ChatUtils.addChatMessage("Invalid command.");
                return;
            }

            String[] args = new String[arguments.length - 1];
            System.arraycopy(arguments, 1, args, 0, args.length);
            command.onCommand(args);
        }
    }
}
