package de.shatterwake.game;

/** Deterministic trajectory curves: actual attack lines, banking turns and a rare loop. */
public final class FlightPaths {
    private FlightPaths(){}
    private static float clamp(float v,float lo,float hi){
        return Math.max(lo,Math.min(hi,v));
    }
    private static float smooth(float v){
        v=clamp(v,0f,1f);return v*v*(3f-2f*v);
    }
    public static void position(int kind,float startX,float startY,float speed,
                                float side,float targetX,float seconds,float[] out){
        float t=Math.max(0f,seconds),y=startY+speed*t,x=startX;
        switch(kind){
            case 0:x+=side*118f*smooth(t/4.5f);break;
            case 1:x+=side*126f*(float)Math.sin(Math.PI*clamp(t/6f,0f,1f));break;
            case 2:
                x+=(targetX-startX)*smooth(t/5.5f);
                x+=side*36f*(float)Math.sin(Math.PI*clamp(t/5.5f,0f,1f));
                y+=38f*smooth(t/3.6f);break;
            case 3:x+=side*140f*(1f-(float)Math.cos(
                    Math.PI*clamp(t/7f,0f,1f)))*.5f;break;
            case 4:x+=side*138f*(float)Math.sin(
                    2f*Math.PI*clamp(t/10f,0f,1f));break;
            case 5:x+=side*144f*(float)Math.sin(
                    .5f*Math.PI*clamp(t/5.1f,0f,1f));break;
            case 6:x+=side*(24f*t+23f*(float)Math.sin(t*.85f));break;
            case 7:
                // A golden high-value target flies in, draws 1.5 circular
                // revolutions around the arena, then breaks toward the bottom.
                float cx=clamp(startX,101f,319f),cy=252f,r=77f;
                if(t<2f){
                    float a=smooth(t/2f);
                    x=startX+(cx+r-startX)*a;
                    y=startY+(cy-startY)*a;
                }else if(t<9.3f){
                    float a=(t-2f)*(float)(3f*Math.PI/7.3f);
                    x=cx+r*(float)Math.cos(a);
                    y=cy+r*(float)Math.sin(a);
                }else{
                    float dt=t-9.3f;
                    x=cx-r+side*dt*32f;
                    y=cy+dt*145f;
                }
                break;
            default:break;
        }
        out[0]=x;out[1]=y;
    }
}
