package de.shatterwake.game;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

/**
 * Offline, optional push-to-talk. Reuses a prepared local recognizer;
 * the first valid partial result can activate an action immediately. Actions
 * are deduplicated per utterance, including between partial and final results.
 */
final class VoiceCommandController {
    static final int PERMISSION_REQUEST=6104;
    private static final long LISTEN_MS=3300L;
    private static final long FINAL_RESULT_GRACE_MS=1000L;
    private final Activity activity;
    private final GameView game;
    private final boolean localAvailable;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private boolean listening=false;
    private int generation=0;
    private int dispatchedMask=0;

    VoiceCommandController(Activity activity,GameView game){
        this.activity=activity;
        this.game=game;
        boolean available=false;
        if(Build.VERSION.SDK_INT>=31){
            try{available=SpeechRecognizer.isOnDeviceRecognitionAvailable(activity);}
            catch(Exception ignored){}
        }
        localAvailable=available;
    }

    boolean isLocalAvailable(){return localAvailable;}
    /** Warm an already-approved recognizer at startup; never prompts for mic access. */
    void prepareIfEnabled(){prepareRecognizer();}
    boolean isListening(){return listening;}

    /** The local recognizer is prepared when voice is enabled, not in combat. */
    void requestMicrophonePermission(){
        if(!localAvailable)return;
        if(activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                !=PackageManager.PERMISSION_GRANTED){
            activity.requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},PERMISSION_REQUEST);
        }else{
            prepareRecognizer();
        }
    }

    void onPermissionResult(int requestCode,int[] grantResults){
        if(requestCode!=PERMISSION_REQUEST)return;
        if(grantResults.length==0||grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            game.onVoicePermissionDenied();
        }else{
            prepareRecognizer();
            game.onVoiceStatus("MIKROFON BEREIT",false);
        }
    }

    private void prepareRecognizer(){
        if(!localAvailable||recognizer!=null||!game.voiceEnabled())return;
        if(activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                !=PackageManager.PERMISSION_GRANTED)return;
        try{recognizer=SpeechRecognizer.createOnDeviceSpeechRecognizer(activity);}
        catch(Exception ignored){recognizer=null;}
    }

    void requestVoiceCommand(){
        if(listening||!game.voiceEnabled()||!game.isPlaying())return;
        if(!localAvailable){
            game.onVoiceStatus("OFFLINE-SPRACHE NICHT VERFUEGBAR",false);
            return;
        }
        if(activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                !=PackageManager.PERMISSION_GRANTED){
            game.onVoicePermissionDenied();
            return;
        }
        prepareRecognizer();
        if(recognizer==null){
            game.onVoiceStatus("OFFLINE-SPRACHE NICHT BEREIT",false);
            return;
        }
        final int session=++generation;
        listening=true;
        dispatchedMask=0;
        game.onVoiceStatus("JETZT SPRECHEN",true);
        try{
            recognizer.setRecognitionListener(new RecognitionListener(){
                @Override public void onReadyForSpeech(Bundle params){
                    if(isCurrent(session))game.onVoiceStatus("JETZT SPRECHEN",true);
                }
                @Override public void onBeginningOfSpeech(){}
                @Override public void onRmsChanged(float rmsdB){}
                @Override public void onBufferReceived(byte[] buffer){}
                @Override public void onEndOfSpeech(){}
                @Override public void onEvent(int eventType,Bundle params){}
                @Override public void onPartialResults(Bundle partialResults){
                    if(!isCurrent(session))return;
                    int parsed=firstRecognizedMask(partialResults);
                    // First grammatically valid partial fires immediately.
                    // Each action is deduplicated across all partials and
                    // the final recognition by dispatch()/newActions().
                    dispatch(session,parsed);
                }
                @Override public void onResults(Bundle results){
                    if(!isCurrent(session))return;
                    dispatch(session,firstRecognizedMask(results));
                    finish(session,dispatchedMask!=0?"BEFEHLE AKTIV":"KEIN BEFEHL ERKANNT");
                }
                @Override public void onError(int error){
                    if(!isCurrent(session))return;
                    String message=dispatchedMask!=0?"BEFEHLE AKTIV":
                            (error==SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE
                            ||error==SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED)
                            ?"DEUTSCHE OFFLINE-SPRACHE FEHLT":
                            error==SpeechRecognizer.ERROR_NO_MATCH
                            ||error==SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                            ?"KEIN BEFEHL ERKANNT":"SPRACHERKENNUNG NICHT BEREIT";
                    finish(session,message);
                }
            });
            Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"de-DE");
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
            // Service may ignore these hints; no assumption about fixed latency.
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,400L);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,650L);
            recognizer.startListening(intent);
            handler.postDelayed(()->{
                if(!isCurrent(session))return;
                try{recognizer.stopListening();}
                catch(Exception ignored){}
                handler.postDelayed(()->{
                    if(isCurrent(session))
                        finish(session,dispatchedMask!=0?"BEFEHLE AKTIV":"KEIN BEFEHL ERKANNT");
                },FINAL_RESULT_GRACE_MS);
            },LISTEN_MS);
        }catch(Exception e){
            finish(session,"OFFLINE-SPRACHE NICHT BEREIT");
            releaseRecognizer();
        }
    }

    private int firstRecognizedMask(Bundle result){
        ArrayList<String> phrases=result==null?null:
                result.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(phrases==null)return 0;
        // Highest ranked recognition wins; don't combine contradictory guesses.
        for(String phrase:phrases){
            int mask=game.voiceMaskForPhrase(phrase);
            if(mask!=0)return mask;
        }
        return 0;
    }

    private boolean isCurrent(int session){return listening&&generation==session;}

    private void dispatch(int session,int mask){
        if(!isCurrent(session))return;
        int fresh=VoiceCommandRules.newActions(mask,dispatchedMask);
        if(fresh==0)return;
        dispatchedMask|=fresh; // before callback so repeated results are idempotent
        game.onVoiceRecognizedMask(fresh);
    }

    private void finish(int session,String message){
        if(!isCurrent(session))return;
        ++generation;
        listening=false;
        handler.removeCallbacksAndMessages(null);
        game.onVoiceStatus(message,false);
    }

    void cancelListening(){
        if(listening){
            ++generation;
            listening=false;
            handler.removeCallbacksAndMessages(null);
            if(recognizer!=null){
                try{recognizer.cancel();}catch(Exception ignored){}
            }
        }
        game.onVoiceStatus("",false);
    }

    /** Use when the user explicitly opts out and at final Activity destruction. */
    void releaseRecognizer(){
        cancelListening();
        if(recognizer!=null){
            try{recognizer.destroy();}catch(Exception ignored){}
            recognizer=null;
        }
    }
}
