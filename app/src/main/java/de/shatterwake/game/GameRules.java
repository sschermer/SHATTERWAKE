package de.shatterwake.game;

/** Deterministic rules, deliberately independent of Android for local testing. */
public final class GameRules {
    private GameRules() { }
    public static final float AIM_RADIANS = (float) Math.toRadians(15.0);
    public static final int MAX_CONTINUES = 2;
    public static final int[] FIRE_COST = {250, 350, 480, 640, 830};
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
    public static int bossReward(int world) { return 145 + worldTier(world) * 80; }
    /**
     * Encounter budgets are time- and sector-based, never scaled to installed equipment.
     * The first sector includes a genuine learning ramp rather than an immediate barrage.
     */
    public static float enemySpawnInterval(int tier, float seconds) {
        // Sector one: readable opening, then increasingly active waves.
        if(tier<=1) return seconds<18f?3.35f:seconds<50f?2.9f
                :seconds<90f?2.55f:2.35f;
        if(tier==2) return seconds<25f?2.7f:2.35f;
        return seconds<25f?2.55f:2.15f;
    }
    public static int enemyCap(int tier, float seconds) {
        if(tier<=1) return seconds<18f?4:seconds<50f?6
                :seconds<90f?8:10;
        return tier==2?10:12;
    }
    public static boolean enemyFireAllowed(int tier, float seconds) {
        return seconds >= (tier<=1?9f:5f);
    }
    public static int hostileShotCap(int tier, float seconds, boolean boss) {
        if(boss) return tier<=1?11:tier==2?14:17;
        if(tier<=1) return seconds<9f?0:seconds<40f?5
                :seconds<80f?7:9;
        return tier==2?11:13;
    }
    public static float enemyShotInterval(int tier, float unitRandom) {
        return 2.6f + Math.max(0f,Math.min(1f,unitRandom))*1.6f
                - Math.max(0,Math.min(2,tier-1))*.2f;
    }
    public static int bossVolleyCount(int phase, int tier) {
        return Math.min(7,2+Math.max(1,Math.min(3,phase))
                +(tier>=3?1:0));
    }
    public static float bossFireInterval(int phase, int tier) {
        return 1.65f-(Math.max(1,Math.min(3,phase))-1)*.16f
                -(Math.max(1,Math.min(3,tier))-1)*.07f;
    }
    public static float bossArrivalSeconds(int tier) {
        return tier<=1?112f:tier==2?124f:132f;
    }

    // The transition is deterministic: stop spawns, clear the field,
    // sound/display a full 3-second warning, then fly in from above.
    public static final int BOSS_NORMAL=0, BOSS_CLEARING=1,
            BOSS_WARNING=2, BOSS_ENTERING=3, BOSS_COMBAT=4;
    public static final float BOSS_WARNING_SECONDS=3f;
    public static final float BOSS_ENTRY_SECONDS=3.7f;
    public static int nextBossPhase(int phase,boolean timerExpired,boolean fieldEmpty) {
        if(phase==BOSS_NORMAL && timerExpired) return BOSS_CLEARING;
        if(phase==BOSS_CLEARING && fieldEmpty) return BOSS_WARNING;
        if(phase==BOSS_WARNING && timerExpired) return BOSS_ENTERING;
        if(phase==BOSS_ENTERING && timerExpired) return BOSS_COMBAT;
        return phase;
    }
    public static int scaleStarReward(int value) {
        return Math.max(1,Math.round(Math.max(0,value)*.55f));
    }
    public static int nextDamageCost(int level){return 300+level*170;}
    public static int nextMagnetCost(int level){return 260+level*160;}
    public static int nextDroneCost(int level){return 510+level*220;}
    public static int nextShieldCost(int level){return 485+level*230;}
    public static boolean isFinalWorld(int world) { return world >= 3; }
}
