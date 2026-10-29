package pantheonorbitalworks.world.systems;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

public class POW_Sol implements SectorGeneratorPlugin {

    // orbit radii around the star (star radius 550, corona 800)
    private static final float GEHENNA_ORBIT = 1800f;
    private static final float GEHENNA_DAYS = 60f;
    private static final float PANTHEON_ORBIT = 3000f;
    private static final float PANTHEON_DAYS = 250f;
    private static final float THE_DUST_ORBIT = 5200f;
    private static final float THE_DUST_DAYS = 300f;
    private static final float ATHENAS_ORBIT = 7500f;
    private static final float ATHENAS_DAYS = 400f;
    private static final float ARENA_ORBIT = 9500f;
    private static final float ARENA_DAYS = 600f;

    @Override
    public void generate(SectorAPI sector) {
        Global.getLogger(this.getClass()).info("in sol gen");
		StarSystemAPI system = sector.createStarSystem("Sol");
		system.setBackgroundTextureFilename("graphics/POW/backgrounds/POW_sol.jpg");
		ProcgenUsedNames.isUsed("Sol");
        system.getLocation().set(-28000f, 7000f);// horizontal, vertical

		PlanetAPI solStar = system.initStar("POW_Pantheon_Star", "star_yellow", 550f,  800);
		PlanetAPI pantheonPlanet = system.addPlanet("pow_patheon_planet", solStar, "Pantheon", "terran", 180, 150, PANTHEON_ORBIT, PANTHEON_DAYS);
		pantheonPlanet.setCustomDescriptionId("pow_patheon_planet");
        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("pow_sol_jumppoint", "Charon Jump-point");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(solStar, 85, 4000, 150);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setRelatedPlanet(pantheonPlanet);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        addGehennaFoundry(system, solStar);
        addPantheonObjectives(system, solStar);
        addTheDust(system, solStar);
        addAthenas(system, solStar);
        addTorgueArena(system, solStar);

        // every planet gets its conditions above (or from POW_Sol.json for markets), same as vanilla core systems
        system.autogenerateHyperspaceJumpPoints(true, true);
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
	    NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;
        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

    // Hyperion smelting complex on the lava world (market in POW_Sol.json), Helios mirrors focus sunlight onto it
    private void addGehennaFoundry(StarSystemAPI system, PlanetAPI star) {
        float angle = 40;
        PlanetAPI gehenna = system.addPlanet("pow_gehenna", star, "Gehenna", "lava", angle, 110, GEHENNA_ORBIT, GEHENNA_DAYS);
        gehenna.setCustomDescriptionId("pow_gehenna");

        // same period as Gehenna's orbit keeps the mirrors locked on its sunward side
        float sunward = angle + 180;
        addMirror(system, gehenna, "pow_helios_mirror_1", "Helios Mirror Alpha", sunward - 30);
        addMirror(system, gehenna, "pow_helios_mirror_2", "Helios Mirror Beta", sunward);
        addMirror(system, gehenna, "pow_helios_mirror_3", "Helios Mirror Gamma", sunward + 30);
    }

    private void addMirror(StarSystemAPI system, PlanetAPI gehenna, String id, String name, float angle) {
        SectorEntityToken mirror = system.addCustomEntity(id, name, Entities.STELLAR_MIRROR, Factions.INDEPENDENT);
        mirror.setCustomDescriptionId("pow_helios_mirror");
        mirror.setCircularOrbitPointingDown(gehenna, angle, 270, GEHENNA_DAYS);
    }

    // comm relay at Pantheon's L4, nav buoy at L5
    private void addPantheonObjectives(StarSystemAPI system, PlanetAPI star) {
        SectorEntityToken relay = system.addCustomEntity("pow_pantheon_relay", "Pantheon Relay", Entities.COMM_RELAY,
                Factions.INDEPENDENT);
        relay.setCircularOrbitPointingDown(star, 180 + 60, PANTHEON_ORBIT, PANTHEON_DAYS);

        SectorEntityToken buoy = system.addCustomEntity("pow_pantheon_buoy", "Pantheon Nav Buoy", Entities.NAV_BUOY,
                Factions.INDEPENDENT);
        buoy.setCircularOrbitPointingDown(star, 180 - 60, PANTHEON_ORBIT, PANTHEON_DAYS);
    }

    // asteroid belt with the Dahl mining outpost (market in POW_Sol.json) and a few hidden caches
    private void addTheDust(StarSystemAPI system, PlanetAPI star) {
        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, THE_DUST_ORBIT - 80, THE_DUST_DAYS - 10);
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, THE_DUST_ORBIT + 60, THE_DUST_DAYS + 10);
        system.addAsteroidBelt(star, 200, THE_DUST_ORBIT, 300, THE_DUST_DAYS - 40, THE_DUST_DAYS + 40, Terrain.ASTEROID_BELT,
                "The Dust");

