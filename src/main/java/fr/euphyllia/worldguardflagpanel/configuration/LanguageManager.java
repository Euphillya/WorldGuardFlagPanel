package fr.euphyllia.worldguardflagpanel.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;

import fr.euphyllia.worldguardflagpanel.WorldGuardFlagPanel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageManager {

    private static final Logger log = LoggerFactory.getLogger(LanguageManager.class);
    private static LanguageManager instance;

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<Locale, Map<String, String>> translations = new HashMap<>();
    private final Map<Locale, CommentedFileConfig> localeFiles = new HashMap<>();
    private final Locale defaultLocale = Locale.of("en", "GB");
    private final WorldGuardFlagPanel plugin;

    public LanguageManager(WorldGuardFlagPanel plugin) {
        instance = this;
        this.plugin = plugin;
        instance.load();
    }

    public static LanguageManager getInstance() {
        return instance;
    }

    public void load() {
        File langDir = new File(plugin.getDataFolder(), "language");
        if (!langDir.exists()) langDir.mkdirs();

        for (String bundled : new String[]{"en_GB.toml", "fr_FR.toml"}) {
            File dest = new File(langDir, bundled);
            if (!dest.exists()) {
                plugin.saveResource("language/" + bundled, false);
            }
        }

        File[] files = langDir.listFiles((dir, name) -> name.endsWith(".toml"));
        if (files == null) {
            log.error("No language files found in {}", langDir.getAbsolutePath());
            return;
        }

        translations.clear();
        localeFiles.clear();

        for (File file : files) {
            Locale locale = parseLocale(file.getName());
            CommentedFileConfig cfg = CommentedFileConfig.builder(file).sync().autosave().build();
            cfg.load();
            localeFiles.put(locale, cfg);

            Map<String, String> messages = new HashMap<>();
            flattenConfig("", cfg, messages);
            translations.put(locale, messages);
        }

        if (!translations.containsKey(defaultLocale)) {
            log.warn("Default locale {} not found in translations! Defaulting to first available locale.", defaultLocale);
            if (!translations.isEmpty()) {
                Locale first = translations.keySet().iterator().next();
                log.warn("Using {} as default locale.", first);
            } else {
                log.error("No translations loaded! LanguageManager will not work.");
            }
        }
    }

    public void reload() {
        load();
    }

    public Component translate(Player player, String key, Map<String, String> placeholders) {
        return translate(player.locale(), key, placeholders);
    }

    public Component translate(Player player, String key) {
        return translate(player.locale(), key, Map.of());
    }

    public Component translate(Locale locale, String key, Map<String, String> placeholders) {
        String raw = resolveRaw(locale, key);
        raw = applyPlaceholders(raw, placeholders);

        Map<String, String> lang = getOrDefault(locale);
        String prefix = lang.getOrDefault("prefix", "<gold>[WorldGuardFlagPanel]</gold>");
        return miniMessage.deserialize(prefix + " " + raw);
    }

    public Component translateRaw(Player player, String key, Map<String, String> placeholders) {
        return translateRaw(player.locale(), key, placeholders);
    }

    public Component translateRaw(Locale locale, String key, Map<String, String> placeholders) {
        String raw = resolveRaw(locale, key);
        raw = applyPlaceholders(raw, placeholders);
        return miniMessage.deserialize(raw);
    }

    public String raw(Locale locale, String key, Map<String, String> placeholders) {
        String raw = resolveRaw(locale, key);
        return applyPlaceholders(raw, placeholders);
    }

    public String raw(Player player, String key, Map<String, String> placeholders) {
        return raw(player.locale(), key, placeholders);
    }

    public String raw(Player player, String key) {
        return raw(player.locale(), key, Map.of());
    }

    private String resolveRaw(Locale locale, String key) {
        Map<String, String> lang = getOrDefault(locale);

        if (lang.containsKey(key)) return lang.get(key);

        String localeFile = locale.toLanguageTag().replace("-", "_") + ".toml";
        String fallback = readFromResource(localeFile, key);
        if (fallback != null) {
            lang.put(key, fallback);
            writeToFile(locale, key, fallback);
            return fallback;
        }

        Map<String, String> defaultLang = translations.get(defaultLocale);
        if (defaultLang != null && defaultLang.containsKey(key)) return defaultLang.get(key);

        String missing = "<red>Missing: " + key + "</red>";
        lang.put(key, missing);
        writeToFile(locale, key, missing);
        return missing;
    }

    private Map<String, String> getOrDefault(Locale locale) {
        Map<String, String> lang = translations.get(locale);
        if (lang != null) return lang;
        lang = translations.get(defaultLocale);
        if (lang != null) return lang;
        return Map.of();
    }

    private String applyPlaceholders(String message, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }

    private void writeToFile(Locale locale, String key, String value) {
        CommentedFileConfig cfg = localeFiles.get(locale);
        if (cfg == null) return;
        cfg.set(key, value);
        TomlWriter writer = new TomlWriter();
        writer.setIndent(IndentStyle.NONE);
        writer.write(cfg, cfg.getFile(), WritingMode.REPLACE);
    }

    private String readFromResource(String localeFileName, String key) {
        try (InputStream stream = plugin.getResource("language/" + localeFileName)) {
            if (stream == null) return null;
            TomlParser parser = new TomlParser();
            CommentedConfig parsed = parser.parse(stream);
            Map<String, String> messages = new HashMap<>();
            flattenConfig("", parsed, messages);
            return messages.get(key);
        } catch (Exception e) {
            log.error("Error reading fallback translation for key {} in locale {}: {}", key, localeFileName, e.getMessage());
            return null;
        }
    }

    private void flattenConfig(String prefix, CommentedConfig config, Map<String, String> out) {
        for (String key : config.valueMap().keySet()) {
            Object value = config.get(key);
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (value instanceof CommentedConfig sub) {
                flattenConfig(fullKey, sub, out);
            } else {
                out.put(fullKey, String.valueOf(value));
            }
        }
    }

    private Locale parseLocale(String filename) {
        String base = filename.replace(".toml", "");
        String[] parts = base.split("_");
        return parts.length == 2 ? Locale.of(parts[0], parts[1]) : defaultLocale;
    }
}
