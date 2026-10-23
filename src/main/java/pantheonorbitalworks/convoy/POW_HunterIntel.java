package pantheonorbitalworks.convoy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

// A price on the player's head: one mercenary fleet that keeps tracking the player until it is destroyed.
public class POW_HunterIntel extends BaseIntelPlugin implements FleetEventListener {

    public static final String IMPORTANT_REASON = "pow_hunter";

    public static final String UPDATE_LAUNCHED = "launched";
    public static final String UPDATE_RECLAIMED = "reclaimed";
    public static final String UPDATE_DESTROYED = "destroyed";

    protected final String factionId;
    protected final String reason;
    protected final List<FleetMemberAPI> shipsToReclaim;
    protected final Random random = new Random();

    protected CampaignFleetAPI hunter;
    protected float retargetDays = 0f;
    protected float cooldownDays = 0f;
    protected int reclaimedCount = 0;
    protected boolean destroyed = false;

    public POW_HunterIntel(FactionAPI faction, String reason, List<FleetMemberAPI> shipsToReclaim) {
        this.factionId = faction.getId();
        this.reason = reason;
        this.shipsToReclaim = new ArrayList<FleetMemberAPI>(shipsToReclaim);
    }

    public void start() {
        Global.getSector().addScript(this);
        Global.getSector().getIntelManager().addIntel(this);
    }

    protected FactionAPI getFaction() {
        return Global.getSector().getFaction(factionId);
    }

    @Override
    protected void advanceImpl(float amount) {
        if (destroyed) return;
        float days = Misc.getDays(amount);
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();

        if (hunter == null) {
            // like vanilla's bounty hunters: show up once the player is out in hyperspace
            if (player.isInHyperspace()) {
                spawnHunter();
            }
            return;
        }

        if (cooldownDays > 0f) {
            cooldownDays -= days;
            if (cooldownDays <= 0f) {
                retarget();
            }
            return;
        }

        retargetDays -= days;
        if (retargetDays <= 0f) {
            retarget();
        }
    }

    protected void spawnHunter() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        float mult = POW_ConvoyConfig.HUNTER_MIN_STRENGTH_MULT + random.nextFloat()
                * (POW_ConvoyConfig.HUNTER_MAX_STRENGTH_MULT - POW_ConvoyConfig.HUNTER_MIN_STRENGTH_MULT);
        float points = player.getFleetPoints() * mult;
        points = Math.max(POW_ConvoyConfig.HUNTER_MIN_POINTS, Math.min(POW_ConvoyConfig.HUNTER_MAX_POINTS, points));

