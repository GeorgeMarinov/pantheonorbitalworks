package pantheonorbitalworks.convoy;

// All convoy tuning in one place.
public class POW_ConvoyConfig {
    public static final String PANTHEON_MARKET_ID = "pow_patheon_planet";
    public static final String MANUFACTURER = "torgue";

    // spawning
    public static final float MIN_DAYS_BETWEEN_CONVOYS = 30f;
    public static final float MAX_DAYS_BETWEEN_CONVOYS = 60f;
    public static final int MAX_ACTIVE_CONVOYS = 2;
    public static final int MIN_BUYER_MARKET_SIZE = 4;

    // cargo
    public static final int MIN_CARGO_SHIPS = 2;
    public static final int MAX_CARGO_SHIPS = 4;
    public static final float EXPERT_PACKAGE_CHANCE = 0.4f; // otherwise advanced
    // superb, legendary, masterwork, miracle
    public static final String[] CARGO_QUALITIES = { "superb", "legendary", "masterwork", "miracle" };
    public static final float[] CARGO_QUALITY_WEIGHTS = { 50f, 30f, 15f, 5f };

    // escorts: combat points per 10,000 credits of cargo value, clamped
    public static final float ESCORT_POINTS_PER_10K_CARGO = 1f;
    public static final float MIN_ESCORT_POINTS = 60f;
    public static final float MAX_ESCORT_POINTS = 220f;

    // travel
    public static final float LOADING_DAYS = 3f;
    public static final float UNLOADING_DAYS = 2f;
    public static final float MAX_TRAVEL_DAYS = 120f;
    public static final float DAYS_TO_KEEP_INTEL_AFTER_END = 7f;

    // pirate ambush, rolled once per trip when the convoy first reaches hyperspace
    public static final float AMBUSH_CHANCE = 0.4f;
    public static final float AMBUSH_MIN_STRENGTH_MULT = 0.8f;
    public static final float AMBUSH_MAX_STRENGTH_MULT = 1.2f;
    public static final float AMBUSH_SPAWN_DISTANCE = 1500f;
    public static final float AMBUSH_INTERCEPT_DAYS = 15f;

    // bounty
    public static final float BOUNTY_CHANCE = 0.6f;
    public static final int MIN_POSTER_MARKET_SIZE = 4;
    public static final float DESTROY_REWARD_PER_ESCORT_POINT = 300f;
    public static final float DELIVERY_VALUE_MULT = 2.5f;
    public static final float DELIVERY_DEADLINE_DAYS = 60f;
    public static final float DELIVERY_REP_GAIN = 0.05f;

    // what happens after the escorts are beaten
    public static final float DEFENSELESS_DAYS = 10f; // haulers drift this long before the buyer's tugs take them

    // hunters sent after the player
    public static final float HUNTER_MIN_STRENGTH_MULT = 0.9f;
    public static final float HUNTER_MAX_STRENGTH_MULT = 1.3f;
    public static final float HUNTER_MIN_POINTS = 60f;
    public static final float HUNTER_MAX_POINTS = 300f;
    public static final float HUNTER_SPAWN_DISTANCE = 1500f;
    public static final float HUNTER_RETARGET_DAYS = 3f;
    public static final float HUNTER_COOLDOWN_AFTER_WIN_DAYS = 10f;
}
