package de.shatterwake.game;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Offline German voice grammar. Six independent actions, four custom spoken
 * triggers and deterministic unions of actions. No Android runtime needed.
 */
final class VoiceCommandRules {
    private VoiceCommandRules(){}

    static final int ACTION_COUNT=6;
    static final int SLOT_COUNT=4;
    static final int ALL_ACTIONS=(1<<ACTION_COUNT)-1;
    static final int ALL_POWERUPS=(1<<4)-1;
    static final String[] ACTION_LABELS={
            "SCHILD","TURBO","DREIFACH","PLASMA","RAKETEN","EMP"
    };

    static String defaultWord(int slot){
        switch(slot){
            case 0:return "Overdrive";
            case 1:return "Inferno";
            case 2:return "Notfall";
            case 3:return "Angriff";
            default:throw new IllegalArgumentException("slot");
        }
    }
    static int defaultMask(int slot){
        switch(slot){
            case 0:return ALL_POWERUPS; // Overdrive NEVER silently uses rockets/EMP
            case 1:return (1<<1)|(1<<2)|(1<<3)|(1<<4);
            case 2:return (1<<0)|(1<<5);
            case 3:return (1<<1)|(1<<4);
            default:throw new IllegalArgumentException("slot");
        }
    }

    static String normalize(String value){
        if(value==null)return "";
        return Normalizer.normalize(value.toLowerCase(Locale.GERMAN).replace("ß","ss"),
                Normalizer.Form.NFD).replaceAll("\\p{M}+","")
                .replaceAll("[^a-z0-9]+"," ").trim();
    }

    private static boolean filler(String word){
        return word.equals("bitte")||word.equals("jetzt")
                ||word.equals("aktiviere")||word.equals("aktivier")
                ||word.equals("starte")||word.equals("start")
                ||word.equals("schalte")||word.equals("ein")
                ||word.equals("an")||word.equals("los")
                ||word.equals("plus")||word.equals("und")
                ||word.equals("mit");
    }
    private static int directAction(String word){
        switch(word){
            case "schild":case "schutzschild":return 0;
            case "turbo":case "rapid":case "schnellfeuer":return 1;
            case "dreifach":case "dreifachschuss":case "dreifachfeuer":return 2;
            case "plasma":return 3;
            case "rakete":case "raketen":case "raketenfeuer":return 4;
            case "emp":case "schockwelle":case "schockimpuls":return 5;
            default:return -1;
        }
    }

    /** Validate a single spoken word, not a phrase: avoids speech ambiguity. */
    static boolean validCustomWord(String word,String[] existing,int editingSlot){
        if(word==null||existing==null||existing.length!=SLOT_COUNT)return false;
        String normalized=normalize(word);
        if(!normalized.matches("[a-z]{3,20}"))return false;
        if(directAction(normalized)>=0||filler(normalized))return false;
        for(int i=0;i<SLOT_COUNT;i++){
            if(i!=editingSlot&&normalized.equals(normalize(existing[i])))return false;
        }
        return true;
    }

    /**
     * Recognizes direct commands and user-defined names in a short utterance.
     * Unknown/negated prose is rejected. Each action appears once in the mask,
     * including repeated words and overlapping saved combinations.
     *
     * 0 means no acceptable command, not a charge count.
     */
    static int maskFromSpeech(String phrase,String[] words,int[] masks){
        if(words==null||masks==null||words.length!=SLOT_COUNT
                ||masks.length!=SLOT_COUNT)return 0;
        String s=normalize(phrase);
        if(s.isEmpty())return 0;
        String[] parts=s.split(" ");
        if(parts.length>10)return 0;
        int actionMask=0;
        boolean recognized=false;
        for(String part:parts){
            int direct=directAction(part);
            if(direct>=0){
                actionMask|=1<<direct;
                recognized=true;
                continue;
            }
            boolean found=false;
            for(int i=0;i<SLOT_COUNT;i++){
                if(part.equals(normalize(words[i]))){
                    actionMask|=masks[i]&ALL_ACTIONS;
                    found=true;
                    recognized=true;
                    break;
                }
            }
            if(found||filler(part))continue;
            // Negations, unrelated words and unrecognized commands do nothing.
            return 0;
        }
        return recognized?actionMask:0;
    }

    /** Legacy four-ability parser retained for compatibility and old tests. */
    static int commandFromSpeech(String phrase){
        String[] words={"____","_____","______","_______"};
        int[] masks={0,0,0,0};
        int bits=maskFromSpeech(phrase,words,masks);
        return Integer.bitCount(bits)==1&&bits<16?Integer.numberOfTrailingZeros(bits):-1;
    }

    /**
     * Any number of distinct actions can be triggered as partial results arrive,
     * but a given recognized action may be dispatched only once per utterance.
     */
    static int newActions(int recognizedMask,int alreadyTriggered){
        return (recognizedMask&ALL_ACTIONS)&~alreadyTriggered;
    }

    /** Only ACTION_POINTER_DOWN during steering in the free playfield. */
    static boolean acceptsSecondFinger(boolean enabled,boolean inPlay,
            boolean isSteering,boolean isTutorial,boolean guideOpen,
            boolean alreadyListening,float px,float py){
        return enabled&&inPlay&&isSteering&&!isTutorial&&!guideOpen
                &&!alreadyListening&&px>=0f&&px<=420f&&py>=0f&&py<650f
                &&!(px>=365f&&px<=401f&&py>=51f&&py<=82f);
    }
}
