package pantheonorbitalworks.convoy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import pantheonorbitalworks.RefitableShips;

// One Torgue delivery: escorts from the buyer collect mothballed refits at Pantheon and fly them home.
// Optionally carries a bounty from a faction hostile to the buyer: destroy the escort, or capture and deliver the ships.
public class POW_ConvoyIntel extends BaseIntelPlugin implements FleetEventListener {

    public static final String INTEL_TAG = "Pantheon Orbital Works";
    public static final String FLEET_TYPE = "pow_convoy";
    public static final String DEFEAT_TRIGGER = "POWConvoyDefeated";
    public static final String MEM_INTEL = "$pow_convoyIntel";
    public static final String MEM_CONVOY = "$pow_convoy";
    public static final String MEM_DEFENSELESS = "$pow_convoyDefenseless";
    public static final String IMPORTANT_REASON = "pow_convoy";

    public enum Stage {
        LOADING, TRAVELLING, UNLOADING, DEFENSELESS, CAPTURED, DONE
    }

    public enum Outcome {
        DELIVERED, TIMED_OUT, LOST_TO_PIRATES, LOST_OTHER, DESTROYED_BY_PLAYER, RECOVERED_BY_BUYER,
        CAPTURED_KEPT, CAPTURED_DELIVERED, CAPTURE_DEADLINE_MISSED, SCUTTLED
    }

    // update notifications (listInfoParam)
    public static final String UPDATE_DEPARTED = "departed";
    public static final String UPDATE_AMBUSH = "ambush";
    public static final String UPDATE_DEFENSELESS = "defenseless";
    public static final String UPDATE_CAPTURED = "captured";
    public static final String UPDATE_DELIVERED_SOME = "delivered_some";

    protected final MarketAPI origin;
    protected final MarketAPI buyer;
    protected final MarketAPI poster; // null when there is no bounty
    protected final Random random = new Random();

    protected CampaignFleetAPI fleet;
    protected List<FleetMemberAPI> cargo = new ArrayList<FleetMemberAPI>();
    protected List<FleetMemberAPI> delivered = new ArrayList<FleetMemberAPI>();
    protected Stage stage = Stage.LOADING;
    protected Outcome outcome = null;

    protected float escortPoints;
    protected int destroyReward;
    protected boolean destroyRewardPaid = false;
    protected int deliveredCredits = 0;
    protected float elapsedDays = 0f;
    protected float stageDays = 0f;
    protected boolean ambushRolled = false;

    public POW_ConvoyIntel(MarketAPI origin, MarketAPI buyer, MarketAPI poster) {
        this.origin = origin;
        this.buyer = buyer;
        this.poster = poster;
    }

    // --- lifecycle
    // -------------------------------------------------------------------------------------------

    public boolean start() {
        createCargo();
        float cargoValue = 0f;
        for (FleetMemberAPI member : cargo) {
            cargoValue += member.getBaseValue();
        }
        escortPoints = cargoValue / 10000f * POW_ConvoyConfig.ESCORT_POINTS_PER_10K_CARGO;
        escortPoints = Math.max(POW_ConvoyConfig.MIN_ESCORT_POINTS,
                Math.min(POW_ConvoyConfig.MAX_ESCORT_POINTS, escortPoints));
        float maxFp = buyer.getFaction().getApproximateMaxFPPerFleet(ShipPickMode.PRIORITY_THEN_ALL);
        if (maxFp > 0 && escortPoints > maxFp) {
            escortPoints = maxFp + (maxFp * 0.10f);
        }
        destroyReward = Math.round(escortPoints * POW_ConvoyConfig.DESTROY_REWARD_PER_ESCORT_POINT / 1000f) * 1000;

        if (!spawnFleet()) {
            return false;
        }

        Global.getSector().addScript(this);
        Global.getSector().getIntelManager().addIntel(this);
        return true;
    }

