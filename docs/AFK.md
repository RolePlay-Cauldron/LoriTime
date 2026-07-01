## Configuration
> ⚠️ If you want to use the AFK feature on your proxy, you need a [multi-setup](https://github.com/Lorias-Jak/LoriTime/wiki/Setup#multi-setup-network-setup). Install LoriTime on the proxy and on every backend server that should detect AFK state.<br>
> ⚠️ If you use multi-setup, enable the AFK feature on the proxy master and on every Paper/Folia slave where players should be detected as AFK. The backend detects inactivity and sends the AFK state to the proxy; the proxy applies time removal, kicks, and announcements.<br>

* Set `afk.enabled` to `true`.
* Set `afk.after` to the time after which a player should be considered AFK. You can use the [TimeString](https://github.com/Lorias-Jak/LoriTime/wiki/Commands#timestring-examples) format.
* Set `afk.removeTime` to `true` if AFK time should be removed from the player's online time.
* Set `afk.autoKick` to `true` if players should be kicked when they are considered AFK.
* Set `afk.repeatCheck` to the interval for checking whether a player has moved, interacted, or written a chat message. The default is 30 seconds. It is recommended to keep this at 10 seconds or higher.

## The config part
<details>
<summary>AFK-Config (config.yml)</summary>

```yml
###########
#   AFK   #
###########
afk:

  # In case you're using multi-setup, enable this on the proxy master and on
  # every Paper/Folia slave where AFK detection should run.
  # Paper/Folia slaves detect inactivity; the proxy master applies kicks and time removal.
  # Do not change the value while the server is running!
  # The required classes will not be loaded if this option is false on startup.
  # If you change the value to false in runtime and reload the plugin, it could lead to issues with the afk detection.
  enabled: false

  # The time after which a player is considered AFK. You can use the unit-modifier for this.
  # Currently, the player will be considered afk after 15 minutes.
  after: '15m'

  # If true, the time that the player is afk will be removed.
  removeTime: true

  # If true, the player will be kicked after the time specified in 'afk.after'.
  autoKick: true

  # The time how often the plugin checks if a player is afk.
  # The unit for this is seconds.
  repeatCheck: 30
```

</details>

## Permissions

| Feature                                                                                                                  | Permission                          |
|--------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| The player's time will not be removed when they are AFK and `afk.removeTime` is enabled                                  | `loritime.afk.bypass.timeRemove`    |
| Prevents the kick if the player went AFK                                                                                 | `loritime.afk.bypass.kick`          |
| Bypasses the AFK stop-count behavior, so the player's time keeps counting while AFK                                      | `loritime.afk.bypass.stopCount`     |
| Sends a message to all players with the permission when a player becomes AFK                                             | `loritime.afk.announce.afkAnnounce` |
| Sends a kick message to all players with the permission when a player is kicked for being AFK                            | `loritime.afk.announce.kick`        |

## Time Entry Reasons

AFK handling can write dedicated session reasons:

| Situation | Reason |
|-----------|--------|
| The player becomes AFK, is not kicked, and does not have `loritime.afk.bypass.stopCount` | `PLAYER_AFK` |
| The player is kicked by AFK auto-kick enforcement | `PLAYER_AFK_KICK` |
| AFK removes already counted time through `afk.removeTime` | `AFK_ADJUSTMENT` |

AFK start and resume messages describe the player's AFK state. They are sent independently from whether `loritime.afk.bypass.stopCount` keeps the player's time counting.
