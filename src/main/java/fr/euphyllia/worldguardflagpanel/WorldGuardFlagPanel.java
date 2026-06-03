package fr.euphyllia.worldguardflagpanel;

import dev.faststats.ErrorTracker;
import dev.faststats.Metrics;
import dev.faststats.bukkit.BukkitContext;
import fr.euphyllia.worldguardflagpanel.commands.WGPanelCommand;
import fr.euphyllia.worldguardflagpanel.configuration.LanguageManager;
import fr.euphyllia.worldguardflagpanel.listener.FlagPanelListener;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldGuardFlagPanel extends JavaPlugin {

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware();

    private final BukkitContext context = new BukkitContext.Factory(this, "f79fbbda8fb29e6f4219c74945aa945d")
            .metrics(Metrics.Factory::create)
            .errorTrackerService(ERROR_TRACKER)
            .create();

    @Override
    public void onEnable() {
        // Plugin startup logic
        context.ready();
        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            getLogger().severe("WorldGuard plugin not found! Disabling WorldGuardFlagPanel.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new LanguageManager(this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var commands = event.registrar();
            commands.register("wgfpanel", new WGPanelCommand());
        });

        getServer().getPluginManager().registerEvents(new FlagPanelListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        context.shutdown();
    }
}
