package pantheonorbitalworks.world;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import pantheonorbitalworks.world.systems.POW_Sol;

public class POW_Gen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
        new POW_Sol().generate(sector);
    }
}
