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
        System.out.println("PASS: 12 core rule assertions");
    }
}