        SectorEntityToken platform = system.addCustomEntity("pow_dahl_platform", "Dahl Excavation Platform",
                "station_mining00", Factions.INDEPENDENT);
        platform.setCustomDescriptionId("pow_dahl_platform");
        platform.setInteractionImage("illustrations", "industrial_megafacility");
        platform.setCircularOrbitWithSpin(star, 300, THE_DUST_ORBIT, THE_DUST_DAYS, 2, 5);

        CargoAPI supplies = Global.getFactory().createCargo(true);
        supplies.addCommodity(Commodities.SUPPLIES, 120);
        supplies.addCommodity(Commodities.METALS, 200);
        addCache(system, star, "pow_dust_cache_supplies", Entities.SUPPLY_CACHE, 20, THE_DUST_ORBIT - 100, supplies);

        CargoAPI equipment = Global.getFactory().createCargo(true);
        equipment.addCommodity(Commodities.HEAVY_MACHINERY, 60);
        equipment.addCommodity(Commodities.GAMMA_CORE, 1);
        addCache(system, star, "pow_dust_cache_equipment", Entities.EQUIPMENT_CACHE, 140, THE_DUST_ORBIT + 120, equipment);

        CargoAPI weapons = Global.getFactory().createCargo(true);
        weapons.addWeapons("pow_torgue_lightmortar", 2);
        weapons.addWeapons("pow_torgue_heavymortar", 2);
        weapons.addWeapons("pow_torgue_devastator", 1);
        addCache(system, star, "pow_dust_cache_weapons", Entities.WEAPONS_CACHE, 230, THE_DUST_ORBIT + 40, weapons);
    }

    // gas giant with two moons and asteroid clusters at its L4/L5 points
    // Vladof skims volatiles from Athenas, Promethea cracks them into fuel (markets in POW_Sol.json)
    private void addAthenas(StarSystemAPI system, PlanetAPI star) {
        float angle = 260;
        PlanetAPI athenas = system.addPlanet("pow_athenas", star, "Athenas", "gas_giant", angle, 300, ATHENAS_ORBIT, ATHENAS_DAYS);
        athenas.setCustomDescriptionId("pow_athenas");

        SectorEntityToken siphon = system.addCustomEntity("pow_vladof_siphon", "Vladof Siphon Array", "station_side06",
                Factions.INDEPENDENT);
        siphon.setCustomDescriptionId("pow_vladof_siphon");
        siphon.setInteractionImage("illustrations", "orbital");
        siphon.setCircularOrbitPointingDown(athenas, 120, 450, 25);

        PlanetAPI promethea = system.addPlanet("pow_promethea", athenas, "Promethea", "frozen", 30, 70, 700, 30);
        promethea.setCustomDescriptionId("pow_promethea");

        PlanetAPI nekrotafeyo = system.addPlanet("pow_nekrotafeyo", athenas, "Nekrotafeyo", "barren", 200, 50, 1050, 50);
        nekrotafeyo.setCustomDescriptionId("pow_nekrotafeyo");
        addConditions(nekrotafeyo, Conditions.NO_ATMOSPHERE, Conditions.LOW_GRAVITY, Conditions.ORE_SPARSE);

        SectorEntityToken l4 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(300f, 500f, 20, 35, 4f, 14f, "Athenas L4 Asteroids"));
        l4.setCircularOrbit(star, angle + 60, ATHENAS_ORBIT, ATHENAS_DAYS);

        SectorEntityToken l5 = system.addTerrain(Terrain.ASTEROID_FIELD,
                new AsteroidFieldParams(300f, 500f, 20, 35, 4f, 14f, "Athenas L5 Asteroids"));
        l5.setCircularOrbit(star, angle - 60, ATHENAS_ORBIT, ATHENAS_DAYS);
    }

    // Torgue's weapons testing range: scrap field and a few blown-up test ships
    private void addTorgueArena(StarSystemAPI system, PlanetAPI star) {
        float angle = 120;

        DebrisFieldParams params = new DebrisFieldParams(
                400f, // field radius - should not go above 1000 for performance reasons
                1f, // density, visual - affects number of debris pieces
                10000000f, // duration in days, effectively permanent
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldSource.SALVAGE;
        params.baseSalvageXP = 250;
        SectorEntityToken debris = Misc.addDebrisField(system, params, StarSystemGenerator.random);
        debris.setId("pow_torgue_arena");
        debris.setName("Torgue Arena");
        debris.setCustomDescriptionId("pow_torgue_arena");
        SalvageSpecialAssigner.assignSpecialForDebrisField(debris);
        // always visible on the map, no discovery notification
        debris.setSensorProfile(null);
        debris.setDiscoverable(null);
        debris.setCircularOrbit(star, angle, ARENA_ORBIT, ARENA_DAYS);

        addWreck(system, star, "pow_arena_wreck_1", "torgue_basic_lasher_Standard", ShipCondition.WRECKED, angle - 2, ARENA_ORBIT + 150);
        addWreck(system, star, "pow_arena_wreck_2", "torgue_advanced_hammerhead_Standard", ShipCondition.BATTERED, angle + 1, ARENA_ORBIT - 200);
        addWreck(system, star, "pow_arena_wreck_3", "torgue_basic_dominator_Standard", ShipCondition.WRECKED, angle + 3, ARENA_ORBIT + 250);

        SectorEntityToken stableLocation = system.addCustomEntity("pow_sol_stable_location", null, Entities.STABLE_LOCATION,
                Factions.NEUTRAL);
        stableLocation.setCircularOrbitPointingDown(star, angle + 40, ARENA_ORBIT, ARENA_DAYS);
    }

    private void addConditions(PlanetAPI planet, String... conditions) {
        Misc.initConditionMarket(planet);
        for (String condition : conditions) {
            planet.getMarket().addCondition(condition);
        }
    }

    // discoverable cache with fixed loot and no defenders, gone once salvaged
    private void addCache(StarSystemAPI system, PlanetAPI star, String id, String type, float angle, float orbitRadius,
            CargoAPI loot) {
        SectorEntityToken cache = BaseThemeGenerator.addSalvageEntity(system, type, Factions.NEUTRAL);
        cache.setId(id);
        cache.setDiscoverable(true);
        cache.setCircularOrbit(star, angle, orbitRadius, THE_DUST_DAYS);
        Misc.setDefenderOverride(cache, new DefenderDataOverride(Factions.DERELICT, 0f, 0f, 0f));
        BaseSalvageSpecial.addExtraSalvage(loot, cache.getMemoryWithoutUpdate(), -1);
    }

    // salvage-only wreck: TorgueRefit stats need the "modIds=" fleet member id, which a recovered derelict would not have
    private void addWreck(StarSystemAPI system, PlanetAPI star, String id, String variantId, ShipCondition condition,
            float angle, float orbitRadius) {
        DerelictShipData params = new DerelictShipData(new PerShipData(variantId, condition, 0f), false);
        SectorEntityToken wreck = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, params);
        wreck.setId(id);
        wreck.setDiscoverable(true);
        wreck.setCircularOrbit(star, angle, orbitRadius, ARENA_DAYS);
    }
}
