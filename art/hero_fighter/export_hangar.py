"""Export a physically lit, empty shipyard background from the approved art master.

One exact Blender camera, matching the transparent WERFT ship sprites.
All fighters and upgrade geometry are suppressed; only drydock meshes remain.
"""
import bpy,os,sys
from mathutils import Vector
root=os.path.abspath(sys.argv[sys.argv.index("--")+1] if "--" in sys.argv else "art/hero_fighter")
scene=bpy.context.scene
cam=bpy.data.objects["ART-001 WERFT / cinematic 3-quarter"]
scene.camera=cam
room_prefixes=(
 "Industrial drydock floor","Hangar yellow maintenance path",
 "Drydock recessed floor seam","Hangar deck flood lamp base",
 "Muted amber floor work light","Hangar structural bulkhead",
 "Hangar reinforced support beam","Hangar overhead gantry",
 "Overhead maintenance suspension cable"
)
scene_meshes=[o for o in scene.objects if o.type in ("MESH","FONT","CURVE")]
room_count=0
for ob in scene_meshes:
 is_room=ob.name.startswith(room_prefixes)
 ob.hide_render=not is_room
 room_count+=int(is_room)
assert room_count>=12,("Missing environment meshes",room_count)
scene.render.engine="CYCLES"
scene.cycles.samples=64
scene.cycles.use_denoising=True
scene.render.film_transparent=False
scene.render.resolution_x=2048
scene.render.resolution_y=2048
scene.render.resolution_percentage=100
scene.render.image_settings.file_format="PNG"
scene.render.image_settings.color_mode="RGBA"
scene.render.image_settings.color_depth="8"
scene.render.image_settings.compression=24
out=os.path.join(root,"staging","werft","hangar_background.png")
scene.render.filepath=out
bpy.ops.render.render(write_still=True)
assert os.path.getsize(out)>1000,out
print("ART HANGAR: rendered matching orthographic warehouse background",out)
