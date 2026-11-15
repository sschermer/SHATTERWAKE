package de.shatterwake.game;

public final class TutorialChecks {
    public static void main(String[] args){
        int step=TutorialRules.MOVE;
        assert TutorialRules.nextStep(step,TutorialRules.ACTION_STAR_COLLECTED)==TutorialRules.MOVE;
        step=TutorialRules.nextStep(step,TutorialRules.ACTION_MOVE);
        assert step==TutorialRules.TARGET;
        step=TutorialRules.nextStep(step,TutorialRules.ACTION_TARGET_KILLED);
        assert step==TutorialRules.COLLECT_STAR;
        step=TutorialRules.nextStep(step,TutorialRules.ACTION_STAR_COLLECTED);
        assert step==TutorialRules.ACTIVATE_SHIELD;
        assert TutorialRules.nextStep(step,TutorialRules.ACTION_SECONDARY_FIRED)==TutorialRules.ACTIVATE_SHIELD;
        step=TutorialRules.nextStep(step,TutorialRules.ACTION_SHIELD_USED);
        assert step==TutorialRules.FIRE_SECONDARY;
        step=TutorialRules.nextStep(step,TutorialRules.ACTION_SECONDARY_FIRED);
        assert step==TutorialRules.COMPLETE;
        assert TutorialRules.nextStep(step,TutorialRules.ACTION_MOVE)==TutorialRules.COMPLETE;

        assert TutorialRules.canPromptAbility(0,0);
        int seen=TutorialRules.markAbilitySeen(0,0);
        assert !TutorialRules.canPromptAbility(seen,0);
        assert TutorialRules.canPromptAbility(seen,2);
        seen=TutorialRules.markAbilitySeen(seen,2);
        assert seen==5;
        assert !TutorialRules.canPromptAbility(seen,2);
        assert !TutorialRules.canPromptAbility(seen,-1);
        assert !TutorialRules.canPromptAbility(seen,4);
        assert TutorialRules.markAbilitySeen(seen,9)==seen;

        assert TutorialRules.allowAbilityTap(TutorialRules.GUIDE_NONE,1,0);
        assert TutorialRules.allowAbilityTap(TutorialRules.GUIDE_ABILITY,0,0);
        assert !TutorialRules.allowAbilityTap(TutorialRules.GUIDE_ABILITY,1,0);
        assert !TutorialRules.allowAbilityTap(TutorialRules.GUIDE_SECONDARY,0,0);
        assert TutorialRules.allowSecondaryTap(TutorialRules.GUIDE_SECONDARY,0,0);
        assert !TutorialRules.allowSecondaryTap(TutorialRules.GUIDE_SECONDARY,1,0);
        assert TutorialRules.abilityExplanation(0).contains("acht");
        System.out.println("PASS: onboarding path, first-discovery persistence and targeted controls");
    }
}
