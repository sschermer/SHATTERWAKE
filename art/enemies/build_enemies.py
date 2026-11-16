"""SHATTERWAKE v0.6: eight original hard-surface enemy craft.
No stock models, no image textures, no bump/noise shaders.
Camera-facing and transparent sprites from a consistent physically-lit stage.
All ships point UP inside their sprite; Canvas rotates along their fixed paths.
"""
import bpy,math,os,sys
from mathutils import Vector
outroot=os.path.abspath(sys.argv[sys.argv.index("--")+1] if "--" in sys.argv else ".")
renderdir=os.path.join(outroot,"app","src","main","assets","rendered","enemies")
previewdir=os.path.join(outroot,"art","enemies")
os.makedirs(renderdir,exist_ok=True)
os.makedirs(previewdir,exist_ok=True)

bpy.ops.object.select_all(action="SELECT")
bpy.ops.object.delete(use_global=False)
scene=bpy.context.scene
scene.render.engine="CYCLES"
scene.cycles.samples=32
scene.cycles.use_denoising=True
scene.render.film_transparent=True
scene.render.image_settings.file_format="PNG"
scene.render.image_settings.color_mode="RGBA"
scene.render.image_settings.color_depth="8"
scene.render.image_settings.compression=25
scene.render.resolution_percentage=100
scene.render.resolution_x=640
scene.render.resolution_y=640
scene.view_settings.view_transform="Standard"
scene.view_settings.look="Medium High Contrast"
scene.view_settings.exposure=0
scene.view_settings.gamma=1
scene.camera=None
world=bpy.data.worlds.new("Untextured black studio")
world.use_nodes=True
world.node_tree.nodes["Background"].inputs["Color"].default_value=(.06,.075,.10,1)
world.node_tree.nodes["Background"].inputs["Strength"].default_value=.5
scene.world=world

def mat(name,rgb,metal=.8,rough=.32,emission=None):
    m=bpy.data.materials.new(name)
    m.diffuse_color=(*rgb,1)
    m.use_nodes=True
    bsdf=m.node_tree.nodes.get("Principled BSDF")
    bsdf.inputs["Base Color"].default_value=(*rgb,1)
    bsdf.inputs["Metallic"].default_value=metal
    bsdf.inputs["Roughness"].default_value=rough
    if emission:
        if bsdf.inputs.get("Emission"):
            bsdf.inputs["Emission"].default_value=(*emission,1)
        if bsdf.inputs.get("Emission Color"):
            bsdf.inputs["Emission Color"].default_value=(*emission,1)
        if bsdf.inputs.get("Emission Strength"):
            bsdf.inputs["Emission Strength"].default_value=.8
    return m
carbon=mat("Satin graphite / no grain",(.061,.079,.093),.86,.31)
gunmetal=mat("Soft clean titanium",(.21,.245,.275),.88,.26)
silver=mat("Scuffed steel / clean finish",(.37,.415,.43),.82,.31)
dark=mat("Black ceramic composite",(.038,.052,.063),.35,.36)
panel=mat("Dark armor insert",(.11,.15,.168),.82,.34)
azure=mat("Muted cyan optic",(.042,.23,.32),.36,.19,(.015,.12,.20))
amber=mat("Low amber discharge",(.39,.15,.052),.42,.26,(.28,.095,.018))
red=mat("Crimson targeting glass",(.28,.068,.063),.4,.2,(.18,.018,.014))
purple=mat("Violet sensor",(.18,.085,.28),.35,.23,(.1,.026,.16))
gold=mat("Rare warm brass",(.58,.39,.15),.86,.28)
goldlight=mat("Rare gold sensor",(.43,.26,.05),.42,.23,(.28,.16,.016))
warmgun=mat("Copper-gray armor",(.24,.215,.22),.86,.3)

ships=[]
def register(o):
    ships.append(o)
    return o

def finish(o,material,bevel=0):
    o.data.materials.append(material)
    if bevel>0:
        mod=o.modifiers.new("Machined bevel","BEVEL")
        mod.width=bevel;mod.segments=2
        mod.limit_method="ANGLE"
        mod.angle_limit=.35
        mod2=o.modifiers.new("Weighted machined normals","WEIGHTED_NORMAL")
        mod2.keep_sharp=True
    for face in o.data.polygons:face.use_smooth=True
    return register(o)

