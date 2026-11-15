package de.shatterwake.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

/** SHATTERWAKE: original, offline arcade game rendered entirely with Android Canvas.
 * All artwork is procedural vector geometry; sounds are generated from mathematical waveforms.
 */
public final class GameView extends View {
    private static final float W=420f, H=780f, FIELD_BOTTOM=643f;
    private static final int MENU=0, PLAY=1, SHOP=2, ROUTE=3, DEAD=4,
            GAME_OVER=5, SETTINGS=6, ARCHIVE=7, WIN=8, PAUSE=9;
    private static final int WHITE=0xFFF4FBFF, MUTED=0xFF9BB8CB, CYAN=0xFF52DCF7,
            GOLD=0xFFFFC963, RED=0xFFFF6679, DEEP=0xFF081125, GREEN=0xFF63E4AB;
    private static final String[] WORLD_NAMES={"SCHROTTGÜRTEL", "MASCHINENKOLONIE", "NEBELGRABEN",
            "STURMKERN", "NULL-ZITADELLE"};
    private static final String[] BOSS_NAMES={"KETTENWURM", "WANDELFESTUNG", "PRISMENLEVIATHAN",
            "SCHWARMHERZ", "DER ARCHITEKT"};
    private static final int[] WORLD_COLORS={0xFF10233C,0xFF283342,0xFF2E2047,0xFF192F48,0xFF27233D};
    private static final String[] ABILITY_NAMES={"SCHILD","RAPID","DREIFACH","PLASMA"};
    private static final String[] SECONDARY_NAMES={"RAKETEN","EMP"};
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SpriteAtlas atlas=new SpriteAtlas();
    private final Random random=new Random();
    private final SharedPreferences prefs;
    private final ArrayList<Enemy> enemies=new ArrayList<>();
    private final ArrayList<Shot> shots=new ArrayList<>();
    private final ArrayList<Pickup> pickups=new ArrayList<>();
    private final ArrayList<Particle> particles=new ArrayList<>();
    private final ArrayList<Offer> offers=new ArrayList<>();
    private final float[] starX=new float[120], starY=new float[120], starV=new float[120];
    private final int[] abilityCharges=new int[4];
    private final int[] secCharges=new int[]{3,2};
    private final int[] unlockLevels=new int[5];
    private SoundPool soundPool;
    private int[] sounds=new int[5];
    private boolean soundEnabled=true, vibrationEnabled=true;
    private int state=MENU, previousState=MENU, inputMode=0, world=0, stage=1;
    private boolean tutorialActive=false, tutorialComplete=false, secondarySeen=false,
            shopIntroSeen=false, shopIntroShowing=false;
    private int tutorialStep=TutorialRules.MOVE, tutorialNextStep=TutorialRules.MOVE;
    private int guideKind=TutorialRules.GUIDE_NONE, guideId=-1, seenAbilityMask=0;
    private boolean tutorialCheckpoint=false, tutorialRocketFired=false;
    private float tutorialTravel=0f;
    private int stars=0, score=0, best=0, hp=5, maxHp=5, continuesUsed=0;
    private int fireLevel=0, damageLevel=0, magnetLevel=0, shieldLevel=0, droneLevel=0;
    private int techMask=0, newlyUnlocked=-1, armedSecondary=-1;
    private float x=W/2, y=573, targetX=x, targetY=y;
    private float stageTime=0, spawnTimer=0, starTimer=0, shootTimer=0, secondaryRegen=0;
    private float invulnerable=0, shieldTime=0, rapidTime=0, tripleTime=0, plasmaTime=0;
    private float planetOffset=0, idleTime=0, flashTime=0, bannerTime=0, nextEnemySide=1;
    private float droneShootTimer=0;
    private boolean bossActive=false, downWasButton=false;
    private int bossSequence=GameRules.BOSS_NORMAL;
    private float bossSequenceTimer=0f;
    private int touchPointer=-1;
    private float touchOffsetX=0, touchOffsetY=0;
    private Boss boss;
    private String banner="", shopMessage="";
    private long lastFrame=0;

    private static final class Enemy {
        int kind, hp, maxHp, value, fragments;
        float x,y,vx,vy,t,shoot,phase,radius;
        Enemy(int kind,float x,float y,int hp) {
            this.kind=kind;this.x=x;this.y=y;this.hp=hp;maxHp=hp;
            radius=kind==3?25:kind==5?24:kind==6?24:15;
            value=(kind==3||kind==5)?38:kind==6?25:20;
            shoot=1.65f+(kind%3)*.45f;
        }
    }
    private static final class Shot {
        float x,y,vx,vy,damage,radius,age;
        int kind; boolean hostile;
        Shot(float x,float y,float vx,float vy,float damage,int kind,boolean hostile) {
            this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.damage=damage;
            this.kind=kind;this.hostile=hostile;
            radius=kind==2?7:kind==3?8:kind==4?12:4;
        }
    }
    private static final class Pickup {
        float x,y,vy,t; int kind,value;
        Pickup(float x,float y,int kind,int value) {
            this.x=x;this.y=y;this.kind=kind;this.value=value;vy=72;
        }
    }
    private static final class Particle {
        float x,y,vx,vy,life,maxLife,radius;int color;
        Particle(float x,float y,float vx,float vy,float life,float radius,int color) {
            this.x=x;this.y=y;this.vx=vx;this.vy=vy;
            this.life=life;maxLife=life;this.radius=radius;this.color=color;
        }
    }
    private static final class Node {
        float x,y,hp,maxHp; int type,id;boolean burst=false;
        Node(int id,int type,float hp){this.id=id;this.type=type;this.hp=hp;maxHp=hp;}
    }
    private static final class Boss {
        ArrayList<Node> nodes=new ArrayList<>();
        float hp,maxHp,time,fireTimer=.6f,phaseTransition=0;
        int type,phase=1;
        boolean split=false;
        Boss(int type,int tier) {
            this.type=type;
            int count=type==0?12:type==1?9:type==2?8:type==3?10:11;
            for(int i=0;i<count;i++) {
                int part=i==0?0:(i%5==0?3:(i%4==0?2:(i%3==0?4:1)));
                nodes.add(new Node(i,part, i==0?135f+(tier-1)*50:39f+(tier-1)*18));
            }
            hp=610+(tier-1)*355;
            maxHp=hp;
        }
    }
    private static final class Offer {
        final int id,base;final String title,description;
        Offer(int id,String name,String detail,int base){this.id=id;title=name;description=detail;this.base=base;}
    }

