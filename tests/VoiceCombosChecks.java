package de.shatterwake.game;

/** No Android dependencies: voice profiles, six actions, masks and partials. */
public final class VoiceCombosChecks {
    public static void main(String[] args){
        String[] words=new String[VoiceCommandRules.SLOT_COUNT];
        int[] masks=new int[VoiceCommandRules.SLOT_COUNT];
        for(int i=0;i<words.length;i++){
            words[i]=VoiceCommandRules.defaultWord(i);
            masks[i]=VoiceCommandRules.defaultMask(i);
        }
        assert words.length==4;
        assert VoiceCommandRules.ALL_ACTIONS==63;
        assert VoiceCommandRules.ALL_POWERUPS==15;
        assert masks[0]==15 : "Overdrive must NOT fire rockets/EMP";
        assert masks[1]==30 : "Inferno: rapid + triple + plasma + rockets";
        assert masks[2]==33 : "Notfall: shield + EMP";
        assert masks[3]==18 : "Angriff: rapid + rockets";

        for(int i=0;i<words.length;i++)
            assert VoiceCommandRules.validCustomWord(words[i],words,i);
        assert !VoiceCommandRules.validCustomWord("schild",words,0);
        assert !VoiceCommandRules.validCustomWord("Raketen",words,0);
        assert !VoiceCommandRules.validCustomWord("Rakete",words,0);
        assert !VoiceCommandRules.validCustomWord("EMP",words,0);
        assert !VoiceCommandRules.validCustomWord("Schockwelle",words,0);
        assert !VoiceCommandRules.validCustomWord("TURBO",words,0);
        assert !VoiceCommandRules.validCustomWord("Start",words,0);
        assert !VoiceCommandRules.validCustomWord("plasma",words,0);
        assert !VoiceCommandRules.validCustomWord("Inferno",words,0);
        assert !VoiceCommandRules.validCustomWord("a",words,0);
        assert !VoiceCommandRules.validCustomWord("a b",words,0);
        assert !VoiceCommandRules.validCustomWord("abc def",words,0);
        assert !VoiceCommandRules.validCustomWord("abc123",words,0);
        assert VoiceCommandRules.validCustomWord("Großangriff",words,0);
        assert VoiceCommandRules.normalize("Großangriff").equals("grossangriff");
        assert VoiceCommandRules.validCustomWord("Hyperraum",words,0);

        // Four reusable user profiles; no special, hard-wired Overdrive path.
        assert VoiceCommandRules.maskFromSpeech("Overdrive",words,masks)==15;
        assert VoiceCommandRules.maskFromSpeech("INFERNO!",words,masks)==30;
        assert VoiceCommandRules.maskFromSpeech("notfall",words,masks)==33;
        assert VoiceCommandRules.maskFromSpeech("Angriff",words,masks)==18;
        words[0]="Grossangriff";masks[0]=(1<<1)|(1<<4)|(1<<5);
        assert VoiceCommandRules.maskFromSpeech("Grossangriff",words,masks)==50;
        assert VoiceCommandRules.maskFromSpeech("Overdrive",words,masks)==0;
        words[0]="Overdrive";masks[0]=15;

        // Six individual commands retain their existing charge-bearing actions.
        assert VoiceCommandRules.maskFromSpeech("Schild",words,masks)==1;
        assert VoiceCommandRules.maskFromSpeech("TURBO",words,masks)==2;
        assert VoiceCommandRules.maskFromSpeech("dreifach",words,masks)==4;
        assert VoiceCommandRules.maskFromSpeech("PLASMA",words,masks)==8;
        assert VoiceCommandRules.maskFromSpeech("Raketen",words,masks)==16;
        assert VoiceCommandRules.maskFromSpeech("Rakete",words,masks)==16;
        assert VoiceCommandRules.maskFromSpeech("Schockwelle",words,masks)==32;
        assert VoiceCommandRules.maskFromSpeech("EMP",words,masks)==32;

        // Arbitrary direct combinations and custom word unions.
        assert VoiceCommandRules.maskFromSpeech("Dreifach, Turbo und Plasma",words,masks)==14;
        assert VoiceCommandRules.maskFromSpeech("Dreifach plus Rapid plus Plasma",words,masks)==14;
        assert VoiceCommandRules.maskFromSpeech("Dreifach Turbo Plasma",words,masks)==14;
        assert VoiceCommandRules.maskFromSpeech("Raketen Schockwelle",words,masks)==48;
        assert VoiceCommandRules.maskFromSpeech("Plasma Raketen Dreifach",words,masks)==28;
        assert VoiceCommandRules.maskFromSpeech("bitte Overdrive Raketen",words,masks)==31;
        assert VoiceCommandRules.maskFromSpeech("Inferno Notfall",words,masks)==63;
        assert VoiceCommandRules.maskFromSpeech("Schild Schild Schild",words,masks)==1;
        assert VoiceCommandRules.maskFromSpeech("Overdrive Overdrive",words,masks)==15;
        assert VoiceCommandRules.maskFromSpeech("Plasma Inferno",words,masks)==30;
        assert VoiceCommandRules.maskFromSpeech("bitte jetzt schnellfeuer",words,masks)==2;

        assert VoiceCommandRules.maskFromSpeech("nicht Plasma",words,masks)==0;
        assert VoiceCommandRules.maskFromSpeech("Schild niemals!",words,masks)==0;
        assert VoiceCommandRules.maskFromSpeech("Lautstärke",words,masks)==0;
        assert VoiceCommandRules.maskFromSpeech(null,words,masks)==0;
        assert VoiceCommandRules.maskFromSpeech("",words,masks)==0;
        assert VoiceCommandRules.maskFromSpeech("abc def ghi",words,masks)==0;
        assert VoiceCommandRules.maskFromSpeech("starte",words,masks)==0;
        assert VoiceCommandRules.maskFromSpeech("Overdrive",
                new String[3],new int[4])==0;
        masks[3]=0;
        assert VoiceCommandRules.maskFromSpeech("Angriff",words,masks)==0;

        // Partial recognition is stable after two updates, and old actions
        // NEVER fire a second time even if final recognition repeats them.
        int sent=0,previous=0;
        int[] hypotheses={4,6,14,14};
        int[] expectedNew={0,4,2,8};
        for(int i=0;i<hypotheses.length;i++){
            int stable=hypotheses[i]&previous;
            int fresh=VoiceCommandRules.newActions(stable,sent);
            assert fresh==expectedNew[i] : "unexpected partial step "+i;
            sent|=fresh;
            previous=hypotheses[i];
        }
        assert sent==14;
        assert VoiceCommandRules.newActions(14,sent)==0;
        assert VoiceCommandRules.newActions(31,sent)==17;
        assert VoiceCommandRules.newActions(63,63)==0;
        assert VoiceCommandRules.newActions(63,-1)==0;

        // Empty charges are skipped independently by GameView. This mask
        // contract does not make combinations atomic or reset timers.
        assert (VoiceCommandRules.defaultMask(0)&0x30)==0;
        assert (VoiceCommandRules.defaultMask(1)&(1<<4))!=0;
        System.out.println("PASS: six voice actions, four editable profiles, Overdrive, "
                +"ambiguity guards and idempotent partial-to-final dispatch");
    }
}
