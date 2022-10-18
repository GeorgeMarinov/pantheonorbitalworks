package pantheonorbitalworks;

import java.util.HashMap;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;

public class ConvertWeapon implements EveryFrameScript {
    private final HashMap<String, Integer> _weaponList;
    private final CargoAPI _storageCargo;
    private final int _refitDuration;
    private float elapsedTime = 0;

    public ConvertWeapon(HashMap<String, Integer> weaponList, CargoAPI storageCargo, int refitDuration) {
        _weaponList = weaponList;
        _storageCargo = storageCargo;
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
                String message = "";
                Integer totalQuantity = 0;
                for (String weaponId : _weaponList.keySet()) {
                    Integer quantity = _weaponList.get(weaponId);
                    totalQuantity += quantity;
                    _storageCargo.addWeapons(weaponId, quantity);
                    message += quantity + " " + Helpers.weaponIdToLabel(weaponId.replace("pow_", "")) + ", ";
                }

                if (totalQuantity > 1) {
                    Global.getSector().getCampaignUI()
                            .addMessage(message.substring(0, message.lastIndexOf(","))
                                    + " conversion complete, they are waiting inside Storage");
                } else {
                    Global.getSector().getCampaignUI()
                            .addMessage(message.substring(0, message.lastIndexOf(","))
                                    + " conversion complete, it is waiting inside Storage");
                }
            }
        }
    }
}
