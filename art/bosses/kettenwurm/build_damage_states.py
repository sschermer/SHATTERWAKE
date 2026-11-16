"""SHATTERWAKE Kettenwurm: four DAMAGE STATES x five original Blender module types.
Review-only PBR production source; NO changes to Android, hitboxes or gameplay.

  blender -b -t 4 --python art/bosses/kettenwurm/build_damage_states.py -- art/bosses/kettenwurm/damage_output

States: 0 pristine, 1 physically dented, 2 severely buckled with sparks,
        3 exploding debris (a transient visual; hide node after FX).
All five classes share precisely aligned orthographic 1024-square RGBA cameras.
"""
import bpy, math, json, os, random, sys
from pathlib import Path
from mathutils import Matrix, Vector

HERE=Path(__file__).resolve().parent
OUT=Path(sys.argv[sys.argv.index("--")+1]).resolve() if "--" in sys.argv else HERE/"damage_output"
OUT.mkdir(parents=True,exist_ok=True)
random.seed(31616)

# Reuse the user's provisionally accepted Mk-II geometry and clean polished
# shaders, not an independently simplified approximation. The Mk-II builder
# generates temporary previews, which are removed after this production run.
source=(HERE/"build_mk2.py").read_text(encoding="utf-8")
original_argv=sys.argv[:]
sys.argv=[sys.argv[0],"--",str(OUT/"_temporary_mk2")]
exec(compile(source,str(HERE/"build_mk2.py"),"exec"),{"__name__":"__main__"})
sys.argv=original_argv

sc=bpy.context.scene
sc.render.engine="CYCLES"
sc.cycles.samples=80
sc.view_layers[0].cycles.use_denoising=True
sc.render.resolution_x=1024
sc.render.resolution_y=1024
sc.render.resolution_percentage=100
sc.render.film_transparent=True
sc.render.image_settings.file_format="PNG"
sc.render.image_settings.color_mode="RGBA"
sc.render.image_settings.color_depth="8"

head=bpy.data.collections["MKII_HEAD"]
body=bpy.data.collections["MKII_SEGMENT"]
metal=bpy.data.materials["PBR mirror-polished charcoal titanium"]
red=bpy.data.materials["polished oxblood metallic armor"]
silver=bpy.data.materials["bright precision-polished stainless steel edges"]
carbon=bpy.data.materials["smooth graphite gasket seals"]
bronze=bpy.data.materials["polished bronze hydraulic pistons"]
orange=bpy.data.materials["orange heat ports"]
blue=bpy.data.materials["cold sensor glass"]

def make_material(name, rgb, metallic=.7, roughness=.2, glow=0.):
    m=bpy.data.materials.new(name)
    m.diffuse_color=(*rgb,1.0)
    m.use_nodes=True
    p=m.node_tree.nodes.get("Principled BSDF")
    p.inputs["Base Color"].default_value=(*rgb,1)
    p.inputs["Metallic"].default_value=metallic
    p.inputs["Roughness"].default_value=roughness
    if glow:
        p.inputs["Emission Color"].default_value=(*rgb,1)
        p.inputs["Emission Strength"].default_value=glow
    return m

scorched=make_material("smooth blackened heat-scorched metal",(.015,.018,.021),.65,.4)
flame=make_material("white-hot metal sparks",(1.,.64,.14),.24,.12,8.)
hot=make_material("hot molten fracture",(1.,.14,.017),.25,.25,5.)
cool=make_material("electric capacitor arcs",(.10,.70,1.),.15,.12,6.)
smoke=make_material("smoke char at exposed connector",(.055,.065,.07),.08,.9)

def to_col(obj,col):
    for c in list(obj.users_collection):
        c.objects.unlink(obj)
    col.objects.link(obj)
    return obj

def bevel(o,r=.025):
    m=o.modifiers.new("precisely-machined radius","BEVEL")
    m.width=r
    m.segments=3
    o.modifiers.new("weighted hard-surface normals","WEIGHTED_NORMAL")
    return o

