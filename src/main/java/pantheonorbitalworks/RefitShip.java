package pantheonorbitalworks;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;

public class RefitShip implements EveryFrameScript {
    private final CargoAPI _storageCargo;
    private final String _newHull;
    private final String _shipName;
    private final String _hullName;
    private final int _refitDuration;
    private float elapsedTime = 0;

    public RefitShip(CargoAPI storageCargo, String newHull, String shipName, String hullName, int refitDuration) {
        _storageCargo = storageCargo;
        _newHull = newHull;
        _shipName = shipName;
        _hullName = hullName;
        _refitDuration = refitDuration;
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
        if (elapsedTime < _refitDuration)
            {
                elapsedTime += Global.getSector().getClock().convertToDays(amount);
                if (elapsedTime >= _refitDuration)
                {
                    _storageCargo.addMothballedShip(FleetMemberType.SHIP, _newHull, _shipName);
                    Global.getSector().getCampaignUI().addMessage(_shipName + " " + _hullName + " refit complete, it is waiting inside Storage");
                }
            }
    }
}
