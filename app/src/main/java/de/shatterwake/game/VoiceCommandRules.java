package de.shatterwake.game;

import java.text.Normalizer;
import java.util.Locale;

/** Pure, deterministic German keyword and second-finger safety rules. */
final class VoiceCommandRules {
    private VoiceCommandRules() { }

    /** 0 shield, 1 rapid, 2 triple, 3 plasma. Returns -1 on ambiguity. */
    static int commandFromSpeech(String phrase) {
        if(phrase==null)return -1;
        String normalized=Normalizer.normalize(phrase.toLowerCase(Locale.GERMAN),
                Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
                .replaceAll("[^a-z0-9]+"," ").trim();
        if(normalized.isEmpty())return -1;
        String[] words=normalized.split(" ");
        if(words.length>4)return -1;
        int identified=-1;
        for(String word:words){
            int candidate=word.equals("schild")||word.equals("schutzschild")?0:
                    word.equals("turbo")||word.equals("rapid")||
                            word.equals("schnellfeuer")?1:
                    word.equals("dreifach")||word.equals("dreifachschuss")||
                            word.equals("dreifachfeuer")?2:
                    word.equals("plasma")?3:-1;
            if(candidate>=0){
                // Never fire two charges for a single or ambiguous utterance.
                if(identified>=0)return -1;
                identified=candidate;
            }else if(!isFiller(word)){
                return -1;
            }
        }
        return identified;
    }

    private static boolean isFiller(String word){
        return word.equals("bitte")||word.equals("jetzt")||
                word.equals("aktiviere")||word.equals("aktivier")||
                word.equals("start")||word.equals("starte")||
                word.equals("schalte")||word.equals("ein")||
                word.equals("an")||word.equals("los");
    }

    /**
     * Called ONLY on ACTION_POINTER_DOWN (not primary ACTION_DOWN or MOVE).
     * The original steering finger must already be active. HUD secondary,
     * ability buttons and pause button are never converted into voice taps.
     */
    static boolean acceptsSecondFinger(boolean enabled,boolean inPlay,
            boolean isSteering,boolean isTutorial,boolean guideOpen,
            boolean alreadyListening,float px,float py){
        return enabled&&inPlay&&isSteering&&!isTutorial&&!guideOpen
                &&!alreadyListening&&px>=0f&&px<=420f&&py>=0f&&py<650f
                &&!(px>=365f&&px<=401f&&py>=51f&&py<=82f);
    }
}
