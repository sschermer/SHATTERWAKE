"""SHATTERWAKE licensed CC0 3D ship and modular upgrade renderer.
Run: blender -b -t 2 --python tools/render_cc0_fighter.py -- SHIP.glb OUTDIR
Source model: Grab3D 'Spaceship', CC0-1.0.
"""
import bpy,math,os,sys
from mathutils import Vector
args=sys.argv[sys.argv.index("--")+1:] if "--" in sys.argv else []
if len(args)!=2:raise SystemExit("Expected GLB file and destination directory")
model_path,dest=args;os.makedirs(dest,exist_ok=True)
bpy.ops.object.select_all(action="SELECT");bpy.ops.object.delete(use_global=False)
bpy.ops.import_scene.gltf(filepath=model_path)
hull=[x for x in bpy.context.scene.objects if x.type=="MESH"]
if not hull:raise RuntimeError("The imported GLB contains no mesh")
corners=[o.matrix_world@Vector(v) for o in hull for v in o.bound_box]
mins=Vector([min(v[i] for v in corners) for i in range(3)])
maxs=Vector([max(v[i] for v in corners) for i in range(3)])
mid=(mins+maxs)*.5
w=maxs.x-mins.x;l=maxs.y-mins.y;span=max(w,l);top=maxs.z+.03*span
if span<.01:raise RuntimeError("Invalid GLB dimensions")
for mat in bpy.data.materials:
    if mat.use_nodes:
        for node in mat.node_tree.nodes:
            if node.type=="BSDF_PRINCIPLED":
                if "Metallic" in node.inputs:node.inputs["Metallic"].default_value=.53
                if "Roughness" in node.inputs:node.inputs["Roughness"].default_value=.37
def material(name,color,metal=.7,rough=.34,emission=None):
    m=bpy.data.materials.new(name);m.diffuse_color=(*color,1);m.use_nodes=True
    n=m.node_tree.nodes.get("Principled BSDF")
    n.inputs["Base Color"].default_value=(*color,1)
    n.inputs["Metallic"].default_value=metal
    n.inputs["Roughness"].default_value=rough
    if emission:
        key="Emission Color" if "Emission Color" in n.inputs else "Emission"
        n.inputs[key].default_value=(*emission,1)
        if "Emission Strength" in n.inputs:n.inputs["Emission Strength"].default_value=2
    return m
metal=material("Dark blue-gray titanium",(.15,.2,.24))
steel=material("Brushed gunmetal",(.39,.45,.48),.85,.26)
carbon=material("Recessed gun bore",(.025,.032,.039),.28,.7)
amber=material("Oxide stripes",(.42,.19,.065),.48,.5)
cyan=material("Contained ion optics",(.08,.4,.67),.18,.3,(.04,.34,.7))
violet=material("Plasma charging ceramic",(.28,.12,.4),.2,.3,(.33,.08,.48))
groups={k:[] for k in range(8)};active=0
def finish(obj,mat):
    obj.data.materials.append(mat);obj["MODULE"]=active;groups[active].append(obj)
    mod=obj.modifiers.new("Hard-surface chamfer","BEVEL")
    mod.width=.008*span;mod.segments=2
    obj.modifiers.new("Weighted edge normals","WEIGHTED_NORMAL")
    return obj
def point(u,v,h):return (mid.x+u*w,mid.y+v*l,top+h*span)
def cuboid(name,u,v,h,sx,sy,sz,mat):
    bpy.ops.mesh.primitive_cube_add(size=1,location=point(u,v,h))
    ob=bpy.context.object;ob.name=name;ob.dimensions=(sx*span,sy*span,sz*span)
    bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
    return finish(ob,mat)
def cyl(name,u,v,h,r,d,mat,forward=False):
    bpy.ops.mesh.primitive_cylinder_add(vertices=24,radius=r*span,
                                        depth=d*span,location=point(u,v,h))
    ob=bpy.context.object;ob.name=name
    if forward:ob.rotation_euler.x=math.pi/2
    return finish(ob,mat)
def torus(name,u,v,h,r,t,mat):
    bpy.ops.mesh.primitive_torus_add(major_radius=r*span,minor_radius=t*span,
              major_segments=32,minor_segments=8,location=point(u,v,h))
    return finish(bpy.context.object,mat)
