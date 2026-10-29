package pantheonorbitalworks;

import java.util.List;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.VariantSource;

public class RefitShip implements EveryFrameScript {
    private final String _hullModIds;
    private final CargoAPI _storageCargo;
    private final String _newHull;
    private final String _shipName;
    private final String _hullName;
    private final int _refitDuration;
    // s-mods carried over from the ship being upgraded; null for refits saved before this existed
    private final List<String> _sMods;
    private float elapsedTime = 0;

    public RefitShip(String hullModIds, CargoAPI storageCargo, String newHull, String shipName, String hullName,
            int refitDuration, List<String> sMods) {
        _hullModIds = hullModIds;
        _storageCargo = storageCargo;
        _newHull = newHull;
        _shipName = shipName;
        _hullName = hullName;
        _refitDuration = refitDuration;
        _sMods = sMods;
    }

    @Override
    public boolean isDone() {
        return elapsedTime >= _refitDuration;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (elapsedTime < _refitDuration) {
            elapsedTime += Global.getSector().getClock().convertToDays(amount);
            if (elapsedTime >= _refitDuration) {
                _storageCargo.addMothballedShip(FleetMemberType.SHIP, _newHull, _shipName);
                _storageCargo.initMothballedShips("player");
                FleetDataAPI shipsInStorage = _storageCargo.getMothballedShips();
                for (FleetMemberAPI shipInStorage : shipsInStorage.getMembersListCopy()) {
                    // older refits in storage can share the name, only the new ship has no modIds yet
                    if (shipInStorage.getShipName().equals(_shipName) && !shipInStorage.getId().contains("modIds=")) {
                        String shipId = shipInStorage.getId();
                        String newId = shipId + "modIds=" + _hullModIds;
                        shipInStorage.setId(newId);
                        if (_sMods != null && !_sMods.isEmpty()) {
                            ShipVariantAPI variant = shipInStorage.getVariant().clone();
                            variant.setSource(VariantSource.REFIT);
                            for (String sMod : _sMods) {
                                variant.addPermaMod(sMod, true);
                            }
                            shipInStorage.setVariant(variant, false, true);
                        }
                        break;
                    }
                }

                Global.getSector().getCampaignUI()
                        .addMessage(_shipName + " " + _hullName + " refit complete, it is waiting inside Storage");
            }
        }
    }
}
