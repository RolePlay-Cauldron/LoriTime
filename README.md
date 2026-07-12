# LoriTime

[![License: GPLv3](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-21-orange.svg)](pom.xml)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21%2B-brightgreen.svg)](docs/Compatibility.md)
[![Platform](https://img.shields.io/badge/platform-Paper%20%7C%20Folia%20%7C%20Velocity-5865f2.svg)](docs/Compatibility.md)

LoriTime tracks how long players are connected to a Minecraft server or network. It supports standalone Paper/Folia servers as well as Velocity-based networks with proxy-owned storage and backend context reporting.

## Features

- Tracks playtime globally, per server, per world, and inside time ranges.
- Supports Paper, Folia, and Velocity runtimes.
- Provides single-server and proxy/backend multi-setup modes.
- Stores data in SQLite, MySQL, or MariaDB.
- Includes AFK handling, configurable commands, placeholders, localization, backups, and update checks.
- Provides bounded network statistics and durable AFK-period history through `/ltstats`.

## Network statistics

`/ltstats` shows a compact overview for the previous day by default. Use `time:<range>`, `server:<server>`,
and `world:<world>` in any order to select a different bounded scope, for example
`/ltstats time:7d server:survival`. The `users`, `sessions`, `usage`, `top`, `afk`, and `retention`
subcommands provide focused views. The command requires `loritime.stats` and is available only on a
standalone/master backend or a proxy that owns canonical storage.

The default range is configured with `stats.default-range` (`1d`). A bounce is a completed logical network
session shorter than `stats.bounce-threshold` (`3m`). World and backend-server switches within 30 seconds are
merged into one logical session. Playtime, usage, concurrency, and AFK durations are clipped to the selected
range; incomplete sessions are not classified as bounces. Seven-day retention excludes cohorts that have not
yet had the complete return window.

AFK duration history begins when the version 3 AFK-period migration is installed; LoriTime does not infer older
AFK intervals. Open intervals left by shutdowns or crashes are recovered with the `SHUTDOWN` end reason.

> LoriTime 2 development builds require Java 21. See [Compatibility](https://roleplay-cauldron.github.io/loritime/reference/compatibility/) for the full platform matrix.

## Installation

1. Download the latest build from [Modrinth](https://modrinth.com/plugin/loritime#download) or [GitHub](https://github.com/Lorias-Jak/LoriTime/releases).
2. Put the matching plugin jar into the `plugins` directory of every server or proxy that should run LoriTime.
3. Start and stop the server once so LoriTime can create its configuration files.
4. Configure `config.yml`, `commands.yml`, and localization files as needed.
5. Start the server again.

For proxy networks, run Velocity as `master` and Paper/Folia backends as `slave` when the proxy should own canonical storage. See [Setup](https://roleplay-cauldron.github.io/loritime/guide/setup/) and [Storage](https://roleplay-cauldron.github.io/loritime/reference/storage/) for the exact configuration.
