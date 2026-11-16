package de.shatterwake.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Runtime generated, alpha-transparent, reusable 192x192 game sprites.
 * All silhouettes, panel detail, highlights and glows are original project code.
 * No external artwork, font, model or remotely loaded resource is embedded.
 */
final class SpriteAtlas {
    private static final int S=384;
    private final Context context;
    final Bitmap fighter;
    final Bitmap renderedFighter;
    final Bitmap[] enemies=new Bitmap[7];
    final Bitmap[] modules=new Bitmap[5];
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    // BitmapFactory downsampling preserves realistic 3D surfaces while
    // avoiding hundreds of MiB of resident 2048px Werft sprite variants.
    private final LruCache<String,Bitmap> gameCache=new LruCache<String,Bitmap>(26*1024*1024){
        @Override protected int sizeOf(String key,Bitmap bitmap){return bitmap.getByteCount();}
    };
    private final LruCache<String,Bitmap> werftCache=new LruCache<String,Bitmap>(48*1024*1024){
        @Override protected int sizeOf(String key,Bitmap bitmap){return bitmap.getByteCount();}
    };
    private final float[] focusX=new float[8],focusY=new float[8];
    private final float[] muzzleX=new float[]{-18,18},muzzleY=new float[]{-25,-25};
    private final float[] engineX=new float[]{-11,11},engineY=new float[]{26,26};
    private boolean approvedAnchors=false;

    SpriteAtlas(Context ctx){
        context=ctx.getApplicationContext();
        for(int i=0;i<8;i++){
            focusX[i]=GameRules.werftFocusX(i);
            focusY[i]=GameRules.werftFocusY(i);
        }
        // Use the exact sprites staged from art/hero_fighter, never a
        // reconstructed or independently rotated Android-only drawing.
        renderedFighter=bitmapFor(false,"fighter_base.png");
        fighter=renderedFighter==null?makeFighter():null;
        readAnchors();
        for(int i=0;i<enemies.length;i++)enemies[i]=makeEnemy(i);
        for(int i=0;i<modules.length;i++)modules[i]=makeModule(i);
    }

