# IraqueCore

**IraqueCore** is an all-in-one Spigot/Paper core plugin. It started as a merger of two separate plugins — **IraqueCore** and **IraqueScoreboard** — into a single one, improving them and adding a lot of new features so a server can have a complete experience without feeling overloaded.

It is built for **semi-vanilla** servers where the goal is to play chill with friends rather than juggle hundreds of obscure commands and complex systems. Everything is configurable, every feature can be toggled on or off, and everything looks nice.

> Current version: **1.0.0** · API: **26.2** · Java: **21**

---

## Features

### Player features

| Command | What it does |
|---------|--------------|
| `/spawn` | Teleport back to spawn |
| `/msg` and `/r` | Private messaging |
| `/tags` | Open the tag menu to equip a chat prefix |
| `/scoreboard` (alias `/sb`) | Toggle your sidebar on/off |
| `/playtime` (alias `/pt`) | Check how long you've played |
| `/stats` (alias `/me`) | Your stats: blocks broken, deaths, playtime, rank... |
| `/leaderboards` (alias `/lb`) | Leaderboards for blocks, deaths, playtime |
| `/gm` (aliases `gmc`, `gms`, `gma`, `gmsp`) | Change your gamemode (with optional duration) |
| `/home` / `/sethome` / `/delhome` | Set and teleport to your home |
| `/tpa` / `/tpahere` | Teleport request system |
| `/trash` (alias `/lixeira`) | Open a disposal inventory |
| `/iraquecore` (alias `/ic`) | Plugin info |
| `/whitelist` (alias `/wl`) | Whitelist management |
| `/chatcolor` (alias `/cc`) | Chat color selector GUI |
| `/profile` (alias `/whois`) | View a player's profile |
| `/heal` | Heal yourself or another player |
| `/feed` | Feed yourself or another player |
| `/ban` `[-s]` | Ban a player (with time and reason) |
| `/unban` | Unban a player |
| `/mute` `[-s]` | Mute a player (with time and reason) |
| `/unmute` | Unmute a player |
| `/kick` `[-s]` | Kick a player |

### Automatic systems

- **Ranks** — with prefixes, suffixes and colors. Define your own in config.
- **Formatted chat** — show rank, tag and colors in every message.
- **AFK** — after a few minutes without moving, players are marked as away with a custom prefix.
- **Sleep voting** — if enough players sleep, the night is skipped.
- **Durability warning** — warns players when a tool is about to break.
- **Anvil colors** — allow `&` and `&#RRGGBB` color codes in anvil renames.
- **Armor stand editor** — sneak + right-click an armor stand to edit it from a GUI.
- **Animated MOTD** — the server-list message can cycle through frames.
- **Image MOTD** — render an image as text art in the server list, with optional favicon.
- **Animated scoreboard** — animated title (fade, wave, blink, glitch, typing, bounce, scroll) with live stats.
- **Totem notifications** — broadcast when a player uses a totem of undying.
- **Respawn at spawn** — players respawn at the configured spawn point.

### Staff & technical features

- **Ranks & grants** — grant/revoke ranks with expiration dates (`/grant`, `/grants`, `/revoke`).
- **Permission manager** — add/remove/list/clear permissions per player (`/perm`).
- **Alerts** — configurable fullscreen alerts with title, description and sound (`/alert`).
- **Discord integration** — chat bridge, advancement/death/join/leave notifications, webhook support and automatic whitelist sync from Discord.
- **Hex colors** — use `&#RRGGBB` anywhere (messages.yml, tags, scoreboard, items).
- **PlaceholderAPI** — `%iraquecore_...%` placeholders (see below).
- **Multilanguage** — all messages live in `messages.yml`, translate them as you like.
- **Teleport warmup** — spawn/home/tpa teleports have a configurable delay that cancels on movement or damage.

---

## Troll plugin

An advanced trolling system with a GUI (`/troll <player>`), over **70 effects**, per-effect durations/cooldowns, a blocklist and safety features.

| Command | What it does |
|---------|--------------|
| `/troll <player>` | Open the troll effect GUI for a player |
| `/trollf <player> <effect>` | Apply an effect directly from command |
| `/troll undo <player>` | Remove all effects from a player |
| `/troll reload` | Reload the troll configuration |
| `/troll toggle-troll-op` | Allow/deny trolling operators |
| `/troll add-blocked <player>` | Add a player to the blocklist |
| `/troll remove-blocked <player>` | Remove a player from the blocklist |
| `/troll giveskull <player>` | Give yourself a player's head |
| `/untroll <player>` | Remove troll effects from a player |
| `/panicstoptroll` | Emergency stop of all active trolls |

Effects are grouped in categories: visual, movement, inventory, sound/chat, combat/world, interface, classic, explosion, beds, chat/name and random/event-driven. Every effect has its own duration and cooldown in `config.yml` under `troll.durations` and `troll.cooldowns`, and its own permission (`troll.effect.<id>` or `troll.effect.*`).

---

## Placeholders

Requires **PlaceholderAPI**. Identifier: `iraquecore`.

