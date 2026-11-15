package de.shatterwake.game;

/**
 * Offline onboarding state machine. Independent of the Android UI for regression tests.
 *
 * The onboarding runs exactly once per installed profile unless replayed from the menu.
 * The user can skip without losing access to later contextual first-discovery lessons.
 */
public final class TutorialRules {
    private TutorialRules() {}
    public static final int MOVE=0, TARGET=1, COLLECT_STAR=2,
            ACTIVATE_SHIELD=3, FIRE_SECONDARY=4, COMPLETE=5;
    public static final int ACTION_MOVE=0, ACTION_TARGET_KILLED=1,
            ACTION_STAR_COLLECTED=2, ACTION_SHIELD_USED=3,
            ACTION_SECONDARY_FIRED=4;
    public static final int GUIDE_NONE=-1, GUIDE_ABILITY=0, GUIDE_SECONDARY=1;
    public static final int ALL_ABILITIES=(1<<4)-1;
    public static int nextStep(int step,int action) {
        switch(step) {
            case MOVE: return action==ACTION_MOVE?TARGET:MOVE;
            case TARGET: return action==ACTION_TARGET_KILLED?COLLECT_STAR:TARGET;
            case COLLECT_STAR: return action==ACTION_STAR_COLLECTED?ACTIVATE_SHIELD:COLLECT_STAR;
            case ACTIVATE_SHIELD: return action==ACTION_SHIELD_USED?FIRE_SECONDARY:ACTIVATE_SHIELD;
            case FIRE_SECONDARY: return action==ACTION_SECONDARY_FIRED?COMPLETE:FIRE_SECONDARY;
            default: return COMPLETE;
        }
    }
    public static boolean canPromptAbility(int seenMask,int which) {
        return which>=0 && which<4 && (seenMask&(1<<which))==0;
    }
    public static int markAbilitySeen(int seenMask,int which) {
        return which<0 || which>3?seenMask:seenMask|(1<<which);
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
