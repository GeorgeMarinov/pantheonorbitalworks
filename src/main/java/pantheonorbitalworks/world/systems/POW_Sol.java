package pantheonorbitalworks.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

public class POW_Sol implements SectorGeneratorPlugin {    
    
    @Override
    public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Sol");
		//system.setBackgroundTextureFilename("graphics/POW/backgrounds/POW_sol.jpg");
		//ProcgenUsedNames.isUsed("Sol");
        system.getLocation().set(-28000f, 7000f);// horizontal, vertical

		// PlanetAPI solStar = system.initStar("POW_Pantheon_Star", "star_yellow", 550f,  800);
		// PlanetAPI pantheonPlanet = system.addPlanet("pow_patheon_planet", solStar, "Pantheon", "terran", 180, 150, 3000, 250);
		// pantheonPlanet.setCustomDescriptionId("pow_patheon_planet");
        // JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("pow_sol_jumppoint", "Charon Jump-point");
        // OrbitAPI orbit = Global.getFactory().createCircularOrbit(solStar, 85, 4000, 150);
        // jumpPoint.setOrbit(orbit);
        // jumpPoint.setRelatedPlanet(pantheonPlanet);
        // jumpPoint.setStandardWormholeToHyperspaceVisual();
        // system.addEntity(jumpPoint);
        // system.autogenerateHyperspaceJumpPoints(true, true, true);
        // HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
	    // NebulaEditor editor = new NebulaEditor(plugin);        
        // float minRadius = plugin.getTileSize() * 2f;
        // float radius = system.getMaxRadiusInHyperspace();
        // editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
        // editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }
}