# 0 rapid weapon: two real twin-barrel mounts with feed rails.
active=0
for side in (-1,1):
    u=side*.23
    cuboid("RAPID housing",u,.1,.055,.12,.24,.065,metal)
    for du in (-.028,.028):
        cyl("RAPID barrel",u+du,.27,.11,.021,.34,steel,True)
        cyl("RAPID muzzle",u+du,.45,.11,.016,.02,carbon,True)
    cuboid("Ammunition feed",u,-.03,.1,.07,.13,.06,amber)
# 1 damage: heavier front cannon bodies and upper radiator.
active=1
for side in (-1,1):
    u=side*.16
    cuboid("Heavy turret carrier",u,.15,.10,.10,.18,.095,steel)
    cyl("Heavy autocannon",u,.28,.16,.034,.37,metal,True)
    cyl("Heavy muzzle",u,.48,.16,.036,.035,carbon,True)
# 2 armor: plated wing cassettes.
active=2
for side in (-1,1):
    for n in range(3):
        cuboid("Armor cassette",side*(.23+n*.12),-.11+n*.05,
               .105,.085,.13,.04,steel)
# 3 magnet: mechanical collector coils, no exaggerated ring glyphs.
active=3
for side in (-1,1):
    u=side*.21
    cyl("Collector base",u,-.10,.135,.06,.038,steel)
    torus("Superconductive pickup rim",u,-.10,.165,.053,.01,cyan)
# 4 drones: real docking cradles with launch bays.
active=4
for side in (-1,1):
    u=side*.34
    cuboid("Drone cradle",u,-.29,.15,.14,.2,.07,metal)
    cuboid("Drone launch latch",u,-.23,.2,.09,.06,.04,steel)
    cyl("Charging contact",u,-.2,.23,.025,.02,cyan)
# 5 shield: paired armored emitters on each wing.
active=5
for side in (-1,1):
    u=side*.35
    cyl("Emitter housing",u,.04,.16,.09,.04,steel)
    torus("Field focus ring",u,.04,.19,.073,.012,cyan)
    cyl("Emitter core",u,.04,.215,.032,.02,cyan)
# 6 plasma: armored capacitor vessel and two heat fins.
active=6
cuboid("Plasma capacitor",0,-.24,.16,.2,.17,.09,metal)
cyl("Containment cell",0,-.24,.235,.066,.04,violet)
for side in (-1,1):
    cuboid("Plasma heat sink",side*.15,-.24,.17,.03,.23,.06,steel)
# 7 new target-correction radar is a visible sensor package.
active=7
cuboid("Fire control pedestal",0,.28,.18,.13,.11,.07,metal)
cyl("Precision targeting lens",0,.28,.24,.048,.043,cyan)
torus("Targeting optic surround",0,.28,.268,.045,.009,steel)
# Identical camera and lighting for full hull and every independent module.
bpy.ops.object.camera_add(location=(mid.x,mid.y-.5*l,mid.z+2.5*span))
cam=bpy.context.object
target=Vector((mid.x,mid.y,mid.z))
cam.rotation_euler=(target-cam.location).to_track_quat("-Z","Y").to_euler()
cam.data.type="ORTHO";cam.data.ortho_scale=span*1.48
scene=bpy.context.scene;scene.camera=cam
for x,y,z,power,size in [(-1,.7,1.7,1750,1.2),(1,-.5,1.5,950,.9)]:
    bpy.ops.object.light_add(type="AREA",location=(mid.x+x*span,mid.y+y*span,mid.z+z*span))
    ob=bpy.context.object;ob.data.energy=power;ob.data.shape="DISK";ob.data.size=size*span
    ob.rotation_euler=(target-ob.location).to_track_quat("-Z","Y").to_euler()
scene.render.engine="CYCLES";scene.cycles.samples=18
scene.render.resolution_x=640;scene.render.resolution_y=640
scene.render.resolution_percentage=100;scene.render.film_transparent=True
scene.render.image_settings.file_format="PNG"
scene.render.image_settings.color_mode="RGBA"
scene.render.image_settings.compression=35
scene.world.color=(.025,.03,.05)
def output(name,base,which):
    for ob in hull:ob.hide_render=not base
    for id,objects in groups.items():
        for ob in objects:ob.hide_render=(id!=which)
    scene.render.filepath=os.path.join(dest,name)
    bpy.ops.render.render(write_still=True)
    print("Rendered:",scene.render.filepath,flush=True)
output("fighter_base.png",True,-1)
for id in range(8):output("fighter_module_%d.png"%id,False,id)