    private Bitmap bitmapFor(boolean werft,String filename){
        String full=(werft?"rendered/werft/":"rendered/")+filename;
        LruCache<String,Bitmap> cache=werft?werftCache:gameCache;
        Bitmap result=cache.get(full);
        if(result!=null)return result;
        try(InputStream in=context.getAssets().open(full)){
            BitmapFactory.Options opt=new BitmapFactory.Options();
            opt.inScaled=false;
            opt.inPreferredConfig=Bitmap.Config.ARGB_8888;
            opt.inSampleSize=2; // GAME 512px, WERFT 1024px
            result=BitmapFactory.decodeStream(in,null,opt);
            if(result!=null)cache.put(full,result);
            return result;
        }catch(Exception ignored){return null;}
    }
    boolean hasRenderedFighter(){return renderedFighter!=null;}
    boolean hasWerftArt(){return bitmapFor(true,"fighter_base.png")!=null;}
    private void paintBitmap(Canvas c,Bitmap bitmap,RectF rect){
        p.setColor(Color.WHITE);p.setAlpha(255);p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        c.drawBitmap(bitmap,null,rect,p);
    }
    void drawHardware(Canvas c,int id,int level,boolean werft){
        if(id<0||id>=8)return;
        String name="fighter_module_"+id;
        if(level>1&&(id==3||id==7))name+="_L"+level;
        Bitmap bitmap=bitmapFor(werft,name+".png");
        if(bitmap!=null)paintBitmap(c,bitmap,new RectF(-50,-47,50,47));
    }
    void drawHullDamage(Canvas c,int damageTier,boolean werft){
        if(damageTier<1)return;
        Bitmap bitmap=bitmapFor(werft,"fighter_hull_damage_"+Math.min(3,damageTier)+".png");
        if(bitmap!=null)paintBitmap(c,bitmap,new RectF(-50,-47,50,47));
    }
    boolean drawWerftHangar(Canvas c,float left,float top,float right,float bottom){
        Bitmap bg=bitmapFor(true,"hangar_background.png");
        if(bg==null)return false;
        paintBitmap(c,bg,new RectF(left,top,right,bottom));
        return true;
    }
    float werftFocusX(int id){return id>=0&&id<8?focusX[id]:0f;}
    float werftFocusY(int id){return id>=0&&id<8?focusY[id]:0f;}
    float muzzleX(int side){return muzzleX[side<=0?0:1];}
    float muzzleY(int side){return muzzleY[side<=0?0:1];}
    float engineX(int side){return engineX[side<=0?0:1];}
    float engineY(int side){return engineY[side<=0?0:1];}
    private static float xCoord(JSONArray point,float width){
        return ((float)point.optDouble(0)/width-.5f)*100f;
    }
    private static float yCoord(JSONArray point,float height){
        return ((float)point.optDouble(1)/height-.5f)*94f;
    }
    private void readAnchors(){
        try(InputStream in=context.getAssets().open("rendered/fighter_layout.json")){
            ByteArrayOutputStream result=new ByteArrayOutputStream();
            byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1)result.write(buffer,0,n);
            JSONObject root=new JSONObject(result.toString("UTF-8"));
            JSONObject game=root.getJSONObject("views").getJSONObject("game");
            JSONObject werft=root.getJSONObject("views").getJSONObject("werft");
            JSONArray gameSize=game.getJSONArray("resolution");
            JSONArray werftSize=werft.getJSONArray("resolution");
            float gx=gameSize.getInt(0),gy=gameSize.getInt(1);
            float wx=werftSize.getInt(0),wy=werftSize.getInt(1);
            JSONObject focus=werft.getJSONObject("module_focus_pixels");
            for(int i=0;i<8;i++){
                JSONArray point=focus.getJSONArray(Integer.toString(i));
                focusX[i]=xCoord(point,wx);focusY[i]=yCoord(point,wy);
            }
            JSONObject gamePoints=game.getJSONObject("projected_anchors");
            String[] guns={"weapon_muzzle_left","weapon_muzzle_right"};
            String[] engines={"engine_exhaust_left","engine_exhaust_right"};
            for(int i=0;i<2;i++){
                JSONArray m=gamePoints.getJSONArray(guns[i]),e=gamePoints.getJSONArray(engines[i]);
                muzzleX[i]=xCoord(m,gx);muzzleY[i]=yCoord(m,gy);
                engineX[i]=xCoord(e,gx);engineY[i]=yCoord(e,gy);
            }
            // Verify pose is nose-up in gameplay, not the earlier sideways import.
            JSONArray nose=gamePoints.getJSONArray("nose"),tail=gamePoints.getJSONArray("tail");
            approvedAnchors=nose.getDouble(1)<tail.getDouble(1)
                    && Math.abs(nose.getDouble(0)-tail.getDouble(0))<25;
        }catch(Exception ignored){approvedAnchors=false;}
        if(!approvedAnchors){
            for(int i=0;i<8;i++){
                focusX[i]=GameRules.werftFocusX(i);
                focusY[i]=GameRules.werftFocusY(i);
            }
            muzzleX[0]=-18;muzzleX[1]=18;
            muzzleY[0]=muzzleY[1]=-25;
            engineX[0]=-11;engineX[1]=11;
            engineY[0]=engineY[1]=26;
        }
    }
    private Canvas bitmap(Canvas[] out){
        Bitmap b=Bitmap.createBitmap(S,S,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);out[0]=c;
        return c;
    }
    private void path(Canvas c,int hi,int lo,float... pts){
        Path sh=new Path();sh.moveTo(pts[0],pts[1]);
        float top=pts[1],bottom=pts[1];
        for(int i=2;i<pts.length;i+=2){
            sh.lineTo(pts[i],pts[i+1]);
            top=Math.min(top,pts[i+1]);bottom=Math.max(bottom,pts[i+1]);
        }
        sh.close();
        p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);
        p.setShader(new LinearGradient(0,top,0,Math.max(top+1,bottom),hi,lo,Shader.TileMode.CLAMP));
        c.drawPath(sh,p);p.setShader(null);
    }
    private void stroke(Canvas c,int color,float size,float... pts){
        p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(size);
        p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);
        Path line=new Path();line.moveTo(pts[0],pts[1]);
        for(int i=2;i<pts.length;i+=2)line.lineTo(pts[i],pts[i+1]);
        c.drawPath(line,p);p.setStyle(Paint.Style.FILL);
    }
    private void oval(Canvas c,float x,float y,float rx,float ry,int color){
        p.setColor(color);p.setShader(null);p.setStyle(Paint.Style.FILL);
        c.drawOval(x-rx,y-ry,x+rx,y+ry,p);
    }
    private void glow(Canvas c,float x,float y,float r,int color) {
        p.setColor(Color.WHITE);p.setShader(new RadialGradient(x,y,r,
                new int[]{color,color&0x00FFFFFF},null,Shader.TileMode.CLAMP));
        c.drawCircle(x,y,r,p);p.setShader(null);
    }
    private void rivets(Canvas c,float x,float y,float step,int count,int color){
        for(int i=0;i<count;i++){
            oval(c,x+i*step,y,1.65f,1.65f,color);
            oval(c,x+i*step-.5f,y-.5f,.6f,.6f,0xFFE2F4FF);
        }
    }
    private Bitmap makeFighter(){
        Bitmap b=Bitmap.createBitmap(S,S,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);c.scale(2f,2f);
        // Long engine shadow and wing slots: the entire bitmap is transparent.
        glow(c,96,149,63,0x2837B6FF);
        path(c,0xFF233747,0xFF081322,89,73,64,108,34,117,23,147,61,148,86,133,96,155,
                107,133,131,148,169,147,158,117,129,108,103,73);
        path(c,0xFF8DB0B9,0xFF20445E,89,66,61,109,23,122,18,145,56,138,84,124,
                96,143,108,124,136,138,174,145,169,122,131,109,103,66);
        // Faceted leading armor and lateral stabilizers.
        path(c,0xFFE6FAFF,0xFF3B769B,96,24,72,79,65,118,87,130,96,142,105,130,
                127,118,120,79);
        path(c,0xFF4F819A,0xFF1B304D,96,35,85,79,96,113,107,79);
        path(c,0xFFD3E6F0,0xFF477997,57,105,20,141,48,135,73,119);
        path(c,0xFFAACAD8,0xFF263F59,135,105,172,141,144,135,119,119);
        path(c,0xFF91B6C8,0xFF1C3854,64,112,44,152,75,140,87,123);
        path(c,0xFF91B6C8,0xFF1C3854,128,112,148,152,117,140,105,123);
        // Recessed cockpit canopy and two luminous instrumentation slits.
        path(c,0xFF243B59,0xFF05162A,96,44,86,82,96,109,106,82);
        path(c,0xFF8DDAEF,0xFF176887,96,48,91,72,96,95,101,72);
        stroke(c,0xFFEAFBFC,2.1f,96,26,96,51);
        stroke(c,0xFFB9ECF6,1.8f,77,75,59,111,31,133);
        stroke(c,0xFFB9ECF6,1.8f,115,75,133,111,161,133);
        stroke(c,0xFF24394E,3,42,137,62,130,70,134);
        stroke(c,0xFF24394E,3,150,137,130,130,122,134);
        // High-contrast hatches, inlaid metal seams and recessed armored vents.
        path(c,0xFFDEF0F1,0xFF405B72,66,90,79,81,84,96,68,115,55,125,48,116);
        path(c,0xFFE6EFF4,0xFF3A5975,126,90,113,81,108,96,124,115,137,125,144,116);
        path(c,0xFF93B5C5,0xFF26394E,88,115,96,108,104,115,102,137,90,137);
        for(int side=-1;side<=1;side+=2){
            float sx=side;
            stroke(c,0xFF1C4057,2.7f,96+sx*24,99,96+sx*48,131);
            stroke(c,0xFFC6E0E9,1f,96+sx*22,99,96+sx*44,124);
            for(int j=0;j<5;j++){
                float xx=96+sx*(30+j*5),yy=127+j*1.2f;
                stroke(c,0xFF243C4E,2.3f,xx,yy,xx+sx*3f,yy+4f);
            }
            for(int j=0;j<4;j++){
                float px=96+sx*(19+j*4),py=70+j*11;
                oval(c,px,py,1.1f,1.1f,0xFF0C2839);
                oval(c,px-.35f,py-.4f,.5f,.5f,0xFFD7EAF2);
            }
        }
        // Tiny machined vent grilles, warning markings and wing rivets.
        for(int i=0;i<4;i++){
            stroke(c,0xFF18364B,2,66+i*3,112,70+i*2,123);
            stroke(c,0xFF18364B,2,126-i*3,112,122-i*2,123);
        }
        rivets(c,32,133,4.7f,5,0xFF335269);
        rivets(c,137,133,4.7f,5,0xFF335269);
        path(c,0xFFFFD894,0xFFE67C42,47,142,58,138,65,140,58,146);
        path(c,0xFFFFD894,0xFFE67C42,145,142,134,138,127,140,134,146);
        // Independent, ringed engine nozzles for a strong three-dimensional read.
        for(int side=-1;side<=1;side+=2){
            float ex=96+side*24;
            oval(c,ex,151,13,9,0xFF0B2139);
            oval(c,ex,148,10,6,0xFF7093A7);
            glow(c,ex,151,16,0xBB2BAEF9);
            oval(c,ex,151,6,4,0xFFDAF8FF);
        }
        return b;
    }
    private Bitmap makeEnemy(int type){
        Bitmap b=Bitmap.createBitmap(S,S,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);c.scale(2f,2f);
        final int[][] palette={
            {0xFFB1A7B7,0xFF402A50,0xFFFF7A93},
            {0xFFC5ADAA,0xFF50303D,0xFFFF9B67},
            {0xFF9DAFC1,0xFF2D4359,0xFF9BE6FF},
            {0xFFB6C2CA,0xFF405069,0xFFFFB36E},
            {0xFFAAA9BF,0xFF303151,0xFFCC90FF},
            {0xFFB4ACD3,0xFF41376B,0xFFAD8BFF},
            {0xFFFFDCA3,0xFF6C451C,0xFFFFD45C}
        };
        int hi=palette[type][0],lo=palette[type][1],light=palette[type][2];
        glow(c,96,105,65,(light&0x00FFFFFF)|0x18000000);
        float[] outline;
        switch(type){
            case 0: outline=new float[]{96,27,69,64,28,92,46,122,81,112,96,151,111,112,146,122,164,92,123,64};break;
            case 1: outline=new float[]{96,18,72,78,12,124,65,125,96,139,127,125,180,124,120,78};break;
            case 2: outline=new float[]{96,23,54,55,46,99,64,145,96,130,128,145,146,99,138,55};break;
            case 3: outline=new float[]{96,18,31,65,26,130,63,147,96,132,129,147,166,130,161,65};break;
            case 4: outline=new float[]{96,17,63,38,44,87,25,122,68,145,96,122,124,145,167,122,148,87,129,38};break;
            case 5:outline=new float[]{96,24,51,39,29,90,51,137,96,154,141,137,163,90,141,39};break;
            default:outline=new float[]{96,16,64,44,28,90,42,129,72,135,
                    96,153,120,135,150,129,164,90,128,44};break;
        }
        path(c,hi,lo,outline);
        path(c,0xFFF0E1E2,lo,96,31,75,78,94,115,116,78);
        path(c,light,lo,96,47,83,91,96,116,109,91);
        stroke(c,0x99F6F2F2,2,66,70,46,101,68,108);
        stroke(c,0x99F6F2F2,2,126,70,146,101,124,108);
        stroke(c,0xFF20283F,2,66,117,87,127);
        stroke(c,0xFF20283F,2,126,117,105,127);
        // Recessed wing mechanics, thermal scorching and riveted segmentation.
        for(int side=-1;side<=1;side+=2){
            float sx=side;
            for(int j=0;j<4;j++){
                float xx=96+sx*(35+j*10);
                stroke(c,0xFF243347,2f,xx,108,xx+sx*5,120);
                stroke(c,0xFFB1BDCD,.8f,xx+sx*2,104,xx+sx*7,114);
            }
            for(int j=0;j<3;j++){
                oval(c,96+sx*(49+j*11),96+j*13,1.2f,1.2f,0xFFDBE6F1);
            }
        }
        // Each model has asymmetric visible armor details and own cores.
        if(type==3){
            for(int i=0;i<3;i++){
                path(c,0xFFB8CFD8,0xFF3C4F61,30+i*53,85,40+i*49,57,
                        52+i*46,85,40+i*49,107);
                rivets(c,38+i*52,87,5,3,0xFF233B51);
            }
        }else if(type==4){
            for(int side=-1;side<=1;side+=2){
                stroke(c,0xFFBA95F4,4,96+side*20,89,96+side*49,111);
                oval(c,96+side*51,117,11,7,0xFF263053);
                oval(c,96+side*51,116,5,4,light);
            }
        }else if(type==6){
            for(int side=-1;side<=1;side+=2){
                stroke(c,0xFFFFE5A2,3.2f,96+side*16,53,96+side*46,82);
                oval(c,96+side*49,84,8f,5f,0xFF532F21);
                oval(c,96+side*49,84,3f,2f,light);
            }
            for(int k=0;k<9;k++){
                float a=(float)(k*6.283185f/9f);
                oval(c,96+(float)Math.cos(a)*49,89+(float)Math.sin(a)*36,
                        2.5f,1.4f,0xFFFFE7AF);
            }
        }else if(type==5){
            for(int i=0;i<5;i++){
                float a=i*1.256f;
                float xx=96+(float)Math.sin(a)*34, yy=92+(float)Math.cos(a)*36;
                oval(c,xx,yy,7,5,0xFF111A30);
                oval(c,xx,yy,3,2,light);
            }
        }
        glow(c,96,91,27,(light&0x00FFFFFF)|0x8B000000);
        oval(c,96,91,type==3?11:9,type==3?13:10,0xFF14283D);
        oval(c,96,91,5,7,light);
        oval(c,94,88,2,2,Color.WHITE);
        for(int side=-1;side<=1;side+=2){
            float ex=96+side*31;
            oval(c,ex,132,11,7,0xFF16263A);
            glow(c,ex,132,10,0x556EEBFF);
            oval(c,ex,132,4,3,0xFFE8F7FF);
        }
        return b;
    }
    private Bitmap makeModule(int type){
        Bitmap b=Bitmap.createBitmap(S,S,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);c.scale(2f,2f);
        int[] core={0xFFFF728B,0xFFFF9A74,0xFF6BECCC,0xFFFFD378,0xFFC69DFF};
        glow(c,96,96,70,(core[type]&0x00FFFFFF)|0x23000000);
        path(c,0xFFC1CDDA,0xFF334E68,96,16,142,31,171,74,166,126,128,162,64,162,
                26,126,21,74,50,31);
        path(c,0xFFF1F4F6,0xFF5E8198,96,23,130,42,146,78,129,104,96,115,
                63,104,46,78,62,42);
        path(c,0xFF60849E,0xFF172E46,96,113,129,104,150,129,126,151,66,151,42,129,63,104);
        for(int i=0;i<4;i++){
            float yy=58+i*19;
            stroke(c,0xFF24384D,3,44,yy,60,yy-5);
            stroke(c,0xFF24384D,3,148,yy,132,yy-5);
        }
        for(int i=0;i<3;i++){
            stroke(c,0xFFB7DCEA,2,68+i*14,42,70+i*12,51);
            stroke(c,0xFF33516B,2,68+i*14,136,74+i*11,145);
        }
        oval(c,96,89,29,25,0xFF17273C);
        glow(c,96,88,35,(core[type]&0x00FFFFFF)|0xA0000000);
        oval(c,96,89,19,17,core[type]);
        oval(c,92,84,7,6,0xFFE9F9FC);
        rivets(c,60,117,14,6,0xFF455C72);
        return b;
    }
    void drawPlayer(Canvas c,float x,float y){drawPlayer(c,x,y,false);}
    void drawPlayer(Canvas c,float x,float y,boolean werft){
        Bitmap body=werft?bitmapFor(true,"fighter_base.png"):renderedFighter;
        if(body!=null)paintBitmap(c,body,new RectF(x-50,y-47,x+50,y+47));
        else if(fighter!=null)paintBitmap(c,fighter,new RectF(x-44,y-47,x+44,y+46));
    }
    void drawEnemy(Canvas c,Bitmap b,float x,float y,float radius){
        p.setColor(Color.WHITE);p.setAlpha(255);p.setShader(null);
        float width=radius*2.15f,height=radius*2.3f;
        c.drawBitmap(b,null,new RectF(x-width/2,y-height/2,x+width/2,y+height/2),p);
    }
    void drawModule(Canvas c,int type,float radius){
        Bitmap b=modules[Math.max(0,Math.min(modules.length-1,type))];
        p.setColor(Color.WHITE);p.setAlpha(255);p.setShader(null);
        c.drawBitmap(b,null,new RectF(-radius,-radius,radius,radius),p);
    }
}
