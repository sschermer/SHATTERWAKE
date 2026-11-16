package de.shatterwake.game;

/** No reactive evasions, bounded continuous readable motion. */
public final class FlightPathChecks {
    public static void main(String[] args){
        float[] a=new float[2],b=new float[2],before=new float[2];
        for(int kind=0;kind<8;kind++){
            for(float side:new float[]{-1f,1f}){
                for(int sample=0;sample<=120;sample++){
                    float t=sample*.1f;
                    FlightPaths.position(kind,210f,-40f,68f,side,-1000f,t,a);
                    FlightPaths.position(kind,210f,-40f,68f,side,1000f,t,b);
                    assert Math.abs(a[0]-b[0])<.001f
                            &&Math.abs(a[1]-b[1])<.001f:
                            "Trajectories must ignore player's aim/position";
                    assert Float.isFinite(a[0])&&Float.isFinite(a[1]);
                    if(sample>0){
                        assert Math.abs(a[0]-before[0])<38f:
                                "No sudden evasive horizontal jump: kind "+kind;
                        assert Math.abs(a[1]-before[1])<42f:
                                "No unpredictable vertical jump: kind "+kind;
                    }
                    before[0]=a[0];before[1]=a[1];
                }
            }
        }
        // Standard enemy flight always progresses into screen (rare loop excepted).
        for(int kind=0;kind<7;kind++){
            FlightPaths.position(kind,210f,-40f,68f,1,0f,3f,a);
            FlightPaths.position(kind,210f,-40f,68f,1,0f,4f,b);
            assert b[1]>a[1];
        }
        System.out.println("PASS: eight deterministic readable paths without reactive dodges");
    }
}
