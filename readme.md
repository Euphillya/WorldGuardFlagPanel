<div align="center">

# WorldGuardFlagPanel

### A native GUI panel for managing WorldGuard flags — fully compatible with Folia

[![Folia](https://img.shields.io/badge/Folia-Compatible-green.svg)](https://papermc.io/software/folia)
[![Paper](https://img.shields.io/badge/Paper-1.21.8+-blue.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![WorldGuard](https://img.shields.io/badge/WorldGuard-7.0.13+-red.svg)](https://dev.enginehub.org/worldguard/)
[![License](https://img.shields.io/badge/license-MIT-blue)](https://github.com/Euphillya/WorldGuardFlagPanel)

[GitHub](https://github.com/Euphillya/WorldGuardFlagPanel) • [Issues](https://github.com/Euphillya/WorldGuardFlagPanel/issues)  • [Modrinth](*) • [Discord](https://discord.gg/uUJQEB7XNN)

[![Servers & Players](https://faststats.dev/embed/default:83f973ef-4bbd-4059-b386-4b08cdbac339:servers-and-players.svg?w=960&h=340&theme=dark)](https://faststats.dev/project/worldguardflagpanel/minecraft-plugin)

</div>

---

## 📖 About

**WorldGuardFlagPanel** is a lightweight Paper/Folia plugin that adds a fully interactive **in-game GUI** to manage [WorldGuard](https://dev.enginehub.org/worldguard/) region flags — no complex commands needed.

Instead of typing `/region flag <region> <flag> <value>` every time, simply open the panel, click a flag, and the value cycles or a form opens for you to type a new one. Changes are saved instantly.

---

## ✨ Key Features

### 🖱️ Native In-Game Panel
- **Click-through interface** — Flags are listed with their current value; one click toggles or opens an editor
- **State flags** — Cycle through `ALLOW` → `DENY` → *(unset)* with a single click
- **Boolean flags** — Toggle `TRUE` / `FALSE` instantly
- **Text / numeric flags** — Opens a native input form pre-filled with the current value
- **Set flags** — Multiline editor, one entry per line
- **game-mode flag** — Dropdown selector: `Survival`, `Creative`, `Adventure`, `Spectator`
- **Location flags** — Accepts `x,y,z` or `x,y,z,world` format

### 🏝️ Smart Region Detection
- **Auto-detect** — Opens the panel for the region you're standing in
- **Multi-region selector** — If you're inside overlapping regions, a picker lets you choose which one to edit
- **Direct access** — Target any region by name: `/wgfpanel <region>`
- **Global region** — Falls back to `__global__` when no region is found at your position

### 🌍 Multi-language Support
- Ships with `fr_FR` 🇫🇷 and `en_GB` 🇬🇧
- Language is picked automatically from each player's Minecraft client locale
- Fully customizable via `.toml` files

### ⚡ Performance
- **Instant saves** — Region changes are persisted to disk immediately after each edit

---

## 📋 Requirements

| Component          | Minimum version |
|--------------------|-----------------|
| Paper **or** Folia | 1.21.8          |
| WorldGuard         | 7.0.13          |
| WorldEdit          | 7.3.11          |
| Java               | 21              |

---

## 🚀 Installation

1. **Download** the latest `.jar` from [GitHub Releases](https://github.com/Euphillya/WorldGuardFlagPanel/releases).
2. **Place** it in your server's `plugins/` folder.
3. Make sure **WorldGuard** and **WorldEdit** are already installed.
4. **Restart** the server.

No configuration file is required to get started.

---

## 🎮 Commands

### `/wgfpanel [region]`

| Usage                | Behaviour                                                                                             |
|----------------------|-------------------------------------------------------------------------------------------------------|
| `/wgfpanel`          | Opens the panel for the region at your position. Shows a region selector if multiple regions overlap. |
| `/wgfpanel <region>` | Opens the panel for the specified region, wherever you are.                                           |

> Tab-completion is available for region names.

---

## 🔐 Permissions

| Permission                  | Description                                              | Default |
|-----------------------------|----------------------------------------------------------|---------|
| `worldguardflagpanel.use`   | Allows opening the panel for regions the player **owns** | OP      |
| `worldguardflagpanel.admin` | Full access to **all** regions in any world              | OP      |

> Without `worldguardflagpanel.admin`, players can only edit regions where they are set as **owner** in WorldGuard.

---

## 🌍 Languages

The plugin ships with two built-in translations:

- 🇫🇷 `fr_FR` — French
- 🇬🇧 `en_GB` — English

The displayed language is automatically determined by **each player's Minecraft client locale** — no configuration needed.

To customize messages, edit the `.toml` files located in:
```
plugins/WorldGuardFlagPanel/language/
```

---

## 📊 Statistics

This plugin collects **anonymous** usage statistics (server count, player count) via [FastStats](https://faststats.dev/project/worldguardflagpanel/minecraft-plugin). No personal data is ever transmitted. These metrics help track adoption and guide future development.

---

## 📞 Support

- **GitHub**: [Euphillya/WorldGuardFlagPanel](https://github.com/Euphillya/WorldGuardFlagPanel)
- **Issues**: [Report a bug](https://github.com/Euphillya/WorldGuardFlagPanel/issues)
- **Discord**: [Join the discussion](https://discord.gg/uUJQEB7XNN)

---

## 📜 License

WorldGuardFlagPanel is open source. See the [LICENSE](LICENSE.txt) file for details.