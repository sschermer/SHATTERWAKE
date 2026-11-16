package de.shatterwake.game;

/**
 * Optional menu training: one successful task reaches a WEITER checkpoint.
 * The single Plasma lesson includes both activation and visible shots.
 */
public final class TutorialRules {
    private TutorialRules() {}
    public static final int MOVE=0, TARGET=1, COLLECT_STAR=2,
            ACTIVATE_SHIELD=3, ACTIVATE_RAPID=4, ACTIVATE_TRIPLE=5,
            ACTIVATE_PLASMA=6, FIRE_SECONDARY=7, COMPLETE=8;
    public static final int TOTAL_STEPS=8;
    public static final int ACTION_MOVE=0, ACTION_TARGET_KILLED=1,
            ACTION_STAR_COLLECTED=2, ACTION_SHIELD_USED=3,
            ACTION_RAPID_USED=4, ACTION_TRIPLE_USED=5, ACTION_PLASMA_USED=6,
            ACTION_SECONDARY_TARGET_KILLED=7;
    public static final int GUIDE_NONE=-1, GUIDE_ABILITY=0, GUIDE_SECONDARY=1;
    public static final int ALL_ABILITIES=(1<<4)-1;
    public static final float POWERUP_TRIAL_SECONDS=5f;
    public static boolean isPowerupStep(int step) {
        return step>=ACTIVATE_SHIELD && step<=ACTIVATE_PLASMA;
    }
    public static int abilityForStep(int step) {
        return isPowerupStep(step)?step-ACTIVATE_SHIELD:-1;
    }
    public static int actionForStep(int step) {
        return isPowerupStep(step)?ACTION_SHIELD_USED+step-ACTIVATE_SHIELD:-1;
    }
    public static float remainingTrialTime(float remaining,float elapsed) {
        return Math.max(0f,remaining-Math.max(0f,elapsed));
    }
    public static int nextStep(int step,int action) {
        switch(step) {
            case MOVE: return action==ACTION_MOVE?TARGET:MOVE;
            case TARGET: return action==ACTION_TARGET_KILLED?COLLECT_STAR:TARGET;
            case COLLECT_STAR: return action==ACTION_STAR_COLLECTED?ACTIVATE_SHIELD:COLLECT_STAR;
            case ACTIVATE_SHIELD: return action==ACTION_SHIELD_USED?ACTIVATE_RAPID:ACTIVATE_SHIELD;
            case ACTIVATE_RAPID: return action==ACTION_RAPID_USED?ACTIVATE_TRIPLE:ACTIVATE_RAPID;
            case ACTIVATE_TRIPLE: return action==ACTION_TRIPLE_USED?ACTIVATE_PLASMA:ACTIVATE_TRIPLE;
            case ACTIVATE_PLASMA: return action==ACTION_PLASMA_USED?FIRE_SECONDARY:ACTIVATE_PLASMA;
            case FIRE_SECONDARY: return action==ACTION_SECONDARY_TARGET_KILLED?COMPLETE:FIRE_SECONDARY;
            default: return COMPLETE;
        }
    }
    public static boolean canPromptAbility(int seenMask,int which) {
        return which>=0 && which<4 && (seenMask&(1<<which))==0;
    }
    public static int markAbilitySeen(int seenMask,int which) {
        return which<0 || which>3?seenMask:seenMask|1<<which;
    }
    public static boolean allowAbilityTap(int guideType,int requested,int expected) {
        return guideType==GUIDE_NONE || (guideType==GUIDE_ABILITY && requested==expected);
    }
    public static boolean allowSecondaryTap(int guideType,int requested,int expected) {
        return guideType==GUIDE_NONE || (guideType==GUIDE_SECONDARY && requested==expected);
    }
    public static String abilityExplanation(int id) {
        switch(id) {
            case 0: return "Schuetzt acht Sekunden gegen feindliche Treffer.";
            case 1: return "Erhoeht neun Sekunden lang deine Schussfrequenz.";
            case 2: return "Feuert neun Sekunden lang drei Schuesse zugleich.";
            case 3: return "Verstaerkt die Geschosse sieben Sekunden mit Plasma.";
            default: return "Unbekannte Faehigkeit";
        }
    }
}
