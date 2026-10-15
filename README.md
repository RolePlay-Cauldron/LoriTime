# LoriTime

[![License: GPLv3](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-21-orange.svg)](pom.xml)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21%2B-brightgreen.svg)](https://roleplay-cauldron.github.io/loritime/reference/compatibility/)
[![Platform](https://img.shields.io/badge/platform-Paper%20%7C%20Folia%20%7C%20Velocity-5865f2.svg)](https://roleplay-cauldron.github.io/loritime/reference/compatibility/)

LoriTime tracks how long players are connected to a Minecraft server or network. It supports standalone Paper/Folia servers as well as Velocity-based networks with proxy-owned storage and backend context reporting.

## Features

- Tracks playtime globally, per server, per world, and inside time ranges.
- Supports Paper, Folia, and Velocity runtimes.
- Provides single-server and proxy/backend multi-setup modes.
- Stores data in SQLite, MySQL, or MariaDB.
- Includes AFK handling, configurable commands, placeholders, localization, backups, and update checks.
- Provides bounded network statistics and durable AFK-period history through `/ltstats`.

## Network statistics

`/ltstats` shows a compact overview for the current local calendar day by default. Use `time:<range>`,
`calendar:<period>`, `server:<server>`, and `world:<world>` in any order to select another bounded range or
scope, for example `/ltstats sessions time:7d server:survival`. Duration ranges are rolling: `time:1d` is
exactly 24 hours and `time:7d` is exactly 168 hours.

Use the mutually exclusive `calendar:<period>` flag for local calendar boundaries. `calendar:today` starts at
local midnight, `calendar:this-week` starts Monday at 00:00, and `calendar:this-month` starts on the first day
of the month. Counted forms include the current partial unit and preceding complete units: `calendar:2d`,
`calendar:2w`, and `calendar:2mo`. Two-ended forms select complete ordinal units: `calendar:2d-3d` selects
yesterday and the day before, while `calendar:2w-3w` selects the previous two complete ISO weeks. Both endpoints
must use the same unit and the nearer ordinal must come first. Calendar boundaries use `stats.calendar-time-zone`
(`system` by default, or an IANA id such as `Europe/Berlin`) and follow daylight-saving transitions; a calendar
day may contain 23 or 25 elapsed hours. `system` means the JVM/OS timezone of the canonical storage owner—the
standalone/master backend or storage-owning proxy, not each backend independently. Explicit region identifiers are
recommended for networks whose nodes run in different countries. ISO weeks always begin Monday, and stored history
remains absolute instants when the calendar timezone changes. `time:` and `calendar:` cannot be combined.

The `users`, `sessions`, `usage`, `top`, `afk`, and `retention`
subcommands provide focused views. The command requires `loritime.stats` and is available only on a
standalone/master backend or a proxy that owns canonical storage.

The default range is configured with `stats.default-range` (`calendar:today`). It also accepts an explicit rolling
selector such as `time:1d`. A bounce is a completed logical network
session shorter than `stats.bounce-threshold` (`3m`). World and backend-server switches within 30 seconds are
merged into one logical session. Active sessions are evaluated at the request time and participate in session
totals and durations when they started inside the range, but are never classified as bounces. Playtime, usage,
concurrency, and AFK durations are clipped to the selected range. Seven-day retention excludes cohorts that have not
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

## Using the API in your Plugins

We release this project to **GitHub Packages** but also provide a Mirror for this repository.

### Option 1: Adding the **GitHub Packages** Repository

> [!IMPORTANT]
> Note that GitHub **always** requires you to authenticate against the GitHub Packages Repository,
> even though this is a public repository. This is a limitation by GitHub, not by us.

1) Configure your Maven `~/.m2/settings.xml` with credentials. Create your Token (classic) [here](https://github.com/settings/tokens), you only require the `read:packages` scope
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GH_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

2) Add the repository to your plugins `pom.xml`
```xml
<repositories>
  <repository>
    <id>github</id>
    <name>GitHub Packages</name>
    <url>https://maven.pkg.github.com/RolePlay-Cauldron/Spellbook</url>
  </repository>
</repositories>
```

### Option 2: Adding a **Mirror** Repository

1) Add the repository to your plugins `pom.xml`
```xml
<repositories>
  <repository>
    <id>mvn.velyn.me-github</id>
    <url>https://mvn.velyn.me/github</url>
  </repository>
</repositories>
```

> [!IMPORTANT]
> This Mirror is hosted by one of our maintainers and is subject to change or downtime.
> This mirror is maintained by a maintainer and is not guaranteed.
> It may change or go offline without notice.
> Prefer GitHub Packages whenever possible, especially for reliable builds in CI/CD Environments!

### Add the Dependencies

Only choose the dependencies that you want. `common` contains the platform independent API,
`loritimepaper` and `loritimevelocity` contain the platform specific plugin classes.
```xml
<dependencies>
  <dependency>
    <groupId>com.jannik_kuehn</groupId>
    <artifactId>common</artifactId>
    <version>2.3.1</version>
    <scope>provided</scope>
  </dependency>
  <dependency>
    <groupId>com.jannik_kuehn</groupId>
    <artifactId>loritimepaper</artifactId>
    <version>2.3.1</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

Since LoriTime is a standalone Plugin, use the `provided` scope and do **not** shade it into your plugin.
Declare LoriTime as a plugin dependency instead, so the server loads it before your plugin.

### Declare the Plugin Dependency

Paper / Folia (`paper-plugin.yml`):
```yaml
dependencies:
  server:
    LoriTimePaper:
      load: BEFORE
      required: true
      join-classpath: true
```

Paper / Folia (legacy `plugin.yml`):
```yaml
depend: [ LoriTimePaper ]
```

Velocity (`velocity-plugin.json`):
```json
"dependencies": [
  {
    "id": "loritimevelocity",
    "optional": false
  }
]
```

Velocity (`@Plugin` annotation):
```java
@Plugin(id = "myplugin", dependencies = {@Dependency(id = "loritimevelocity")})
```
