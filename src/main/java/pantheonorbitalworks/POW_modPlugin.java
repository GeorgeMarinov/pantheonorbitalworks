package pantheonorbitalworks;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import pantheonorbitalworks.world.POW_Gen;
import org.apache.log4j.Logger;

public class POW_modPlugin extends BaseModPlugin {
		
    @Override
    public void onNewGame() {
    Global.getLogger(this.getClass()).info("Hooray My mod plugin in a jar is loaded!");
    Logger log = Global.getLogger(POW_modPlugin.class);
    log.error("Hooray My mod plugin in a jar is loaded!2");
	new POW_Gen().generate(Global.getSector());
    }
}
