package fr.euphyllia.worldguardflagpanel.dialog;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.euphyllia.worldguardflagpanel.configuration.LanguageManager;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class FlagPanelDialog {

    public static final String NS = "wgflagpanel";
    private static final TextColor COLOR_ALLOW = TextColor.color(0x5CFF8A);
    private static final TextColor COLOR_DENY = TextColor.color(0xFF5C6A);
    private static final TextColor COLOR_UNSET = TextColor.color(0xAAAAAA);
    private static final TextColor COLOR_ACCENT = TextColor.color(0x64B5F6);
    private static final Logger log = LoggerFactory.getLogger(FlagPanelDialog.class);

    public static void openRegionSelector(Player player, List<ProtectedRegion> regions) {
        LanguageManager lang = LanguageManager.getInstance();
        List<ActionButton> buttons = new ArrayList<>();

        for (ProtectedRegion region : regions) {
            String id = region.getId();
            Key key = Key.key(NS, "select_region/" + id);
            buttons.add(ActionButton.builder(Component.text(id, COLOR_ACCENT))
                    .action(DialogAction.customClick(key, null))
                    .width(200).build());
        }
        buttons.add(ActionButton.builder(lang.translateRaw(player, "selector.cancel-button", Map.of()))
                .action(null).width(100).build());

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(lang.translateRaw(player, "selector.title", Map.of()))
                        .body(List.of(DialogBody.plainMessage(
                                lang.translateRaw(player, "selector.instruction", Map.of()))))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons).build()));

        player.showDialog(dialog);
    }

    public static void openFlagPanel(Player player, ProtectedRegion region) {
        LanguageManager lang = LanguageManager.getInstance();
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        List<Flag<?>> flags = new ArrayList<>(registry.getAll());
        flags.sort(Comparator.comparing(Flag::getName));

        List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.plainMessage(
                lang.translateRaw(player, "panel.region-label",
                        Map.of("<region>", region.getId()))));
        body.add(DialogBody.plainMessage(
                lang.translateRaw(player, "panel.instruction", Map.of())));

        List<ActionButton> buttons = new ArrayList<>();
        for (Flag<?> flag : flags) {
            Object value = region.getFlag(flag);
            Component label = buildFlagLabel(flag, value);
            Key key = buildActionKey(flag, value, region);

            ActionButton.Builder btn = ActionButton.builder(label).width(280);
            if (key != null) btn.action(DialogAction.customClick(key, null));
            buttons.add(btn.build());
        }

        buttons.add(ActionButton.builder(lang.translateRaw(player, "panel.close-button", Map.of()))
                .action(null).width(100).build());

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(lang.translateRaw(player, "panel.title", Map.of()))
                        .body(body)
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons).build()));

        player.showDialog(dialog);
    }

    public static void openEditDialog(Player player, ProtectedRegion region, Flag<?> flag) {
        LanguageManager lang = LanguageManager.getInstance();
        Object currentValue = region.getFlag(flag);

        String currentStr;
        if (currentValue instanceof Set<?> set) {
            StringJoiner joiner = new StringJoiner("\n");
            for (Object o : set) joiner.add(o.toString());
            currentStr = joiner.toString();
        } else {
            currentStr = currentValue != null ? currentValue.toString() : "";
        }

        Key confirmKey = Key.key(NS, "confirm_edit/" + region.getId() + "/" + flag.getName());
        Key backKey = Key.key(NS, "back/" + region.getId());

        boolean isSet = flag instanceof SetFlag<?>;
        String inputKey = flag.getName().replace("-", "_");

        var inputBuilder = DialogInput.text(inputKey,
                        lang.translateRaw(player, "edit.input-label", Map.of()))
                .width(300)
                .initial(currentStr)
                .multiline(isSet ? TextDialogInput.MultilineOptions.create(null, 120) : null);

        String hintKey = isSet ? "edit.hint-set"
                : (flag instanceof LocationFlag ? "edit.hint-location" : "edit.hint-empty");

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(
                                lang.translateRaw(player, "edit.title", Map.of("<flag>", flag.getName())))
                        .body(List.of(
                                DialogBody.plainMessage(lang.translateRaw(player, "edit.current-value",
                                        Map.of("<value>", currentStr.isEmpty() ? "(none)" : currentStr.replace("\n", ", ")))),
                                DialogBody.plainMessage(lang.translateRaw(player, hintKey, Map.of()))
                        ))
                        .inputs(List.of(inputBuilder.build()))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(lang.translateRaw(player, "edit.confirm-button", Map.of()))
                                .action(DialogAction.customClick(confirmKey, null)).width(130).build(),
                        ActionButton.builder(lang.translateRaw(player, "edit.cancel-button", Map.of()))
                                .action(DialogAction.customClick(backKey, null)).width(100).build()
                )));

        player.showDialog(dialog);
    }

    public static void openGameModeDialog(Player player, ProtectedRegion region, Flag<?> flag) {
        LanguageManager lang = LanguageManager.getInstance();
        Object currentValue = region.getFlag(flag);
        String currentStr = currentValue != null ? currentValue.toString().toLowerCase() : "";

        List<String> gameModes = List.of("survival", "creative", "adventure", "spectator");

        List<SingleOptionDialogInput.OptionEntry> entries = gameModes.stream()
                .map(gm -> SingleOptionDialogInput.OptionEntry.create(
                        gm,
                        Component.text(gm.substring(0, 1).toUpperCase() + gm.substring(1)),
                        gm.equals(currentStr)
                ))
                .toList();

        Key confirmKey = Key.key(NS, "confirm_gamemode/" + region.getId() + "/" + flag.getName());
        Key backKey = Key.key(NS, "back/" + region.getId());

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(
                                lang.translateRaw(player, "edit.title", Map.of("<flag>", flag.getName())))
                        .body(List.of(
                                DialogBody.plainMessage(lang.translateRaw(player, "edit.current-value",
                                        Map.of("<value>", currentStr.isEmpty() ? "(none)" : currentStr)))
                        ))
                        .inputs(List.of(
                                DialogInput.singleOption(
                                        "game_mode",
                                        lang.translateRaw(player, "edit.input-label", Map.of()),
                                        entries
                                ).width(300).build()
                        ))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(lang.translateRaw(player, "edit.confirm-button", Map.of()))
                                .action(DialogAction.customClick(confirmKey, null)).width(130).build(),
                        ActionButton.builder(lang.translateRaw(player, "edit.cancel-button", Map.of()))
                                .action(DialogAction.customClick(backKey, null)).width(100).build()
                )));

        player.showDialog(dialog);
    }

    private static Component buildFlagLabel(Flag<?> flag, Object value) {
        Component nameComp = Component.text(flag.getName() + "  ", NamedTextColor.WHITE);
        Component valComp;
        switch (value) {
            case null -> valComp = Component.text("—", COLOR_UNSET);
            case StateFlag.State s -> valComp = s == StateFlag.State.ALLOW
                    ? Component.text("ALLOW", COLOR_ALLOW).decoration(TextDecoration.BOLD, true)
                    : Component.text("DENY", COLOR_DENY).decoration(TextDecoration.BOLD, true);
            case Boolean b -> valComp = b
                    ? Component.text("TRUE", COLOR_ALLOW).decoration(TextDecoration.BOLD, true)
                    : Component.text("FALSE", COLOR_DENY).decoration(TextDecoration.BOLD, true);
            default -> {
                String str = value.toString();
                valComp = Component.text(str.length() > 30 ? str.substring(0, 28) + "…" : str, COLOR_ACCENT);
            }
        }
        return nameComp.append(valComp);
    }

    private static Key buildActionKey(Flag<?> flag, Object value, ProtectedRegion region) {
        String base = region.getId() + "/" + flag.getName();
        switch (flag) {
            case StateFlag stateFlag -> {
                String next = nextStateStr((StateFlag.State) value);
                return Key.key(NS, "set_state/" + base + "/" + next);
            }
            case BooleanFlag booleanFlag -> {
                boolean next = !Boolean.TRUE.equals(value);
                return Key.key(NS, "set_bool/" + base + "/" + next);
            }
            default -> {
            }
        }
        if (flag instanceof StringFlag || flag instanceof IntegerFlag || flag instanceof DoubleFlag
                || flag instanceof SetFlag<?> || flag instanceof LocationFlag) {
            return Key.key(NS, "edit_flag/" + base);
        }
        if (flag instanceof RegistryFlag<?>) {
            if (flag.getName().equals("game-mode")) {
                return Key.key(NS, "set_gamemode/" + base);
            }
            return Key.key(NS, "edit_flag/" + base);
        }
        log.error("Unsupported flag type for editing: {} ({}), defaulting to no action", flag.getClass().getName(), flag.getName());
        return null;
    }

    private static String nextStateStr(StateFlag.State current) {
        if (current == null) return "allow";
        if (current == StateFlag.State.ALLOW) return "deny";
        return "null";
    }
}