def block(label,center,dimensions,material,bevel=.05,angle=0):
    bpy.ops.mesh.primitive_cube_add(size=1,location=center)
    o=bpy.context.object;o.name=label
    o.dimensions=dimensions
    bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
    o.rotation_euler[2]=angle
    return finish(o,material,bevel)

def armor(label,contour,zBottom,zTop,material,bevel=.07):
    coords=list(contour)
    area=sum(coords[i][0]*coords[(i+1)%len(coords)][1]-coords[(i+1)%len(coords)][0]*coords[i][1] for i in range(len(coords)))
    if area<0:coords.reverse()
    n=len(coords)
    verts=[(x,y,zBottom) for x,y in coords]+[(x,y,zTop) for x,y in coords]
    faces=[tuple(range(n-1,-1,-1)),tuple(range(n,2*n))]
    faces += [(i,(i+1)%n,(i+1)%n+n,i+n) for i in range(n)]
    mesh=bpy.data.meshes.new(label+"_mesh")
    mesh.from_pydata(verts,[],faces);mesh.update()
    o=bpy.data.objects.new(label,mesh);bpy.context.collection.objects.link(o)
    return finish(o,material,bevel)

def cyl(label,center,radius,depth,material,axis="Z",n=20):
    bpy.ops.mesh.primitive_cylinder_add(vertices=n,radius=radius,depth=depth,location=center)
    o=bpy.context.object;o.name=label
    if axis=="Y":o.rotation_euler[0]=math.pi/2
    if axis=="X":o.rotation_euler[1]=math.pi/2
    return finish(o,material,.025)

def sphere(label,center,scale,material):
    bpy.ops.mesh.primitive_uv_sphere_add(segments=24,ring_count=12,location=center)
    o=bpy.context.object;o.name=label
    o.scale=scale
    return finish(o,material)

def beam(label,a,b,thick,material):
    a,b=Vector(a),Vector(b)
    mid=(a+b)*.5
    o=block(label,mid,(thick,(b-a).length,thick),material,.035)
    o.rotation_euler=(b-a).to_track_quat("Y","Z").to_euler()
    return o

def mirrored(label,f):
    for side in (-1,1):f(label+(" L" if side<0 else " R"),side)

def studio():
    bpy.ops.object.camera_add(location=(0,-3.6,22))
    cam=bpy.context.object;cam.name="ENEMY ART / fixed nose-up"
    cam.rotation_euler=(Vector((0,0,0))-cam.location).to_track_quat("-Z","Y").to_euler()
    cam.data.type="ORTHO";cam.data.ortho_scale=9.9
    scene.camera=cam
    def light(name,loc,power,size,rgb):
        bpy.ops.object.light_add(type="AREA",location=loc)
        ob=bpy.context.object;ob.name=name
        ob.data.energy=power;ob.data.shape="DISK";ob.data.size=size
        ob.data.color=rgb
        ob.rotation_euler=(Vector((0,0,0))-ob.location).to_track_quat("-Z","Y").to_euler()
    light("Large titanium softbox",(-5,4,10),1250,7,(.88,.95,1))
    light("Muted blue edge",(6,-2,8),780,5,(.56,.76,1))
    light("Low white anti-aliased fill",(1,6,12),850,8,(1,.96,.9))
studio()

def base(label,outline,width=1):
    # Thin black mechanical substructure behind polished alloy plates.
    armor(label+" / black load-bearing chassis",outline,-.28,.10,carbon,.12)
    armor(label+" / heavy faceted steel skin",[
        (x*.93,y*.92) for x,y in outline],.10,.38,gunmetal,.09)
    armor(label+" / raised spine",[
        (0,3.05),(-.47,1.2),(-.70,-1.9),(0,-2.7),
        (.70,-1.9),(.47,1.2)],.35,.65,panel,.07)
    sphere(label+" / low reflective radar hood",(0,.38,.67),(.44,.85,.24),dark)
    sphere(label+" / cold lens",(0,.64,.81),(.17,.3,.055),azure)

def engines(style="blue",x=1.0,tail=-2.35):
    glow=azure if style=="blue" else amber if style=="amber" else purple if style=="purple" else goldlight
    for side in (-1,1):
        cx=side*x
        cyl("Machined enclosed rear jet nozzle",(cx,tail,.02),.22,.25,carbon,"Y")
        cyl("Recessed cold thruster lens",(cx,tail-.16,.02),.14,.06,glow,"Y")
        block("Jet engine outer thermal cover",(cx,tail+.28,.10),(.5,.76,.35),silver,.06)

