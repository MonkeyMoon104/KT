
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

resource_pack:
  url: "https://download.mc-packs.net/pack/d5889f788003340479e3f767852eddee152ee544.zip"
  sha1: "d5889f788003340479e3f767852eddee152ee544"
  sounds:
    bow-hit:
      name: "kt.hs1"

effects:
  fire:
    name: "&cFire"
    description: "&cBurn &7your enemies with fire"
  lightning:
    name: "&eLightning"
    description: "&bSummon a &elightning bolt &bon your target"
  explosion:
    name: "&4Explosion"
    description: "&4Boom! &7A visual explosion effect"
  hearts:
    name: "&dHearts"
    description: "&dLove hearts &7when you kill"
  notes:
    name: "&aNotes"
    description: "&aMusical effect &ein the rhythm of death"
  cloud:
    name: "&fCloud"
    description: "&fMysterious &7and haunting fog"
  smoke:
    name: "&8Smoke"
    description: "&8Smoke &7and shadows in the air"
  totem:
    name: "&6Totem"
    description: "&6Epic &eparticles &6of the totem"
  end:
    name: "&5End"
    description: "&5Creepy &dEnderman &5effect"
  pigstep:
    name: "&dPigStep"
    description: "&dPig &bStep &eeffect"
  warden:
    name: "&3Warden"
    description: "&3Warden &eeffect"
  glowmissile:
    name: "&bGlowMissile"
    description: "&bGlow missile &7effect"

  sniper:
    name: "&cSniper"
    description:
      - "&7Long Mortal Shot"
      - "&8Only kill with bow"

  enchantcolumn:
    name: "&rEnchant&7Column"
    description: "&rEnchant&7Column &eExplosion!"
    effectexplosion:
      type: REGENERATION
      amplifier: 2
      duration: 10

  fireworks:
    name: "&bFireworks"
    description: "&cFire &fworks &7and explosions"

  wither:
    enabled: true
    name: "&0Wither"
    description: "&0Wither &8and &fflash"

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
