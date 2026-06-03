package fr.euphyllia.worldguardflagpanel.commands;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.euphyllia.worldguardflagpanel.configuration.LanguageManager;
import fr.euphyllia.worldguardflagpanel.dialog.FlagPanelDialog;
import fr.euphyllia.worldguardflagpanel.util.WGUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class WGPanelCommand implements BasicCommand {

    public WGPanelCommand() {
    }

    @Override
    public void execute(@NonNull CommandSourceStack commandSourceStack, String @NonNull [] args) {
        LanguageManager lang = LanguageManager.getInstance();
        CommandSender sender = commandSourceStack.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(lang.translate(java.util.Locale.of("en", "GB"), "command.players-only", Map.of()));
            return;
        }

        if (!player.hasPermission(this.permission())) {
            player.sendMessage(lang.translate(player, "command.no-permission"));
            return;
        }

        RegionManager regionManager = WGUtil.getRegionManager(player.getWorld());
        if (regionManager == null) {
            player.sendMessage(lang.translate(player, "command.no-region-manager"));
            return;
        }

        if (args.length >= 1) {
            ProtectedRegion region = regionManager.getRegion(args[0]);
            if (region == null) {
                player.sendMessage(lang.translate(player, "command.region-not-found",
                        Map.of("<region>", args[0])));
                return;
            }
            if (!WGUtil.canEdit(player, region)) {
                player.sendMessage(lang.translate(player, "command.not-owner"));
                return;
            }
            FlagPanelDialog.openFlagPanel(player, region);
        } else {
            List<ProtectedRegion> regions = WGUtil.getRegionsAt(player);
            if (regions.isEmpty()) {
                ProtectedRegion global = regionManager.getRegion("__global__");
                if (global == null) {
                    player.sendMessage(lang.translate(player, "command.no-region-here"));
                    return;
                }
                FlagPanelDialog.openFlagPanel(player, global);
            } else if (regions.size() == 1) {
                ProtectedRegion region = regions.getFirst();
                if (!WGUtil.canEdit(player, region)) {
                    player.sendMessage(lang.translate(player, "command.not-owner"));
                    return;
                }
                FlagPanelDialog.openFlagPanel(player, region);
            } else {
                FlagPanelDialog.openRegionSelector(player, regions);
            }
        }

    }

    @Override
    public @NonNull Collection<String> suggest(@NonNull CommandSourceStack commandSourceStack, String @NonNull [] args) {
        if (!(commandSourceStack.getSender() instanceof Player player)) return List.of();
        if (args.length != 1) return List.of();

        RegionManager manager = WGUtil.getRegionManager(player.getWorld());
        if (manager == null) return List.of();

        String input = args[0].toLowerCase();
        List<String> list = new ArrayList<>();
        for (String id : manager.getRegions().keySet()) {
            if (id.startsWith(input)) {
                list.add(id);
            }
        }
        return list;
    }

    @Override
    public boolean canUse(@NonNull CommandSender sender) {
        return BasicCommand.super.canUse(sender);
    }

    @Override
    public @NonNull String permission() {
        return "worldguardflagpanel.use";
    }
}
