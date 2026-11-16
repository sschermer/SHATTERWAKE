package de.shatterwake.game;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;

/** Native fullscreen activity; owns the short-lived on-device speech recognizer. */
public final class MainActivity extends Activity {
    private GameView gameView;
    private VoiceCommandController voiceController;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        gameView=new GameView(this);
        voiceController=new VoiceCommandController(this,gameView);
        gameView.setVoiceController(voiceController);
        setContentView(gameView);
    }

    @Override protected void onPause(){
        if(voiceController!=null)voiceController.cancelListening();
        if(gameView!=null)gameView.suspend();
        super.onPause();
    }
    @Override protected void onResume(){
        super.onResume();
        if(gameView!=null)gameView.resumeClock();
    }
    @Override protected void onDestroy(){
        if(voiceController!=null)voiceController.releaseRecognizer();
        super.onDestroy();
    }
    @Override public void onRequestPermissionsResult(
            int requestCode,String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(voiceController!=null)
            voiceController.onPermissionResult(requestCode,grantResults);
    }
    @Override public void onBackPressed(){
        if(gameView!=null&&gameView.handleBack()){
            if(voiceController!=null)voiceController.cancelListening();
            return;
        }
        if(voiceController!=null)voiceController.cancelListening();
        super.onBackPressed();
    }
}
