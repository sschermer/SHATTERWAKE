package de.shatterwake.game;

public final class TutorialChecks {
    public static void main(String[] args){
        final int[] actions={
            TutorialRules.ACTION_MOVE,
            TutorialRules.ACTION_TARGET_KILLED,
            TutorialRules.ACTION_STAR_COLLECTED,
            TutorialRules.ACTION_SHIELD_USED,
            TutorialRules.ACTION_RAPID_USED,
            TutorialRules.ACTION_TRIPLE_USED,
            TutorialRules.ACTION_PLASMA_USED,
            TutorialRules.ACTION_PLASMA_TARGET_KILLED,
            TutorialRules.ACTION_SECONDARY_TARGET_KILLED
        };
        int step=TutorialRules.MOVE;
        assert actions.length==TutorialRules.TOTAL_STEPS;
        for(int i=0;i<actions.length;i++){
            assert step==i;
            assert TutorialRules.nextStep(step,(actions[i]+1)%actions.length)==step :
                "An unrelated action must never skip a lesson";
            step=TutorialRules.nextStep(step,actions[i]);
        }
        assert step==TutorialRules.COMPLETE;
        assert TutorialRules.nextStep(step,TutorialRules.ACTION_MOVE)==TutorialRules.COMPLETE;
        assert TutorialRules.nextStep(TutorialRules.FIRE_SECONDARY,
                TutorialRules.ACTION_PLASMA_USED)==TutorialRules.FIRE_SECONDARY;

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
        assert TutorialRules.abilityExplanation(3).contains("Plasma");
        assert TutorialRules.POWERUP_TRIAL_SECONDS==15f;
        for(int i=0;i<4;i++){
            int powerupStep=TutorialRules.ACTIVATE_SHIELD+i;
            assert TutorialRules.isPowerupStep(powerupStep);
            assert TutorialRules.abilityForStep(powerupStep)==i;
            assert TutorialRules.actionForStep(powerupStep)==TutorialRules.ACTION_SHIELD_USED+i;
            assert TutorialRules.nextStep(powerupStep,TutorialRules.ACTION_MOVE)==powerupStep;
        }
        assert !TutorialRules.isPowerupStep(TutorialRules.PLASMA_TARGET);
        assert TutorialRules.abilityForStep(TutorialRules.FIRE_SECONDARY)==-1;
        assert TutorialRules.actionForStep(TutorialRules.MOVE)==-1;
        float trial=TutorialRules.POWERUP_TRIAL_SECONDS;
        trial=TutorialRules.remainingTrialTime(trial,14f);
        assert trial==1f : "The lesson must not advance before 15 active seconds";
        assert TutorialRules.remainingTrialTime(trial,-5f)==1f : "A pause must not count";
        trial=TutorialRules.remainingTrialTime(trial,1f);
        assert trial==0f : "The lesson completes only at 15 seconds";
        assert TutorialRules.remainingTrialTime(0f,1f)==0f;
        System.out.println("PASS: nine deliberate lessons and four full 15-second power-up trials");
    }
}