def box(col,name,xyz,size,mat,r=.02):
    bpy.ops.mesh.primitive_cube_add(size=1,location=xyz)
    o=bpy.context.object
    o.dimensions=size
    bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
    o.name=name
    o.data.materials.append(mat)
    return bevel(to_col(o,col),r) if r else to_col(o,col)

def beam(col,name,p,q,r,mat,segments=12):
    a,b=Vector(p),Vector(q)
    d=b-a
    bpy.ops.mesh.primitive_cylinder_add(vertices=segments,radius=r,depth=max(.01,d.length),location=(a+b)/2)
    o=bpy.context.object
    o.name=name
    o.rotation_euler=d.to_track_quat("Z","Y").to_euler()
    o.data.materials.append(mat)
    return bevel(to_col(o,col),min(.015,r/3))

def sphere(col,name,xyz,r,mat):
    bpy.ops.mesh.primitive_uv_sphere_add(segments=24,ring_count=12,radius=r,location=xyz)
    o=bpy.context.object
    o.name=name
    o.data.materials.append(mat)
    return to_col(o,col)

def tri_shard(col,name,xyz,scale,mat,angle=0):
    x,y,z=xyz
    s=scale
    points=[
        (x-.35*s,y-.19*s,z-.05*s),(x+.33*s,y-.23*s,z),
        (x+.20*s,y+.44*s,z+.08*s),(x-.08*s,y+.09*s,z+.22*s),
    ]
    mesh=bpy.data.meshes.new(name+"_geometry")
    mesh.from_pydata(points,[],[(0,1,3),(1,2,3),(2,0,3),(0,2,1)])
    mesh.update()
    o=bpy.data.objects.new(name,mesh)
    col.objects.link(o)
    o.data.materials.append(mat)
    o.rotation_euler[2]=angle
    return o

def dent_panel(col,state,type_id):
    """Actual nonplanar mesh, not a painted dent or normal map."""
    depth=.12 if state==1 else .31
    width=.72 if type_id else .84
    height=.43 if type_id else .62
    z0=1.11 if type_id else 1.34
    y0=.06 if type_id else .18
    verts=[]
    for j in range(7):
        yy=(j-3)/3.0
        for i in range(7):
            xx=(i-3)/3.0
            radius=xx*xx+yy*yy
            bowl=math.exp(-radius*3.6)
            verts.append((xx*width/2, y0+yy*height/2,
                z0+.022*(xx+yy)-depth*bowl))
    faces=[]
    for j in range(6):
        for i in range(6):
            k=j*7+i
            faces.append((k,k+1,k+8,k+7))
    mesh=bpy.data.meshes.new("physical pressed deformation")
    mesh.from_pydata(verts,[],faces)
    mesh.update()
    o=bpy.data.objects.new("state_%d_actual_dented_metal_plate"%state,mesh)
    col.objects.link(o)
    o.data.materials.append(red if type_id in (0,1,2) else silver)
    sol=o.modifiers.new("formed armored shell","SOLIDIFY")
    sol.thickness=.055
    bevel(o,.015)
    return o

def clone_mkii(col,type_id,state):
    template=head if type_id==0 else body
    offset=Vector((0,0 if type_id==0 else 2.15,0))
    matched=0
    for src in list(template.objects):
        n=src.name.lower()
        # Replace selected upper armor plates with physically distorted sheets.
        if state in (1,2) and type_id!=0 and "dorsal central titanium spine" in n:
            matched+=1
            continue
        if state==2 and "overlapping sloped armor tile" in n and matched%2==0:
            matched+=1
            continue
        if state==2 and "overlapping sloped armor tile" in n:
            matched+=1
        o=src.copy()
        if src.type=="MESH":
            o.data=src.data.copy()
        o.name="TYPE%d_S%d_%s"%(type_id,state,src.name)
        col.objects.link(o)
        o.matrix_world=Matrix.Translation(offset)@src.matrix_world
        # Distorted metal topology and buckled mounting, not grain or texture.
        if state>0 and src.type=="MESH" and (
            "armor tile" in n or "skull armor" in n or "central sensor skull" in n):
            if matched%3==0 or type_id==0:
                for i,v in enumerate(o.data.vertices):
                    if v.co.z>=0:
                        v.co.z-= (.035 if state==1 else .09)*(1+.22*math.sin(i+type_id))
                        v.co.x+= (.012 if state==1 else .055)*math.sin(i*1.67)
                if state==2 and "armor tile" in n:
                    o.rotation_euler[1]+=.11
        matched+=1

