# ManticHooks

[![Build](https://github.com/Mantic-Development/ManticHooks/actions/workflows/build.yml/badge.svg)](https://github.com/Mantic-Development/ManticHooks/actions/workflows/build.yml)

Posts your server's leaderboards to Discord through webhooks.

Runs on Spigot, Paper and forks from **1.8 to the latest release**, on Java 8 or newer.

## Features

- Leaderboards posted as Discord embeds on a timer, each with its own webhook, title, colour, size and interval
- Rank by Vault balance or by **any numeric PlaceholderAPI placeholder** (kills, deaths, playtime, blocks mined...)
- Command name, aliases, permission and every message are configurable
- Player data is read without stalling the server: spread across ticks by default, or fully async for thread-safe sources

## Install

1. Put [ManticLib](https://github.com/Mantic-Development/ManticLib/releases) and [ManticHooks](https://github.com/Mantic-Development/ManticHooks/releases) in `plugins/`.
2. For money leaderboards, install [Vault](https://www.spigotmc.org/resources/vault.34315/) and an economy plugin.
   For placeholder leaderboards, install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) and the expansion you need
   (e.g. `/papi ecloud download Statistic`).
3. Start the server, then set `webhook-url` in `plugins/ManticHooks/config.yml`.
   In Discord: channel settings → Integrations → Webhooks → New Webhook → Copy Webhook URL.
4. Run `/mantichooks reload`.

Treat the webhook URL like a password: anyone who has it can post in your channel.

## Config

```yaml
command-name: mantichooks
command-aliases:
- hooks
command-permission: mantichooks.admin
messages:
  discord-line: '**{rank}.** {player} — {value}'
  # ...every chat message is here too
leaderboards:
- name: money
  webhook-url: https://discord.com/api/webhooks/...
  source: vault-balance
  limit: 10
  interval-minutes: 60
  read-async: false
  # embed look; empty values are left out
  webhook-name: ''
  webhook-avatar: ''
  title: Richest Players
  colour: '#F1C40F'
  footer: ManticHooks
  footer-icon: ''
  thumbnail: https://mc-heads.net/avatar/{top_uuid}
  image: ''
  timestamp: true
- name: kills
  webhook-url: https://discord.com/api/webhooks/...
  source: placeholder
  placeholder: '%statistic_player_kills%'
  title: Top Killers
  colour: '#E74C3C'
  limit: 10
  interval-minutes: 60
  read-async: false
```

`title`, `footer` and every image URL may use `{top_player}` and `{top_uuid}` (the #1 player), for example
`thumbnail: https://mc-heads.net/avatar/{top_uuid}`. Image URLs must be http(s).

Placeholder leaderboards rank every player the expansion answers for, so the expansion must support offline players.
Values that aren't plain numbers (like `1.2k`) are skipped.

`read-async: true` reads data off the main thread. Only turn it on if your economy plugin or placeholder expansion is thread-safe.
With it off, reads are spread over as many ticks as needed, using at most 2 ms per tick.

## Commands

| Command | Permission | |
|---|---|---|
| `/mantichooks reload` | `command-permission` | Reload the config and restart timers |
| `/mantichooks post <name>` | `command-permission` | Post a leaderboard now |

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/`. Needs JDK 21+ to build (Gradle downloads one if missing); the output runs on Java 8.
If a `ManticLib` checkout sits next to this folder, the build uses it instead of the JitPack release.

## Adding a leaderboard source

Implement `dev.mantic.hooks.leaderboard.StatSource` and register a factory for it in `ManticHooks#onEnable` under a new `source` id.
Code must compile against the Spigot 1.8.8 API with Java 8.

## License

[MIT](LICENSE)
