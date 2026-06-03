package fr.euphyllia.worldguardflagpanel.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.euphyllia.worldguardflagpanel.configuration.LanguageManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WGUtil {

    private static final Logger log = LoggerFactory.getLogger(WGUtil.class);

    private WGUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static RegionManager getRegionManager(World world) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world));
    }

    public static List<ProtectedRegion> getRegionsAt(Player player) {
        RegionManager manager = getRegionManager(player.getWorld());
        if (manager == null) return List.of();

        var pos = BukkitAdapter.asBlockVector(player.getLocation());
        List<ProtectedRegion> result = new ArrayList<>();
        for (ProtectedRegion region : manager.getApplicableRegions(pos)) {
            if (!region.getId().equals("__global__")) {
                result.add(region);
            }
        }
        return result;
    }

    public static boolean canEdit(Player player, ProtectedRegion region) {
        if (player.hasPermission("wgflagpanel.admin")) return true;
        com.sk89q.worldguard.LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        return region.isOwner(wgPlayer);
    }

    @SuppressWarnings("unchecked")
    public static boolean applyStateFlag(Player player, ProtectedRegion region,
                                         String flagName, String stateStr) {
        Flag<?> flag = WorldGuard.getInstance().getFlagRegistry().get(flagName);
        if (!(flag instanceof StateFlag sf)) return false;
        StateFlag.State newState = switch (stateStr.toLowerCase()) {
            case "allow" -> StateFlag.State.ALLOW;
            case "deny" -> StateFlag.State.DENY;
            default -> null;
        };
        region.setFlag(sf, newState);
        saveRegion(player, region);
        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean applyBooleanFlag(Player player, ProtectedRegion region,
                                           String flagName, boolean value) {
        Flag<?> flag = WorldGuard.getInstance().getFlagRegistry().get(flagName);
        if (!(flag instanceof BooleanFlag bf)) return false;
        region.setFlag(bf, value);
        saveRegion(player, region);
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean applyStringFlag(Player player, ProtectedRegion region,
                                          String flagName, String rawValue) {
        Flag flag = WorldGuard.getInstance().getFlagRegistry().get(flagName);
        if (flag == null) return false;
        try {
            com.sk89q.worldguard.LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            FlagContext ctx = FlagContext.create()
                    .setSender(wgPlayer).setInput(rawValue)
                    .setObject("region", region).build();
            region.setFlag(flag, flag.parseInput(ctx));
            saveRegion(player, region);
            return true;
        } catch (InvalidFlagFormat e) {
            player.sendMessage(LanguageManager.getInstance().translate(player, "notify.invalid-value",
                    Map.of("<error>", e.getMessage())));
            return false;
        }
    }

    public static void saveRegion(Player player, ProtectedRegion region) {
        RegionManager manager = WGUtil.getRegionManager(player.getWorld());
        if (manager == null) return;
        try {
            manager.saveChanges();
        } catch (Exception e) {
            log.error("Failed to save region changes for region {}: {}", region.getId(), e.getMessage());
        }
    }
}
