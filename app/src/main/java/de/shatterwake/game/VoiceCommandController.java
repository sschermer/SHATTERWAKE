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
 * Tap-to-listen, on-device-only controller. No network fallback, background
 * microphone service, audio recording files, third-party SDK, or telemetry.
 * The Android on-device recognition engine must already be installed.
 */
final class VoiceCommandController {
    static final int PERMISSION_REQUEST=6104;
    private static final long LISTEN_MS=3300L;
    private static final long FINAL_RESULT_GRACE_MS=1600L;
    private final Activity activity;
    private final GameView game;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private boolean listening=false;
    private int generation=0;

    VoiceCommandController(Activity activity,GameView game){
        this.activity=activity;
        this.game=game;
    }

    boolean isLocalAvailable(){
        if(Build.VERSION.SDK_INT<31)return false;
        try{return SpeechRecognizer.isOnDeviceRecognitionAvailable(activity);}
        catch(Exception ignored){return false;}
    }

    boolean isListening(){return listening;}

    /** Called only after explicit user selection in the settings screen. */
    void requestMicrophonePermission(){
        if(activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                !=PackageManager.PERMISSION_GRANTED){
            activity.requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},PERMISSION_REQUEST);
        }
    }

    void onPermissionResult(int requestCode,int[] grantResults){
        if(requestCode!=PERMISSION_REQUEST)return;
        if(grantResults.length==0||grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            game.onVoicePermissionDenied();
        }else{
            game.onVoiceStatus("MIKROFON BEREIT",false);
        }
    }

    /** Never opens microphone while disabled, unavailable, or already active. */
    void requestVoiceCommand(){
        if(listening||!game.voiceEnabled()||!game.isPlaying() )return;
        if(!isLocalAvailable()){
            game.onVoiceStatus("OFFLINE-SPRACHE NICHT VERFUEGBAR",false);
            return;
        }
        if(activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                !=PackageManager.PERMISSION_GRANTED){
            game.onVoicePermissionDenied();
            return;
        }
        final int session=++generation;
        listening=true;
        game.onVoiceStatus("MIKROFON STARTET",true);
        try{
            recognizer=SpeechRecognizer.createOnDeviceSpeechRecognizer(activity);
            recognizer.setRecognitionListener(new RecognitionListener(){
                @Override public void onReadyForSpeech(Bundle params){
                    if(isCurrent(session))game.onVoiceStatus("JETZT SPRECHEN",true);
                }
                @Override public void onBeginningOfSpeech(){}
                @Override public void onRmsChanged(float rmsdB){}
                @Override public void onBufferReceived(byte[] buffer){}
                @Override public void onEndOfSpeech(){}
                @Override public void onPartialResults(Bundle partialResults){}
                @Override public void onEvent(int eventType,Bundle params){}
                @Override public void onError(int error){
                    if(!isCurrent(session))return;
                    String message=(error==SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE
                            ||error==SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED)
                            ?"DEUTSCHE OFFLINE-SPRACHE FEHLT":
                            error==SpeechRecognizer.ERROR_NO_MATCH
                            ||error==SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                            ?"KEIN BEFEHL ERKANNT":"SPRACHERKENNUNG NICHT BEREIT";
                    finish(session,message,-1);
                }
                @Override public void onResults(Bundle results){
                    if(!isCurrent(session))return;
                    int command=-1;
                    ArrayList<String> guesses=results==null?null:
                            results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if(guesses!=null){
                        for(String guess:guesses){
                            command=VoiceCommandRules.commandFromSpeech(guess);
                            if(command>=0)break;
                        }
                    }
                    finish(session,command<0?"KEIN BEFEHL ERKANNT":"BEFEHL ERKANNT",command);
                }
            });
            Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"de-DE");
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);
            recognizer.startListening(intent);
            handler.postDelayed(()->{
                if(!isCurrent(session))return;
                try{recognizer.stopListening();}
                catch(Exception ignored){}
                handler.postDelayed(()->finish(session,"KEIN BEFEHL ERKANNT",-1),
                        FINAL_RESULT_GRACE_MS);
            },LISTEN_MS);
        }catch(Exception e){
            finish(session,"OFFLINE-SPRACHE NICHT BEREIT",-1);
        }
    }

    private boolean isCurrent(int session){return listening&&generation==session;}

    private void finish(int session,String feedback,int command){
        if(!isCurrent(session))return;
        cleanup();
        game.onVoiceStatus(feedback,false);
        if(command>=0)game.onVoiceRecognized(command);
    }

    void cancelListening(){
        cleanup();
        game.onVoiceStatus("",false);
    }

    private void cleanup(){
        ++generation;
        listening=false;
        handler.removeCallbacksAndMessages(null);
        if(recognizer!=null){
            try{recognizer.cancel();recognizer.destroy();}
            catch(Exception ignored){}
            recognizer=null;
        }
    }
}
