package pantheonorbitalworks;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;

public class ConvertWeapon implements EveryFrameScript {
    private final String _weaponId;
    private final CargoAPI _storageCargo;
    private final int _quantity;
    private final int _refitDuration;
    private float elapsedTime = 0;

    public ConvertWeapon(String weaponId, CargoAPI storageCargo, int quantity, int refitDuration) {
        _weaponId = weaponId;
        _storageCargo = storageCargo;
        _quantity = quantity;
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
        if (elapsedTime < _refitDuration) {
            elapsedTime += Global.getSector().getClock().convertToDays(amount);
            if (elapsedTime >= _refitDuration) {
                _storageCargo.addWeapons(_weaponId, _quantity);

                if (_quantity > 1) {
                    Global.getSector().getCampaignUI()
                            .addMessage(Helpers.weaponIdToLabel(_weaponId.replace("pow_", ""))
                                    + " conversion complete, they are waiting inside Storage");
                } else {
                    Global.getSector().getCampaignUI()
                            .addMessage(Helpers.weaponIdToLabel(_weaponId.replace("pow_", ""))
                                    + " conversion complete, it is waiting inside Storage");
                }
            }
        }
    }
}
