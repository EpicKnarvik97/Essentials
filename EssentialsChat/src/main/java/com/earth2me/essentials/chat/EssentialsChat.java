package com.earth2me.essentials.chat;

import com.earth2me.essentials.EssentialsLogger;
import com.earth2me.essentials.metrics.MetricsWrapper;
import net.ess3.api.IEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.earth2me.essentials.I18n.tl;

public class EssentialsChat extends JavaPlugin {
    private transient IEssentials ess;
    private transient MetricsWrapper metrics = null;

    @Override
    public void onEnable() {
        final PluginManager pluginManager = getServer().getPluginManager();
        ess = (IEssentials) pluginManager.getPlugin("Essentials");
        if (!this.getDescription().getVersion().equals(ess.getDescription().getVersion())) {
            getLogger().log(Level.WARNING, tl("versionMismatchAll"));
        }
        if (!ess.isEnabled()) {
            this.setEnabled(false);
            return;
        }

        final Map<AsyncPlayerChatEvent, ChatStore> chatStore = Collections.synchronizedMap(new HashMap<>());

        final EssentialsChatPlayerListenerLowest playerListenerLowest = new EssentialsChatPlayerListenerLowest(getServer(), ess, this, chatStore);
        final EssentialsChatPlayerListenerNormal playerListenerNormal = new EssentialsChatPlayerListenerNormal(getServer(), ess, this, chatStore);
        final EssentialsChatPlayerListenerHighest playerListenerHighest = new EssentialsChatPlayerListenerHighest(getServer(), ess, this, chatStore);
        pluginManager.registerEvents(playerListenerLowest, this);
        pluginManager.registerEvents(playerListenerNormal, this);
        pluginManager.registerEvents(playerListenerHighest, this);

        if (metrics == null) {
            metrics = new MetricsWrapper(this, 3814, false);
        }
    }

    @Override
    public Logger getLogger() {
        try {
            return EssentialsLogger.getLoggerProvider(this);
        } catch (Throwable ignored) {
            // In case Essentials isn't installed/loaded
            return super.getLogger();
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        metrics.markCommand(command.getName(), true);
        return ess.onCommandEssentials(sender, command, commandLabel, args, EssentialsChat.class.getClassLoader(), "com.earth2me.essentials.chat.Command", "essentials.", null);
    }
}
