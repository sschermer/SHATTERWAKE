package de.shatterwake.game;

/** Offline voice grammar and two-finger regression tests; plain Java, no Android dependency. */
public final class VoiceCommandChecks {
    public static void main(String[] args){
        assert VoiceCommandRules.commandFromSpeech("Schild!")==0;
        assert VoiceCommandRules.commandFromSpeech("SCHUTZSCHILD")==0;
        assert VoiceCommandRules.commandFromSpeech("Aktiviere Schild")==0;
        assert VoiceCommandRules.commandFromSpeech("Turbo")==1;
        assert VoiceCommandRules.commandFromSpeech("RAPID")==1;
        assert VoiceCommandRules.commandFromSpeech("bitte jetzt schnellfeuer")==1;
        assert VoiceCommandRules.commandFromSpeech("dreifach")==2;
        assert VoiceCommandRules.commandFromSpeech("Dreifachschuss")==2;
        assert VoiceCommandRules.commandFromSpeech("DREIFACHFEUER")==2;
        assert VoiceCommandRules.commandFromSpeech("Plasma")==3;
        assert VoiceCommandRules.commandFromSpeech("Aktivier bitte Plasma jetzt")==3;
        assert VoiceCommandRules.commandFromSpeech("PLASMA!!!")==3;
        assert VoiceCommandRules.commandFromSpeech(null)==-1;
        assert VoiceCommandRules.commandFromSpeech("")==-1;
        assert VoiceCommandRules.commandFromSpeech("Feuer")==-1;
        assert VoiceCommandRules.commandFromSpeech("Schilder")==-1;
        assert VoiceCommandRules.commandFromSpeech("Rakete")==-1;
        assert VoiceCommandRules.commandFromSpeech("EMP")==-1;
        assert VoiceCommandRules.commandFromSpeech("Zwei Schilde")==-1;
        assert VoiceCommandRules.commandFromSpeech("Schild Plasma")==-1;
        // New contract: repeated command in one utterance fires exactly once.
        assert VoiceCommandRules.commandFromSpeech("Schild Schild")==0;
        assert VoiceCommandRules.commandFromSpeech("Nicht Plasma")==-1;
        assert VoiceCommandRules.commandFromSpeech("Ich möchte doch kein Plasma")==-1;
        assert VoiceCommandRules.commandFromSpeech("Bitte starte den Turbo im Schiff")==-1;
        // A second finger can trigger while steering, but not across UI controls.
        assert VoiceCommandRules.acceptsSecondFinger(true,true,true,false,false,
                false,210f,400f);
        assert VoiceCommandRules.acceptsSecondFinger(true,true,true,false,false,
                false,10f,120f);
        assert !VoiceCommandRules.acceptsSecondFinger(false,true,true,false,false,
                false,210f,400f);
        assert !VoiceCommandRules.acceptsSecondFinger(true,false,true,false,false,
                false,210f,400f);
        assert !VoiceCommandRules.acceptsSecondFinger(true,true,false,false,false,
                false,210f,400f);
        assert !VoiceCommandRules.acceptsSecondFinger(true,true,true,true,false,
                false,210f,400f);
        assert !VoiceCommandRules.acceptsSecondFinger(true,true,true,false,true,
                false,210f,400f);
        assert !VoiceCommandRules.acceptsSecondFinger(true,true,true,false,false,
                true,210f,400f);
        assert !VoiceCommandRules.acceptsSecondFinger(true,true,true,false,false,
                false,275f,674f); // rocket, must still fire on single tap
        assert !VoiceCommandRules.acceptsSecondFinger(true,true,true,false,false,
                false,376f,737f); // plasma icon, still single tap
        assert !VoiceCommandRules.acceptsSecondFinger(true,true,true,false,false,
                false,387f,66f); // pause button
        assert !VoiceCommandRules.acceptsSecondFinger(true,true,true,false,false,
                false,12f,650f); // bottom HUD, never mic
        assert !VoiceCommandRules.acceptsSecondFinger(true,true,true,false,false,
                false,-2f,400f);
        System.out.println("PASS: offline speech grammar, ambiguity rejection and two-finger HUD safety");
    }
}
