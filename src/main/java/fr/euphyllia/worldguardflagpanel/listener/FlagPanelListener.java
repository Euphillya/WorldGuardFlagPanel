package fr.euphyllia.worldguardflagpanel.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.euphyllia.worldguardflagpanel.configuration.LanguageManager;
import fr.euphyllia.worldguardflagpanel.dialog.FlagPanelDialog;
import fr.euphyllia.worldguardflagpanel.util.WGUtil;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class FlagPanelListener implements Listener {

    private static final String NS = FlagPanelDialog.NS;
    private static final Logger log = LoggerFactory.getLogger(FlagPanelListener.class);

    public FlagPanelListener() {
    }

    @EventHandler
    public void onCustomClick(final PlayerCustomClickEvent event) {
        Key key = event.getIdentifier();
        log.debug("Received custom click with key: {}", key);
        if (!key.namespace().equals(NS)) return;

        if (!(event.getCommonConnection() instanceof PlayerGameConnection conn)) return;
        Player player = conn.getPlayer();

        String[] parts = key.value().split("/", -1);
        if (parts.length == 0) return;

        LanguageManager lang = LanguageManager.getInstance();
        String action = parts[0];

        switch (action) {
            case "select_region", "back" -> {
                if (parts.length < 2) return;
                ProtectedRegion region = getRegion(player, parts[1]);
                if (region == null) {
                    notFound(player, parts[1]);
                    return;
                }
                FlagPanelDialog.openFlagPanel(player, region);
            }

            case "set_state" -> {
                if (parts.length < 4) return;
                ProtectedRegion region = getRegion(player, parts[1]);
                if (region == null) {
                    notFound(player, parts[1]);
                    return;
                }
                if (!WGUtil.canEdit(player, region)) {
                    noPermission(player);
                    return;
                }

                if (WGUtil.applyStateFlag(player, region, parts[2], parts[3])) {
                    notifyChange(player, region, parts[2],
                            parts[3].equals("null") ? "unset" : parts[3].toUpperCase());
                    FlagPanelDialog.openFlagPanel(player, region);
                }
            }

            case "set_bool" -> {
                if (parts.length < 4) return;
                ProtectedRegion region = getRegion(player, parts[1]);
                if (region == null) {
                    notFound(player, parts[1]);
                    return;
                }
                if (!WGUtil.canEdit(player, region)) {
                    noPermission(player);
                    return;
                }

                boolean newVal = Boolean.parseBoolean(parts[3]);
                if (WGUtil.applyBooleanFlag(player, region, parts[2], newVal)) {
                    notifyChange(player, region, parts[2], String.valueOf(newVal));
                    FlagPanelDialog.openFlagPanel(player, region);
                }
            }

            case "edit_flag" -> {
                if (parts.length < 3) return;
                ProtectedRegion region = getRegion(player, parts[1]);
                if (region == null) {
                    notFound(player, parts[1]);
                    return;
                }
                if (!WGUtil.canEdit(player, region)) {
                    noPermission(player);
                    return;
                }

                FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
                Flag<?> flag = registry.get(parts[2]);
                if (flag == null) return;
                FlagPanelDialog.openEditDialog(player, region, flag);
            }

            case "confirm_edit" -> {
                if (parts.length < 3) return;
                ProtectedRegion region = getRegion(player, parts[1]);
                if (region == null) {
                    notFound(player, parts[1]);
                    return;
                }
                if (!WGUtil.canEdit(player, region)) {
                    noPermission(player);
                    return;
                }

                io.papermc.paper.dialog.DialogResponseView view = event.getDialogResponseView();
                if (view == null) {
                    player.sendMessage(lang.translate(player, "notify.error-no-view"));
                    return;
                }

                String rawInput;
                try {
                    rawInput = view.getText(parts[2].replace("-", "_"));
                } catch (Exception e) {
                    player.sendMessage(lang.translate(player, "notify.error-read"));
                    return;
                }

                FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
                Flag<?> flag = registry.get(parts[2]);
                if (flag == null) return;


                if (rawInput == null || rawInput.isBlank()) {
                    region.setFlag(flag, null);
                    WGUtil.saveRegion(player, region);
                    notifyRemoved(player, region, parts[2]);
                } else if (flag instanceof SetFlag<?>) {
                    Set<String> values = new LinkedHashSet<>();
                    for (String s : rawInput.split("\n")) {
                        String trim = s.trim();
                        if (!trim.isEmpty()) {
                            values.add(trim);
                        }
                    }
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    SetFlag<String> setFlag = (SetFlag<String>) flag;
                    region.setFlag(setFlag, values);
                    WGUtil.saveRegion(player, region);
                    notifyChange(player, region, parts[2], String.join(", ", values));
                } else {
                    if (!WGUtil.applyStringFlag(player, region, parts[2], rawInput)) return;
                    notifyChange(player, region, parts[2], rawInput);
                }

                FlagPanelDialog.openFlagPanel(player, region);
            }

            case "set_gamemode" -> {
                if (parts.length < 3) return;
                ProtectedRegion region = getRegion(player, parts[1]);
                if (region == null) {
                    notFound(player, parts[1]);
                    return;
                }
                if (!WGUtil.canEdit(player, region)) {
                    noPermission(player);
                    return;
                }

                FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
                Flag<?> flag = registry.get(parts[2]);
                if (flag == null) return;
                FlagPanelDialog.openGameModeDialog(player, region, flag);
            }

            case "confirm_gamemode" -> {
                if (parts.length < 3) return;
                ProtectedRegion region = getRegion(player, parts[1]);
                if (region == null) {
                    notFound(player, parts[1]);
                    return;
                }
                if (!WGUtil.canEdit(player, region)) {
                    noPermission(player);
                    return;
                }

                io.papermc.paper.dialog.DialogResponseView view = event.getDialogResponseView();
                if (view == null) {
                    player.sendMessage(lang.translate(player, "notify.error-no-view"));
                    return;
                }

                String selected = view.getText("game_mode");

                FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
                Flag<?> flag = registry.get(parts[2]);
                if (flag == null) return;

                if (selected == null || selected.isBlank()) {
                    region.setFlag(flag, null);
                    WGUtil.saveRegion(player, region);
                    notifyRemoved(player, region, parts[2]);
                } else {
                    if (!WGUtil.applyStringFlag(player, region, parts[2], selected)) return;
                    notifyChange(player, region, parts[2], selected);
                }

                FlagPanelDialog.openFlagPanel(player, region);
            }

        }
    }

    private ProtectedRegion getRegion(Player player, String regionId) {
        RegionManager manager = WGUtil.getRegionManager(player.getWorld());
        return manager != null ? manager.getRegion(regionId) : null;
    }

    private void notFound(Player player, String regionId) {
        player.sendMessage(LanguageManager.getInstance().translate(player,
                "notify.region-not-found", Map.of("<region>", regionId)));
    }

    private void noPermission(Player player) {
        player.sendMessage(LanguageManager.getInstance().translate(player, "notify.flag-no-permission"));
    }

    private void notifyChange(Player editor, ProtectedRegion region, String flagName, String value) {
        LanguageManager lang = LanguageManager.getInstance();
        Map<String, String> ph = Map.of("<region>", region.getId(), "<flag>", flagName, "<value>", value);

        editor.sendMessage(lang.translate(editor, "notify.flag-changed", ph));

        for (Player other : Bukkit.getServer().getOnlinePlayers()) {
            if (other.equals(editor) || !other.hasPermission("wgflagpanel.use")) continue;
            other.sendMessage(lang.translate(other, "notify.other-player-changed",
                    Map.of("<player>", editor.getName(), "<region>", region.getId(),
                            "<flag>", flagName, "<value>", value)));
        }
    }

    private void notifyRemoved(Player editor, ProtectedRegion region, String flagName) {
        LanguageManager lang = LanguageManager.getInstance();
        Map<String, String> ph = Map.of("<region>", region.getId(), "<flag>", flagName);

        editor.sendMessage(lang.translate(editor, "notify.flag-removed", ph));

        for (Player other : Bukkit.getServer().getOnlinePlayers()) {
            if (other.equals(editor) || !other.hasPermission("wgflagpanel.use")) continue;
            other.sendMessage(lang.translate(other, "notify.other-player-changed",
                    Map.of("<player>", editor.getName(), "<region>", region.getId(),
                            "<flag>", flagName, "<value>", "removed")));
        }
    }
}
