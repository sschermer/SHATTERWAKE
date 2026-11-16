package de.shatterwake.game;
/** Deterministic economy, difficulty and boss-sequence regression checks. */
public final class RuleChecks {
    public static void main(String[] args) {
        assert GameRules.continueCost(0)==0;
        assert GameRules.continueCost(1)==1;
        assert GameRules.continueCost(101)==51;
        assert GameRules.repairsNeeded(4,6)==2;
        assert GameRules.fullRepairCost(4,6,160)==320;
        assert GameRules.hullPercent(4,6)==67;
        assert GameRules.repairsNeeded(5,6)==1;
        assert GameRules.fullRepairCost(5,6,160)==160;
        assert GameRules.repairsNeeded(6,6)==0;
        assert GameRules.hullPercent(6,6)==100;
        assert GameRules.fullRepairCost(6,6,160)==0;
        assert GameRules.repairsNeeded(9,6)==0;
        assert GameRules.repairsNeeded(-1,6)==6;
        assert GameRules.hullPercent(0,6)==0;
        assert GameRules.hullDamageTier(6,6)==0;
        assert GameRules.hullDamageTier(5,6)==1;
        assert GameRules.hullDamageTier(4,6)==2;
        assert GameRules.hullDamageTier(2,6)==3;
        assert GameRules.hullDamageTier(0,6)==3;
        assert GameRules.visibleWeaponModule(0,0,-1)==-1;
        assert GameRules.visibleWeaponModule(1,0,-1)==0;
        assert GameRules.visibleWeaponModule(0,1,-1)==1;
        assert GameRules.visibleWeaponModule(5,5,0)==1;
        assert GameRules.visibleWeaponModule(0,0,1)==1;
        assert GameRules.visualUpgradeLevel(0,3,true)==1;
        assert GameRules.visualUpgradeLevel(2,3,true)==3;
        assert GameRules.visualUpgradeLevel(3,3,true)==3;
        assert GameRules.visualUpgradeLevel(4,5,true)==5;
        assert GameRules.visualUpgradeLevel(0,5,false)==0;
        assert GameRules.werftBalanceLabel(225).equals("★ 225");
        assert GameRules.werftBalanceLabel(65).equals("★ 65");
        assert GameRules.werftBalanceLabel(0).equals("★ 0");
        assert GameRules.FIGHTER_ART_SCALE>1f
                &&GameRules.FIGHTER_ART_SCALE<1.2f;
        assert GameRules.FIGHTER_SAFE_MARGIN>=GameRules.FIGHTER_ART_SCALE*40f;
        assert GameRules.fighterBankDegrees(100f,100f)==0f;
        assert GameRules.fighterBankDegrees(300f,0f)==7f;
        assert GameRules.fighterBankDegrees(0f,300f)==-7f;
        assert Math.abs(GameRules.fighterAnchorX(20f,-30f,0f)-22.4f)<.001f;
        assert Math.abs(GameRules.fighterAnchorY(20f,-30f,0f)+33.6f)<.001f;
        double r7=Math.toRadians(7);
        assert Math.abs(GameRules.fighterAnchorX(20f,-30f,7f)
                -GameRules.FIGHTER_ART_SCALE*(20*Math.cos(r7)+30*Math.sin(r7)))<.0001f;
        assert Math.abs(GameRules.fighterAnchorY(20f,-30f,7f)
                -GameRules.FIGHTER_ART_SCALE*(20*Math.sin(r7)-30*Math.cos(r7)))<.0001f;
        assert GameRules.MAX_CONTINUES==2;
        assert GameRules.routesAfter(1)[0]!=GameRules.routesAfter(1)[1];
        assert GameRules.routesAfter(2)[0]==3;
        assert GameRules.routesAfter(3).length==0;
        assert GameRules.worldTier(2)==2;
        assert GameRules.isFinalWorld(4);
        assert GameRules.canAim(0,100);
        assert !GameRules.canAim(100,100);
        assert GameRules.fireInterval(5,false)<GameRules.fireInterval(0,false);
        assert GameRules.aimCorrectionFraction(0)==0f;
        assert Math.abs(GameRules.aimCorrectionFraction(5)-.15f)<.0001f;
        assert GameRules.aimCorrectionFraction(8)<=.15f;
        assert GameRules.nextTargetingCost(0)<GameRules.nextTargetingCost(4);

        assert GameRules.enemyCap(1,0)==4;
        assert GameRules.enemyCap(1,35)==6;
        assert GameRules.enemyCap(1,100)==10;
        assert GameRules.enemySpawnInterval(1,0)>GameRules.enemySpawnInterval(1,90);
        assert !GameRules.enemyFireAllowed(1,8.99f);
        assert GameRules.enemyFireAllowed(1,9f);
        assert GameRules.hostileShotCap(1,0,false)==0;
        assert GameRules.hostileShotCap(1,30,false)==5;
        assert GameRules.hostileShotCap(1,60,false)==7;
        assert GameRules.hostileShotCap(1,100,false)==9;
        assert GameRules.hostileShotCap(1,50,true)==11;
        assert GameRules.enemyShotInterval(1,.5f)>3f;
        assert GameRules.bossVolleyCount(1,1)==3;
        assert GameRules.bossVolleyCount(3,1)==5;
        assert GameRules.bossFireInterval(3,1)>1.2f;
        assert GameRules.bossArrivalSeconds(1)<130f;

        assert GameRules.BOSS_WARNING_SECONDS==3f;
        assert GameRules.BOSS_ENTRY_SECONDS>=3f;
        assert GameRules.BOSS_EXPLOSION_SECONDS>2f;
        assert GameRules.DOCKING_SECONDS>3f;
        assert !GameRules.canEnterShipyard(0f,0,false);
        assert !GameRules.canEnterShipyard(100f,1,false);
        assert !GameRules.canEnterShipyard(100f,0,true);
        assert GameRules.canEnterShipyard(GameRules.BOSS_EXPLOSION_SECONDS,0,false);
        assert !GameRules.werftPurchaseEnabled(.75f,true,1000,100);
        assert !GameRules.werftPurchaseEnabled(1f,false,1000,100);
        assert !GameRules.werftPurchaseEnabled(1f,true,90,100);
        assert GameRules.werftPurchaseEnabled(1f,true,100,100);
        for(int part=0;part<8;part++){
            assert !GameRules.werftPartLabel(part).isEmpty();
            assert GameRules.werftFocusX(part)>-100f;
            assert GameRules.werftFocusY(part)<100f;
        }
        assert GameRules.nextBossPhase(GameRules.BOSS_SALVAGE,true,true)
                ==GameRules.BOSS_SALVAGE;
        int s=GameRules.BOSS_NORMAL;
        s=GameRules.nextBossPhase(s,true,false);
        assert s==GameRules.BOSS_CLEARING;
        assert GameRules.nextBossPhase(s,false,false)==GameRules.BOSS_CLEARING;
        assert GameRules.nextBossPhase(s,false,true)==GameRules.BOSS_WARNING;
        s=GameRules.BOSS_WARNING;
        assert GameRules.nextBossPhase(s,false,true)==GameRules.BOSS_WARNING;
        assert GameRules.nextBossPhase(s,true,true)==GameRules.BOSS_ENTERING;
        s=GameRules.BOSS_ENTERING;
        assert GameRules.nextBossPhase(s,false,true)==GameRules.BOSS_ENTERING;
        assert GameRules.nextBossPhase(s,true,true)==GameRules.BOSS_COMBAT;

        assert GameRules.bossReward(0)<GameRules.bossReward(4);
        assert GameRules.bossReward(0)<300;
        assert GameRules.scaleStarReward(100)==55;
        assert GameRules.scaleStarReward(0)==1;
        assert GameRules.nextFireCost(4)>GameRules.nextFireCost(0);
        assert GameRules.nextDamageCost(4)>GameRules.nextDamageCost(0);
        assert GameRules.nextMagnetCost(2)>GameRules.nextMagnetCost(0);
        assert GameRules.nextDroneCost(2)>GameRules.nextDroneCost(0);
        assert GameRules.nextShieldCost(1)>GameRules.nextShieldCost(0);
        System.out.println("PASS: SHATTERWAKE v0.3 balancing, economy and boss-phase assertions");
    }
}
