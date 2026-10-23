package pantheonorbitalworks.convoy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;

import pantheonorbitalworks.RefitablePhase;

// Builds refitted ships the same way the refit dialog does: the Standard variant of the refit hull, with the
// refit hullmod id appended to the fleet member id ("modIds=..."), which is what TorgueRefit reads its stats from.
public class POW_RefitFactory {

    public static boolean isPhase(String baseHullId) {
        for (RefitablePhase phase : RefitablePhase.values()) {
            if (phase.toString().equals(baseHullId)) {
                return true;
            }
        }
        return false;
    }

    public static String getVariantId(String baseHullId, boolean isPhase, String manufacturer, String chosenPackage) {
        return manufacturer + "_" + chosenPackage + (isPhase ? "_phase_" : "_") + baseHullId + "_Standard";
    }

    public static String getRefitModId(String quality, String manufacturer, String chosenPackage) {
        return quality + "_" + manufacturer + "_" + chosenPackage + "_refit";
    }

    public static FleetMemberAPI createRefitMember(String baseHullId, boolean isPhase, String manufacturer,
            String chosenPackage, String quality) {
        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP,
                getVariantId(baseHullId, isPhase, manufacturer, chosenPackage));
        member.setId(member.getId() + "modIds=" + getRefitModId(quality, manufacturer, chosenPackage));
        return member;
    }
}