    protected void createCargo() {
        RefitableShips[] hulls = RefitableShips.values();
        WeightedRandomPicker<String> qualities = new WeightedRandomPicker<String>(random);
        for (int i = 0; i < POW_ConvoyConfig.CARGO_QUALITIES.length; i++) {
            qualities.add(POW_ConvoyConfig.CARGO_QUALITIES[i], POW_ConvoyConfig.CARGO_QUALITY_WEIGHTS[i]);
        }

        int count = POW_ConvoyConfig.MIN_CARGO_SHIPS
                + random.nextInt(POW_ConvoyConfig.MAX_CARGO_SHIPS - POW_ConvoyConfig.MIN_CARGO_SHIPS + 1);
        for (int i = 0; i < count; i++) {
            String hullId = hulls[random.nextInt(hulls.length)].toString();
            String chosenPackage = random.nextFloat() < POW_ConvoyConfig.EXPERT_PACKAGE_CHANCE ? "expert" : "advanced";
            FleetMemberAPI member = POW_RefitFactory.createRefitMember(hullId, POW_RefitFactory.isPhase(hullId),
                    POW_ConvoyConfig.MANUFACTURER, chosenPackage, qualities.pick());

            // own copy of the variant, and keep the faction autofit from replacing the
            // Torgue loadout
            ShipVariantAPI variant = member.getVariant().clone();
            variant.setSource(VariantSource.REFIT);
            variant.addTag(Tags.TAG_NO_AUTOFIT_UNLESS_PLAYER);
            member.setVariant(variant, false, true);
            member.setShipName(buyer.getFaction().pickRandomShipName());
            cargo.add(member);
        }
    }

    protected boolean spawnFleet() {
        FleetParamsV3 params = new FleetParamsV3(buyer, origin.getLocationInHyperspace(), buyer.getFactionId(), null,
                FLEET_TYPE, escortPoints, 0f, 0f, 0f, 0f, 0f, 0f);
        params.ignoreMarketFleetSizeMult = true;
        fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) {
            return false;
        }

