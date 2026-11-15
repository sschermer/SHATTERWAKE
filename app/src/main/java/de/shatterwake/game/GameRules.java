package de.shatterwake.game;

/** Deterministic rules, deliberately independent of Android for local testing. */
public final class GameRules {
    private GameRules() { }
    public static final float AIM_RADIANS = (float) Math.toRadians(15.0);
    public static final int MAX_CONTINUES = 2;
    public static final int[] FIRE_COST = {160, 215, 280, 365, 470};
    public static int continueCost(int stars) { return (Math.max(0, stars) + 1) / 2; }
    public static int nextFireCost(int level) { return FIRE_COST[Math.max(0, Math.min(level, FIRE_COST.length - 1))]; }
    public static float fireInterval(int level, boolean rapid) {
        float base = Math.max(.105f, .265f * (1f - .055f * Math.max(0, Math.min(5, level))));
        return rapid ? base * .59f : base;
    }
    public static boolean canAim(float dx, float forwardDistance) {
        return forwardDistance > 0 && Math.abs(Math.atan2(dx, forwardDistance)) <= AIM_RADIANS;
    }
    public static int[] routesAfter(int level) {
        if (level == 1) return new int[]{1, 2};
        if (level == 2) return new int[]{3, 4};
        return new int[0];
    }
    public static int worldTier(int world) { return world == 0 ? 1 : (world <= 2 ? 2 : 3); }
    public static int bossReward(int world) { return 300 + worldTier(world) * 135; }
    /**
     * Encounter budgets are time- and sector-based, never scaled to installed equipment.
     * The first sector includes a genuine learning ramp rather than an immediate barrage.
     */
    public static float enemySpawnInterval(int tier, float seconds) {
        if (tier <= 1) return seconds < 20f ? 4.3f : seconds < 50f ? 3.7f
                : seconds < 90f ? 3.25f : 2.95f;
        if (tier == 2) return seconds < 25f ? 3.45f : 3.05f;
        return seconds < 25f ? 3.2f : 2.85f;
    }
    public static int enemyCap(int tier, float seconds) {
        if (tier <= 1) return seconds < 20f ? 3 : seconds < 50f ? 5
                : seconds < 90f ? 7 : 8;
        return tier == 2 ? 8 : 10;
    }
    public static boolean enemyFireAllowed(int tier, float seconds) {
        return seconds >= (tier <= 1 ? 18f : 7f);
    }
    public static int hostileShotCap(int tier, float seconds, boolean boss) {
        if (boss) return tier <= 1 ? 8 : tier == 2 ? 10 : 13;
        if (tier <= 1) return seconds < 18f ? 0 : seconds < 55f ? 3
                : seconds < 90f ? 5 : 7;
        return tier == 2 ? 8 : 11;
    }
    public static float enemyShotInterval(int tier, float unitRandom) {
        return 3.8f + Math.max(0f, Math.min(1f, unitRandom)) * 2.5f
                - Math.max(0, Math.min(2, tier-1)) * .27f;
    }
    public static int bossVolleyCount(int phase, int tier) {
        return Math.min(5, 1 + Math.max(1, Math.min(3, phase))
                + (tier >= 3 ? 1 : 0));
    }
    public static float bossFireInterval(int phase, int tier) {
        return 1.85f - (Math.max(1, Math.min(3, phase))-1)*.16f
                - (Math.max(1, Math.min(3, tier))-1)*.10f;
    }
    public static float bossArrivalSeconds(int tier) {
        return tier <= 1 ? 112f : tier == 2 ? 124f : 132f;
    }

    public static boolean isFinalWorld(int world) { return world >= 3; }
}