    public GameView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        prefs=context.getSharedPreferences("shatterwake_save_v1",Context.MODE_PRIVATE);
        best=prefs.getInt("best",0);
        techMask=prefs.getInt("tech",0);
        inputMode=prefs.getInt("control",0);
        soundEnabled=prefs.getBoolean("sound",true);
        vibrationEnabled=prefs.getBoolean("vibration",true);
        tutorialComplete=prefs.getBoolean("tutorial_complete",false);
        seenAbilityMask=prefs.getInt("seen_ability_mask",0);
        secondarySeen=prefs.getBoolean("secondary_seen",false);
        shopIntroSeen=prefs.getBoolean("shop_intro_seen",false);
        for(int i=0;i<starX.length;i++) resetBackgroundStar(i,true);
        initSounds();
        setFocusable(true);
    }

    public void suspend(){if(state==PLAY){state=PAUSE;}lastFrame=0;}
    public void resumeClock(){lastFrame=0;invalidate();}
    public boolean handleBack(){
        if(state==PLAY){state=PAUSE;return true;}
        if(state==PAUSE){state=PLAY;return true;}
        if(state==MENU)return false;
        state=MENU;return true;
    }
    private void save(){prefs.edit().putInt("best",best).putInt("tech",techMask)
            .putInt("control",inputMode).putBoolean("sound",soundEnabled)
            .putBoolean("vibration",vibrationEnabled)
            .putBoolean("tutorial_complete",tutorialComplete)
            .putInt("seen_ability_mask",seenAbilityMask)
            .putBoolean("secondary_seen",secondarySeen)
            .putBoolean("shop_intro_seen",shopIntroSeen).apply();}
    private void resetBackgroundStar(int i,boolean anywhere){
        starX[i]=random.nextFloat()*W;
        starY[i]=anywhere?random.nextFloat()*H:-random.nextFloat()*25;
        starV[i]=20+random.nextFloat()*75;
    }
    private void sound(int kind){
        if(!soundEnabled||soundPool==null||kind<0||kind>=sounds.length||sounds[kind]<=0)return;
        try {float volume=kind==4?.58f:.18f;
            soundPool.play(sounds[kind],volume,volume,1,0,1);}catch(Exception ignored){}
    }
    private void vibrate(long duration){
        if(!vibrationEnabled)return;
        try {Vibrator v=(Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if(v!=null && v.hasVibrator())v.vibrate(VibrationEffect.createOneShot(duration,110));
        } catch(Exception ignored){}
    }
    /** PCM waves are generated at runtime; there are no third-party recordings. */
    private void initSounds(){
        try {
            soundPool=new SoundPool.Builder().setMaxStreams(5)
                    .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
            for(int s=0;s<5;s++) {
                int rate=16000, count=s==4?rate*3:s==2?5400:2400;
                double phase=0;
                ByteBuffer buf=ByteBuffer.allocate(44+count*2).order(ByteOrder.LITTLE_ENDIAN);
                buf.put(new byte[]{'R','I','F','F'}).putInt(36+count*2)
                        .put(new byte[]{'W','A','V','E','f','m','t',' '}).putInt(16)
                        .putShort((short)1).putShort((short)1).putInt(rate).putInt(rate*2)
                        .putShort((short)2).putShort((short)16)
                        .put(new byte[]{'d','a','t','a'}).putInt(count*2);
                for(int n=0;n<count;n++) {
                    double q=(double)n/count, f=s==0?720-450*q:s==1?220+420*q
                            :s==2?100+140*q:s==3?470-300*q:
                            520+310*(.5-.5*Math.cos(2*Math.PI*q*5.0));
                    phase+=2*Math.PI*f/rate;
                    double wave=Math.sin(phase);
                    if(s==2)wave+=.32*Math.sin(phase*.45);
                    if(s==4)wave=.76*Math.sin(phase)+.24*Math.sin(phase*1.99);
                    double env=s==4?Math.min(1,Math.min(n/1300.0,(count-n)/1800.0))
                            :Math.pow(1-q,s==2?1.9:2.6);
                    buf.putShort((short)Math.max(-32767,Math.min(32767,wave*env*11500)));
                }
                File file=new File(getContext().getCacheDir(),"sw_sfx_"+s+".wav");
                FileOutputStream out=new FileOutputStream(file);out.write(buf.array());out.close();
                sounds[s]=soundPool.load(file.getAbsolutePath(),1);
            }
        }catch(Exception ignored){soundPool=null;}
    }
    @Override protected void onDetachedFromWindow(){super.onDetachedFromWindow();if(soundPool!=null)soundPool.release();}

    private void startRun(){
        tutorialActive=false;guideKind=TutorialRules.GUIDE_NONE;guideId=-1;
        stage=1;world=0;stars=0;score=0;hp=maxHp=6;continuesUsed=0;
        fireLevel=damageLevel=magnetLevel=shieldLevel=droneLevel=0;
        shieldTime=rapidTime=tripleTime=plasmaTime=invulnerable=0;
        secCharges[0]=3;secCharges[1]=2;armedSecondary=-1;
        for(int i=0;i<abilityCharges.length;i++)abilityCharges[i]=1;
        startWorld(0);
    }
    /** Training is isolated from score, stars and ordinary sector balancing. */
    private void startTutorial(){
        startRun();
        tutorialActive=true;
        tutorialStep=tutorialNextStep=TutorialRules.MOVE;
        tutorialCheckpoint=false;tutorialRocketFired=false;
        tutorialTravel=0;
        enemies.clear();shots.clear();pickups.clear();particles.clear();
        x=targetX=W/2;y=targetY=570;
        stageTime=0;stars=0;score=0;
        invulnerable=0;bannerTime=0;
        guideKind=TutorialRules.GUIDE_NONE;guideId=-1;
    }
    /** A lesson never jumps to the next lesson without explicit confirmation. */
    private void tutorialAction(int action) {
        if(!tutorialActive||tutorialCheckpoint)return;
        int next=TutorialRules.nextStep(tutorialStep,action);
        if(next==tutorialStep)return;
        tutorialNextStep=next;
        tutorialCheckpoint=true;
    }
    /** Set up each safe, repeatable training situation after the player taps WEITER. */
    private void continueTutorial() {
        if(!tutorialActive||!tutorialCheckpoint)return;
        tutorialCheckpoint=false;
        tutorialStep=tutorialNextStep;
        tutorialRocketFired=false;
        enemies.clear();shots.clear();pickups.clear();
        shootTimer=0f;
        if(tutorialStep==TutorialRules.TARGET){
            Enemy target=new Enemy(0,x>W/2?85:335,265,42);
            target.vy=0;target.shoot=9999;target.value=0;
            enemies.add(target);
        }else if(tutorialStep==TutorialRules.COLLECT_STAR){
            Pickup star=new Pickup(x>W/2?90:330,459,0,12);
            star.vy=0;pickups.add(star);
        }else if(tutorialStep>=TutorialRules.ACTIVATE_SHIELD
                && tutorialStep<=TutorialRules.ACTIVATE_PLASMA){
            int id=tutorialStep-TutorialRules.ACTIVATE_SHIELD;
            abilityCharges[id]=Math.max(1,abilityCharges[id]);
            if(id==1)shieldTime=0;
            if(id==2)rapidTime=0;
            if(id==3)tripleTime=0;
            beginGuide(TutorialRules.GUIDE_ABILITY,id);
        }else if(tutorialStep==TutorialRules.PLASMA_TARGET){
            plasmaTime=Math.max(plasmaTime,7f);
            Enemy target=new Enemy(3,x>W/2?100:320,265,112);
            target.vy=0;target.shoot=9999;target.value=0;
            enemies.add(target);
        }else if(tutorialStep==TutorialRules.FIRE_SECONDARY){
            plasmaTime=0f;
            secCharges[0]=Math.max(1,secCharges[0]);
            Enemy target=new Enemy(3,x>W/2?100:320,262,78);
            target.vy=0;target.shoot=9999;target.value=0;
            enemies.add(target);
            beginGuide(TutorialRules.GUIDE_SECONDARY,0);
        }
    }
    private void beginGuide(int kind,int id) {
        guideKind=kind;guideId=id;
        armedSecondary=-1;
        touchPointer=-1;downWasButton=false;
    }
    private void dismissGuide() {
        if(guideKind==TutorialRules.GUIDE_ABILITY && guideId>=0) {
            seenAbilityMask=TutorialRules.markAbilitySeen(seenAbilityMask,guideId);
        }else if(guideKind==TutorialRules.GUIDE_SECONDARY)secondarySeen=true;
        guideKind=TutorialRules.GUIDE_NONE;guideId=-1;
        save();
    }
    private void finishTutorial(){
        tutorialComplete=true;tutorialActive=false;dismissGuide();save();
        startRun();
    }
    private void updateTutorial(float dt){
        if(tutorialCheckpoint)return;
        x+=(targetX-x)*Math.min(1,dt*16f);
        y+=(targetY-y)*Math.min(1,dt*16f);
        x=clamp(x,24,W-24);y=clamp(y,120,FIELD_BOTTOM-22);
        if(tutorialStep==TutorialRules.TARGET||tutorialStep==TutorialRules.PLASMA_TARGET){
            shootTimer-=dt;
            if(shootTimer<=0){firePrimary();shootTimer=GameRules.fireInterval(0,false);}
        }
        if(tutorialStep==TutorialRules.TARGET||tutorialStep==TutorialRules.COLLECT_STAR
                ||tutorialStep==TutorialRules.PLASMA_TARGET||tutorialStep==TutorialRules.FIRE_SECONDARY){
            updateShots(dt);
            updatePickups(dt);
        }
        if(tutorialStep==TutorialRules.MOVE && Math.abs(targetX-W/2)>65) {
            tutorialAction(TutorialRules.ACTION_MOVE);
        }
    }
    private void startWorld(int next){
        world=next;stage=GameRules.worldTier(world);
        enemies.clear();shots.clear();pickups.clear();particles.clear();
        boss=null;bossActive=false;bossSequence=GameRules.BOSS_NORMAL;bossSequenceTimer=0f;
        stageTime=0;spawnTimer=2.5f;starTimer=6.2f;
        shootTimer=0;secondaryRegen=0;droneShootTimer=0;
        x=targetX=W/2;y=targetY=573;invulnerable=4.0f;
        state=PLAY;flashTime=.7f;
        banner="SEKTOR "+stage+" · "+WORLD_NAMES[world];bannerTime=3.1f;
    }
    private float rnd(float lo,float hi){return lo+random.nextFloat()*(hi-lo);}
    private void spawnEnemy(){
        if(enemies.size()>=GameRules.enemyCap(stage,stageTime))return;
        int tier=GameRules.worldTier(world);
        int kind=random.nextInt(100);
        kind=kind<33?0:kind<54?1:kind<74?2:kind<85?3:kind<95?4:5;
        if(stage==1 && kind==5)kind=1;
        if(stage==1 && stageTime<25f)kind=random.nextBoolean()?0:1;
        int count=(stage==1 && stageTime<22f)?1:(kind==3||kind==5?1:(random.nextFloat()<.37f?2:1));
        count=Math.min(count,GameRules.enemyCap(stage,stageTime)-enemies.size());
        float base=rnd(45,365);
        for(int i=0;i<count;i++) {
            float xx=clamp(base+(i-(count-1)/2f)*44,30,W-30);
            Enemy e=new Enemy(kind,xx,-40-i*27,25+tier*8+(kind>=3?34:0));
            e.phase=rnd(0,6.28f);e.vx=random.nextBoolean()?1:-1;
            e.vy=(rnd(48,75)+tier*12)*.86f;e.fragments=kind==6?4:0;
            e.shoot=rnd(1.6f,3.1f);
            enemies.add(e);
        }
    }
    /** Only invoked once the previous wave has cleared and the siren completed. */
    private void spawnBoss(){
        bossActive=true;
        boss=new Boss(world,stage);
        bossSequence=GameRules.BOSS_ENTERING;
        bossSequenceTimer=GameRules.BOSS_ENTRY_SECONDS;
        boss.fireTimer=1.65f;
        positionBossNodes();
        banner="BOSS IM ANFLUG · "+BOSS_NAMES[world];bannerTime=2f;
    }
    private void beginBossWarning(){
        bossSequence=GameRules.BOSS_WARNING;
        bossSequenceTimer=GameRules.BOSS_WARNING_SECONDS;
        // No lingering stray enemy bullets can unfairly hit during the warning.
        for(int i=shots.size()-1;i>=0;i--)if(shots.get(i).hostile)shots.remove(i);
        sound(4);vibrate(80);
    }
    private void spawnPickup(float xx,float yy,int kind,int val){
        if(pickups.size()>55)return;
        pickups.add(new Pickup(xx,yy,kind,val));
    }
    private void starBurst(float xx,float yy,int total,int pieces){
        total=GameRules.scaleStarReward(total);
        int each=Math.max(1,total/Math.max(1,pieces));
        for(int i=0;i<pieces;i++){
            Pickup q=new Pickup(xx+rnd(-34,34),yy+rnd(-22,22),0,each);
            q.vy=rnd(55,100);pickups.add(q);
        }
    }
    private void explode(float px,float py,int color,int number){
        for(int i=0;i<number;i++){
            float a=rnd(0,6.283f),v=rnd(22,175),life=rnd(.3f,.85f);
            if(particles.size()<300)particles.add(new Particle(px,py,(float)Math.cos(a)*v,
                    (float)Math.sin(a)*v,life,rnd(1.8f,5.5f),i%4==0?WHITE:color));
        }
        sound(1);
    }
    private void addScore(int points){score+=points;if(score>best){best=score;save();}}
    /** Plasma has a silent, distinctive purple impact even against armored targets. */
    private void plasmaImpact(float px,float py){
        if(particles.size()>260)return;
        for(int i=0;i<7;i++){
            float angle=i*6.283f/7f+rnd(-.16f,.16f);
            float speed=rnd(30,118);
            particles.add(new Particle(px,py,
                    (float)Math.cos(angle)*speed,(float)Math.sin(angle)*speed,
                    rnd(.18f,.38f),rnd(2.5f,5f),i%3==0?WHITE:0xFFBE83FF));
        }
    }
    private void takeHit(int damage){
        if(invulnerable>0||shieldTime>0||state!=PLAY)return;
        int absorbed=Math.min(shieldLevel,damage);
        damage=Math.max(0,damage-absorbed);
        if(damage==0){invulnerable=.45f;return;}
        hp-=damage;invulnerable=2.0f;flashTime=.3f;
        explode(x,y,CYAN,13);sound(3);vibrate(90);
        if(hp<=0){hp=0;state=DEAD;save();}
    }
    private void enemyKilled(int i,Enemy e){
        explode(e.x,e.y,e.kind==5?0xFFC391FF:RED,e.kind>=3?19:9);
        addScore(e.kind>=3?110:40);
        if(tutorialActive && (tutorialStep==TutorialRules.TARGET
                || tutorialStep==TutorialRules.PLASMA_TARGET
                || tutorialStep==TutorialRules.FIRE_SECONDARY)){
            enemies.remove(i);
            if(tutorialStep==TutorialRules.TARGET)
                tutorialAction(TutorialRules.ACTION_TARGET_KILLED);
            else if(tutorialStep==TutorialRules.PLASMA_TARGET)
                tutorialAction(TutorialRules.ACTION_PLASMA_TARGET_KILLED);
            else if(tutorialRocketFired)
                tutorialAction(TutorialRules.ACTION_SECONDARY_TARGET_KILLED);
            return;
        }
        starBurst(e.x,e.y,e.value,3);
        if(random.nextFloat()<.16f)spawnPickup(e.x,e.y,1+random.nextInt(4),1);
        if(random.nextFloat()<.09f)spawnPickup(e.x,e.y,5,1);
        secondaryRegen+=1.1f;
        enemies.remove(i);
    }
    private void firePrimary(){
        float damage=(13+damageLevel*2.7f)*(plasmaTime>0?1.55f:1f);
        float angle=findAimAngle();
        int kind=plasmaTime>0?3:0;
        spawnFriendly(x,y-25,angle,damage,kind);
        if(tripleTime>0){spawnFriendly(x-13,y-14,angle-.17f,damage*.67f,kind);
            spawnFriendly(x+13,y-14,angle+.17f,damage*.67f,kind);}
        if(random.nextInt(3)==0)sound(0);
    }
    private void spawnFriendly(float sx,float sy,float angle,float damage,int kind){
        if(shots.size()>175)return;
        float speed=kind==3?510:420;
        shots.add(new Shot(sx,sy,(float)Math.sin(angle)*speed,
                -(float)Math.cos(angle)*speed,damage,kind,false));
    }
    /** Small center-biased aim correction; explicitly permits targeting individual boss nodes. */
    private float findAimAngle(){
        float chosen=0,bestCost=Float.MAX_VALUE;
        if(bossActive && boss!=null && bossSequence==GameRules.BOSS_COMBAT){
            for(Node n:boss.nodes){
                if(n.hp<=0)continue;
                float dy=y-n.y,dx=n.x-x;
                if(!GameRules.canAim(dx,dy))continue;
                float a=(float)Math.atan2(dx,dy);
                float cost=Math.abs(a)*220+dy*.12f;
                if(cost<bestCost){chosen=a;bestCost=cost;}
            }
        }
        for(Enemy e:enemies){
            float dy=y-e.y,dx=e.x-x;
            if(!GameRules.canAim(dx,dy))continue;
            float a=(float)Math.atan2(dx,dy);
            float cost=Math.abs(a)*230+dy*.13f;
            if(cost<bestCost){chosen=a;bestCost=cost;}
        }
        return chosen;
    }
    private float[] nearestTarget(float sx,float sy){
        float[] ret={sx,sy-240};float dmin=Float.MAX_VALUE;
        for(Enemy e:enemies){float d=dist2(sx,sy,e.x,e.y);if(d<dmin){dmin=d;ret[0]=e.x;ret[1]=e.y;}}
        if(bossActive && boss!=null && bossSequence==GameRules.BOSS_COMBAT)for(Node n:boss.nodes){
            if(n.hp<=0)continue;float d=dist2(sx,sy,n.x,n.y);
            if(d<dmin){dmin=d;ret[0]=n.x;ret[1]=n.y;}
        }
        return ret;
    }
    private boolean useSecondary(int which){
        if(state!=PLAY||which<0||which>1||secCharges[which]<=0)return false;
        if(!TutorialRules.allowSecondaryTap(guideKind,which,guideId))return false;
        secCharges[which]--;
        if(which==0){
            for(int k=-1;k<=1;k++){
                float[] near=nearestTarget(x,y);
                float ang=(float)Math.atan2(near[0]-x, y-near[1])+k*.18f;
                spawnFriendly(x+k*12,y-15,ang,40+damageLevel*4,2);
            }
            banner="SCHWARMRAKETEN";bannerTime=.65f;
        } else {
            for(int i=shots.size()-1;i>=0;i--){
                Shot sh=shots.get(i);
                if(sh.hostile&&dist2(sh.x,sh.y,x,y)<200*200){explode(sh.x,sh.y,CYAN,3);shots.remove(i);}
            }
            for(int i=enemies.size()-1;i>=0;i--){
                Enemy e=enemies.get(i);
                if(dist2(e.x,e.y,x,y)<205*205){e.hp-=55+damageLevel*6;
                    if(e.hp<=0)enemyKilled(i,e);}
            }
            if(bossActive&&boss!=null&&bossSequence==GameRules.BOSS_COMBAT){
                boss.fireTimer+=.65f;
                for(Node n:boss.nodes)if(n.hp>0&&dist2(n.x,n.y,x,y)<210*210){
                    n.hp-=20;boss.hp-=20;nodeDamage(n);
                }
                if(boss.hp<=0)finishBoss();
            }
            explode(x,y,CYAN,32);flashTime=.15f;
            banner="EMP-IMPULS";bannerTime=.7f;
        }
        sound(2);vibrate(30);
        boolean prompted=guideKind==TutorialRules.GUIDE_SECONDARY;
        secondarySeen=true;
        if(prompted)dismissGuide();
        else save();
        if(tutorialActive && tutorialStep==TutorialRules.FIRE_SECONDARY && which==0)
            tutorialRocketFired=true;
        return true;
    }
    private void useAbility(int which){
        if(which<0||which>3||abilityCharges[which]<=0||state!=PLAY)return;
        if(!TutorialRules.allowAbilityTap(guideKind,which,guideId))return;
        abilityCharges[which]--;
        switch(which){
            case 0:shieldTime=Math.max(shieldTime,8f);break;
            case 1:rapidTime=Math.max(rapidTime,9f);break;
            case 2:tripleTime=Math.max(tripleTime,9f);break;
            case 3:plasmaTime=Math.max(plasmaTime,7f);break;
            default:break;
        }
        banner=ABILITY_NAMES[which]+" AKTIV";bannerTime=1.1f;
        explode(x,y,which==3?0xFFD095FF:CYAN,10);
        seenAbilityMask=TutorialRules.markAbilitySeen(seenAbilityMask,which);
        if(guideKind==TutorialRules.GUIDE_ABILITY)dismissGuide();
        else save();
        if(tutorialActive){
            if(tutorialStep==TutorialRules.ACTIVATE_SHIELD && which==0)
                tutorialAction(TutorialRules.ACTION_SHIELD_USED);
            else if(tutorialStep==TutorialRules.ACTIVATE_RAPID && which==1)
                tutorialAction(TutorialRules.ACTION_RAPID_USED);
            else if(tutorialStep==TutorialRules.ACTIVATE_TRIPLE && which==2)
                tutorialAction(TutorialRules.ACTION_TRIPLE_USED);
            else if(tutorialStep==TutorialRules.ACTIVATE_PLASMA && which==3)
                tutorialAction(TutorialRules.ACTION_PLASMA_USED);
        }
    }
    private void fireEnemy(float ex,float ey,float tx,float ty,int type){
        float dx=tx-ex,dy=ty-ey;
        float len=(float)Math.sqrt(dx*dx+dy*dy);
        if(len<.01f)len=1;
        int active=0;
        for(Shot q:shots)if(q.hostile)active++;
        if(active>=GameRules.hostileShotCap(stage,stageTime,bossActive))return;
        float speed=type==1?123:(98+stage*13);
        shots.add(new Shot(ex,ey,dx/len*speed,dy/len*speed,1,type==1?4:1,true));
    }
    private void bossAttack(){
        if(boss==null)return;
        int active=0;for(Shot shot:shots)if(shot.hostile)active++;
        final int cap=GameRules.hostileShotCap(stage,stageTime,true);
        if(active>=cap){boss.fireTimer=.4f;return;}
        int live=0;
        for(Node n:boss.nodes)if(n.hp>0&&n.type==1)live++;
        if(live==0){boss.fireTimer=1.25f;return;}
        int amount=GameRules.bossVolleyCount(boss.phase,stage);
        float start=-.5f;
        for(int i=0;i<amount;i++){
            float a=start+i/(float)Math.max(1,amount-1);
            float base=(float)Math.atan2(x-210,y-225);
            float theta=base+a*.9f;
            if(active>=cap)break;
            shots.add(new Shot(210,245,(float)Math.sin(theta)*140,
                    (float)Math.cos(theta)*114,1,1,true));active++;
        }
        if(boss.phase>1&&world!=0&&stageTime>35f){
            for(int i=0;i<3;i++){
                float a=boss.time*1.4f+i*2.09f;
                if(active>=cap)break;
                shots.add(new Shot(210+(float)Math.cos(a)*82,
                        245+(float)Math.sin(a)*49,(float)Math.cos(a)*112,(float)Math.sin(a)*112,1,4,true));active++;
            }
        }
        boss.fireTimer=GameRules.bossFireInterval(boss.phase,stage)+rnd(0,.25f);
    }
    private static float clamp(float v,float a,float b){return Math.max(a,Math.min(v,b));}
    private static float dist2(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return dx*dx+dy*dy;}

    private void update(float dt){
        idleTime+=dt;
        // True simulation pause during a first-discovery lesson.
        if(state==PLAY&&guideKind!=TutorialRules.GUIDE_NONE)return;
        planetOffset+=dt*22;
        for(int i=0;i<starX.length;i++){
            starY[i]+=starV[i]*dt;
            if(starY[i]>H)resetBackgroundStar(i,false);
        }
        for(int i=particles.size()-1;i>=0;i--){
            Particle q=particles.get(i);q.life-=dt;
            if(q.life<=0){particles.remove(i);continue;}
            q.x+=q.vx*dt;q.y+=q.vy*dt;q.vx*=1f-dt*.5f;q.vy*=1f-dt*.5f;
        }
        if(flashTime>0)flashTime-=dt;
        if(bannerTime>0)bannerTime-=dt;
        if(state!=PLAY)return;
        // A discovered power-up pauses the entire combat simulation; only the
        // highlighted real HUD control (or explicit skip) may resume it.
        if(guideKind!=TutorialRules.GUIDE_NONE)return;
        if(tutorialActive) {
            if(tutorialStep!=TutorialRules.COMPLETE)updateTutorial(dt);
            return;
        }
        stageTime+=dt;
        x+=(targetX-x)*Math.min(1,dt*16);
        y+=(targetY-y)*Math.min(1,dt*16);
        x=clamp(x,24,W-24);y=clamp(y,120,FIELD_BOTTOM-22);
        if(invulnerable>0)invulnerable-=dt;
        if(shieldTime>0)shieldTime-=dt;
        if(rapidTime>0)rapidTime-=dt;
        if(tripleTime>0)tripleTime-=dt;
        if(plasmaTime>0)plasmaTime-=dt;
        shootTimer-=dt;
        if(shootTimer<=0){firePrimary();shootTimer=GameRules.fireInterval(fireLevel,rapidTime>0);}
        secondaryRegen+=dt*.19f;
        if(secondaryRegen>=9f){
            secondaryRegen-=9f;
            secCharges[0]=Math.min(3,secCharges[0]+1);
            secCharges[1]=Math.min(2,secCharges[1]+1);
        }
        if(droneLevel>0){
            droneShootTimer-=dt;
            if(droneShootTimer<=0){
                for(int k=0;k<Math.min(3,droneLevel);k++){
                    float dx=x+(k-(Math.min(3,droneLevel)-1)/2f)*35;
                    if(shots.size()<170)shots.add(new Shot(dx,y+7,0,-320,7+damageLevel*1.5f,0,false));
                }
                droneShootTimer=.8f;
            }
        }
        if(!bossActive){
            if(bossSequence==GameRules.BOSS_NORMAL
                    &&stageTime>=GameRules.bossArrivalSeconds(stage)){
                bossSequence=GameRules.BOSS_CLEARING;spawnTimer=Float.MAX_VALUE;
                banner="SEKTOR GESICHERT · RESTLICHE FEINDE BESEITIGEN";bannerTime=2f;
            }
            if(bossSequence==GameRules.BOSS_NORMAL){
                spawnTimer-=dt;
                if(spawnTimer<=0){spawnEnemy();spawnTimer=GameRules.enemySpawnInterval(stage,stageTime)*rnd(.90f,1.12f);}
                starTimer-=dt;
                if(starTimer<=0){spawnPickup(rnd(30,W-30),-15,0,random.nextBoolean()?11:14);starTimer=rnd(6.7f,9.5f);}
            } else if(bossSequence==GameRules.BOSS_CLEARING && enemies.isEmpty()){
                beginBossWarning();
            } else if(bossSequence==GameRules.BOSS_WARNING){
                bossSequenceTimer-=dt;
                if(bossSequenceTimer<=0)spawnBoss();
            }
        } else if(boss!=null){
            boss.time+=dt;
            if(bossSequence==GameRules.BOSS_ENTERING){
                bossSequenceTimer-=dt;
                positionBossNodes();
                if(bossSequenceTimer<=0){
                    bossSequence=GameRules.BOSS_COMBAT;
                    bossSequenceTimer=0;
                    invulnerable=Math.max(invulnerable,1.3f);
                    boss.fireTimer=1.5f;
                    banner="BOSS · "+BOSS_NAMES[world];bannerTime=1.5f;
                    sound(2);
                }
            } else if(bossSequence==GameRules.BOSS_COMBAT){
                boss.fireTimer-=dt;
                if(boss.fireTimer<=0)bossAttack();
                float ratio=boss.hp/boss.maxHp;
                int phase=ratio>.65f?1:ratio>.30f?2:3;
                if(phase>boss.phase){boss.phase=phase;boss.phaseTransition=1.2f;
                    flashTime=.30f;banner="BOSS · PHASE "+phase;bannerTime=1.7f;sound(2);}
                if(boss.phaseTransition>0)boss.phaseTransition-=dt;
                positionBossNodes();
                for(Node n:boss.nodes){
                    if(n.hp>0&&dist2(n.x,n.y,x,y)<(n.id==0?31*31:25*25))takeHit(1);
                }
            }
        }
        updateEnemies(dt);
        updateShots(dt);
        updatePickups(dt);
    }
    private void positionBossNodes(){
        if(boss==null)return;
        for(Node n:boss.nodes){
            int i=n.id;float t=boss.time;
            switch(boss.type){
                case 0:
                    n.x=210+(float)Math.sin(t*1.2-i*.58)*(68+boss.phase*10);
                    n.y=155+i*28+(float)Math.sin(t*1.4-i*.42)*7;
                    break;
                case 1:
                    float f=(float)(i*2*Math.PI/Math.max(1,boss.nodes.size()-1));
                    n.x=i==0?210:210+(float)Math.cos(f+t*.27)*115;
                    n.y=i==0?240:242+(float)Math.sin(f+t*.27)*87;
                    break;
                case 2:
                    float a=i*2.399f+t*.53f;
                    n.x=210+(float)Math.cos(a)*(48+i*9);
                    n.y=254+(float)Math.sin(a)*(35+i*7);
                    break;
                case 3:
                    n.x=210+(float)Math.sin(t*.88+i*.74)*(85+(i%3)*12);
                    n.y=176+i*27+(float)Math.cos(t*.9+i)*11;
                    break;
                default:
                    float ang=i*6.283f/boss.nodes.size()+t*.35f;
                    n.x=210+(float)Math.cos(ang)* (i%2==0?96:127);
                    n.y=255+(float)Math.sin(ang)* (i%2==0?76:104);
                    break;
            }
        }
        if(bossSequence==GameRules.BOSS_ENTERING){
            float fraction=clamp(1f-bossSequenceTimer/GameRules.BOSS_ENTRY_SECONDS,0f,1f);
            float ease=fraction*fraction*(3f-2f*fraction);
            float bottom=-10000f;
            for(Node n:boss.nodes)bottom=Math.max(bottom,n.y);
            // At t=0 even the lowest segment is above the playfield.
            float offset=-(bottom+75f)*(1f-ease);
            for(Node n:boss.nodes)n.y+=offset;
        }
    }
    private void updateEnemies(float dt){
        for(int i=enemies.size()-1;i>=0;i--){
            Enemy e=enemies.get(i);e.t+=dt;e.y+=e.vy*dt;
            switch(e.kind){
                case 0:e.x+=(float)Math.sin(e.t*2.6+e.phase)*33*dt;break;
                case 1:e.x+=(float)Math.cos(e.t*3.2+e.phase)*68*dt;break;
                case 2:
                    if(e.y>105)e.x+=clamp(x-e.x,-130,130)*dt*.70f;
                    e.y+=dt*12;break;
                case 3:e.x+=(float)Math.sin(e.t*1.25+e.phase)*22*dt;break;
                case 4:e.x+=(float)Math.sin(e.t*2+e.phase)*100*dt;break;
                case 5:e.x+=(float)Math.sin(e.t*1.7+e.phase)*45*dt;break;
                case 6:e.x+=(float)Math.sin(e.t*3.6+e.phase)*115*dt;break;
                default:break;
            }
            e.x=clamp(e.x,24,W-24);
            e.shoot-=dt;
            if(GameRules.enemyFireAllowed(stage,stageTime) && e.y>35 && e.y<FIELD_BOTTOM-85 && e.shoot<=0 && e.kind!=6){
                fireEnemy(e.x,e.y,x,y,e.kind==5?1:0);
                e.shoot=GameRules.enemyShotInterval(stage,random.nextFloat())*(e.kind>=3?.88f:1f);
            }
            if(e.kind==3 && e.t>3.2 && (int)(e.t*2)%7==0 && random.nextFloat()<dt*.5f){
                Enemy child=new Enemy(0,e.x+rnd(-15,15),e.y+14,24+stage*8);
                child.vy=82;if(enemies.size()<GameRules.enemyCap(stage,stageTime))enemies.add(child);
            }
            if(e.y>FIELD_BOTTOM+20){enemies.remove(i);continue;}
            if(dist2(e.x,e.y,x,y)<(e.radius+12)*(e.radius+12)){
                takeHit(1);e.hp-=28;
                if(e.hp<=0){enemyKilled(i,e);continue;}
            }
        }
    }
    private void updateShots(float dt){
        for(int i=shots.size()-1;i>=0;i--){
            Shot s=shots.get(i);s.age+=dt;
            if(s.kind==2 && !s.hostile){
                float[] target=nearestTarget(s.x,s.y);
                float dx=target[0]-s.x,dy=target[1]-s.y;
                float len=(float)Math.sqrt(dx*dx+dy*dy);
                if(len>1){s.vx+=(dx/len*310-s.vx)*Math.min(1,dt*5);
                    s.vy+=(dy/len*310-s.vy)*Math.min(1,dt*5);}
            }
            s.x+=s.vx*dt;s.y+=s.vy*dt;
            if(s.x<-45||s.x>W+45||s.y<-80||s.y>H+35||s.age>4){shots.remove(i);continue;}
            if(s.hostile){
                if(dist2(s.x,s.y,x,y)<(10+s.radius)*(10+s.radius)){
                    takeHit(1);shots.remove(i);
                }
                continue;
            }
            boolean consumed=false;
            if(bossActive&&boss!=null&&bossSequence==GameRules.BOSS_COMBAT){
                for(Node n:boss.nodes){
                    if(n.hp<=0)continue;
                    if(dist2(s.x,s.y,n.x,n.y)<(s.radius+17)*(s.radius+17)){
                        n.hp-=s.damage;boss.hp-=s.damage;
                        if(s.kind==3)plasmaImpact(s.x,s.y);
                        if(random.nextFloat()<.1f)explode(s.x,s.y,GOLD,2);
                        nodeDamage(n);
                        if(s.kind==3){s.damage*=.62f;
                            if(s.damage<4)consumed=true;
                        } else consumed=true;
                        if(boss.hp<=0){finishBoss();return;}
                        break;
                    }
                }
            }
            if(!consumed){
                for(int j=enemies.size()-1;j>=0;j--){
                    Enemy e=enemies.get(j);
                    if(dist2(s.x,s.y,e.x,e.y)<(e.radius+s.radius)*(e.radius+s.radius)){
                        e.hp-=s.damage;
                        if(s.kind==3)plasmaImpact(s.x,s.y);
                        if(e.hp<=0)enemyKilled(j,e);
                        else if(random.nextFloat()<.3f)explode(s.x,s.y,CYAN,3);
                        if(s.kind==3){s.damage*=.65f;if(s.damage<4)consumed=true;}
                        else consumed=true;
                        break;
                    }
                }
            }
            if(consumed)shots.remove(i);
        }
    }
    private void nodeDamage(Node n){
        if(n.hp>0||n.burst)return;
        n.burst=true;
        explode(n.x,n.y,n.type==3?GOLD:CYAN,20);
        addScore(70);
        if(n.type==2){
            starBurst(n.x,n.y,70,5);
            if(!boss.split&&boss.type==0){
                boss.split=true;
                Enemy fragment=new Enemy(6,n.x+45,n.y+20,160);
                fragment.fragments=4;fragment.vy=23;
                enemies.add(fragment);
                banner="KETTENWURM GETEILT";bannerTime=1.3f;
            }
        }else if(n.type==1){
            starBurst(n.x,n.y,60,4);
            if(random.nextFloat()<.45f)spawnPickup(n.x,n.y,5,1);
        }else if(n.type==3){
            starBurst(n.x,n.y,105,6);
            boss.hp-=35;
            for(Node neighbor:boss.nodes)if(neighbor!=n&&neighbor.hp>0
                    &&dist2(neighbor.x,neighbor.y,n.x,n.y)<95*95){neighbor.hp-=18;}
        }else if(n.type==4){starBurst(n.x,n.y,85,5);}
        if(boss!=null){
            boolean alive=false;
            for(Node part:boss.nodes)if(part.hp>0){alive=true;break;}
            if(!alive)boss.hp=0;
        }
    }
    private void updatePickups(float dt){
        float range=37+magnetLevel*27;
        for(int i=pickups.size()-1;i>=0;i--){
            Pickup q=pickups.get(i);
            q.y+=q.vy*dt;q.t+=dt;
            float d=(float)Math.sqrt(dist2(q.x,q.y,x,y));
            if(d<range+30 && d>1){
                q.x+=(x-q.x)*Math.min(1,dt*(120+magnetLevel*60)/d);
                q.y+=(y-q.y)*Math.min(1,dt*(120+magnetLevel*60)/d);
            }
            if(d<20){
                if(q.kind==0){
                    stars+=q.value;addScore(q.value);
                    if(tutorialActive && tutorialStep==TutorialRules.COLLECT_STAR)
                        tutorialAction(TutorialRules.ACTION_STAR_COLLECTED);
                }else if(q.kind==5){
                    int randomSlot=random.nextInt(4);
                    abilityCharges[randomSlot]=Math.min(3,abilityCharges[randomSlot]+1);
                    if(!tutorialActive && TutorialRules.canPromptAbility(seenAbilityMask,randomSlot))
                        beginGuide(TutorialRules.GUIDE_ABILITY,randomSlot);
                }else{
                    int slot=q.kind-1;
                    if(slot>=0&&slot<4){
                        abilityCharges[slot]=Math.min(3,abilityCharges[slot]+1);
                        if(!tutorialActive && TutorialRules.canPromptAbility(seenAbilityMask,slot))
                            beginGuide(TutorialRules.GUIDE_ABILITY,slot);
                    }
                }
                sound(0);pickups.remove(i);
                if(guideKind!=TutorialRules.GUIDE_NONE)break;
                continue;
            }
            if(q.y>FIELD_BOTTOM+38)pickups.remove(i);
        }
    }
    private void finishBoss(){
        if(state!=PLAY||boss==null||bossSequence!=GameRules.BOSS_COMBAT)return;
        addScore(1000*stage);
        int bonus=GameRules.bossReward(world);
        stars+=bonus;
        for(Node n:boss.nodes)if(n.hp>0)explode(n.x,n.y,GOLD,12);
        explode(210,245,CYAN,60);
        newlyUnlocked=(techMask&(1<<world))==0?world:-1;
        techMask|=1<<world;save();
        boss=null;bossActive=false;bossSequence=GameRules.BOSS_NORMAL;
        enemies.clear();shots.clear();pickups.clear();
        buildShop();state=SHOP;
        if(!shopIntroSeen)shopIntroShowing=true;
        shopMessage="BOSS BESIEGT · +"+bonus+" STERNE";
        sound(2);vibrate(100);
    }
    private void buildShop(){
        offers.clear();
        offers.add(new Offer(0,"SCHNELLFEUER +5,5%","Bis zu 5 Stufen",GameRules.nextFireCost(fireLevel)));
        offers.add(new Offer(1,"WAFFENSCHADEN +12%","Mehr Schaden pro Treffer",GameRules.nextDamageCost(damageLevel)));
        offers.add(new Offer(2,"REPARATUR +1 HP","Stellt einen Lebenspunkt wieder her",160));
        offers.add(new Offer(3,"STERNENMAGNET","Sammelradius erweitern",GameRules.nextMagnetCost(magnetLevel)));
        int special=(world+random.nextInt(4))%4;
        if(special==0|| (techMask&1)!=0)offers.add(new Offer(4,"BEGLEITDROHNE","Autonomes Zusatzgeschütz",GameRules.nextDroneCost(droneLevel)));
        else offers.add(new Offer(5,"SCHILDVERSTÄRKUNG","Absorbiert einen weiteren Treffer",GameRules.nextShieldCost(shieldLevel)));
        offers.add(new Offer(6,"SPEZIAL-LADUNGEN","Je eine Ladung für vier Fähigkeiten",390));
    }
    private int offerPrice(Offer o){
        switch(o.id){
            case 0:return GameRules.nextFireCost(fireLevel);
            case 1:return GameRules.nextDamageCost(damageLevel);
            case 2:return 160;
            case 3:return GameRules.nextMagnetCost(magnetLevel);
            case 4:return GameRules.nextDroneCost(droneLevel);
            case 5:return GameRules.nextShieldCost(shieldLevel);
            case 6:return 390;
            default:return o.base;
        }
    }
    private boolean offerAvailable(Offer o){
        switch(o.id){
            case 0:return fireLevel<5;
            case 1:return damageLevel<5;
            case 2:return hp<maxHp;
            case 3:return magnetLevel<3;
            case 4:return droneLevel<3;
            case 5:return shieldLevel<2;
            case 6:return true;
            default:return false;
        }
    }
    private void buyOffer(int index){
        if(index<0||index>=offers.size())return;
        Offer o=offers.get(index);
        int price=offerPrice(o);
        if(!offerAvailable(o)){shopMessage="AUSBAU BEREITS MAXIMAL";return;}
        if(stars<price){shopMessage="NICHT GENUG STERNE";return;}
        stars-=price;
        switch(o.id){
            case 0:fireLevel++;break;
            case 1:damageLevel++;break;
            case 2:hp=Math.min(maxHp,hp+1);break;
            case 3:magnetLevel++;break;
            case 4:droneLevel++;break;
            case 5:shieldLevel++;break;
            case 6:for(int a=0;a<4;a++)abilityCharges[a]=Math.min(3,abilityCharges[a]+1);break;
            default:break;
        }
        shopMessage=o.title+" GEKAUFT";sound(0);
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        long now=System.nanoTime();
        float dt=lastFrame==0?0:(float)((now-lastFrame)*1e-9);
        lastFrame=now;
        dt=clamp(dt,0,.042f);
        update(dt);
        c.save();c.scale(getWidth()/W,getHeight()/H);
        drawBackground(c);
        if(state==PLAY||state==PAUSE){
            drawWorld(c);drawEnemies(c);drawBoss(c);drawShots(c);drawPickups(c);drawParticles(c);
            drawPlayer(c);drawHud(c);
            drawBossTransition(c);
            if(tutorialActive || guideKind!=TutorialRules.GUIDE_NONE)drawTutorialOverlay(c);
            if(state==PAUSE)drawPause(c);
        }else{
            if(state==MENU)drawMenu(c);
            else if(state==SHOP){drawShop(c);if(shopIntroShowing)drawShopIntro(c);}
            else if(state==ROUTE)drawRoute(c);
            else if(state==DEAD)drawDead(c);
            else if(state==GAME_OVER)drawGameOver(c,false);
            else if(state==WIN)drawGameOver(c,true);
            else if(state==SETTINGS)drawSettings(c);
            else if(state==ARCHIVE)drawArchive(c);
            else if(state==10)drawLicenses(c);
        }
        c.restore();
        postInvalidateOnAnimation();
    }
    private void fill(Canvas c,int color){c.drawColor(color);}
    private void box(Canvas c,float left,float top,float width,float height,int color){
        p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);
        c.drawRect(left,top,left+width,top+height,p);
    }
    private void panel(Canvas c,float px,float py,float w,float h,int bg,int stroke,float r){
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(bg);
        c.drawRoundRect(px,py,px+w,py+h,r,r,p);
        if(stroke!=0){p.setColor(stroke);p.setStrokeWidth(1.2f);p.setStyle(Paint.Style.STROKE);
            c.drawRoundRect(px,py,px+w,py+h,r,r,p);p.setStyle(Paint.Style.FILL);}
    }
    private void line(Canvas c,float x1,float y1,float x2,float y2,int color,float width){
        p.setColor(color);p.setShader(null);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);
        p.setStyle(Paint.Style.STROKE);c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);
    }
    private void circle(Canvas c,float cx,float cy,float r,int color){
        if(r<=0)return;
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawCircle(cx,cy,r,p);
    }
    private void txt(Canvas c,String s,float px,float py,float size,int color,Paint.Align align,boolean bold){
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        p.setTextSize(size);p.setTextAlign(align);
        p.setTypeface(Typeface.create(bold?"sans-serif-condensed":"sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
        c.drawText(s,px,py,p);
    }
    private void textCenter(Canvas c,String s,float py,float size,int color,boolean bold){
        txt(c,s,W/2,py,size,color,Paint.Align.CENTER,bold);
    }
    private void triangle(Canvas c,float ax,float ay,float bx,float by,float cx,float cy,int color){
        Path path=new Path();path.moveTo(ax,ay);path.lineTo(bx,by);path.lineTo(cx,cy);path.close();
        p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawPath(path,p);
    }
    private void polygon(Canvas c,float[] pts,int color){
        Path path=new Path();path.moveTo(pts[0],pts[1]);
        for(int i=2;i<pts.length;i+=2)path.lineTo(pts[i],pts[i+1]);path.close();
        p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawPath(path,p);
    }
    private void drawBackground(Canvas c){
        final int w=(state==PLAY||state==PAUSE)?world:2;
        final int[][] tone={
            {0xFF050A17,0xFF101C35,0xFF213E4C},
            {0xFF090D19,0xFF1C223C,0xFF35415C},
            {0xFF0D0820,0xFF241936,0xFF4D2B68},
            {0xFF050D1B,0xFF142944,0xFF1F5578},
            {0xFF090A1C,0xFF1A2040,0xFF36314F}
        };
        p.setStyle(Paint.Style.FILL);p.setColor(WHITE);
        p.setShader(new LinearGradient(0,0,W,H,tone[w],null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,W,H,p);p.setShader(null);
        float scroll=planetOffset;
        float offset=(float)Math.sin(idleTime*.09f)*18f;
        int highlight=w==2?0x665C3C9F:w==3?0x6631C8FF:w==4?0x666D6CAD:0x5651B3CA;
        p.setShader(new RadialGradient(85+offset,222,270,new int[]{highlight,0x004D6789},null,Shader.TileMode.CLAMP));
        c.drawCircle(85+offset,222,270,p);p.setShader(null);
        p.setShader(new RadialGradient(360-offset,492,295,
                new int[]{w==2?0x666E3A9B:0x664468A0,0x00335A7A},null,Shader.TileMode.CLAMP));
        c.drawCircle(360-offset,492,295,p);p.setShader(null);
        // Distant dust wisps are semi-transparent, slow parallax bands.
        for(int i=0;i<7;i++){
            float yy=(i*171+scroll*.22f)%1120-210;
            float xx=52+(i*117)%330;
            p.setShader(new RadialGradient(xx,yy,47+i%3*17,
                    new int[]{i%2==0?0x1620DDE8:0x204E8CCB,0x001A3C72},
                    null,Shader.TileMode.CLAMP));
            c.drawOval(xx-105,yy-36,xx+105,yy+36,p);p.setShader(null);
        }
        for(int i=0;i<starX.length;i++){
            float r=i%13==0?2.3f:i%5==0?1.4f:.7f;
            int color=i%9==0?0xFFF8E8C9:i%7==0?0xFF96EEFF:0x99DAEFFF;
            if(i%13==0)glow(c,starX[i],starY[i],7,0x44BBE9FF,0x00BBE9FF);
            circle(c,starX[i],starY[i],r,color);
            if(i%15==0)line(c,starX[i],starY[i]-starV[i]*.11f,
                    starX[i],starY[i]+4,0x666EBBEF,1.4f);
        }
        // A rim-lit distant planet provides scale without obstructing bullets.
        float moonX=w==3?369:52;
        float moonY=(w==4?232:114)+(float)Math.sin(idleTime*.06f)*9f;
        float radius=w==4?71:48;
        glow(c,moonX,moonY,radius*2.2f,0x1836BBDD,0x0036BBDD);
        p.setShader(new RadialGradient(moonX-radius*.25f,moonY-radius*.25f,radius*1.45f,
                new int[]{0xFF536D93,0xFF0C1C32,0xFF040918},null,Shader.TileMode.CLAMP));
        c.drawCircle(moonX,moonY,radius,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.4f);
        p.setColor(0xAA6E9FC8);c.drawArc(moonX-radius,moonY-radius,moonX+radius,moonY+radius,192,144,false,p);
        p.setStyle(Paint.Style.FILL);
        if(flashTime>0)box(c,0,0,W,H,0x14FFFFFF);
    }
    private void drawWorld(Canvas c){
        float scroll=planetOffset;
        if(world==0){
            // Debris field and a shattered ship silhouette, each independently scrolling.
            for(int k=0;k<8;k++){
                float px=33+k*73+(float)Math.sin(k*8.2)*23;
                float py=(k*127+scroll*(.65f+k%3*.18f))%1010-140;
                float rr=11+(k%4)*10;
                asteroid(c,px,py,rr);
                if(k%2==0){
                    shadedPolygon(c,new float[]{px-rr*.8f,py+rr*.7f,px-rr*.2f,py-rr*.9f,
                            px+rr,py-rr*.3f,px+rr*.7f,py+rr*.6f},
                            0xFF697E8A,0xFF213447,0xFF426779);
                    line(c,px-rr*.35f,py-rr*.5f,px+rr*.8f,py+rr*.3f,0xAAB6DBE8,1.4f);
                }
            }
            for(int i=0;i<2;i++){
                float yy=(i*560+scroll*.42f)%1120-170;
                drawShipwreck(c,i==0?-27:346,yy, i==0?1f:-1f);
            }
        }else if(world==1){
            for(int k=0;k<7;k++){
                float py=(k*177+scroll*.9f)%1050-125;
                drawIndustrialFrame(c,k%2==0?-29:449,py,k%2==0?1:-1);
            }
        }else if(world==2){
            for(int k=0;k<10;k++){
                float xx=25+(k*111)%395;
                float yy=(k*135+scroll*.66f)%1060-140;
                drawCrystal(c,xx,yy,12+(k%3)*10,k%2==0);
            }
        }else if(world==3){
            for(int k=0;k<7;k++){
                float yy=(k*163+scroll*1.13f)%1030-130;
                float anchor=k%2==0?-24:444;
                drawStormSpire(c,anchor,yy,k%2==0?1:-1);
            }
        }else{
            for(int k=0;k<6;k++){
                float yy=(k*211+scroll*.6f)%1090-160;
                drawCitadel(c,k%2==0?-35:455,yy,k%2==0?1:-1);
            }
        }
    }
    private void drawShipwreck(Canvas c,float x,float y,float dir){
        c.save();c.translate(x,y);c.scale(dir,1);
        glow(c,28,12,88,0x2926B8DB,0x0026B8DB);
        shadedPolygon(c,new float[]{-18,-27,38,-56,116,-23,143,10,87,45,22,37},
                0xFF7A8C9A,0xFF1C3046,0xFF8AACBD);
        shadedPolygon(c,new float[]{8,-18,45,-38,94,-16,74,10,7,19},
                0xFF9CB2BB,0xFF3A5C73,0xFFB9D4DB);
        shadedPolygon(c,new float[]{70,-7,123,-21,142,11,79,31},
                0xFF27475E,0xFF122237,0xFF608297);
        for(int k=0;k<3;k++){
            line(c,27+k*24,-11,25+k*23,18,0xBB1D283B,3);
            circle(c,31+k*25,1,3,k==1?0xFFFF925E:0xFF59B3D3);
        }
        line(c,65,-33,88,-53,0xFFEAC0A2,2);
        c.restore();
    }
    private void drawIndustrialFrame(Canvas c,float x,float y,int direction){
        c.save();c.translate(x,y);c.scale(direction,1);
        shadedPolygon(c,new float[]{0,-36,74,-53,101,-31,102,54,68,66,0,46},
                0xFF8D9CA9,0xFF233C50,0xFF7DA7BD);
        shadedPolygon(c,new float[]{28,-27,72,-36,87,-20,85,31,56,45,21,21},
                0xFF2B5E70,0xFF0B263D,0xFF72DCE1);
        for(int k=0;k<3;k++){
            line(c,9,-24+k*26,81,-17+k*26,0xFF7C9AAB,3);
            glow(c,67,-24+k*24,14,0x705CE7DC,0x005CE7DC);
            circle(c,67,-24+k*24,2,0xFF9EF8E7);
        }
        c.restore();
    }
    private void drawCrystal(Canvas c,float x,float y,float r,boolean cyanCrystal){
        int bright=cyanCrystal?0xFF8DEBF2:0xFFC5A5FF;
        int dark=cyanCrystal?0xFF20647E:0xFF52357A;
        glow(c,x,y,r*2.7f,cyanCrystal?0x443AE6EB:0x557D4DD6,0x00000000);
        shadedPolygon(c,new float[]{x,y-r*1.6f,x-r*.65f,y-r*.2f,x-r*.3f,y+r,
                        x+r*.55f,y+r*.8f,x+r*.7f,y-r*.3f},
                bright,dark,0xAAE2DDF8);
        triangle(c,x,y-r*1.55f,x+r*.65f,y-r*.3f,x,y+r*.8f,0x6678BCDA);
        line(c,x,y-r*1.5f,x-r*.3f,y+r*.9f,0x99FFFFFF,1.1f);
    }
    private void drawStormSpire(Canvas c,float x,float y,int dir){
        c.save();c.translate(x,y);c.scale(dir,1);
        shadedPolygon(c,new float[]{0,-42,35,-74,66,-52,103,9,78,62,15,42},
                0xFF6B95B4,0xFF152D4B,0xFF8ED8E9);
        glow(c,60,-11,56,0x4444AFFF,0x0044AFFF);
        for(int i=0;i<3;i++){
            float yy=-29+i*29;
            line(c,25,yy,65,yy+11,0xFF9DEAFF,1.8f);
        }
        line(c,65,-29,92,-8,0xBB8BDFFF,2);
        line(c,92,-8,78,15,0xBB8BDFFF,2);
        c.restore();
    }
    private void drawCitadel(Canvas c,float x,float y,int dir){
        c.save();c.translate(x,y);c.scale(dir,1);
        glow(c,60,13,75,0x333BA2D9,0x003BA2D9);
        shadedPolygon(c,new float[]{0,-43,65,-65,111,-31,121,60,62,87,0,51},
                0xFF8C86B7,0xFF22233F,0xFF9F9FC7);
        shadedPolygon(c,new float[]{20,-32,65,-47,94,-20,100,43,62,62,20,37},
                0xFF465B86,0xFF161A38,0xFF6B71B0);
        for(int k=0;k<5;k++){
            float yy=-19+k*15;
            line(c,33,yy,88,yy+6,0xFF998ED1,2);
        }
        glow(c,64,10,27,0x6645D7FD,0x0045D7FD);
        circle(c,64,10,4,0xFF8CFFF5);
        c.restore();
    }
    private void asteroid(Canvas c,float sx,float sy,float r){
        glow(c,sx,sy+r*.45f,r*1.45f,0x22000000,0x00000000);
        shadedPolygon(c,new float[]{sx-r,sy-r*.4f,sx-r*.54f,sy-r,sx+r*.42f,sy-r*.78f,
                sx+r,sy-r*.05f,sx+r*.6f,sy+r*.84f,sx-r*.6f,sy+r},
                0xFF8293A7,0xFF24354B,0xFF64768D);
        shadedPolygon(c,new float[]{sx-r*.54f,sy-r,sx+r*.42f,sy-r*.78f,
                sx+r*.1f,sy-r*.1f,sx-r*.6f,sy+r},
                0xFF8497A7,0xFF43586D,0);
        shadedPolygon(c,new float[]{sx+r*.42f,sy-r*.78f,sx+r,sy-r*.05f,
                sx+r*.6f,sy+r*.84f,sx+r*.1f,sy-r*.1f},
                0xFF566C83,0xFF273B57,0);
        line(c,sx-r*.3f,sy-r*.18f,sx+r*.15f,sy+r*.05f,0x775D748B,1.6f);
        glow(c,sx-r*.28f,sy-r*.33f,r*.2f,0x3A88BCD8,0x0088BCD8);
    }
    /**
     * Procedurally shaded pseudo-isometric craft. Every shape is project-owned;
     * the project does not ship third-party textures, models or sample images.
     */
    private void shadedPolygon(Canvas c,float[] pts,int lit,int dark,int edge){
        Path path=new Path();path.moveTo(pts[0],pts[1]);
        float minY=pts[1],maxY=pts[1];
        for(int k=2;k<pts.length;k+=2){
            path.lineTo(pts[k],pts[k+1]);
            minY=Math.min(minY,pts[k+1]);maxY=Math.max(maxY,pts[k+1]);
        }
        path.close();
        p.setStyle(Paint.Style.FILL);
        p.setColor(WHITE);
        p.setShader(new LinearGradient(0,minY,0,maxY+1,lit,dark,Shader.TileMode.CLAMP));
        c.drawPath(path,p);p.setShader(null);
        if(edge!=0){
            p.setStyle(Paint.Style.STROKE);p.setColor(edge);p.setStrokeWidth(1f);
            c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
        }
    }
    private void glow(Canvas c,float gx,float gy,float radius,int inner,int outer){
        p.setShader(new RadialGradient(gx,gy,radius,new int[]{inner,outer},null,Shader.TileMode.CLAMP));
        p.setColor(WHITE);p.setStyle(Paint.Style.FILL);
        c.drawCircle(gx,gy,radius,p);p.setShader(null);
    }
    private void drawPlayer(Canvas c){
        c.save();c.translate(x,y);
        float lateral=clamp((targetX-x)*.12f,-7,7);
        c.rotate(lateral);
        float inv=invulnerable>0?(.5f+.5f*(float)Math.sin(idleTime*7)):0;
        glow(c,0,24,46,0x40000000,0x00000000);
        if(plasmaTime>0){
            float pulse=(float)Math.sin(idleTime*12f);
            glow(c,0,-1,53+4*pulse,0xA6A354F5,0x00190A39);
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setColor(0xE2D5A0FF);p.setStrokeWidth(2.4f);
            c.drawCircle(0,-2,35+2*pulse,p);
            p.setStyle(Paint.Style.FILL);
        }
        if(invulnerable>0){
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);
            p.setColor(((Math.round(100+100*inv))<<24)|0x004DE5FF);
            c.drawCircle(0,-2,31,p);p.setStyle(Paint.Style.FILL);
        }
        if(shieldTime>0){
            glow(c,0,-4,39,0x335DE8FF,0x005DE8FF);
            p.setStyle(Paint.Style.STROKE);p.setColor(0xFF72EAFF);p.setStrokeWidth(2);
            c.drawOval(-34,-37,34,31,p);p.setStyle(Paint.Style.FILL);
        }
        float flame=15+7*(float)Math.sin(idleTime*23);
        glow(c,-9,23,24,0x6648C8FF,0x0048C8FF);
        glow(c,9,23,24,0x66FFB55F,0x00FFB55F);
        triangle(c,-15,18,-4,18,-9,20+flame,0xFF2F92D4);
        triangle(c,4,18,15,18,9,20+flame,0xFFFAB367);
        triangle(c,-12,19,-7,19,-9,18+flame*.75f,WHITE);
        triangle(c,7,19,12,19,9,18+flame*.75f,WHITE);
        // Detailed 192px transparent sprite, with independently animated thrusters
        // and protective shield painted at runtime.
        atlas.drawPlayer(c,0,-2);
        if(plasmaTime>0){
            glow(c,0,-10,21,0xDDB578FF,0x00751CFF);
            circle(c,0,-10,6,0xFFDCC0FF);
            circle(c,0,-10,2.7f,WHITE);
        }
        if(droneLevel>0){
            for(int i=0;i<Math.min(3,droneLevel);i++){
                float dx=(i-(Math.min(3,droneLevel)-1)/2f)*43;
                float dy=31+(float)Math.sin(idleTime*3+i)*4;
                glow(c,dx,dy,16,0x5068E6FF,0x0068E6FF);
                shadedPolygon(c,new float[]{dx,dy-11,dx-13,dy+5,dx,dy+1,dx+13,dy+5},
                        0xFF9EDBE9,0xFF255675,0xFF67D6F2);
                circle(c,dx,dy,2.8f,GOLD);
            }
        }
        c.restore();
    }
    private void drawEnemies(Canvas c){
        for(Enemy e:enemies){
            c.save();c.translate(e.x,e.y);
            float wobble=(float)Math.sin(e.t*4+e.phase);
            c.rotate(wobble*3);
            int core=e.kind==5?0xFFA4A2FF:e.kind==3?0xFFFFC080:0xFFFF839C;
            glow(c,0,17,e.radius+17,0x2A000000,0x00000000);
            if(e.kind==6){
                for(int j=0;j<4;j++){
                    float xx=(float)Math.sin(e.t*4+j*.8f)*10;
                    float yy=j*13;
                    glow(c,xx,yy,16,0x6049A4E2,0x0049A4E2);
                    c.save();c.translate(xx,yy);
                    atlas.drawModule(c,2,16f);
                    c.restore();
                }
            }else {
                atlas.drawEnemy(c,atlas.enemies[Math.min(5,Math.max(0,e.kind))],0,0,e.radius);
            }
            if(e.hp<e.maxHp){
                box(c,-17,-35,34,3,0xDD111B30);
                box(c,-17,-35,34*clamp((float)e.hp/e.maxHp,0,1),3,GOLD);
            }
            c.restore();
        }
    }
    private void drawBoss(Canvas c){
        if(boss==null)return;
        for(int i=1;i<boss.nodes.size();i++){
            Node n=boss.nodes.get(i), prev=boss.nodes.get(i-1);
            if(n.hp<=0||prev.hp<=0)continue;
            if(boss.type==0||boss.type==3 || i<=4){
                line(c,prev.x,prev.y,n.x,n.y,0xFF3F6376,boss.type==0?16:9);
                line(c,prev.x,prev.y,n.x,n.y,0xAA7BC3CF,2);
            }
        }
        for(Node n:boss.nodes){
            if(n.hp<=0)continue;
            c.save();c.translate(n.x,n.y);
            float r=n.id==0?27:21;
            circle(c,0,7,r+4,0x55000000);
            // Individual hard-surface sprite per modular component.
            atlas.drawModule(c,n.type,r*1.28f);
            if(n.id==0){
                triangle(c,-14,-6,0,-19,14,-6,0xFFBD768A);
                circle(c,0,1,10,RED);
                circle(c,0,1,5,WHITE);
            } else {
                int core=n.type==3?GOLD:n.type==2?GREEN:n.type==4?0xFFAE83FF:RED;
                glow(c,0,0,17,0x883DDBE7,0x003DDBE7);
                circle(c,0,0,10,0x884E1829);
                circle(c,0,0,n.type==3?8:6,core);
                circle(c,-2,-2,2,WHITE);
                if(n.type==1){
                    triangle(c,-6,-11,0,-22,6,-11,0xFFF19EAD);
                }else if(n.type==2){
                    line(c,-12,0,12,0,GREEN,3);
                }
            }
            c.restore();
            if(n.hp<n.maxHp && n.hp>0) {
                box(c,n.x-17,n.y+23,34,3,0xAA0E1626);
                box(c,n.x-17,n.y+23,34*n.hp/n.maxHp,3,GOLD);
            }
        }
    }
    private void drawShots(Canvas c){
        for(Shot s:shots){
            if(s.hostile){
                glow(c,s.x,s.y,s.radius+13,0x55FF667F,0x00FF667F);
                line(c,s.x-s.vx*.037f,s.y-s.vy*.037f,s.x,s.y,
                        0x99FF8B9B,Math.max(1.5f,s.radius*.43f));
                circle(c,s.x,s.y,s.radius,s.kind==4?0xFFFFAA76:0xFFFF7085);
                circle(c,s.x,s.y,Math.max(1.5f,s.radius*.36f),WHITE);
            } else if(s.kind==2){
                circle(c,s.x,s.y,11,0x4471E4FF);circle(c,s.x,s.y,6,GOLD);
                line(c,s.x,s.y+7,s.x-s.vx*.05f,s.y-s.vy*.05f,0xFFED9F4B,3);
            } else if(s.kind==3){
                glow(c,s.x,s.y,21+(float)Math.sin(idleTime*18+s.age*12)*3,
                        0xB9A34DFF,0x001D063F);
                line(c,s.x,s.y+5,s.x-s.vx*.051f,s.y-s.vy*.051f,
                        0xA17F3CE8,10f);
                line(c,s.x,s.y+3,s.x-s.vx*.032f,s.y-s.vy*.032f,
                        0xFFE1AEFF,4.7f);
                circle(c,s.x,s.y,6.8f,0xFFD29BFF);
                circle(c,s.x,s.y,3.1f,WHITE);
            } else {
                line(c,s.x,s.y+13,s.x-s.vx*.015f,s.y-s.vy*.015f,CYAN,2.7f);
                circle(c,s.x,s.y,2,WHITE);
            }
        }
    }
    private void drawPickups(Canvas c){
        for(Pickup q:pickups){
            float pulse=(float)Math.sin(q.t*9)*1.8f;
            int col=q.kind==0?GOLD:q.kind==1?CYAN:q.kind==2?GREEN:
                    q.kind==3?0xFFFFA676:q.kind==4?0xFFBE91FF:0xFFB8F6FB;
            glow(c,q.x,q.y,16+pulse,0x5572CCF9,0x0072CCF9);
            if(q.kind==0){
                float[] vertices=new float[20];
                for(int k=0;k<10;k++){
                    float a=(float)(-Math.PI/2+k*Math.PI/5);
                    float r=k%2==0?10:4.5f;
                    vertices[2*k]=q.x+(float)Math.cos(a)*r;
                    vertices[2*k+1]=q.y+(float)Math.sin(a)*r;
                }
                polygon(c,vertices,col);
            }else{
                circle(c,q.x,q.y,8,col);circle(c,q.x,q.y,3,WHITE);
            }
        }
    }
    private void drawParticles(Canvas c){
        for(Particle q:particles){
            int alpha=(int)(255*clamp(q.life/q.maxLife,0,1));
            circle(c,q.x,q.y,q.radius*clamp(q.life/q.maxLife,.35f,1),
                    (q.color&0x00FFFFFF)|(alpha<<24));
        }
    }
    private void drawBossTransition(Canvas c){
        if(bossSequence==GameRules.BOSS_WARNING){
            boolean blink=(int)(idleTime*4)%2==0;
            box(c,0,0,W,FIELD_BOTTOM,blink?0x22FF284C:0x0A89172D);
            panel(c,15,250,390,141,0xEF240C1F,blink?0xFFFF506E:0xFF8D253D,12);
            line(c,15,250,405,250,0xFFFF728D,3);
            textCenter(c,blink?"!!  BOSS-WARNUNG  !!":"BOSS-WARNUNG",304,34,WHITE,true);
            textCenter(c,BOSS_NAMES[world],341,19,0xFFFFB4C1,true);
            textCenter(c,"ANFLUG IN "+Math.max(1,(int)Math.ceil(bossSequenceTimer))+" SEKUNDEN",373,15,WHITE,true);
        }else if(bossSequence==GameRules.BOSS_ENTERING){
            float progress=clamp(1f-bossSequenceTimer/GameRules.BOSS_ENTRY_SECONDS,0f,1f);
            panel(c,62,141,296,38,0xD7141F39,0x9983C0E9,8);
            textCenter(c,"GROSSKAMPFSCHIFF IM ANFLUG",166,15,WHITE,true);
            box(c,78,181,264,4,0xFF314057);
            box(c,78,181,264*progress,4,0xFFFF7995);
        }else if(bossSequence==GameRules.BOSS_CLEARING && enemies.size()<3){
            textCenter(c,"GEGNERWELLE ENDET",201,17,0xFFFFDFA2,true);
        }
    }
    private void drawHud(Canvas c){
        panel(c,7,7,406,87,0xDE0B1729,0x774DA6C6,12);
        txt(c,"SHATTERWAKE",18,28,16,WHITE,Paint.Align.LEFT,true);
        txt(c,"S"+stage+" · "+WORLD_NAMES[world],18,46,11,MUTED,Paint.Align.LEFT,false);
        txt(c,"★ "+stars,293,29,20,GOLD,Paint.Align.LEFT,true);
        txt(c,"PUNKTE "+score,216,49,10,MUTED,Paint.Align.LEFT,false);
        txt(c,"HÜLLE",18,75,11,MUTED,Paint.Align.LEFT,true);
        box(c,64,63,146,12,0xFF1D3451);
        box(c,64,63,146*(float)hp/maxHp,12,hp<=2?RED:GREEN);
        txt(c,hp+"/"+maxHp,211,74,11,WHITE,Paint.Align.LEFT,true);
        panel(c,365,51,36,31,0xFF203950,0xFF6992AA,7);
        textCenterAt(c,"Ⅱ",383,73,20,WHITE);
        if(boss!=null){
            panel(c,15,101,390,33,0xDD162239,0x77FA7A8B,7);
            txt(c,BOSS_NAMES[world],24,113,10,WHITE,Paint.Align.LEFT,true);
            box(c,24,117,370,9,0xFF392639);
            box(c,24,117,370*clamp(boss.hp/boss.maxHp,0,1),9,RED);
            txt(c,"PHASE "+boss.phase,394,113,9,WHITE,Paint.Align.RIGHT,true);
        }
        if(bannerTime>0){
            panel(c,35,boss==null?112:146,350,34,0xDD142C45,0x7768D6F6,8);
            textCenter(c,banner,boss==null?134:168,15,WHITE,true);
        }
        panel(c,7,650,406,124,0xE60A1728,0x883B7E9E,11);
        txt(c,"SEKUNDÄR",15,667,10,MUTED,Paint.Align.LEFT,true);
        txt(c,"RAKETE / EMP · EINMAL TIPPEN",15,684,10,WHITE,Paint.Align.LEFT,false);
        for(int i=0;i<2;i++){
            int xx=252+i*81;
            int border=inputMode==0&&armedSecondary==i?GOLD:0xFF416D89;
            panel(c,xx,658,75,44,0xFF18384E,border,8);
            if(i==0){
                triangle(c,xx+13,675,xx+22,660,xx+31,675,CYAN);
                line(c,xx+22,671,xx+22,686,GOLD,2);
            }else{
                glow(c,xx+22,679,16,0x668AE6FF,0x008AE6FF);
                circle(c,xx+22,679,5,CYAN);
                line(c,xx+10,676,xx+34,676,WHITE,1.5f);
            }
            txt(c,i==0?"RAKETE":"EMP",xx+51,675,10,WHITE,Paint.Align.CENTER,true);
            txt(c,""+secCharges[i]+"x",xx+51,693,14,secCharges[i]>0?GOLD:MUTED,Paint.Align.CENTER,true);
        }
        if(plasmaTime>0)textCenterAt(c,"PLASMA AKTIV",350,711,10,0xFFE0B6FF);
        for(int i=0;i<4;i++){
            int xx=13+i*100;
            boolean active=(i==0&&shieldTime>0)||(i==1&&rapidTime>0)||(i==2&&tripleTime>0)||(i==3&&plasmaTime>0);
            panel(c,xx,716,94,47,active?0xFF285976:0xFF182D45,
                    active?GOLD:0xFF42647D,8);
            drawPowerupIcon(c,i,xx+22,739,12);
            txt(c,ABILITY_NAMES[i],xx+59,735,i==2?10:11,WHITE,Paint.Align.CENTER,true);
            txt(c,""+abilityCharges[i]+"x",xx+59,754,13,abilityCharges[i]>0?GOLD:MUTED,Paint.Align.CENTER,true);
            float activeLeft=i==0?shieldTime:i==1?rapidTime:i==2?tripleTime:plasmaTime;
            float maxTime=i==0?8f:i==3?7f:9f;
            if(activeLeft>0){
                box(c,xx+5,760,84,2,0xFF14374F);
                box(c,xx+5,760,84*clamp(activeLeft/maxTime,0,1),2,CYAN);
            }
        }
    }
    private void textCenterAt(Canvas c,String s,float xx,float yy,float sz,int color){
        txt(c,s,xx,yy,sz,color,Paint.Align.CENTER,true);
    }
    private void button(Canvas c,String label,float px,float py,float w,float h,boolean active){
        panel(c,px,py,w,h,active?0xFF286888:0xFF25334B,
                active?CYAN:0xFF547082,12);
        txt(c,label,px+w/2,py+h/2+6,17,active?WHITE:MUTED,Paint.Align.CENTER,true);
    }
    private void logo(Canvas c,float py,float size){
        textCenter(c,"SHATTERWAKE",py,size,WHITE,true);
        line(c,48,py+10,372,py+10,CYAN,2);
        textCenter(c,"ZERLEGE  ·  ERBEUTE  ·  ÜBERLEBE",py+29,12,GOLD,false);
    }
    private void drawMenu(Canvas c){
        logo(c,172,43);
        c.save();c.translate(210,309);c.scale(1.8f,1.8f);
        c.translate(-x,-y);drawPlayer(c);
        c.restore();
        textCenter(c,"VERTIKALER SCI-FI ARCADE-SHOOTER",411,13,MUTED,false);
        button(c,"NEUER DURCHLAUF",51,450,318,58,true);
        button(c,"EINSTELLUNGEN",51,524,318,54,false);
        button(c,"TECHNOLOGIE-ARCHIV",51,593,318,54,false);
        button(c,"TUTORIAL "+(tutorialComplete?"WIEDERHOLEN":"STARTEN"),51,663,318,47,false);
        textCenter(c,"PROTOTYP 0.4 · OFFLINE · OHNE TRACKING",754,10,MUTED,false);
    }
    private void screenHeader(Canvas c,String title,String sub){
        panel(c,13,27,394,92,0xE013273E,0x7766B7D1,12);
        textCenter(c,title,65,29,WHITE,true);
        textCenter(c,sub,91,12,MUTED,false);
    }
    private void drawShopIntro(Canvas c){
        box(c,0,0,W,H,0xC7091322);
        panel(c,23,207,374,338,0xF70C253B,0xFF7BDEEB,15);
        textCenter(c,"DIE STERNENWERFT",264,28,GOLD,true);
        textCenter(c,"DAS IST DEIN SHOP NACH JEDEM BOSS",305,13,WHITE,true);
        textCenter(c,"STERNE SIND DIE WAEHRUNG",341,17,GOLD,true);
        textCenter(c,"UPGRADES GELTEN BIS ZUM GAME OVER",376,13,MUTED,false);
        textCenter(c,"PREISE STEIGEN MIT JEDER AUSBAUSTUFE",406,12,MUTED,false);
        textCenter(c,"TIPP: ZUERST WAFFEN ODER REPARATUR",437,12,WHITE,false);
        button(c,"VERSTANDEN",89,467,242,58,true);
    }
    private void drawShop(Canvas c){
        screenHeader(c,"STERNENWERFT","KAUF BLEIBT BIS ZUM GAME OVER AKTIV");
        txt(c,"★ "+stars,29,157,25,GOLD,Paint.Align.LEFT,true);
        txt(c,"ABSCHNITT "+stage+" BEZWUNGEN",393,155,11,WHITE,Paint.Align.RIGHT,true);
        if(newlyUnlocked>=0){
            panel(c,22,167,376,31,0xFF214934,0xFF5ECFA0,6);
            textCenter(c,"NEUE TECHNOLOGIE: "+BOSS_NAMES[newlyUnlocked],188,12,GREEN,true);
        }else{
            txt(c,shopMessage,210,188,11,GREEN,Paint.Align.CENTER,true);
        }
        for(int i=0;i<offers.size();i++){
            Offer o=offers.get(i);int yy=205+i*62;
            int price=offerPrice(o);
            boolean avail=offerAvailable(o),afford=stars>=price;
            panel(c,20,yy,380,55,0xE71C3047,
                    avail&&afford?0xFF5BA3BF:0xFF39566B,9);
            txt(c,o.title,34,yy+21,16,avail?WHITE:MUTED,Paint.Align.LEFT,true);
            txt(c,o.description,34,yy+40,11,MUTED,Paint.Align.LEFT,false);
            txt(c,avail?"★ "+price:"MAX",390,yy+31,16,avail&&afford?GOLD:MUTED,
                    Paint.Align.RIGHT,true);
        }
        txt(c,shopMessage,210,638,11,GREEN,Paint.Align.CENTER,false);
        button(c,stage==3?"RUN ABSCHLIESSEN":"WEITER ZUR ROUTENWAHL",35,673,350,62,true);
        textCenter(c,"Keine Pflichtkäufe · übrige Sterne bleiben erhalten",762,10,MUTED,false);
    }
    private void drawRoute(Canvas c){
        screenHeader(c,"SEKTOR-NAVIGATION","DEIN WEG VERÄNDERT WELTEN UND BOSSE");
        textCenter(c,"WÄHLE DEINEN NÄCHSTEN SEKTOR",178,19,GOLD,true);
        int[] dest=GameRules.routesAfter(stage);
        if(dest.length<2)return;
        for(int i=0;i<2;i++){
            int next=dest[i],yy=221+i*213;
            panel(c,23,yy,374,187,i==0?0xEA203A4D:0xEA332B50,
                    i==0?0xFF64B8D4:0xFFAA83ED,14);
            txt(c,"ROUTE "+(i+1),40,yy+26,12,MUTED,Paint.Align.LEFT,true);
            txt(c,WORLD_NAMES[next],40,yy+65,24,WHITE,Paint.Align.LEFT,true);
            txt(c,"BOSS: "+BOSS_NAMES[next],40,yy+91,14,GOLD,Paint.Align.LEFT,true);
            String detail=next==1?"Modulare Maschinenschiffe, Schutzfelder":
                    next==2?"Kristallgeschosse, optische Täuschungen":
                    next==3?"Sturmzonen und lebende Jägerschwärme":
                    "Taktische Drohnen und umbaubare Bosse";
            txt(c,detail,40,yy+117,12,MUTED,Paint.Align.LEFT,false);
            txt(c,"ANTIPPEN ZUM STARTEN",40,yy+162,14,CYAN,Paint.Align.LEFT,true);
            polygon(c,new float[]{358,yy+139,379,yy+154,358,yy+169},i==0?CYAN:0xFFAA83ED);
        }
        textCenter(c,"Jede Welt hat ihren eigenen Hauptboss.",700,13,MUTED,false);
    }
    private void drawDead(Canvas c){
        screenHeader(c,"SCHIFF ZERSTÖRT","DU HAST NOCH EINE CHANCE");
        textCenter(c,"GAME OVER?",274,40,RED,true);
        textCenter(c,"PUNKTE  "+score,336,23,WHITE,true);
        textCenter(c,"STERNE  ★ "+stars,373,20,GOLD,true);
        int price=GameRules.continueCost(stars);
        boolean can=continuesUsed<GameRules.MAX_CONTINUES;
        if(can){
            textCenter(c,"NEUSTART "+(continuesUsed+1)+" / "+GameRules.MAX_CONTINUES,435,16,MUTED,true);
            textCenter(c,"Kosten: Hälfte der verbliebenen Sterne (★ "+price+")",466,13,WHITE,false);
            button(c,"WEITERKÄMPFEN",41,509,338,66,true);
        }else{textCenter(c,"ALLE NEUSTARTS VERBRAUCHT",494,16,RED,true);}
        button(c,"DURCHLAUF BEENDEN",41,607,338,58,false);
    }
    private void drawGameOver(Canvas c,boolean victory){
        screenHeader(c,victory?"SEKTOREN GERETTET":"DURCHLAUF BEENDET",
                victory?"DU HAST DIE LETZTE WELT BEZWUNGEN":"DER NÄCHSTE VERSUCH WIRD ANDERS");
        textCenter(c,victory?"SIEG":"SHATTERWAKE",300,43,victory?GREEN:RED,true);
        textCenter(c,"PUNKTE: "+score,369,25,WHITE,true);
        textCenter(c,"BESTWERT: "+best,415,20,GOLD,true);
        textCenter(c,"FREIGESCHALTETE TECHNOLOGIEN: "+Integer.bitCount(techMask)+" / 5",473,13,MUTED,false);
        button(c,"NEUER DURCHLAUF",50,540,320,65,true);
        button(c,"HAUPTMENÜ",50,625,320,54,false);
    }
    private void drawPause(Canvas c){
        box(c,0,0,W,H,0xB9000B15);
        screenHeader(c,"PAUSE","SPIELSTAND DIESES DURCHLAUFS BLEIBT ERHALTEN");
        button(c,"WEITERSPIELEN",55,315,310,64,true);
        button(c,"HAUPTMENÜ",55,408,310,58,false);
    }
    private void drawSettings(Canvas c){
        screenHeader(c,"EINSTELLUNGEN","JEDERZEIT VOR DEM NÄCHSTEN RUN ÄNDERBAR");
        textCenter(c,"SEKUNDÄRWAFFEN-STEUERUNG",215,17,GOLD,true);
        button(c,"EINHAND-MODUS "+(inputMode==0?"✓":""),28,246,364,66,inputMode==0);
        textCenter(c,"Schiff ziehen; RAKETE / EMP direkt antippen",333,11,MUTED,false);
        button(c,"ZWEIFINGER-MODUS "+(inputMode==1?"✓":""),28,353,364,66,inputMode==1);
        textCenter(c,"Auch beim Steuern per zweitem Finger abfeuern",443,11,MUTED,false);
        button(c,"TON: "+(soundEnabled?"EIN":"AUS"),40,478,340,52,soundEnabled);
        button(c,"VIBRATION: "+(vibrationEnabled?"EIN":"AUS"),40,540,340,52,vibrationEnabled);
        button(c,"LIZENZEN & DATENSCHUTZ",40,612,340,51,false);
        button(c,"ZURÜCK",90,690,240,55,true);
    }
    private void drawArchive(Canvas c){
        screenHeader(c,"TECHNOLOGIE-ARCHIV","DAUERHAFTE FREISCHALTUNGEN STATT STAT-BOOSTS");
        String[] names={"KETTENDROHNE", "REAKTORSCHILD", "PRISMENPLASMA",
                "STURM-EMP", "ARCHITEKT-MODUL"};
        for(int i=0;i<names.length;i++){
            int yy=170+i*88;
            boolean unlocked=(techMask&(1<<i))!=0;
            panel(c,26,yy,368,75,unlocked?0xCC234638:0xCC1A2A41,
                    unlocked?GREEN:0xFF45617B,9);
            txt(c,(unlocked?"◆  ":"◇  ")+names[i],41,yy+27,18,unlocked?WHITE:MUTED,Paint.Align.LEFT,true);
            txt(c,unlocked?"FREIGESCHALTET · erscheint im Shop":"Boss besiegen: "+BOSS_NAMES[i],
                    42,yy+51,11,unlocked?GREEN:MUTED,Paint.Align.LEFT,false);
        }
        button(c,"ZURÜCK",90,688,240,55,true);
    }
    private void drawLicenses(Canvas c){
        screenHeader(c,"LIZENZEN & DATENSCHUTZ","RECHTLICHE INFORMATIONEN");
        String[] lines={
                "SHATTERWAKE 0.4.0-alpha",
                "Originaler Quellcode und Vektorgrafik.",
                "Keine fremden Spielgrafiken oder Fonts.",
                "Soundeffekte: algorithmisch selbst erzeugt.",
                "Keine externen Runtime-Abhängigkeiten.",
                "Keine Werbung, Analyse oder Netzverbindung.",
                "Lokal gespeicherte Spielstände und Optionen.",
                "Android-Framework: Systembestandteil.",
                "Buildtools: Gradle / Android Gradle Plugin.",
                "Vollständige Rechtshinweise im Repository:",
                "LICENSE · THIRD_PARTY_NOTICES.md",
                "docs/LICENSING_AND_RELEASE.md",
                "docs/PRIVACY.md"
        };
        for(int i=0;i<lines.length;i++)txt(c,lines[i],25,177+i*32,i==0?19:13,
                i==0?GOLD:i>=9?CYAN:WHITE,Paint.Align.LEFT,i==0);
        button(c,"ZURÜCK",90,690,240,55,true);
    }
    private String tutorialStepName(int step){
        String[] names={"STEUERN","ZIELEN","STERNE","SCHILD","SCHNELLFEUER",
                "DREIFACH","PLASMA AKTIVIEREN","PLASMA TESTEN","RAKETEN","ABSCHLUSS"};
        return names[Math.max(0,Math.min(step,names.length-1))];
    }
    private void drawTutorialOverlay(Canvas c){
        if(guideKind==TutorialRules.GUIDE_NONE && !tutorialActive)return;
        if(guideKind!=TutorialRules.GUIDE_NONE){
            box(c,0,0,W,650,0xC9071226);
            String title,description,action;
            boolean ability=guideKind==TutorialRules.GUIDE_ABILITY;
            if(ability){
                title=ABILITY_NAMES[Math.max(0,Math.min(3,guideId))];
                String[] descriptions={
                    "8 SEKUNDEN SCHUTZ VOR TREFFERN",
                    "9 SEKUNDEN SCHNELLER SCHIESSEN",
                    "9 SEKUNDEN DREIFACH-SCHUSS",
                    "7 SEKUNDEN VIOLETTE PLASMA-GESCHOSSE"
                };
                description=descriptions[Math.max(0,Math.min(3,guideId))];
                action="JETZT UNTEN AUF "+title+" TIPPEN";
            }else{
                title="SCHWARMRAKETEN";
                description="RAKETEN VERFOLGEN DEN TRAININGSGEGNER";
                action="JETZT UNTEN RECHTS RAKETE ANTIPPEN";
            }
            panel(c,22,205,376,282,0xFA0C2036,0xFF76D9FF,15);
            txt(c,tutorialActive?"TRAINING · SCHRITT "+(tutorialStep+1)+"/"+TutorialRules.TOTAL_STEPS:
                    "NEUE TECHNOLOGIE",210,240,14,GOLD,Paint.Align.CENTER,true);
            textCenter(c,title,290,29,WHITE,true);
            textCenter(c,description,330,13,MUTED,false);
            if(ability)drawPowerupIcon(c,guideId,210,375,39);
            else{
                glow(c,210,371,38,0x665DD9FF,0x005DD9FF);
                triangle(c,200,382,210,352,220,382,0xFF95DBF9);
                line(c,210,354,210,335,0xFFF9C46D,4f);
            }
            textCenter(c,action,447,12,GOLD,true);
            textCenter(c,"DAS SPIEL WARTET AUF DEINE AKTION",473,10,WHITE,false);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);
            p.setColor((int)(idleTime*4)%2==0?GOLD:CYAN);
            if(ability){
                int left=13+guideId*100;
                c.drawRoundRect(left,714,left+94,766,8,8,p);
            }else{
                c.drawRoundRect(250,656,329,706,8,8,p);
            }
            p.setStyle(Paint.Style.FILL);
            if(!tutorialActive)button(c,"UEBERSPRINGEN",114,552,192,46,false);
        }else if(tutorialActive){
            if(tutorialCheckpoint){
                box(c,0,0,W,650,0xC9071226);
                panel(c,25,212,370,296,0xF90C253B,0xFF70D7B4,15);
                textCenter(c,"SCHRITT "+(tutorialStep+1)+"/"+TutorialRules.TOTAL_STEPS+" GESCHAFFT",
                        259,24,GREEN,true);
                String[] feedback={
                    "DU BEHERRSCHST DIE SCHIFFSSTEUERUNG",
                    "DIE HAUPTWAFFE SCHIESST AUTOMATISCH",
                    "STERNE SIND DIE SHOP-WAEHRUNG",
                    "DER SCHILD SCHUETZT DICH",
                    "SCHNELLFEUER ERHOEHT DIE FEUERRATE",
                    "DREIFACH GIBT DIR MEHR GESCHOSSE",
                    "DU SIEHST JETZT DEN PLASMA-EFFEKT",
                    "PLASMA TRIFFT MIT VIOLETTEN IMPULSEN",
                    "DIE RAKETEN FINDEN IHR ZIEL SELBST"
                };
                textCenter(c,feedback[Math.min(tutorialStep,feedback.length-1)],315,13,WHITE,false);
                textCenter(c,"ALS NAECHSTES: "+tutorialStepName(tutorialNextStep),
                        365,15,GOLD,true);
                button(c,"WEITER",58,419,304,57,true);
            }else if(tutorialStep==TutorialRules.COMPLETE){
                box(c,0,0,W,650,0xC9071226);
                panel(c,26,216,368,306,0xF80D263B,0xFF7CE6C4,15);
                textCenter(c,"TRAINING ABGESCHLOSSEN",281,26,GREEN,true);
                textCenter(c,"ALLE "+TutorialRules.TOTAL_STEPS+" AUFGABEN GESCHAFFT",
                        321,15,WHITE,true);
                textCenter(c,"SCHIFF · WAFFEN · STERNE · PLASMA",362,13,GOLD,true);
                textCenter(c,"BEREIT FUER DEN ERSTEN SEKTOR?",396,13,WHITE,false);
                button(c,"SEKTOR 1 STARTEN",58,419,304,57,true);
            }else{
                if(tutorialStep==TutorialRules.TARGET
                        ||tutorialStep==TutorialRules.PLASMA_TARGET
                        ||tutorialStep==TutorialRules.FIRE_SECONDARY){
                    p.setShader(null);p.setStyle(Paint.Style.STROKE);
                    p.setColor(0xFFDFC27B);p.setStrokeWidth(2.3f);
                    for(Enemy e:enemies)
                        c.drawCircle(e.x,e.y,e.radius+13+
                                (float)Math.sin(idleTime*8)*2,p);
                    p.setStyle(Paint.Style.FILL);
                }
                String detail="";
                switch(tutorialStep){
                    case TutorialRules.MOVE:
                        detail="SCHIFF MIT DEM FINGER SEITLICH ZIEHEN";break;
                    case TutorialRules.TARGET:
                        detail="ZUM ZIEL FLIEGEN · DIE WAFFE FEUERT SELBST";break;
                    case TutorialRules.COLLECT_STAR:
                        detail="ZUM GELBEN STERN FLIEGEN UND IHN EINSAMMELN";break;
                    case TutorialRules.PLASMA_TARGET:
                        detail="ZUM ZIEL FLIEGEN · VIOLETTE TREFFER BEOBACHTEN";break;
                    case TutorialRules.FIRE_SECONDARY:
                        detail=tutorialRocketFired?"RAKETEN UNTERWEGS · WARTE AUF DEN TREFFER":
                                "RAKETE UNTEN RECHTS ANTIPPEN";break;
                    default:break;
                }
                panel(c,18,103,384,89,0xF5122944,0xAA67B5D8,11);
                textCenter(c,(tutorialStep+1)+"/"+TutorialRules.TOTAL_STEPS+" · "+
                        tutorialStepName(tutorialStep),137,20,GOLD,true);
                textCenter(c,detail,165,12,WHITE,false);
            }
        }
        if(tutorialActive){
            panel(c,297,18,109,39,0xDD26364C,0xFF6889A9,7);
            textCenterAt(c,"SKIP",351,44,13,WHITE);
        }
    }
    private void drawPowerupIcon(Canvas c,int which,float px,float py,float radius){
        c.save();c.translate(px,py);
        int color=which==0?0xFF73E8FF:which==1?0xFFFFBE67:
                which==2?0xFF93F6A0:0xFFC597FF;
        glow(c,0,0,radius*1.25f,(color&0x00FFFFFF)|0x55000000,
                color&0x00FFFFFF);
        switch(which){
            case 0:
                shadedPolygon(c,new float[]{0,-radius,-radius*.65f,-radius*.55f,
                        -radius*.52f,radius*.54f,0,radius,radius*.52f,radius*.54f,
                        radius*.65f,-radius*.55f},
                        0xFFE4FBFF,0xFF1E7895,color);
                break;
            case 1:
                polygon(c,new float[]{-radius*.22f,-radius,radius*.5f,-radius*.1f,
                        radius*.11f,radius*.02f,radius*.36f,radius,
                        -radius*.55f,radius*.13f,-radius*.1f,-radius*.12f},color);
                break;
            case 2:
                for(int i=-1;i<=1;i++){
                    line(c,i*radius*.38f,radius*.75f,i*radius*.38f,-radius*.72f,color,4);
                    circle(c,i*radius*.38f,-radius*.8f,5,WHITE);
                }
                break;
            default:
                glow(c,0,0,radius,0x99B06CFF,0x00B06CFF);
                circle(c,0,0,radius*.53f,color);
                circle(c,0,0,radius*.23f,WHITE);break;
        }
        c.restore();
    }
    private boolean inside(float px,float py,float l,float t,float w,float h){
        return px>=l&&px<=l+w&&py>=t&&py<=t+h;
    }
    private boolean tap(float px,float py){
        switch(state){
            case MENU:
                if(inside(px,py,51,450,318,58)){
                    if(!tutorialComplete)startTutorial();else startRun();
                    return true;
                }
                if(inside(px,py,51,524,318,54)){state=SETTINGS;return true;}
                if(inside(px,py,51,593,318,54)){state=ARCHIVE;return true;}
                if(inside(px,py,51,663,318,47)){startTutorial();return true;}
                return true;
            case PLAY:
                if(tutorialActive && inside(px,py,297,18,109,39)){finishTutorial();return true;}
                if(tutorialActive && tutorialCheckpoint){
                    if(inside(px,py,58,419,304,57))continueTutorial();
                    return true;
                }
                if(tutorialActive && tutorialStep==TutorialRules.COMPLETE){
                    if(inside(px,py,58,419,304,57))finishTutorial();
                    return true;
                }
                if(guideKind!=TutorialRules.GUIDE_NONE){
                    if(!tutorialActive && inside(px,py,114,552,192,46)){dismissGuide();return true;}
                    if(guideKind==TutorialRules.GUIDE_ABILITY){
                        if(py>=716&&px>=13&&px<413){
                            int a=(int)((px-13)/100);
                            if(a==guideId)useAbility(a);
                        }
                        return true;
                    }
                    if(py>=650&&py<707&&px>=252){
                        int slot=px>=333?1:0;
                        if(slot==guideId)useSecondary(slot);
                        return true;
                    }
                    return !(inputMode==0&&py<650);
                }
                if(inside(px,py,365,51,36,31)){state=PAUSE;return true;}
                if(py>=716){
                    int a=(int)((px-13)/100);
                    if(a>=0&&a<4 && px>=13){useAbility(a);return true;}
                }
                if(py>=650&&py<707){
                    if(px>=252){int slot=px>=333?1:0;
                        if(!secondarySeen){
                            beginGuide(TutorialRules.GUIDE_SECONDARY,slot);
                        }else useSecondary(slot);
                    }
                    return true;
                }
                return false;
            case PAUSE:
                if(inside(px,py,55,315,310,64)){state=PLAY;return true;}
                if(inside(px,py,55,408,310,58)){state=MENU;return true;}
                return true;
            case SHOP:
                if(shopIntroShowing){
                    if(inside(px,py,89,467,242,58)){
                        shopIntroShowing=false;shopIntroSeen=true;save();
                    }
                    return true;
                }
                for(int i=0;i<offers.size();i++)if(inside(px,py,20,205+i*62,380,55)){
                    buyOffer(i);return true;
                }
                if(inside(px,py,35,673,350,62)){
                    newlyUnlocked=-1;
                    if(stage>=3){state=WIN;save();}
                    else state=ROUTE;
                }
                return true;
            case ROUTE:
                int[] next=GameRules.routesAfter(stage);
                if(next.length==2){
                    if(inside(px,py,23,221,374,187)){startWorld(next[0]);return true;}
                    if(inside(px,py,23,434,374,187)){startWorld(next[1]);return true;}
                }
                return true;
            case DEAD:
                if(continuesUsed<GameRules.MAX_CONTINUES&&inside(px,py,41,509,338,66)){
                    stars-=GameRules.continueCost(stars);
                    continuesUsed++;hp=maxHp;shieldTime=4f;invulnerable=2f;
                    x=targetX=210;y=targetY=550;
                    enemies.clear();shots.clear();
                    state=PLAY;banner="NEUSTART "+continuesUsed+" / 2";bannerTime=2;
                    return true;
                }
                if(inside(px,py,41,607,338,58)){
                    state=GAME_OVER;save();return true;
                }
                return true;
            case GAME_OVER:
            case WIN:
                if(inside(px,py,50,540,320,65)){startRun();return true;}
                if(inside(px,py,50,625,320,54)){state=MENU;return true;}
                return true;
            case SETTINGS:
                if(inside(px,py,28,246,364,66)){inputMode=0;save();return true;}
                if(inside(px,py,28,353,364,66)){inputMode=1;save();return true;}
                if(inside(px,py,40,478,340,52)){soundEnabled=!soundEnabled;save();return true;}
                if(inside(px,py,40,540,340,52)){vibrationEnabled=!vibrationEnabled;save();return true;}
                if(inside(px,py,40,612,340,51)){state=10;return true;}
                if(inside(px,py,90,690,240,55)){state=MENU;return true;}
                return true;
            case ARCHIVE:
                if(inside(px,py,90,688,240,55))state=MENU;
                return true;
            case 10:
                if(inside(px,py,90,690,240,55))state=SETTINGS;
                return true;
            default:return true;
        }
    }
    @Override public boolean onTouchEvent(MotionEvent ev){
        int action=ev.getActionMasked(),idx=ev.getActionIndex();
        float sx=getWidth()>0?W/getWidth():1f;
        float sy=getHeight()>0?H/getHeight():1f;
        if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_POINTER_DOWN){
            float px=ev.getX(idx)*sx,py=ev.getY(idx)*sy;
            if(tap(px,py)){downWasButton=true;return true;}
            if(state==PLAY && touchPointer==-1 && py<650){
                touchPointer=ev.getPointerId(idx);
                touchOffsetX=x-px;touchOffsetY=y-py;
                targetX=clamp(px+touchOffsetX,22,W-22);
                targetY=clamp(py+touchOffsetY,121,FIELD_BOTTOM-22);
                downWasButton=false;
            }
            return true;
        }
        if(action==MotionEvent.ACTION_MOVE){
            if(state==PLAY && touchPointer>=0){
                int at=ev.findPointerIndex(touchPointer);
                if(at>=0){
                    targetX=clamp(ev.getX(at)*sx+touchOffsetX,24,W-24);
                    targetY=clamp(ev.getY(at)*sy+touchOffsetY,120,FIELD_BOTTOM-22);
                }
            }
            return true;
        }
        if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_POINTER_UP||action==MotionEvent.ACTION_CANCEL){
            if(ev.getPointerId(idx)==touchPointer){
                touchPointer=-1;downWasButton=false;
            }
            return true;
        }
        return true;
    }
}