def guns(x=1.3,y=1.9,barrel=1.13):
    for side in (-1,1):
        xx=side*x
        block("Shielded rail-gun mount",(xx,y-.45,.43),(.29,.88,.24),carbon,.04)
        cyl("Recessed main cannon",(xx,y+.08,.44),.105,barrel,gunmetal,"Y",24)
        cyl("Cannon muzzle",(xx,y+.barrel/2,.44),.13,.09,dark,"Y")

def details(count=3):
    for side in (-1,1):
        for k in range(count):
            x=side*(.73+.23*k)
            block("Recessed vent segment",(x,-.45-k*.32,.48),(.10,.20,.025),carbon,.008)

def model(kind):
    if kind==0: # swift forward-arrow interceptor, wide high aspect V
        outline=[(0,3.1),(-.64,1.75),(-1.05,1.15),(-3.04,-.79),
                 (-2.72,-1.12),(-1.28,-.77),(-.85,-2.52),
                 (0,-3.02),(.85,-2.52),(1.28,-.77),
                 (2.72,-1.12),(3.04,-.79),(1.05,1.15),(.64,1.75)]
        base("KESTREL / light interceptor",outline)
        mirrored("Thin pointed wing strake",lambda n,s:beam(n,(s*.86,.81,.54),(s*2.68,-.66,.35),.15,silver))
        guns(.95,1.6,.88);engines("blue",.83);details(2)
    elif kind==1: # compact raider, angled swept boomerang wings
        outline=[(0,2.92),(-.72,1.5),(-1.14,.48),(-3.48,-.38),(-2.85,-1.45),
                 (-1.42,-1.12),(-.62,-2.65),(0,-2.84),(.62,-2.65),
                 (1.42,-1.12),(2.85,-1.45),(3.48,-.38),(1.14,.48),(.72,1.5)]
        base("RAIDER / swept ambush fighter",outline)
        mirrored("Raider copper-braced upper wing",lambda n,s:beam(n,(s*.85,.54,.53),(s*3.08,-.55,.40),.21,warmgun))
        guns(1.1,1.20,1.16);engines("amber",1.1,tail=-2.38);details(3)
    elif kind==2: # fragile high-speed recon needle
        outline=[(0,3.68),(-.48,1.87),(-.88,.70),(-2.12,-.72),(-1.5,-1.49),
                 (-.52,-2.5),(0,-3.14),(.52,-2.5),(1.5,-1.49),(2.12,-.72),
                 (.88,.70),(.48,1.87)]
        base("GHOST / reconnaissance needle",outline)
        armor("GHOST / long precision nose",[(0,3.78),(-.18,1.63),(-.26,-.40),
            (0,-1.45),(.26,-.4),(.18,1.63)],.58,.74,silver,.03)
        mirrored("Recon optical side vane",lambda n,s:beam(n,(s*.65,.7,.50),(s*1.95,-.84,.35),.11,silver))
        guns(.64,.88,.85);engines("blue",.58,tail=-2.60)
    elif kind==3: # genuinely heavy module carrier
        outline=[(0,2.85),(-.97,2.25),(-2.23,2.1),(-3.38,.28),(-3.13,-2.09),
                 (-1.94,-2.76),(0,-2.9),(1.94,-2.76),(3.13,-2.09),
                 (3.38,.28),(2.23,2.1),(.97,2.25)]
        base("CARRIER / plated drone ship",outline)
        for side in (-1,1):
            for j in range(3):
                block("Carrier modular side cargo bay",(side*2.05,1.24-j*1.03,.58),
                    (1.13,.81,.28),warmgun,.07)
                block("Carrier exposed heat exchanger",(side*2.03,1.20-j*1.03,.76),
                    (.82,.14,.045),carbon,.01)
            cyl("Carrier exterior defensive hardpoint",(side*2.65,.8,.85),
                .32,.32,gunmetal)
        engines("amber",1.67,tail=-2.64);details(4)
        block("Command blister",(0,.25,.92),(.94,1.13,.21),dark,.08)
    elif kind==4: # dedicated bomber: broad, clearly heavier wing / ordnance
        outline=[(0,3.02),(-.52,1.37),(-1.45,.9),(-3.27,.48),(-3.4,-.92),
                 (-2.15,-1.8),(-.78,-2.79),(0,-2.93),(.78,-2.79),
                 (2.15,-1.8),(3.4,-.92),(3.27,.48),(1.45,.9),(.52,1.37)]
        base("TEMPEST / tactical bomber",outline)
        mirrored("Bomber armored ordnance pod",lambda n,s:block(n,
            (s*2.17,-.47,.59),(.79,2.14,.38),carbon,.16))
        for side in (-1,1):
            for off in (-.17,.17):
                cyl("Bomb port/recessed magazine",(side*2.17+off,-.33,.80),
                    .10,1.36,warmgun,"Y")
            beam("Boomerang wing reinforcement",(side*.9,.95,.58),
                (side*3.1,-.72,.48),.23,silver)
        engines("amber",.77,tail=-2.51);details(4)
    elif kind==5: # technological shield-enforcer, angular and armored
        outline=[(0,3.34),(-.86,2.49),(-1.96,1.6),(-2.74,.26),(-3.04,-1.84),
                 (-1.92,-2.48),(0,-3.03),(1.92,-2.48),
                 (3.04,-1.84),(2.74,.26),(1.96,1.6),(.86,2.49)]
        base("SENTINEL / enforcer",outline)
        armor("Sentinel armored sensor forehead",[(0,3.1),(-.76,1.42),(-.32,.74),
            (0,.51),(.32,.74),(.76,1.42)],.64,.80,silver,.055)
        for side in (-1,1):
            for yy in (1.25,-.15,-1.55):
                cyl("Sentinel muted violet shield node",(side*2.10,yy,.78),.22,.17,purple)
        engines("purple",1.10,tail=-2.63);guns(.77,1.7,.67)
    elif kind==6: # detached debris module, small distinct functional shape
        outline=[(0,2.0),(-1.15,1.32),(-2.26,.45),(-1.75,-1.55),
                 (0,-2.37),(1.75,-1.55),(2.26,.45),(1.15,1.32)]
        armor("FRAGMENT / small rotating scrap drone",outline,-.22,.20,carbon,.11)
        armor("Fragment serrated alloy crown",[(0,1.79),(-1.06,.65),(-1.27,-.63),
               (0,-1.76),(1.27,-.63),(1.06,.65)],.22,.51,panel,.08)
        cyl("Exposed mechanical transmission",(0,0,.59),.73,.28,silver)
        cyl("Unprotected energy coupler",(0,0,.77),.44,.07,red)
        for side in (-1,1):
            block("Broken mounting flange",(side*1.41,-.31,.39),(.42,1.2,.22),warmgun,.06)
    else: # kind 7 rare warm metallic star hunter, unique and valuable
        outline=[(0,3.55),(-.62,1.48),(-1.50,1.02),(-3.25,.2),(-3.72,-1.07),
                 (-2.42,-1.48),(-.97,-2.85),(0,-3.15),
                 (.97,-2.85),(2.42,-1.48),(3.72,-1.07),
                 (3.25,.2),(1.50,1.02),(.62,1.48)]
        armor("SOLAR HUNTER / graphite under-hull",outline,-.28,.12,carbon,.11)
        armor("SOLAR HUNTER / brushed champagne plating",[
            (x*.94,y*.94) for x,y in outline],.13,.38,gold,.09)
        armor("SOLAR HUNTER / pale titanium central spine",[
            (0,3.29),(-.55,1.3),(-.81,-1.75),(0,-2.76),
            (.81,-1.75),(.55,1.3)],.43,.68,silver,.06)
        for side in (-1,1):
            beam("Hunter precision gilded wing",(side*.7,1.21,.56),
                (side*3.4,-.8,.43),.17,gold)
            cyl("Hunter radiant optical array",(side*.92,.42,.75),.18,.12,goldlight)
        sphere("Hunter rare gold-glass canopy",(0,.47,.81),(.39,.76,.22),goldlight)
        guns(.94,1.60,.78);engines("gold",.79,tail=-2.57)

def render(kind):
    for ob in ships:bpy.data.objects.remove(ob,do_unlink=True)
    ships.clear()
    model(kind)
    path=os.path.join(renderdir,"enemy_%d.png"%kind)
    scene.render.filepath=path
    bpy.ops.render.render(write_still=True)
    if not os.path.isfile(path) or os.path.getsize(path)<2000:
        raise RuntimeError("Render failed: "+path)
    print("ENEMY ART",kind,path,os.path.getsize(path),flush=True)

for kind in range(8):
    render(kind)
print("ENEMY ART / 8 matching nose-up original hard-surface sprites",flush=True)
