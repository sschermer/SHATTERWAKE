package de.shatterwake.game;
public final class RuleChecks {
    public static void main(String[] args) {
        assert GameRules.continueCost(0) == 0;
        assert GameRules.continueCost(1) == 1;
        assert GameRules.continueCost(101) == 51;
        assert GameRules.MAX_CONTINUES == 2;
        assert GameRules.routesAfter(1)[0] != GameRules.routesAfter(1)[1];
        assert GameRules.routesAfter(2)[0] == 3;
        assert GameRules.routesAfter(3).length == 0;
        assert GameRules.worldTier(2) == 2;
        assert GameRules.isFinalWorld(4);
        assert GameRules.canAim(0, 100);
        assert !GameRules.canAim(100, 100);
        assert GameRules.fireInterval(5, false) < GameRules.fireInterval(0, false);
        assert GameRules.enemyCap(1, 0) == 3;
        assert GameRules.enemyCap(1, 30) == 5;
        assert GameRules.enemyCap(1, 100) <= 8;
        assert GameRules.enemySpawnInterval(1, 0) > GameRules.enemySpawnInterval(1, 90);
        assert !GameRules.enemyFireAllowed(1, 17.99f);
        assert GameRules.enemyFireAllowed(1, 18f);
        assert GameRules.hostileShotCap(1, 0, false) == 0;
        assert GameRules.hostileShotCap(1, 40, false) == 3;
        assert GameRules.hostileShotCap(1, 40, true) == 8;
        assert GameRules.enemyShotInterval(1, 0.5f) > 4f;
        assert GameRules.bossVolleyCount(1,1) == 2;
        assert GameRules.bossVolleyCount(3,1) == 4;
        assert GameRules.bossFireInterval(3, 1) > 1.4f;
        assert GameRules.bossArrivalSeconds(1) < 130f;
        System.out.println("PASS: 26 game-rule assertions including early-game difficulty ramp");
    }
}
