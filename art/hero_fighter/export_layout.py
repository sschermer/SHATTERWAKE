"""ART-001 – Export true projected attachment anchors from the saved .blend.

Called with:
 blender -b art/hero_fighter/hero_fighter.blend --python
          art/hero_fighter/export_layout.py -- art/hero_fighter
Outputs true camera-projected points for two render poses, not guessed offsets.
"""
import bpy,sys,os,json
from mathutils import Vector
from bpy_extras.object_utils import world_to_camera_view
root=os.path.abspath(sys.argv[sys.argv.index("--")+1] if "--" in sys.argv else "art/hero_fighter")
scene=bpy.context.scene
cameras={
    "game":bpy.data.objects.get("ART-001 GAME / nose is +Y, rendered UP"),
    "werft":bpy.data.objects.get("ART-001 WERFT / cinematic 3-quarter")
}
assert all(cameras.values()),"Blender master missing one or both cameras"
sizes={"game":[1024,1024],"werft":[2048,2048]}
# Exact locations are derived from the source geometry definitions in
# build_master.py. With each future model change, update *this* source
# configuration and regenerate the master, instead of altering PNGs by hand.
world={
    "pivot":(0,0,0),
    "nose":(0,5.85,.24),
    "tail":(0,-5.25,.35),
    "weapon_muzzle_left":(-2.89,3.61,.94),
    "weapon_muzzle_right":(2.89,3.61,.94),
    "engine_exhaust_left":(-2.13,-4.83,.45),
    "engine_exhaust_right":(2.13,-4.83,.45),
    "shield_emitter_left":(-3.25,-.04,.98),
    "shield_emitter_right":(3.25,-.04,.98),
    "drone_dock_left":(-3.45,-1.95,1.13),
    "drone_dock_right":(3.45,-1.95,1.13),
    "cockpit_center":(0,1.0,1.31)
}
anchors={
    "0":(-2.89,1.4,.94),   # normal weapon hardpoint focus
    "1":(-2.89,1.4,.94),   # dark damage gun at same hardpoint
    "4":(-3.45,-1.95,1.13),# drone dock focus
    "5":(-3.25,-.04,.98)  # shield emitter focus
}
def xy(cam,p,size):
    f=world_to_camera_view(scene,cam,Vector(p))
    return [round(f.x*size[0],2),round((1-f.y)*size[1],2)]
views={}
for key,cam in cameras.items():
    sz=sizes[key]
    # world_to_camera_view uses scene.render aspect; the saved master had a
    # 16:9 look-dev viewport. Match the square production exports explicitly.
    scene.render.resolution_x=sz[0]
    scene.render.resolution_y=sz[1]
    scene.render.resolution_percentage=100
    bpy.context.view_layer.update()
    views[key]={
        "camera":cam.name,
        "resolution":sz,
        "pivot":xy(cam,world["pivot"],sz),
        "projected_anchors":{n:xy(cam,xyz,sz) for n,xyz in world.items()},
        "module_focus_pixels":{id:xy(cam,xyz,sz) for id,xyz in anchors.items()}
    }
# The nose-up GAME orientation is an executable invariant rather than a
# human check left to do after the APK was built.
n=views["game"]["projected_anchors"]["nose"]
t=views["game"]["projected_anchors"]["tail"]
assert n[1]<t[1],f"GAME fighter faces wrong way: nose {n}, tail {t}"
assert abs(n[0]-t[0])<15,"GAME must not be banked ninety degrees"
assert 0<=n[0]<=1024 and 0<=n[1]<=1024,"Nose outside GAME canvas"
assert 0<=t[0]<=1024 and 0<=t[1]<=1024,"Tail outside GAME canvas"
for view_name,view in views.items():
    width,height=sizes[view_name]
    for name,pixel in view["projected_anchors"].items():
        assert 0<=pixel[0]<=width and 0<=pixel[1]<=height,(
            view_name,name,"projected outside image:",pixel)
assert len(views["game"]["module_focus_pixels"])==4
out={
    "schema":"SHATTERWAKE.ART-001.real-render-anchors.v1",
    "status":"visual-prototype-not-approved",
    "master":"art/hero_fighter/hero_fighter.blend",
    "coordinate_conventions":{
        "model_forward":"+Y in Blender",
        "android_forward":"negative screen Y",
        "image_coordinates":"top-left origin, x-right, y-down",
        "game_canvas_android_logical_rect":[-50,-47,50,47],
        "pixel_values_derived_from":"bpy_extras.object_utils.world_to_camera_view",
    },
    "modules_available":[0,1,4,5],
    "android_loader_status":"existing Android main still uses prior graphics; adapter is intentionally deferred",
    "views":views,
    "world_anchor_units":world
}
target=os.path.join(root,"staging","fighter_layout.json")
with open(target,"w",encoding="utf8") as f:
    json.dump(out,f,indent=2,sort_keys=True)
    f.write("\n")
print("ART-001 EXPORTED:",target)
print("GAME NOSE:",n,"GAME TAIL:",t)
