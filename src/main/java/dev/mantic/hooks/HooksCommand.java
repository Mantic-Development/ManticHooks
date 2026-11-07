package dev.mantic.hooks;

import dev.mantic.hooks.config.Messages;
import me.fullpage.manticlib.command.ManticCommand;
import me.fullpage.manticlib.command.TabCompleteBuilder;
import me.fullpage.manticlib.command.TabCompleteElement;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class HooksCommand extends ManticCommand {

    private final ManticHooks plugin;
    private final Messages messages;

    public HooksCommand(ManticHooks plugin, String name, List<String> aliases, String permission, Messages messages) {
        super(name, permission);
        this.plugin = plugin;
        this.messages = messages;
        setAliases(aliases);
        setUsage(messages.getUsage());
        setMinimumArgs(1);
    }

    @Override
    public void run() {
        String action = args[0];
        if (action.equalsIgnoreCase("reload")) {
            // reload() replaces this command, so read the new message set from the plugin, not this instance.
            int count = plugin.reload();
            sendMessage(Placeholders.apply(plugin.getMessages().getReloaded(), "count", count));
        } else if (action.equalsIgnoreCase("post") && args.length == 2) {
            String name = args[1];
            switch (plugin.post(name)) {
                case STARTED:
                    sendMessage(Placeholders.apply(messages.getPosting(), "name", name));
                    break;
                case BUSY:
                    sendMessage(Placeholders.apply(messages.getAlreadyPosting(), "name", name));
                    break;
                default:
                    sendMessage(Placeholders.apply(messages.getUnknownLeaderboard(), "name", name, "active", String.join(", ", plugin.getFeedNames())));
            }
        } else {
            sendUsageMessage();
        }
    }

    @Override
    public String getPermissionMessage() {
        return messages.getNoPermission();
    }

    @Override
    public void sendUsageMessage() {
        sendMessage(messages.getUsage());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return TabCompleteBuilder.create(args, sender)
                .add(new TabCompleteElement(0, "reload", "post").setPermission(permission))
                .add(new TabCompleteElement(1, plugin.getFeedNames()).setPermission(permission))
                .build();
    }
}
