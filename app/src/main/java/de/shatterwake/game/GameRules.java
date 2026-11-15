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
    public static boolean isFinalWorld(int world) { return world >= 3; }
}