| Placeholder | Returns |
|-------------|---------|
| `%iraquecore_rank%` | Rank prefix + name |
| `%iraquecore_rank_name%` | Rank name |
| `%iraquecore_rank_prefix%` | Rank prefix |
| `%iraquecore_rank_suffix%` | Rank suffix |
| `%iraquecore_rank_color%` | Rank color |
| `%iraquecore_tag%` | Equipped tag |
| `%iraquecore_tag_id%` | Equipped tag id |
| `%iraquecore_playtime%` | Formatted playtime |
| `%iraquecore_playtime_raw%` | Raw playtime (seconds) |
| `%iraquecore_afk%` | `yes`/`no` if the player is AFK |
| `%iraquecore_afk_prefix%` | The AFK prefix if AFK |
| `%iraquecore_chatcolor%` | Active chat color |
| `%iraquecore_deaths%` | Player deaths |
| `%iraquecore_blocks_broken%` | Blocks broken |
| `%iraquecore_blocks_placed%` | Blocks placed |
| `%iraquecore_online%` | Online players count |

Placeholders are also usable in the scoreboard as `{player}`, `{world}`, `{ping}`, `{online}`, `{max}`, `{blocks_broken}`, `{blocks_placed}`, `{deaths}`, etc.

---

## Configuration

Files in `plugins/IraqueCore/`:

| File | Purpose |
|------|---------|
| `config.yml` | Main configuration: general, chat, ranks, scoreboard, afk, sleep, playtime, durability, totem, MOTD, troll, teleport, storage |
| `messages.yml` | All plugin messages (translate freely) |
| `discord.yml` | Discord bot settings, channel ids, webhooks, formats, whitelist sync |
| `motd.yml` | Server-list MOTD text and animation frames |
| `tags.yml` | Define tags (id, display, permission, material, lore) |
| `create-alerts.yml` | Alert definitions (title, description, sound) |
| `homes.yml` | Player home data |
| `stats.yml` | Scoreboard/stat data |
| `playtime.yml` | Playtime data |
| `punishments.yml` | Ban/mute data |

Data is stored in **YAML** files. A `storage.type` option (yaml/sqlite/mysql) is present in `config.yml`; currently **YAML** is the implemented storage backend.

### Quick start

1. Drop the `.jar` into `plugins/`
2. Restart the server
3. Configure `config.yml`, `messages.yml`, `discord.yml`, `tags.yml` and `motd.yml` to taste
4. `/reload` or `/icreload` to apply changes

---

## Permissions

| Permission | Default | What it does |
|------------|---------|--------------|
| `iraquecore.tags.use` | ✅ | Use tags |
| `iraquecore.scoreboard` | ✅ | Toggle your scoreboard |
| `iraquecore.msg` | ✅ | Private messages |
| `iraquecore.playtime` | ✅ | Check playtime |
| `iraquecore.playtime.other` | ✅ | Check others' playtime |
| `iraquecore.stats` | ✅ | View stats |
| `iraquecore.stats.other` | ✅ | View others' stats |
| `iraquecore.anvilcolors` | ✅ | Colors in anvils |
| `iraquecore.armorstand` | ✅ | Armor stand editor |
| `iraquecore.home` | ✅ | Homes |
| `iraquecore.tpa` | ✅ | TPA |
| `iraquecore.chatcolor` | ✅ | Chat colors |
| `iraquecore.trash` | ✅ | Trash inventory |
| `iraquecore.leaderboard` | ✅ | Leaderboards |
| `iraquecore.profile` | ✅ | Player profiles |
| `iraquecore.info` | ✅ | Plugin info |
| `iraquecore.gamemode` | 🔒 | Gamemode commands |
| `iraquecore.gamemode.other` | 🔒 | Change others' gamemode |
| `iraquecore.whitelist` | 🔒 | Whitelist management |
| `iraquecore.reload` | 🔒 | Reload the plugin |
| `iraquecore.setspawn` | 🔒 | Set spawn |
| `iraquecore.rank` | 🔒 | Rank commands |
| `iraquecore.rank.set` | 🔒 | Set a player's rank |
| `iraquecore.grant` / `iraquecore.grants` / `iraquecore.grant.revoke` | 🔒 | Grant system |
| `iraquecore.permission` (+ `.add`, `.remove`, `.list`, `.clear`, `.check`) | 🔒 | Permission manager |
| `iraquecore.ban` / `unban` / `mute` / `unmute` / `kick` | 🔒 | Punishments |
| `iraquecore.punishment.silent` | 🔒 | See silent punishments |
| `iraquecore.heal` / `iraquecore.heal.other` | 🔒 | Heal |
| `iraquecore.feed` / `iraquecore.feed.other` | 🔒 | Feed |
| `iraquecore.removearmorstand` | 🔒 | Remove armor stands |
| `alert.use` / `alert.list` / `alert.reload` / `alert.test` | 🔒 | Alerts |
| `troll.use` | 🔒 | Use the troll command |
| `troll.undo` | 🔒 | Remove troll effects |
| `troll.panic` | 🔒 | Emergency stop all trolls |
| `troll.effect.<id>` | 🔒 | Individual troll effects |
| `troll.effect.*` | 🔒 | All troll effects |
| `iraquecore.*` | 🔒 | Everything (admins only) |

✅ = default for everyone · 🔒 = default OP only

---

## Dependencies

- **Required:** Paper/Spigot 26.2+ (API 26.2), Java 21
- **Soft dependency:** [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (optional — enables the placeholders)
- Bundled: JDA for Discord integration (starts async, never blocks startup)

---

## Credits

Made by **Proctocol** — with love and code that works (when there are no merge conflicts).
