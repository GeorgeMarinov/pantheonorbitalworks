package pantheonorbitalworks.convoy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

// Every so often, a faction buys Torgue refits from Pantheon and sends a convoy to pick them up.
public class POW_ConvoyManager implements EveryFrameScript {

    protected final Random random = new Random();
    protected List<POW_ConvoyIntel> convoys = new ArrayList<POW_ConvoyIntel>();
    protected float daysUntilNext;

    public POW_ConvoyManager() {
        daysUntilNext = rollInterval();
    }

    protected float rollInterval() {
        return POW_ConvoyConfig.MIN_DAYS_BETWEEN_CONVOYS + random.nextFloat()
                * (POW_ConvoyConfig.MAX_DAYS_BETWEEN_CONVOYS - POW_ConvoyConfig.MIN_DAYS_BETWEEN_CONVOYS);
    }

    public void advance(float amount) {
        daysUntilNext -= Misc.getDays(amount);
        if (daysUntilNext > 0) return;
        daysUntilNext = rollInterval();

        List<POW_ConvoyIntel> active = new ArrayList<POW_ConvoyIntel>();
        for (POW_ConvoyIntel convoy : convoys) {
            if (convoy.isActive()) {
                active.add(convoy);
            }
        }
        convoys = active;
        if (convoys.size() >= POW_ConvoyConfig.MAX_ACTIVE_CONVOYS) return;

        MarketAPI origin = Global.getSector().getEconomy().getMarket(POW_ConvoyConfig.PANTHEON_MARKET_ID);
        if (origin == null) return;
        MarketAPI buyer = pickBuyer(origin);
        if (buyer == null) return;
        MarketAPI poster = random.nextFloat() < POW_ConvoyConfig.BOUNTY_CHANCE ? pickPoster(buyer) : null;

        POW_ConvoyIntel convoy = new POW_ConvoyIntel(origin, buyer, poster);
        if (convoy.start()) {
            convoys.add(convoy);
        }
    }

    // a major faction market that can fly into Sol without being shot at
    protected MarketAPI pickBuyer(MarketAPI origin) {
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(random);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market == origin || market.isHidden()) continue;
            if (market.getSize() < POW_ConvoyConfig.MIN_BUYER_MARKET_SIZE) continue;
            FactionAPI faction = market.getFaction();
            if (!isMajorFaction(faction) || Factions.INDEPENDENT.equals(faction.getId())) continue;
            if (faction.isHostileTo(origin.getFaction())) continue;
            picker.add(market, market.getSize());
        }
        return picker.pick();
    }

    // a market of a faction at war with the buyer
    protected MarketAPI pickPoster(MarketAPI buyer) {
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(random);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isHidden()) continue;
            if (market.getSize() < POW_ConvoyConfig.MIN_POSTER_MARKET_SIZE) continue;
            FactionAPI faction = market.getFaction();
            if (!isMajorFaction(faction)) continue;
            if (!faction.isHostileTo(buyer.getFaction())) continue;
            picker.add(market, market.getSize());
        }
        return picker.pick();
    }

    protected boolean isMajorFaction(FactionAPI faction) {
        String id = faction.getId();
        if (Factions.PLAYER.equals(id) || Factions.PIRATES.equals(id) || Factions.LUDDIC_PATH.equals(id)) {
            return false;
        }
        return faction.isShowInIntelTab();
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }
}
