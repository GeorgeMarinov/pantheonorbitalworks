package pantheonorbitalworks.convoy;

import com.fs.starfarer.api.Script;

// Named (not anonymous) so it survives save/load and code changes.
public class POW_ConvoyArrivedScript implements Script {
    protected final POW_ConvoyIntel intel;

    public POW_ConvoyArrivedScript(POW_ConvoyIntel intel) {
        this.intel = intel;
    }

    public void run() {
        intel.reportArrived();
    }
}
