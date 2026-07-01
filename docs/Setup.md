This page walks you through installing LoriTime on a Paper/Folia server or a Velocity proxy network.

## Requirements

- Check the [Compatibility](https://github.com/Lorias-Jak/LoriTime/wiki/Compatibility) page before installing.
- Download the matching jar from the [latest release](https://github.com/Lorias-Jak/LoriTime/releases/latest).

## Which Jar to Install

| Platform | Jar |
|----------|-----|
| Paper or Folia | `loritimepaper` |
| Velocity | `loritimevelocity` |

Install the Paper/Folia jar on every backend server that should run LoriTime. Install the Velocity jar on the proxy only when LoriTime should run on the proxy.

## Recommended Setups

| Setup | Use when | `multiSetup.mode` |
|-------|----------|-------------------|
| Single Paper/Folia server | One server owns its own storage. | `standalone` |
| Paper/Folia without a proxy | Each server is independent. | `standalone` |
| Standalone Velocity proxy | The proxy tracks playtime by backend server without Paper/Folia backend coordination. | `standalone` |
| Velocity proxy with Paper/Folia backends | The proxy owns canonical storage and backends report local context. | Proxy: `master`; backends: `slave` |

LoriTime does not support multi-proxy setups.

## First-Start Checklist

1. Put the matching LoriTime jar in the server or proxy `plugins` directory.
2. Start and stop the server or proxy once so LoriTime can create its files.
3. Edit `config.yml`, `commands.yml`, and language files as needed.
4. Start the server or proxy again.
5. Check the startup logs for storage, migration, and update messages.

## Storage Migration Note

LoriTime 2 no longer uses `yml` as a regular storage mode. If legacy `data/names.yml` or `data/time.yml` files are detected, LoriTime imports them into SQLite and renames the old files with a `.migrated` suffix after a successful import.

Back up your LoriTime data folder and SQL database before updating an existing production setup.

## When to Use Multi-Setup

Use multi-setup when a Velocity proxy should own canonical storage for the whole network while Paper/Folia backend servers report player world context, PlaceholderAPI cache data, or AFK state to the proxy.

You usually do not need multi-setup when:

- You run a single Paper/Folia server without a proxy.
- You run only LoriTime on Velocity and do not need Paper/Folia backend reporting.
- Each backend server should keep independent LoriTime data.
- You do not need proxy-owned storage or cross-backend time.

A standalone Velocity proxy can use `multiSetup.mode: standalone`. In that mode LoriTime can track time by Velocity backend server, but it cannot track Bukkit worlds and it does not provide PlaceholderAPI placeholders.

Use multi-setup when:

- You want one shared playtime store across a Velocity network.
- You want PlaceholderAPI values on Paper/Folia backends while the proxy owns storage.
- You want AFK detection on Paper/Folia backends with proxy-owned AFK effects.

## Single Server Setup

1. Install the `loritimepaper` jar in the Paper/Folia `plugins` directory.
2. Start and stop the server once.
3. Keep `multiSetup.mode` set to `standalone`.
4. Configure storage, commands, localization, AFK, and integrations as needed.
5. Start the server again.

```yml
multiSetup:
  mode: 'standalone'
```

## Multi-Setup Network Setup

1. Install the `loritimevelocity` jar in the Velocity proxy `plugins` directory.
2. Install the `loritimepaper` jar in every Paper/Folia backend `plugins` directory.
3. Start and stop the proxy and backend servers once.
4. Set the Velocity proxy `multiSetup.mode` to `master`.
5. Set every Paper/Folia backend `multiSetup.mode` to `slave`.
6. Start the proxy and backend servers again.

The proxy backend server name is used for canonical session rows. A Paper/Folia slave `server.name` is not used for canonical server entries in this setup.

<details>
<summary>Velocity proxy config.yml</summary>

```yml
multiSetup:
  mode: 'master'
```

</details>

<details>
<summary>Paper/Folia backend config.yml</summary>

```yml
multiSetup:
  mode: 'slave'

server:
  # Used only when this Paper/Folia instance owns local session rows.
  # In proxy slave mode, the proxy backend name is canonical instead.
  name: 'survival-1'
```

</details>