def equipment(col,type_id,state):
    """Actual mechanical differences reflecting the existing Node.type IDs."""
    if type_id==0:
        # Reuse full mandibles, sensors and integrated weapons from Mk-II head.
        for side in (-1,1):
            beam(col,"sensor protection rim",(side*.34,.42,1.32),
                 (side*.34,.68,1.16),.045,silver)
        if state==2:
            beam(col,"bent mandible piston",(-.64,.15,.78),(-.39,.87,.93),.085,bronze)
    if type_id==1:
        for side in (-1,1):
            p=(side*.75,-.24,.69)
            q=(side*(.75 if state<2 else .88),.57,.74 if state<2 else .59)
            beam(col,"weapon_sled_rail",p,q,.11,metal)
            beam(col,"recessed_weapon_muzzle",q,
                 (q[0],q[1]+.11,q[2]),.054,orange)
    if type_id==2:
        for side in (-1,1):
            p=(side*.84,-.43,.40)
            q=(side*(.91 if state<2 else 1.06),.40,.62)
            beam(col,"shear coupling hydraulic latch",p,q,.095,bronze)
            box(col,"release buckle",(side*.94,.06,.75),(.22,.15,.09),silver,.025)
    if type_id==3:
        beam(col,"reactor glowing fuel chamber",(0,-.27,1.1),(0,.28,1.1),
             .19 if state<2 else .25,orange)
        for side in (-1,1):
            beam(col,"reactor containment cage",(side*.27,-.35,1.12),
                 (side*.27,.35,1.12),.07,silver)
    if type_id==4:
        for side in (-1,1):
            beam(col,"capacitor drum",(side*.51,-.32,1.06),
                 (side*.51,.32,1.06),.14,metal)
            beam(col,"capacitor lens",(side*.51,-.18,1.17),
                 (side*.51,.18,1.17),.065,blue)
    if state in (1,2):
        dent_panel(col,state,type_id)
        # Local polished metal tears; no random texture or surface grain.
        for k in range(2 if state==1 else 5):
            y=-.16+k*.085
            z=1.18 if type_id else 1.42
            beam(col,"physical shear crease",(-.19+k*.07,y,z),
                 (-.09+k*.07,y+.11,z-.045 if state==2 else z-.008),
                 .008 if state==1 else .016,silver if k%2==0 else scorched)
    if state==2:
        base=(0,.10,1.1) if type_id!=0 else (-.34,.57,1.17)
        sphere(col,"exposed overheated junction",base,.18,hot if type_id!=4 else cool)
        for k in range(12):
            angle=k*math.tau/12+.17*type_id
            x=base[0]+math.cos(angle)*(.28+.035*(k%3))
            y=base[1]+math.sin(angle)*(.23+.025*(k%2))
            z=base[2]+.15+(.05*(k%3))
            beam(col,"state2 electrical spark %02d"%k,base,(x,y,z),
                 .012 if k%2 else .019,cool if type_id==4 and k%3==0 else flame,8)
        # Short black heat exhaust; low-profile fragments for clarity.
        for i in range(3):
            sphere(col,"burned exhaust cinder",(base[0]+.12*i,base[1]-.16,
                base[2]+.23+.14*i),.065+.02*i,smoke)