        FleetParamsV3 params = new FleetParamsV3(null, player.getLocationInHyperspace(), Factions.MERCENARY, null,
                FleetTypes.MERC_BOUNTY_HUNTER, points, 0f, 0f, 0f, 0f, 0f, 0f);
        params.ignoreMarketFleetSizeMult = true;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) return;

        hunter = fleet;
        hunter.setNoFactionInName(true);
        hunter.setName(getFaction().getDisplayName() + " Bounty Hunters");
        hunter.setNoAutoDespawn(true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        hunter.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
        Misc.makeImportant(hunter, IMPORTANT_REASON);
        hunter.addEventListener(this);

        player.getContainingLocation().addEntity(hunter);
        Vector2f loc = Misc.getPointAtRadius(player.getLocation(), POW_ConvoyConfig.HUNTER_SPAWN_DISTANCE);
        hunter.setLocation(loc.x, loc.y);
        retarget();

        sendUpdateIfPlayerHasIntel(UPDATE_LAUNCHED, false);
    }

    // intercept when the player is in sight, otherwise head to where the player is now
    protected void retarget() {
        if (hunter == null) return;
        retargetDays = POW_ConvoyConfig.HUNTER_RETARGET_DAYS;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        hunter.clearAssignments();

        boolean sameLocation = hunter.getContainingLocation() == player.getContainingLocation();
        if (sameLocation && hunter.getVisibilityLevelOfPlayerFleet() != VisibilityLevel.NONE) {
            hunter.addAssignment(FleetAssignment.INTERCEPT, player, POW_ConvoyConfig.HUNTER_RETARGET_DAYS * 2f,
                    "hunting your fleet");
        } else {
            SectorEntityToken lastSeen = player.getContainingLocation().createToken(player.getLocation());
            hunter.addAssignment(FleetAssignment.GO_TO_LOCATION, lastSeen, POW_ConvoyConfig.HUNTER_RETARGET_DAYS * 2f,
                    "tracking your fleet");
        }
    }

    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (fleet != hunter || destroyed) return;

        if (!hunter.isAlive() || hunter.isEmpty()) {
            finishDestroyed();
            return;
        }

        boolean hunterWonAgainstPlayer = battle.isPlayerInvolved() && primaryWinner != null
                && !battle.onPlayerSide(primaryWinner);
        if (hunterWonAgainstPlayer) {
            reclaimShips();
            // back off for a while instead of chasing the beaten player around
            cooldownDays = POW_ConvoyConfig.HUNTER_COOLDOWN_AFTER_WIN_DAYS;
            hunter.clearAssignments();
            SectorEntityToken hold = hunter.getContainingLocation().createToken(hunter.getLocation());
            hunter.addAssignment(FleetAssignment.HOLD, hold, cooldownDays, "regrouping");
        }
    }

    protected void reclaimShips() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        List<FleetMemberAPI> members = player.getFleetData().getMembersListCopy();
        int count = 0;
        for (FleetMemberAPI member : shipsToReclaim) {
            if (members.contains(member)) {
                player.getFleetData().removeFleetMember(member);
                count++;
            }
        }
        if (count > 0) {
            reclaimedCount += count;
            Global.getSector().getCampaignUI().addMessage("The bounty hunters seized " + count
                    + " of the captured Torgue ships from your fleet.", Misc.getNegativeHighlightColor());
            sendUpdateIfPlayerHasIntel(UPDATE_RECLAIMED, false);
        }
    }

    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (fleet != hunter || destroyed) return;
        if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
            finishDestroyed();
        } else {
            // removed by something else: the contract still stands, so another fleet takes it up
            hunter = null;
        }
    }

    protected void finishDestroyed() {
        destroyed = true;
        if (hunter != null) {
            Misc.makeUnimportant(hunter, IMPORTANT_REASON);
            hunter.removeEventListener(this);
        }
        sendUpdateIfPlayerHasIntel(UPDATE_DESTROYED, false);
        endAfterDelay(POW_ConvoyConfig.DAYS_TO_KEEP_INTEL_AFTER_END);
    }

    // --- intel UI --------------------------------------------------------------------------------------------

    @Override
    protected String getName() {
        String name = "Bounty on You: " + getFaction().getDisplayName();
        if (destroyed) {
            return name + " - Hunters Destroyed";
        }
        return name;
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return getFaction();
    }

    @Override
    public String getIcon() {
        return getFaction().getCrest();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(POW_ConvoyIntel.INTEL_TAG);
        tags.add(Tags.INTEL_BOUNTY);
        tags.add(factionId);
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (hunter != null && hunter.isAlive()) {
            return hunter;
        }
        return null;
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
        Color h = Misc.getHighlightColor();
        Object param = getListInfoParam();
        if (isUpdate && UPDATE_LAUNCHED.equals(param)) {
            info.addPara("Hunters are closing in on your fleet", tc, initPad);
        } else if (isUpdate && UPDATE_RECLAIMED.equals(param)) {
            info.addPara("%s captured ships taken back", initPad, tc, h, "" + reclaimedCount);
        } else if (destroyed) {
            info.addPara("The hunting fleet was destroyed", tc, initPad);
        } else if (hunter == null) {
            info.addPara("Hunters will find you in hyperspace", tc, initPad);
        } else {
            info.addPara("Hunters are tracking your fleet", tc, initPad);
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        FactionAPI faction = getFaction();
        info.addImage(faction.getLogo(), width, 128, opad);
        info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " has put a price on your head " + reason
                + ".", opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
        info.addPara("A mercenary fleet has taken the contract and will keep tracking you from system to system. "
                + "The bounty only ends when that fleet is destroyed.", opad);
        if (!shipsToReclaim.isEmpty()) {
            info.addPara("If they beat you, they will take back any of the captured Torgue ships still in your fleet.",
                    opad);
        }
        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public String getSortString() {
        return "Torgue Bounty";
    }

    @Override
    protected float getBaseDaysAfterEnd() {
        return POW_ConvoyConfig.DAYS_TO_KEEP_INTEL_AFTER_END;
    }
}
