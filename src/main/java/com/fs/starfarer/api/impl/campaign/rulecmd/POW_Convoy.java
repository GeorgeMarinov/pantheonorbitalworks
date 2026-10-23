package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

import pantheonorbitalworks.convoy.POW_ConvoyIntel;

// rules.csv hooks for Torgue delivery convoys:
// POW_Convoy defeated | capture | scuttle  (interaction target is the convoy fleet)
// POW_Convoy canDeliver | deliver          (interaction target is a market)
public class POW_Convoy extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        String action = params.get(0).getString(memoryMap);
        SectorEntityToken target = dialog.getInteractionTarget();

        if ("canDeliver".equals(action) || "deliver".equals(action)) {
            MarketAPI market = target == null ? null : target.getMarket();
            if (market == null) return false;
            boolean any = false;
            for (POW_ConvoyIntel intel : getConvoys()) {
                if (intel.canDeliverAt(market)) {
                    any = true;
                    if ("deliver".equals(action)) {
                        intel.deliver(dialog.getTextPanel());
                    }
                }
            }
            return any;
        }

        POW_ConvoyIntel intel = getConvoyFor(target);
        if (intel == null) return false;

        if ("defeated".equals(action)) {
            intel.reportPlayerVictory(dialog.getTextPanel());
            return true;
        } else if ("capture".equals(action)) {
            intel.capture(dialog.getTextPanel());
            return true;
        } else if ("scuttle".equals(action)) {
            intel.scuttle(dialog.getTextPanel());
            return true;
        }
        return false;
    }

    protected POW_ConvoyIntel getConvoyFor(SectorEntityToken target) {
        if (target == null) return null;
        Object intel = target.getMemoryWithoutUpdate().get(POW_ConvoyIntel.MEM_INTEL);
        return intel instanceof POW_ConvoyIntel ? (POW_ConvoyIntel) intel : null;
    }

    protected List<POW_ConvoyIntel> getConvoys() {
        List<POW_ConvoyIntel> result = new ArrayList<POW_ConvoyIntel>();
        for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(POW_ConvoyIntel.class)) {
            result.add((POW_ConvoyIntel) intel);
        }
        return result;
    }
}