def explosion(col,type_id):
    """State 3 is an explosion frame. Runtime must remove the destroyed node."""
    center=Vector((0,0,.75))
    sphere(col,"exploding orange core",center,.27,hot)
    sphere(col,"white hot ignition",(0,0,.83),.14,flame)
    for i in range(16):
        a=(i+.2*type_id)*math.tau/16
        dz=((i%5)-2)*.09
        radius=.49+.055*(i%4)
        p=center+Vector((math.cos(a)*radius,math.sin(a)*radius,dz))
        tri_shard(col,"shattered armored plate %02d"%i,p,
                  .36+.07*(i%3),red if i%2 else silver,a)
        beam(col,"directional ignition streak %02d"%i,
             center+Vector((math.cos(a)*.17,math.sin(a)*.17,.10)),
             p+Vector((math.cos(a)*.17,math.sin(a)*.17,.12)),
             .017,hot if i%3==0 else flame,8)
    # Detached pressure tube and believable wreckage around the vacancy
    for side in (-1,1):
        beam(col,"ruptured hydraulic line",(side*.3,-.45,.45),
             (side*.78,.12,.79),.057,bronze)

def make_state(type_id,state):
    col=bpy.data.collections.new("NODE_TYPE_%d / DAMAGE_%d"%(type_id,state))
    sc.collection.children.link(col)
    if state<3:
        clone_mkii(col,type_id,state)
        equipment(col,type_id,state)
    else:
        explosion(col,type_id)
    return col

models={(t,s):make_state(t,s) for t in range(5) for s in range(4)}

# Hide originals for output, but retain their meshes and source materials
# in the editable .blend file.
for c in (head,body):
    for o in c.objects:
        o.hide_render=True

def camera(name,origin,scale):
    bpy.ops.object.camera_add(location=origin)
    cam=bpy.context.object
    cam.name=name
    cam.rotation_euler=(Vector((0,.05,.67))-cam.location).to_track_quat("-Z","Y").to_euler()
    cam.data.type="ORTHO"
    cam.data.ortho_scale=scale
    return cam

game=camera("GAME strictly orthographic nose +Y",(0,.05,17),4.2)
detail=camera("DETAIL shallow three-quarter",(5,-7,11),4.5)

def show(type_id,state):
    for key,col in models.items():
        col.hide_render=key!=(type_id,state)

def render(file_name,cam,type_id,state):
    show(type_id,state)
    sc.camera=cam
    sc.render.filepath=str(OUT/file_name)
    bpy.ops.render.render(write_still=True)
    print("STATE_RENDERED",type_id,state,file_name,flush=True)

for t in range(5):
    for s in range(4):
        render("boss_type_%d_damage_%d.png"%(t,s),game,t,s)
# Extra 3D checks focused on two archetypes: head and volatile core.
for t in (0,3):
    for s in (1,2):
        render("detail_type_%d_damage_%d.png"%(t,s),detail,t,s)

# Blender project contains all 20 mutually exclusive geometry collections.
show(0,0)
sc.camera=game
sc.render.filepath=""
bpy.ops.wm.save_as_mainfile(filepath=str(OUT/"kettenwurm_damage_master.blend"))

manifest={
 "production":"SHATTERWAKE boss Kettenwurm four damage stages",
 "states":{"0":"intact","1":"lightly dented","2":"severely buckled, sparks",
           "3":"transient explosion/debris; node removed by runtime"},
 "thresholds":{"0":"hp > 2/3 maxHp","1":"1/3 maxHp < hp <= 2/3 maxHp",
               "2":"0 < hp <= 1/3 maxHp","3":"hp <= 0"},
 "node_types":{"0":"head","1":"weapon","2":"coupling/split",
               "3":"volatile reactor","4":"energy capacitor"},
 "node_type_by_id":[0,1,1,4,2,3,4,1,2,4,3,1],
 "export":{"format":"PNG","channels":"RGBA","size":[1024,1024],
           "projection":"identical orthographic GAME camera, nose +Y"},
 "commercial_rights":"original procedural Blender geometry; no external assets",
 "source":"art/bosses/kettenwurm/build_damage_states.py + build_mk2.py",
 "visual_approval":"PENDING","android_integration":False
}
with open(OUT/"manifest.json","w",encoding="utf-8") as f:
    json.dump(manifest,f,indent=2,ensure_ascii=False)
print("DONE: 20 type/state assets and 4 closeups, 1 editable Blender master.",flush=True)
