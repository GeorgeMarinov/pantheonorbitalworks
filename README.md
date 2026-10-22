# Pantheon Orbital Works

Starsector mod that lets you take vanilla ships to Pantheon (Sol system) and pay to have them refit
into upgraded manufacturer variants (currently Torgue: missiles, explosions, no regard for cost).
Refitted ships also show up in NPC fleets around the sector.

Targets **Starsector 0.98a** (Java 17).

## Requirements

1. Starsector 0.98a installed.
1. A Java 17 (or newer, up to 24) JDK, e.g. [Eclipse Temurin 17](https://adoptium.net/temurin/releases/?version=17).

## Setup

The Starsector API (`starfarer.api.jar`) is not published separately - it ships with the game in
`starsector-core`. The build compiles against the jars of your installed game, so updating the game
updates the API.

`starsectorDir` in `gradle.properties` points at the game install folder (the one containing
`starsector.exe` and `starsector-core`). It defaults to `../../Starsector 98/Starsector`, relative to
this project; change it if your install lives elsewhere.

## Building

- `gradlew.bat build` - compiles `pantheonorbitalworks.jar` into the project root.
- `gradlew.bat installMod` - builds and copies the mod into `<starsectorDir>/mods/pantheonorbitalworks`.
- `gradlew.bat release` - builds `pantheonorbitalworks-<version>.zip` (version from `assets/mod_info.json`).

API source and javadoc are in `<starsectorDir>/starsector-core/starfarer.api.zip`. A browsable copy is at
[jaghaimo/starsector-api](https://github.com/jaghaimo/starsector-api).

## Layout

- `assets/` - the mod folder as the game sees it (`mod_info.json`, `data/`, `graphics/`).
- `src/main/java/pantheonorbitalworks/` - mod plugin, hullmods, ship systems, sector generation, NPC fleet refits.
- `src/main/java/com/fs/starfarer/api/impl/campaign/rulecmd/CEO.java` - the refit dialog, called from `data/campaign/rules.csv`.