        fleet.setNoFactionInName(true);
        fleet.setName(buyer.getFaction().getDisplayName() + " Torgue Delivery");
        fleet.setNoAutoDespawn(true);
        fleet.getMemoryWithoutUpdate().set(MEM_CONVOY, true);
        fleet.getMemoryWithoutUpdate().set(MEM_INTEL, this);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_TRADE_FLEET, true);
        fleet.addEventListener(this);
        // any convoy can be raided for its ships; only bounty convoys pay for it
        Misc.addDefeatTrigger(fleet, DEFEAT_TRIGGER);
        if (poster != null) {
            Misc.makeImportant(fleet, IMPORTANT_REASON);
        }

        for (FleetMemberAPI member : cargo) {
            fleet.getFleetData().addFleetMember(member);
            member.getRepairTracker().setMothballed(true);
        }
        fleet.getFleetData().sort();

        SectorEntityToken from = origin.getPrimaryEntity();
        SectorEntityToken to = buyer.getPrimaryEntity();
        from.getContainingLocation().addEntity(fleet);
        fleet.setLocation(from.getLocation().x, from.getLocation().y);

        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, from, POW_ConvoyConfig.LOADING_DAYS,
                "loading Torgue refits", new POW_ConvoyLoadedScript(this));
        fleet.addAssignment(FleetAssignment.DELIVER_RESOURCES, to, POW_ConvoyConfig.MAX_TRAVEL_DAYS,
                "delivering Torgue refits to " + buyer.getName());
        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, to, POW_ConvoyConfig.UNLOADING_DAYS,
                "unloading Torgue refits", new POW_ConvoyArrivedScript(this));
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, to, 10f);
        return true;
    }

    @Override
    protected void advanceImpl(float amount) {
        float days = Misc.getDays(amount);
        elapsedDays += days;
        stageDays += days;

        switch (stage) {
            case LOADING:
            case TRAVELLING:
                if (stage == Stage.TRAVELLING && !ambushRolled && fleet != null && fleet.isInHyperspace()) {
                    ambushRolled = true;
                    if (random.nextFloat() < POW_ConvoyConfig.AMBUSH_CHANCE) {
                        spawnPirates();
                    }
                }
                if (elapsedDays > POW_ConvoyConfig.LOADING_DAYS + POW_ConvoyConfig.MAX_TRAVEL_DAYS + 10f) {
                    removeFleet();
                    finish(Outcome.TIMED_OUT);
                }
                break;
            case DEFENSELESS:
                if (stageDays > POW_ConvoyConfig.DEFENSELESS_DAYS) {
                    removeFleet();
                    finish(Outcome.RECOVERED_BY_BUYER);
                }
                break;
            case CAPTURED:
                if (stageDays > POW_ConvoyConfig.DELIVERY_DEADLINE_DAYS) {
                    // any captured ship that wasn't handed over counts as kept, whether or not it
                    // was sold since
                    List<FleetMemberAPI> undelivered = new ArrayList<FleetMemberAPI>(cargo);
                    undelivered.removeAll(delivered);
                    new POW_HunterIntel(poster.getFaction(), "to recover the Torgue ships you took from "
                            + buyer.getFaction().getDisplayNameWithArticle(), undelivered).start();
                    finish(Outcome.CAPTURE_DEADLINE_MISSED);
                }
                break;
            default:
                break;
        }
    }

    // called by POW_ConvoyLoadedScript
    public void reportLoaded() {
        if (stage != Stage.LOADING)
            return;
        setStage(Stage.TRAVELLING);
        sendUpdateIfPlayerHasIntel(UPDATE_DEPARTED, true);
    }

    // called by POW_ConvoyArrivedScript
    public void reportArrived() {
        if (stage != Stage.TRAVELLING || fleet == null)
            return;
        if (fleet.getContainingLocation() != buyer.getPrimaryEntity().getContainingLocation()) {
            removeFleet();
            finish(Outcome.TIMED_OUT);
            return;
        }
        setStage(Stage.UNLOADING);
        // the buyer takes the ships; the escort heads home and despawns via its last
        // assignment
        for (FleetMemberAPI member : cargo) {
            if (fleet.getFleetData().getMembersListCopy().contains(member)) {
                fleet.getFleetData().removeFleetMember(member);
            }
        }
        finish(Outcome.DELIVERED);
    }

    protected void setStage(Stage stage) {
        this.stage = stage;
        stageDays = 0f;
    }

    protected void finish(Outcome outcome) {
        if (this.outcome != null)
            return;
        this.outcome = outcome;
        setStage(Stage.DONE);
        if (fleet != null) {
            Misc.makeUnimportant(fleet, IMPORTANT_REASON);
            fleet.removeEventListener(this);
            fleet.getMemoryWithoutUpdate().unset(MEM_INTEL);
            fleet.getMemoryWithoutUpdate().unset(MEM_DEFENSELESS);
            Misc.removeDefeatTrigger(fleet, DEFEAT_TRIGGER);
        }
        sendUpdateIfPlayerHasIntel(outcome, false);
        endAfterDelay(POW_ConvoyConfig.DAYS_TO_KEEP_INTEL_AFTER_END);
    }

    protected void removeFleet() {
        if (fleet != null && fleet.isAlive()) {
            fleet.removeEventListener(this);
            fleet.despawn(FleetDespawnReason.OTHER, null);
        }
    }

    // --- pirates
    // ---------------------------------------------------------------------------------------------

    protected void spawnPirates() {
        if (!buyer.getFaction().isHostileTo(Factions.PIRATES))
            return;

        float mult = POW_ConvoyConfig.AMBUSH_MIN_STRENGTH_MULT + random.nextFloat()
                * (POW_ConvoyConfig.AMBUSH_MAX_STRENGTH_MULT - POW_ConvoyConfig.AMBUSH_MIN_STRENGTH_MULT);
        FleetParamsV3 params = new FleetParamsV3(null, fleet.getLocationInHyperspace(), Factions.PIRATES, null,
                FleetTypes.PATROL_MEDIUM, getEscortPoints() * mult, 0f, 0f, 0f, 0f, 0f, 0f);
        params.ignoreMarketFleetSizeMult = true;
        CampaignFleetAPI pirates = FleetFactoryV3.createFleet(params);
        if (pirates == null || pirates.isEmpty())
            return;

        pirates.setName("Convoy Raiders");
        pirates.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        pirates.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        pirates.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
        pirates.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
        pirates.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true);

        fleet.getContainingLocation().addEntity(pirates);
        org.lwjgl.util.vector.Vector2f loc = Misc.getPointAtRadius(fleet.getLocation(),
                POW_ConvoyConfig.AMBUSH_SPAWN_DISTANCE);
        pirates.setLocation(loc.x, loc.y);
        pirates.addAssignment(FleetAssignment.INTERCEPT, fleet, POW_ConvoyConfig.AMBUSH_INTERCEPT_DAYS,
                "hunting the Torgue convoy");
        Misc.giveStandardReturnToSourceAssignments(pirates, false);

        sendUpdateIfPlayerHasIntel(UPDATE_AMBUSH, true);
    }

    protected float getEscortPoints() {
        float points = 0f;
        if (fleet == null)
            return escortPoints;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (!member.isMothballed()) {
                points += member.getFleetPointCost();
            }
        }
        return points;
    }

    // --- battles
    // ---------------------------------------------------------------------------------------------

    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (fleet != this.fleet || outcome != null)
            return;
        if (stage == Stage.CAPTURED)
            return;
        if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE)
            return; // handled in reportBattleOccurred
        finish(Outcome.LOST_OTHER);
    }

    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (fleet != this.fleet || outcome != null)
            return;
        if (stage == Stage.CAPTURED || !battle.isInvolved(fleet))
            return;

        boolean playerWon = battle.isPlayerInvolved() && !battle.onPlayerSide(fleet);
        boolean gone = !fleet.isAlive() || fleet.isEmpty();
        boolean escortsGone = getEscortPoints() <= 0f;

        if (stage == Stage.DEFENSELESS) {
            // the drifting haulers were attacked instead of boarded
            if (gone) {
                finish(playerWon ? Outcome.DESTROYED_BY_PLAYER : Outcome.LOST_OTHER);
            }
            return;
        }

        if (playerWon) {
            // escorts left: the defeat trigger re-arms itself; nothing gone: nothing to do
            if (gone) {
                payDestroyReward(null);
                finish(Outcome.DESTROYED_BY_PLAYER);
            }
            return;
        }

        if (gone || escortsGone) {
            // NPC battle: whoever won takes or scraps the haulers
            removeFleet();
            boolean pirates = primaryWinner != null && Factions.PIRATES.equals(primaryWinner.getFaction().getId());
            finish(pirates ? Outcome.LOST_TO_PIRATES : Outcome.LOST_OTHER);
        }
    }

    // called from rules.csv via the POWConvoyDefeated defeat trigger, right after
    // the player wins a fight
    public void reportPlayerVictory(TextPanelAPI text) {
        if (outcome != null || fleet == null)
            return;

        if (getEscortPoints() > 0f) {
            // the trigger is removed once fired, so re-arm it for the next fight
            Misc.addDefeatTrigger(fleet, DEFEAT_TRIGGER);
            text.addPara(
                    "The surviving escorts close ranks around the haulers. The convoy is battered, but still under guard.");
            return;
        }

        payDestroyReward(text);

        List<FleetMemberAPI> haulers = getCargoInFleet();
        if (haulers.isEmpty()) {
            text.addPara("Nothing is left of the convoy. Whatever Torgue was shipping went up with it.");
            removeFleet();
            finish(Outcome.DESTROYED_BY_PLAYER);
            return;
        }

        setStage(Stage.DEFENSELESS);
        fleet.clearAssignments();
        SectorEntityToken hold = fleet.getContainingLocation().createToken(fleet.getLocation());
        fleet.addAssignment(FleetAssignment.HOLD, hold, POW_ConvoyConfig.DEFENSELESS_DAYS, "drifting without escorts");
        fleet.getMemoryWithoutUpdate().set(MEM_DEFENSELESS, true);
        Misc.makeImportant(fleet, IMPORTANT_REASON);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, IMPORTANT_REASON,
                true, POW_ConvoyConfig.DEFENSELESS_DAYS);

        text.addPara("With the escorts gone, %s mothballed Torgue refits drift on skeleton crews. "
                + "Hail the haulers to board them or scuttle them.", Misc.getHighlightColor(), "" + haulers.size());
        sendUpdateIfPlayerHasIntel(UPDATE_DEFENSELESS, false);
    }

    protected void payDestroyReward(TextPanelAPI text) {
        if (poster == null || destroyRewardPaid)
            return;
        destroyRewardPaid = true;
        Global.getSector().getPlayerFleet().getCargo().getCredits().add(destroyReward);
        if (text != null) {
            AddRemoveCommodity.addCreditsGainText(destroyReward, text);
        }
    }

    protected List<FleetMemberAPI> getCargoInFleet() {
        List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
        if (fleet == null)
            return result;
        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        for (FleetMemberAPI member : cargo) {
            if (members.contains(member)) {
                result.add(member);
            }
        }
        return result;
    }

    // --- capture, scuttle, deliver (rules.csv via POW_Convoy)
    // ------------------------------------------------

    public boolean isDefenseless() {
        return stage == Stage.DEFENSELESS && outcome == null;
    }

    public void capture(TextPanelAPI text) {
        if (!isDefenseless())
            return;
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI member : getCargoInFleet()) {
            fleet.getFleetData().removeFleetMember(member);
            // same member object, so the "modIds=" id and with it the full refit stats come
            // along
            playerFleet.getFleetData().addFleetMember(member);
            member.getRepairTracker().setMothballed(true);
            AddRemoveCommodity.addFleetMemberGainText(member, text);
        }
        playerFleet.getFleetData().sort();
        removeFleet();

        if (poster == null) {
            text.addPara("The ships are yours now. Their buyer will not forget it.");
            finish(Outcome.CAPTURED_KEPT);
            return;
        }

        setStage(Stage.CAPTURED);
        fleet = null;
        text.addPara("Deliver them to %s within %s days to collect the bounty.", Misc.getHighlightColor(),
                poster.getName(), "" + (int) POW_ConvoyConfig.DELIVERY_DEADLINE_DAYS);
        sendUpdateIfPlayerHasIntel(UPDATE_CAPTURED, false);
    }

    public void scuttle(TextPanelAPI text) {
        if (!isDefenseless())
            return;
        for (FleetMemberAPI member : getCargoInFleet()) {
            fleet.getFleetData().removeFleetMember(member);
        }
        removeFleet();
        text.addPara("Charges go off one after another and the haulers come apart. "
                + Misc.ucFirst(buyer.getFaction().getDisplayNameWithArticle())
                + " paid good money for those ships, and will want someone to answer for it.");
        new POW_HunterIntel(buyer.getFaction(), "for destroying the Torgue ships "
                + buyer.getFaction().getDisplayNameWithArticle() + " paid for", new ArrayList<FleetMemberAPI>())
                .start();
        finish(Outcome.SCUTTLED);
    }

    public boolean canDeliverAt(MarketAPI market) {
        return stage == Stage.CAPTURED && outcome == null && poster != null && market == poster
                && !getCargoInPlayerFleet().isEmpty();
    }

    public void deliver(TextPanelAPI text) {
        if (stage != Stage.CAPTURED || outcome != null)
            return;
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        int payment = 0;
        for (FleetMemberAPI member : getCargoInPlayerFleet()) {
            payment += getDeliveryValue(member);
            playerFleet.getFleetData().removeFleetMember(member);
            AddRemoveCommodity.addFleetMemberLossText(member, text);
            delivered.add(member);
        }
        if (payment <= 0)
            return;

        playerFleet.getCargo().getCredits().add(payment);
        deliveredCredits += payment;
        AddRemoveCommodity.addCreditsGainText(payment, text);

        CustomRepImpact impact = new CustomRepImpact();
        impact.delta = POW_ConvoyConfig.DELIVERY_REP_GAIN;
        Global.getSector().adjustPlayerReputation(new RepActionEnvelope(RepActions.CUSTOM, impact, null, text, true),
                poster.getFactionId());

        if (delivered.size() >= cargo.size()) {
            finish(Outcome.CAPTURED_DELIVERED);
        } else {
            sendUpdateIfPlayerHasIntel(UPDATE_DELIVERED_SOME, false);
        }
    }

    public int getDeliveryValue(FleetMemberAPI member) {
        return Math.round(member.getBaseValue() * POW_ConvoyConfig.DELIVERY_VALUE_MULT);
    }

    protected List<FleetMemberAPI> getCargoInPlayerFleet() {
        List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
        List<FleetMemberAPI> members = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
        for (FleetMemberAPI member : cargo) {
            if (members.contains(member) && !delivered.contains(member)) {
                result.add(member);
            }
        }
        return result;
    }

    public boolean isActive() {
        return outcome == null && !isEnding() && !isEnded();
    }

    // --- intel UI
    // --------------------------------------------------------------------------------------------

    @Override
    protected String getName() {
        String name = "Torgue Delivery: " + buyer.getName();
        if (outcome != null) {
            switch (outcome) {
                case DELIVERED:
                    return name + " - Delivered";
                case CAPTURED_DELIVERED:
                    return name + " - Bounty Completed";
                case CAPTURE_DEADLINE_MISSED:
                    return name + " - Bounty Failed";
                default:
                    return name + " - Ended";
            }
        }
        if (stage == Stage.CAPTURED) {
            return "Torgue Bounty: Deliver to " + poster.getName();
        }
        return name;
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return buyer.getFaction();
    }

    @Override
    public String getIcon() {
        return buyer.getFaction().getCrest();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(INTEL_TAG);
        tags.add(Tags.INTEL_FLEET_DEPARTURES);
        if (poster != null) {
            tags.add(Tags.INTEL_BOUNTY);
            tags.add(poster.getFactionId());
        }
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (stage == Stage.CAPTURED) {
            return poster.getPrimaryEntity();
        }
        if (fleet != null && fleet.isAlive() && outcome == null) {
            if (fleet.isVisibleToPlayerFleet() || fleet.isInHyperspace() || fleet.getStarSystem() == null) {
                return fleet;
            }
            return fleet.getStarSystem().getCenter();
        }
        return buyer.getPrimaryEntity();
    }

    @Override
    public List<ArrowData> getArrowData(SectorMapAPI map) {
        if (outcome != null || stage == Stage.CAPTURED)
            return null;
        List<ArrowData> result = new ArrayList<ArrowData>();
        ArrowData arrow = new ArrowData(origin.getPrimaryEntity(), buyer.getPrimaryEntity());
        arrow.color = buyer.getFaction().getBaseUIColor();
        result.add(arrow);
        return result;
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate, Color tc, float initPad) {
        Color h = Misc.getHighlightColor();
        Object param = getListInfoParam();

        if (isUpdate && param instanceof String) {
            String update = (String) param;
            if (UPDATE_DEPARTED.equals(update)) {
                info.addPara("Departed Pantheon with %s Torgue refits", initPad, tc, h, "" + cargo.size());
            } else if (UPDATE_AMBUSH.equals(update)) {
                info.addPara("Pirates are shadowing the convoy", tc, initPad);
            } else if (UPDATE_DEFENSELESS.equals(update)) {
                info.addPara("Escorts destroyed, haulers drifting", tc, initPad);
            } else if (UPDATE_CAPTURED.equals(update)) {
                info.addPara("Deliver the ships to %s", initPad, tc, h, poster.getName());
            } else if (UPDATE_DELIVERED_SOME.equals(update)) {
                info.addPara("%s received so far", initPad, tc, h, Misc.getDGSCredits(deliveredCredits));
            }
            return;
        }

        if (outcome != null) {
            info.addPara(getOutcomeText(), tc, initPad);
            if (outcome == Outcome.CAPTURED_DELIVERED) {
                info.addPara("%s received", 0f, tc, h, Misc.getDGSCredits(deliveredCredits));
            }
            return;
        }

        if (stage == Stage.CAPTURED) {
            int days = Math.max(1, Math.round(POW_ConvoyConfig.DELIVERY_DEADLINE_DAYS - stageDays));
            info.addPara("%s ships to deliver, %s days remaining", initPad, tc, h,
                    "" + getCargoInPlayerFleet().size(), "" + days);
            return;
        }

        info.addPara("Buyer: " + buyer.getFaction().getDisplayName(), initPad, tc, buyer.getFaction().getBaseUIColor(),
                buyer.getFaction().getDisplayName());
        info.addPara("%s Torgue refits on board", 0f, tc, h, "" + cargo.size());
        if (poster != null) {
            info.addPara("Bounty by " + poster.getFaction().getDisplayName(), 0f, tc,
                    poster.getFaction().getBaseUIColor(), poster.getFaction().getDisplayName());
        }
    }

    protected String getOutcomeText() {
        switch (outcome) {
            case DELIVERED:
                return "Delivered to " + buyer.getName();
            case TIMED_OUT:
                return "Delayed indefinitely";
            case LOST_TO_PIRATES:
                return "Lost to pirates";
            case LOST_OTHER:
                return "Lost in transit";
            case DESTROYED_BY_PLAYER:
                return "Destroyed by your fleet";
            case RECOVERED_BY_BUYER:
                return "Haulers recovered by " + buyer.getFaction().getDisplayNameWithArticle();
            case CAPTURED_KEPT:
                return "Ships captured by your fleet";
            case CAPTURED_DELIVERED:
                return "Captured ships delivered to " + poster.getName();
            case CAPTURE_DEADLINE_MISSED:
                return "Ships not delivered in time";
            case SCUTTLED:
                return "Ships scuttled by your fleet";
            default:
                return "";
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        float opad = 10f;
        FactionAPI buyerFaction = buyer.getFaction();

        info.addImage(buyerFaction.getLogo(), width, 128, opad);
        info.addPara(Misc.ucFirst(buyerFaction.getDisplayNameWithArticle()) + " bought %s Torgue refits from "
                + "Pantheon Orbital Works and sent an escort to bring them to " + buyer.getName() + ".", opad, h,
                "" + cargo.size());

        List<FleetMemberAPI> shown = stage == Stage.CAPTURED ? getCargoInPlayerFleet() : cargo;
        if (!shown.isEmpty()) {
            info.addShipList(Math.min(shown.size(), 7), 1, 48f, buyerFaction.getBaseUIColor(), shown, opad);
        }

        if (poster != null) {
            FactionAPI posterFaction = poster.getFaction();
            info.addSectionHeading("Bounty", posterFaction.getBaseUIColor(), posterFaction.getDarkUIColor(),
                    com.fs.starfarer.api.ui.Alignment.MID, opad);
            info.addPara(Misc.ucFirst(posterFaction.getDisplayNameWithArticle()) + " wants this delivery stopped.",
                    opad, posterFaction.getBaseUIColor(), posterFaction.getDisplayNameWithArticleWithoutArticle());
            info.addPara("Destroying the escort pays %s.", opad, h, Misc.getDGSCredits(destroyReward));
            int total = 0;
            for (FleetMemberAPI member : cargo) {
                total += getDeliveryValue(member);
            }
            info.addPara("Capturing the refits and delivering them to %s pays %s per ship, "
                    + "up to %s for the whole shipment. Leave the haulers intact: wrecked ships can't be delivered.",
                    opad, h, poster.getName(),
                    Math.round(POW_ConvoyConfig.DELIVERY_VALUE_MULT * 100f) + "% of its value",
                    Misc.getDGSCredits(total));
            info.addPara("Captured ships must be delivered within %s days, or "
                    + posterFaction.getDisplayNameWithArticle() + " will send hunters to take them back.", opad, h,
                    "" + (int) POW_ConvoyConfig.DELIVERY_DEADLINE_DAYS);
        }

        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public String getSortString() {
        return "Torgue Delivery";
    }

    @Override
    protected float getBaseDaysAfterEnd() {
        return POW_ConvoyConfig.DAYS_TO_KEEP_INTEL_AFTER_END;
    }

    public CampaignFleetAPI getFleet() {
        return fleet;
    }
}
