package de.shatterwake.game;

/**
 * Non-reactive attack paths. Once spawned, an enemy follows a fixed,
 * smooth curve. There is no reading of the player's current position,
 * firing direction or shot locations: no targeted evasive behavior.
 *
 * targetX is intentionally ignored for compatibility with earlier callers.
 */
public final class FlightPaths {
    private FlightPaths(){}
    private static float clamp(float v,float lo,float hi){
        return Math.max(lo,Math.min(hi,v));
    }
    private static float smooth(float v){
        v=clamp(v,0f,1f);
        return v*v*(3f-2f*v);
    }
    public static void position(int kind,float startX,float startY,float speed,
                                float side,float targetX,float seconds,float[] out){
        final float t=Math.max(0f,seconds);
        float x=startX,y=startY+speed*t;
        switch(kind){
            case 0: // Interceptor: gentle steady diagonal sweep.
                x+=side*68f*smooth(t/6.5f);
                break;
            case 1: // Raider: one large predictable attack arc.
                x+=side*78f*(float)Math.sin(Math.PI*clamp(t/9f,0f,1f));
                break;
            case 2: // Recon: shallow S, never homes in on the player.
                x+=side*(48f*smooth(t/7f)+19f*(float)Math.sin(
                        2f*Math.PI*clamp(t/9f,0f,1f)));
                y+=28f*smooth(t/6f);
                break;
            case 3: // Carrier: heavy, easy-to-track pass.
                x+=side*36f*smooth(t/8f);
                break;
            case 4: // Bomber: one measured sine-wave arc.
                x+=side*69f*(float)Math.sin(
                        2f*Math.PI*clamp(t/11f,0f,1f));
                break;
            case 5: // Enforcer: slow flanking sweep.
                x+=side*56f*(float)Math.sin(
                        .5f*Math.PI*clamp(t/8f,0f,1f));
                break;
            case 6: // Detached module: drifting debris.
                x+=side*(10f*t+12f*(float)Math.sin(t*.8f));
                break;
            case 7: // Very rare star hunter: ONE readable full loop.
                final float cx=clamp(startX,105f,315f),cy=247f,r=65f;
                if(t<2f){
                    float a=smooth(t/2f);
                    x=startX+(cx+r-startX)*a;
                    y=startY+(cy-startY)*a;
                }else if(t<8.4f){
                    float a=(t-2f)*(float)(2f*Math.PI/6.4f);
                    x=cx+r*(float)Math.cos(a);
                    y=cy+r*(float)Math.sin(a);
                }else{
                    float dt=t-8.4f;
                    x=cx+r+side*dt*22f;
                    y=cy+dt*132f;
                }
                break;
            default:break;
        }
        out[0]=x;out[1]=y;
    }
}
