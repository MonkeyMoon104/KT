
# 🔥 KillEffect Plugin

[![Version](https://img.shields.io/github/v/release/MonkeyMoon104/KT?style=for-the-badge)](https://github.com/MonkeyMoon104/KT/releases/latest)
[![SpigotMC](https://img.shields.io/badge/SpigotMC-KT-orange?style=for-the-badge&logo=spigotmc)](https://www.spigotmc.org/resources/1-17-1-21-killeffects.125998/)
[![Download JAR](https://img.shields.io/badge/Download-JAR-brightgreen?style=for-the-badge&logo=java)](https://www.spigotmc.org/resources/1-17-1-21-killeffects.125998/download?version=598469)
[![Download ZIP](https://img.shields.io/badge/Download-ZIP-blueviolet?style=for-the-badge&logo=github)](https://github.com/MonkeyMoon104/KT/archive/refs/heads/master.zip)

---

> **Make every kill unforgettable!**  
> `KillEffect` is the ultimate Minecraft plugin for PvP servers, introducing over **15+ unique and spectacular kill effects** that transform ordinary kills player/mobs into epic moments. With full permission control, a sleek GUI, and a customizable config system, this plugin brings style, functionality, and performance together all in one package.

---

## ✨ Features

| Feature                             | Description                                                                                                                                                                                                               |
|-------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 🎇 15+ Unique Effects               | Fire 🔥, Lightning ⚡, Explosion 💥, Hearts 💖, Notes 🎵, Cloud ☁️, Smoke 🌫️, Totem 🔮, and Enderman-inspired End effect 👾, PigStep 🐽, Warden 👹, Glow Missile 🚀, Sniper 🔫, EnchantColumn 🪄, Fireworks 🎆, Wither 💀 |
| 🧑‍💼 Permission-Based Access       | Each effect has its own permission node. OPs automatically bypass them.                                                                                                                                                   |
| 🖱️ Intuitive GUI                   | Use `/kt` or `/killeffect` to easily select effects with a click!                                                                                                                                                         |
| 🔄 Live Reload                     | Use `/kt reload` to reload the config — no server restart needed!                                                                                                                                                         |
| ⚙️ Fully Configurable              | Edit messages, effect names, descriptions, and more in `config.yml`.                                                                                                                                                      |
| 🗃️ Local Storage (SQLite)         | Lightweight, no external database needed. Fast and reliable.                                                                                                                                                              |

---

## 📸 Some effects demo

<div align="center">
  <table>
    <tr>
      <td align="center">
        🎆 <strong>Fireworks</strong><br>
        <a href="https://youtu.be/Dzk_4szGcio">
          <img src="https://img.youtube.com/vi/Dzk_4szGcio/hqdefault.jpg" width="250">
        </a><br>
        <a href="https://youtu.be/Dzk_4szGcio">
          <img src="https://img.shields.io/badge/-▶%20Watch%20Demo-red?style=for-the-badge&logo=youtube">
        </a>
      </td>
      <td align="center">
        🔫 <strong>Sniper</strong><br>
        <a href="https://youtu.be/3Fe-iLyAOwA">
          <img src="https://img.youtube.com/vi/3Fe-iLyAOwA/hqdefault.jpg" width="250">
        </a><br>
        <a href="https://youtu.be/3Fe-iLyAOwA">
          <img src="https://img.shields.io/badge/-▶%20Watch%20Demo-red?style=for-the-badge&logo=youtube">
        </a>
      </td>
    </tr>
    <tr>
      <td align="center">
        🪄 <strong>Enchant Column</strong><br>
        <a href="https://youtu.be/IUMDfLWR7Ro">
          <img src="https://img.youtube.com/vi/IUMDfLWR7Ro/hqdefault.jpg" width="250">
        </a><br>
        <a href="https://youtu.be/IUMDfLWR7Ro">
          <img src="https://img.shields.io/badge/-▶%20Watch%20Demo-red?style=for-the-badge&logo=youtube">
        </a>
      </td>
      <td align="center">
        🚀 <strong>GlowMissile</strong><br>
        <a href="https://youtu.be/kNWW7bRp51Y">
          <img src="https://img.youtube.com/vi/kNWW7bRp51Y/hqdefault.jpg" width="250">
        </a><br>
        <a href="https://youtu.be/kNWW7bRp51Y">
          <img src="https://img.shields.io/badge/-▶%20Watch%20Demo-red?style=for-the-badge&logo=youtube">
        </a>
      </td>
    </tr>
    <tr>
      <td align="center">
        🐽 <strong>Pigstep</strong><br>
        <a href="https://youtu.be/32uD1ZDl-PE">
          <img src="https://img.youtube.com/vi/32uD1ZDl-PE/hqdefault.jpg" width="250">
        </a><br>
        <a href="https://youtu.be/32uD1ZDl-PE">
          <img src="https://img.shields.io/badge/-▶%20Watch%20Demo-red?style=for-the-badge&logo=youtube">
        </a>
      </td>
      <td align="center">
        👹 <strong>Warden</strong><br>
        <a href="https://youtu.be/-iTAhnakGSc">
          <img src="https://img.youtube.com/vi/-iTAhnakGSc/hqdefault.jpg" width="250">
        </a><br>
        <a href="https://youtu.be/-iTAhnakGSc">
          <img src="https://img.shields.io/badge/-▶%20Watch%20Demo-red?style=for-the-badge&logo=youtube">
        </a>
      </td>
    </tr>
  </table>
</div>

## 🚀 Getting Started

### 🔧 Commands

| Command                       | Function                                   |
|------------------------------|--------------------------------------------|
| `/kt` or `/killeffect`       | Open the GUI to choose your kill effect    |
| `/kt reload`                 | Reload plugin configuration on the fly     |
| `/kt set <effect>`           | Set effect manually (without GUI)          |
| `/kt test <effect>`          | Test a kill effect instantly               |

---

### 🔐 Permissions Overview

| Node                   | Description                                |
|------------------------|--------------------------------------------|
| `kt.reload`            | Reload the configuration                   |
| `kt.set`               | Set an effect manually                     |
| `kt.test`              | Test effects                               |
| `kt.killcoins.add`     | Add killcoins                              |
| `kt.killcoins.take`    | Take killcoins                             |
| `kt.killcoins.set`     | Set killcoins                              |
| `kt.killcoins.reset`   | Reset killcoins                            |
| `kt.killcoins.bal`     | Show killcoins                             |
| `kt.fire.use`          | Use the **Fire** effect 🔥                 |
| `kt.lightning.use`     | Use the **Lightning** effect ⚡             |
| `kt.explosion.use`     | Use the **Explosion** effect 💥            |
| `kt.hearts.use`        | Use the **Hearts** effect 💖               |
| `kt.notes.use`         | Use the **Notes** effect 🎵                |
| `kt.cloud.use`         | Use the **Cloud** effect ☁️                |
| `kt.smoke.use`         | Use the **Smoke** effect 🌫️               |
| `kt.totem.use`         | Use the **Totem** effect 🔮                |
| `kt.end.use`           | Use the **End** effect (Enderman style) 👾 |
| `kt.pigstep.use`       | Use the **PigStep** effect 🐽              |
| `kt.warden.use`        | Use the **Warden** effect 👹               |
| `kt.glowmissile.use`   | Use the **GlowMissile** effect 🚀          |
| `kt.sniper.use`        | Use the **Sniper** effect 🔫               |
| `kt.enchantcolumn.use` | Use the **EnchantColumn** effect 🪄        |
| `kt.fireworks.use`     | Use the **Fireworks** effect 🎆            |
| `kt.wither.use`        | Use the **Wither** effect 💀               |

---
Here’s the **Economy System** section translated into English and ready for your `README.md`:

---

## 💰 Economy System

KillEffect includes an optional economy system called **KillCoinsEco**.  
When a player kills another player, they can earn a configurable reward in virtual coins, which can be used to unlock kill effects.

### ✨ Key Features:
- **Kill Rewards**: Each kill grants a configurable amount of coins.
- **Effect Purchases**: Players can buy effects with the coins they earn, if they don't have direct permissions.
- **OP Bypass**: Operators automatically bypass purchase checks.
- **Fully Configurable**: All messages and reward values can be customized in `config.yml`.

### ⚙️ Example Configuration
```yaml
economy:
  enabled: true #If disabled check the permission for the effects
  starting-balance: 10 #After reset the user coins
  reward:
    kill: 5
  currency:
    singular: "KillCoin"
    plural: "KillCoins"
    symbol: "KC"
````

---

### 🔧 How It Works:

* Each time a player kills another, they receive a kill reward message (defined in `messages.kill_reward`).
* If the economy is enabled, commands like `/kt set <effect>` will check whether the player has **purchased** the effect, as well as permissions.
* OP players bypass all purchase checks and can set any effect.

---

## ⚙️ Customization

You have full control over:

- ✅ Kill effect names and descriptions  
- ✅ All plugin messages (`no permissions`, `effect set`, `config reloaded`, etc.)  
- ✅ Resource pack configuration (for sounds)  
- ✅ Particle and potion effects for advanced visual control

---

<details>
  <summary>📁 Click to expand <code>config.yml</code> example</summary>

```yaml
messages:
  commands_no_perm: "&cYou don't have permission to use this command."
  no_permissions: "&cYou don't have permission to use this KillEffect."
  effect_set: "&aYou have selected the KillEffect: &e%effect%"
  effect_already_set: "&eYou have already selected this KillEffect!"
  config_reloaded: "&aConfiguration successfully reloaded!"
  gui_title: "&6Select your KillEffect"
  effect_not_found: "&cKillEffect not found!"
  miss_usage: "&cUse this command &e/kt reload|set|test"
  only_players: "&cOnly players can execute this command!"
  effect_executed: "&aKillEffect executed: &e%effect%"
  invalid_potion: "&cInvalid or missing configured potion: no potion applied."
  potion_set: "&aYou received: &e%potion% %amplifier%"
  not_enough_coins: "&cYou don't have enough KillCoins to buy this effect."
  purchase_failed: "&cAn error occurred while purchasing the effect."
  purchase_success: "&aYou successfully purchased the effect &e%effect%&a!"
  already_bought: "&aAlready purchased"
  price_format: "&6Price: &e%price% %currency%"
  killcoins_only_players: "&cOnly players can use /kt killcoins bal without specifying a player."
  killcoins_balance_self: "&aYour KillCoins balance is: &e%balance%%currency%"
  killcoins_balance_other: "&aThe KillCoins balance of &e%player% &ais: &e%balance%%currency%"
  killcoins_balance_usage: "&cUsage: /kt killcoins bal [player]"
  killcoins_reset: "&aThe balance of &e%player% &ahas been reset to &e%balance%%currency%"
  killcoins_reset_usage: "&cUsage: /kt killcoins reset <player>"
  killcoins_invalid_amount: "&cInvalid amount: &e%amount%"
  killcoins_add: "&aYou added &e%amount%%currency% &ato &e%player%"
  killcoins_take: "&aYou removed &e%amount%%currency% &afrom &e%player%"
  killcoins_take_not_enough: "&c%player% does not have enough %currency%"
  killcoins_set: "&aThe balance of &e%player% &ahas been set to &e%amount%%currency%"
  kill_reward: "&a+%amount% %currency% &7(Kill reward)"
  killcoins_usage: |
    &eCorrect usage:
    &e /kt killcoins add <player> <amount>
    &e /kt killcoins take <player> <amount>
    &e /kt killcoins set <player> <amount>
    &e /kt killcoins reset <player>
    &e /kt killcoins bal [player]


economy:
  enabled: true #If disabled check the permission for the effects
  starting-balance: 10 #After reset the user coins
  reward:
    kill: 5
  currency:
    singular: "KillCoin"
    plural: "KillCoins"
    symbol: "KC"

resource_pack:
  url: "https://download.mc-packs.net/pack/d5889f788003340479e3f767852eddee152ee544.zip"
  sha1: "d5889f788003340479e3f767852eddee152ee544"
  sounds:
    bow-hit:
      name: "kt.hs1"

effects:
  fire:
    enabled: true
    name: "&cFire"
    description: "&cBurn &7your enemies with fire"
    price: 1500

  lightning:
    enabled: true
    name: "&eLightning"
    description: "&bSummon a &elightning bolt &bon your target"
    price: 2500

  explosion:
    enabled: true
    name: "&4Explosion"
    description: "&4Boom! &7A visual explosion effect"
    price: 3000

  hearts:
    enabled: true
    name: "&dHearts"
    description: "&dLove hearts &7when you kill"
    price: 2000

  notes:
    enabled: true
    name: "&aNotes"
    description: "&aMusical effect &ein the rhythm of death"
    price: 1800

  cloud:
    enabled: true
    name: "&fCloud"
    description: "&fMysterious &7and haunting fog"
    price: 2200

  smoke:
    enabled: true
    name: "&8Smoke"
    description: "&8Smoke &7and shadows in the air"
    price: 2100

  totem:
    enabled: true
    name: "&6Totem"
    description: "&6Epic &eparticles &6of the totem"
    price: 4000

  end:
    enabled: true
    name: "&5End"
    description: "&5Creepy &dEnderman &5effect"
    price: 3500

  pigstep:
    enabled: true
    name: "&dPigStep"
    description: "&dPig &bStep &eeffect"
    price: 2750

  warden:
    enabled: true
    name: "&3Warden"
    description: "&3Warden &eeffect"
    price: 7045

  glowmissile:
    enabled: true
    name: "&bGlowMissile"
    description: "&bGlow missile &7effect"
    price: 5300

  sniper:
    enabled: true
    name: "&cSniper"
    description:
      - "&7Long Mortal Shot"
      - "&8Only kill with bow"
    price: 4250

  enchantcolumn:
    enabled: true
    name: "&rEnchant&7Column"
    description: "&rEnchant&7Column &eExplosion!"
    price: 6000
    effectexplosion:
      type: REGENERATION
      amplifier: 2
      duration: 10

  fireworks:
    enabled: true
    name: "&bFireworks"
    description: "&cFire &fworks &7and explosions"
    price: 6345

  wither:
    enabled: true
    name: "&0Wither"
    description: "&0Wither &8and &fflash"
    price: 10500

````

</details>

---

## 🧪 Want a Custom Effect?

<details>
  <summary>✨ Click to request one</summary>

> 💬 Contact MonkeyMoon104 to request your own custom kill effect!
> Custom particles, sounds, and animations can be added on request.

</details>

---

## 📦 Resource Pack Support

Supports sounds through optional resource packs.
Add this to your server config:

```yaml
resource_pack:
  url: "https://download.mc-packs.net/pack/d5889f788003340479e3f767852eddee152ee544.zip"
  sha1: "d5889f788003340479e3f767852eddee152ee544"
  sounds:
    bow-hit:
      name: "kt.hs1"
```

---

## 💡 Why Choose KillEffect?

| ✅ Feature              | 💬 Description                                           |
| ---------------------- | -------------------------------------------------------- |
| Easy to Use            | Intuitive commands and click-based GUI                   |
| Highly Customizable    | Full control over messages, effects, and names           |
| Lightweight & Fast     | Uses SQLite for high performance and minimal lag         |
| Fully Permission-Based | Great for any role-based PvP or mini-game servers        |
| Community Support      | Active development and custom feature requests supported |

---

## 📣 Show Your Support

If you enjoy using **KillEffect**, consider giving it a ⭐ on GitHub and sharing it with others!

> Help this plugin grow and bring more awesome features to the Minecraft community!

---

## 📫 Connect

Want to suggest a feature, report a bug, or contribute?
➡️ [Open an issue](https://github.com/MonkeyMoon104/KT/issues) or [create a pull request](https://github.com/MonkeyMoon104/KT/pulls)

---

🧨 Let the kills be *legendary* with **KillEffect**.
