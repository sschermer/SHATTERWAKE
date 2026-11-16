"""SHATTERWAKE ART-001, editable high-detail hard-surface fighter and upgrades.
Original project-authored 3D geometry/materials. No third-party model or textures.
Run: blender -b -t 4 --python art/hero_fighter/build_master.py -- art/hero_fighter
"""
import bpy, math, os, sys, random
from mathutils import Vector
R=os.path.abspath(sys.argv[sys.argv.index("--")+1] if "--" in sys.argv else "art/hero_fighter")
P=os.path.join(R,"previews");G=os.path.join(R,"staging","game");W=os.path.join(R,"staging","werft")
for d in (P,G,W):os.makedirs(d,exist_ok=True)
random.seed(119)
bpy.ops.object.select_all(action="SELECT");bpy.ops.object.delete(use_global=False)
scene=bpy.context.scene;scene.render.engine="CYCLES"
scene.cycles.samples=14;scene.cycles.use_denoising=True
scene.render.threads_mode="FIXED";scene.render.threads=4
scene.render.image_settings.file_format="PNG"
scene.render.image_settings.color_mode="RGBA"
scene.render.image_settings.color_depth="8"
scene.render.image_settings.compression=28
scene.render.film_transparent=True
scene.view_settings.view_transform="AgX";scene.view_settings.look="AgX - Medium High Contrast"
base=[];modules={0:[],4:[],5:[]};room=[];extras=[]
current=base

def mat(name,color,metal=.7,rough=.4,wear=.15,emission=None):
 m=bpy.data.materials.new(name);m.diffuse_color=(*color,1);m.use_nodes=True
 nodes=m.node_tree.nodes;links=m.node_tree.links
 p=nodes.get("Principled BSDF")
 p.inputs["Metallic"].default_value=metal
 p.inputs["Roughness"].default_value=rough
 p.inputs["Base Color"].default_value=(*color,1)
 uv=nodes.new("ShaderNodeTexCoord")
 noise=nodes.new("ShaderNodeTexNoise");noise.inputs["Scale"].default_value=19
 noise.inputs["Detail"].default_value=3.5
 links.new(uv.outputs["Generated"],noise.inputs["Vector"])
 ramp=nodes.new("ShaderNodeValToRGB")
 ramp.color_ramp.elements[0].color=(rough*.75,)*3+(1,)
 ramp.color_ramp.elements[1].color=(min(.95,rough+wear),)*3+(1,)
 links.new(noise.outputs["Fac"],ramp.inputs["Fac"])
 links.new(ramp.outputs["Color"],p.inputs["Roughness"])
 fine=nodes.new("ShaderNodeTexNoise");fine.inputs["Scale"].default_value=165
 links.new(uv.outputs["Generated"],fine.inputs["Vector"])
 bump=nodes.new("ShaderNodeBump")
 bump.inputs["Strength"].default_value=.13
 bump.inputs["Distance"].default_value=.009
 links.new(fine.outputs["Fac"],bump.inputs["Height"])
 links.new(bump.outputs["Normal"],p.inputs["Normal"])
 if emission:
  p.inputs["Emission Color"].default_value=(*emission,1)
  p.inputs["Emission Strength"].default_value=2.1
 return m

charcoal=mat("A / scratched oxidized titanium",(.12,.15,.19),.83,.37,.19)
light=mat("B / cold machined alloy",(.36,.4,.44),.85,.29,.13)
slate=mat("C / desaturated ceramic armor",(.24,.26,.28),.55,.46,.18)
black=mat("D / carbon recessed mechanics",(.018,.026,.035),.21,.63,.1)
brushed=mat("E / polished bevel edge",(.55,.60,.62),.83,.24,.15)
ochre=mat("F / worn military hazard paint",(.46,.21,.095),.45,.5,.2)
glass=mat("G / smoked cockpit",(.018,.049,.072),.35,.17,.08)
blue=mat("H / restrained ion blue",(.06,.18,.25),.22,.26,.06,(.04,.26,.49))
warm=mat("I / amber worklight",(.2,.15,.07),.22,.27,.12,(.5,.24,.05))

def add(o,m=None,b=.0):
 if m is not None:o.data.materials.append(m)
 current.append(o)
 if b>0:
  be=o.modifiers.new("Rounded CNC metal edges","BEVEL");be.width=b;be.segments=2
  o.modifiers.new("Hard weighted normals","WEIGHTED_NORMAL")
 return o
def box(name,pos,size,m=charcoal,b=.04):
 bpy.ops.mesh.primitive_cube_add(size=1,location=pos)
 o=bpy.context.object;o.name=name;o.dimensions=size
 bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
 return add(o,m,b)
def cyl(name,pos,r,d,m=charcoal,ax="Z",n=24):
 bpy.ops.mesh.primitive_cylinder_add(vertices=n,radius=r,depth=d,location=pos)
 o=bpy.context.object;o.name=name
 if ax=="Y":o.rotation_euler.x=math.pi/2
 if ax=="X":o.rotation_euler.y=math.pi/2
 return add(o,m,.02 if r>.09 else 0)
def ring(name,pos,r,t,m=brushed,ax="Z"):
 bpy.ops.mesh.primitive_torus_add(major_radius=r,minor_radius=t,major_segments=32,
                                 minor_segments=8,location=pos)
 o=bpy.context.object;o.name=name
 if ax=="Y":o.rotation_euler.x=math.pi/2
 return add(o,m)
def tube(name,a,b,r,m=charcoal):
 a,b=Vector(a),Vector(b);d=b-a
 bpy.ops.mesh.primitive_cylinder_add(vertices=10,radius=r,depth=d.length,location=(a+b)*.5)
 o=bpy.context.object;o.name=name
 o.rotation_euler=d.to_track_quat("Z","Y").to_euler()
 return add(o,m)
def panel(name,points,z,thick,m=charcoal,b=.035):
 n=len(points);v=[(x,y,z) for x,y in points]+[(x,y,z-thick) for x,y in points]
 f=[tuple(range(n)),tuple(range(2*n-1,n-1,-1))]
 f += [(i,(i+1)%n,(i+1)%n+n,i+n) for i in range(n)]
 mesh=bpy.data.meshes.new(name+" mesh");mesh.from_pydata(v,[],f);mesh.update()
 o=bpy.data.objects.new(name,mesh);scene.collection.objects.link(o)
 return add(o,m,b)
def loft(name,sections,m):
 # longitudinal rings: y, halfwidth, crown height, undercarriage height
 vs=[]
 for y,wide,z,under in sections:vs.extend([(-wide,y,z),(wide,y,z),(wide,y,under),(-wide,y,under)])
 faces=[]
 for i in range(len(sections)-1):
  a=i*4;b=a+4
  faces.extend([(a,a+1,b+1,b),(a+1,a+2,b+2,b+1),(a+2,a+3,b+3,b+2),(a+3,a,b,b+3)])
 faces.extend([(3,2,1,0),tuple((len(sections)-1)*4+k for k in range(4))])
 mesh=bpy.data.meshes.new(name+" mesh");mesh.from_pydata(vs,[],faces);mesh.update()
 o=bpy.data.objects.new(name,mesh);scene.collection.objects.link(o)
 return add(o,m,.045)
def mark(txt,pos,size=.21):
 cv=bpy.data.curves.new("serial","FONT");cv.body=txt;cv.size=size;cv.extrude=.002
 o=bpy.data.objects.new("Etched "+txt,cv);scene.collection.objects.link(o);o.location=pos
 return add(o,light)

# Central silhouette: real faceted tapered armored fuselage, nose along +Y.
loft("Main fighter frame",[
 (-5.25,.7,.35,-.6),(-4.3,1.05,.6,-.72),(-2.65,1.2,.85,-.73),
 (-.15,1.16,1.08,-.61),(1.25,.91,1.05,-.5),
 (2.75,.63,.81,-.34),(4.1,.43,.55,-.16),(5.85,.065,.24,-.08)],charcoal)
panel("Long stacked dorsal armored spine",[
 (-.68,-4.05),(.68,-4.05),(.94,-1.65),(.62,1.1),
 (.35,3.78),(0,5.12),(-.35,3.78),(-.62,1.1)],1.15,.09,light)
for s in (-1,1):
 side=lambda pairs:[(s*x,y) for x,y in pairs]
 panel("Port/starboard upper cheek "+str(s),side([
   (.58,4.05),(1.13,2.1),(1.23,.35),(.89,-.65),(.63,.35)]),.98,.19,slate)
 # Individually shaped body wings with interior carbon and armor skirts.
 panel("Wing exposed titanium spar "+str(s),side([
  (.9,2.1),(1.5,2.09),(5.84,.48),(5.9,-1.47),
  (4.56,-2.32),(2.36,-3.66),(1.03,-3.7)]),.22,.45,charcoal,.11)
 panel("Wing leading armored top skin "+str(s),side([
  (1.28,1.79),(2.06,1.73),(5.35,.42),(4.56,-.35),
  (2.84,-.2),(1.22,.43)]),.43,.15,light)
 panel("Outer structural armored facet "+str(s),side([
  (3.08,-.58),(5.08,.24),(5.69,-.38),(5.24,-1.42),
  (4.22,-1.72),(3,-1.56)]),.47,.11,slate)
 panel("Aft articulated control plane "+str(s),side([
  (1.24,-1.64),(3.98,-1.88),(4.35,-2.19),
  (2.55,-3.39),(1.22,-3.28)]),.37,.12,light)
 tube("Reinforced exposed leading-edge longeron",(s*1.54,1.89,.56),
      (s*5.72,.43,.46),.056,brushed)
 # Radiator trench with separate insert metal slats.
 panel("Recessed heat-exchanger well "+str(s),side([
  (1.45,.24),(2.52,-.03),(2.68,-.72),(1.5,-.54)]),.56,.04,black,.02)
 for j in range(8):
  box("Machined cooling slot %s %s"%(s,j),
      (s*(1.54+.135*j),-.12-.043*j,.59),(.08,.30,.034),brushed,.008)
 for j in range(8):
  x=1.93+j*.46;y=.75-j*.16
  box("Armor access panel %s %s"%(s,j),(s*x,y,.55),(.25,.18,.043),
      slate if j%3==0 else charcoal,.02)
  cyl("Flush captive bolt %s %s"%(s,j),(s*x,y,.59),.03,.022,brushed,n=12)
 for j in range(4):
  box("Aft wing service access %s %s"%(s,j),
      (s*(2.08+j*.45),-2.22-j*.12,.48),(.3,.13,.03),slate,.01)
 for j in range(11):
  y=3.3-j*.62
  x=s*(.73+.08*math.sin(j*.5))
  cyl("Micro-rivet flush %s %s"%(s,j),(x,y,1.205),.025,.016,black,n=10)
  cyl("Micro-rivet highlight %s %s"%(s,j),(x,y,1.219),.01,.009,brushed,n=10)
 panel("Military orange identification stripe "+str(s),side([
  (.84,.55),(1.05,.51),(1.07,-.91),(.88,-.87)]),1.18,.005,ochre,.005)
 for j in range(4):
  box("Rumpf vent defense plate %s %s"%(s,j),
      (s*(.75+j*.05),-1.7-j*.39,1.205),(.26,.2,.025),light,.016)
 mark("SW-07",(s*3.28,-1.33,.61))
 # Dual heavily enclosed physical engines, cylinder shafts along Y.
 u=s*2.13
 cyl("Engine main armored turbine body "+str(s),(u,-2.7,.45),.72,3.74,charcoal,"Y",32)
 cyl("Engine compressor front bevel "+str(s),(u,-.82,.45),.73,.39,slate,"Y",30)
 for y in (-4.51,-4.0,-3.55,-2.9,-2.12,-1.3):
  ring("Machined circumferential turbine band",(u,y,.45),.69,.065,brushed,"Y")
 cyl("Visible exhaust black nozzle",(u,-4.67,.45),.59,.2,black,"Y",32)
 ring("Exhaust inner constriction",(u,-4.78,.45),.44,.09,slate,"Y")
 cyl("Sunken restrained blue ion exit",(u,-4.83,.45),.37,.06,blue,"Y",32)
 for k in range(12):
  angle=k*math.tau/12
  tube("Turbine radial metal vane",(u,-4.85,.45),
       (u+.53*math.cos(angle),-4.85,.45+.53*math.sin(angle)),.025,black)
 box("Armored engine back housing",(u,-2.25,1.07),(.78,2.3,.21),charcoal,.06)
 for j in range(8):
  box("Working engine radiator grille",(u,-3.27+j*.24,1.19),
      (.49,.11,.038),black,.008)
 for j in range(6):
  box("Nacelle intake port",(u+s*.69,-3.12+j*.29,.54),
      (.055,.14,.28),slate,.012)
 for v in (-2.84,-1.55):
  tube("Engine-hull structural tie",(s*.83,v,.67),(u,v,.83),.1,brushed)
 panel("Tall canted rear stabilizer "+str(s),side([
  (.62,-4.72),(1.12,-4.41),(1.46,-2.67),(1.05,-2.32)]),
  1.62,.3,slate,.06)
 for j in range(4):
  box("Stabilizer shutter",(s*1.16,-3.98+j*.26,1.68),(.16,.12,.04),black,.012)

# Cockpit: sectional glass, exposed heavy multi-segmented canopy framing.
panel("Riveted cockpit underframe",[(-.61,-.65),(.61,-.65),(.71,1.05),
 (.47,2.44),(0,3.16),(-.47,2.44),(-.71,1.05)],1.23,.065,black,.055)
panel("Highly polished armored smoked glass",[(-.43,-.43),(.43,-.43),
 (.51,1.01),(.35,2.35),(0,2.89),(-.35,2.35),(-.51,1.01)],1.31,.055,glass,.03)
for s in (-1,1):
 tube("Dorsal canopy reinforced edge",(s*.55,-.5,1.38),(s*.39,2.37,1.39),
      .045,brushed)
tube("Canopy rear transverse crossbar",(-.47,.5,1.4),(.47,.5,1.4),.045,brushed)
tube("Canopy front brace",(-.35,2.37,1.38),(.35,2.37,1.38),.025,brushed)
for j in range(10):
 y=-4.1+j*.26
 box("Dorsal layered service spine cell",(0,y,1.27),(.31,.12,.04),black,.014)
 if j%2==0:box("Inset heat sink seam",(0,y,1.298),(.22,.02,.02),brushed,.004)
panel("Forward nose armored sensor cover",[(-.3,4.78),(.3,4.78),
 (.38,4.04),(0,3.79),(-.38,4.04)],.74,.1,slate,.025)
cyl("Recessed forward laser scanner",(0,4.37,.86),.15,.07,black)
cyl("Hardened optical sensor lens",(0,4.37,.905),.078,.03,blue)
for j in range(6):
 box("Nose thermal relief vent", (0,3.15+j*.12,.98),(.33,.054,.024),black,.006)
for s in (-1,1):
 for j in range(5):
  box("Aft finned navigation strips",(s*(1.16+j*.21),-3.91,.38),
      (.145,.05,.026),blue,.005)

# Three physically modeled, separately hideable upgrades.
current=modules[0]
for s in (-1,1):
 u=s*2.89
 box("Rapidfire / gun pivot hardpoint",(u,.5,.68),(.7,1.42,.42),black,.08)
 box("Rapidfire / armored twin cannon casing",(u,1.26,.86),(.7,.58,.38),charcoal,.075)
 for dx in (-.15,.15):
  cyl("Rapidfire / fully bored twin heavy barrel",(u+dx,2.37,.94),
      .118,2.4,brushed,"Y",24)
  cyl("Rapidfire / dark rifled muzzle",(u+dx,3.61,.94),
      .077,.16,black,"Y",24)
  ring("Rapidfire / lathe-turned barrel ring",
       (u+dx,3.34,.94),.119,.028,slate,"Y")
 for j in range(6):
  box("Rapidfire / functional heat exchanger",(u,1.12-j*.19,1.09),
      (.55,.085,.064),brushed,.009)
 for j in range(4):
  cyl("Rapidfire / mount bolt",(u+s*.3,.15+j*.3,1.07),.033,.035,ochre)

current=modules[5]
for s in (-1,1):
 u=s*3.25
 cyl("Shield / insulated emitter base",(u,-.04,.7),.51,.22,charcoal,n=32)
 cyl("Shield / silver housing",(u,-.04,.84),.43,.18,brushed,n=32)
 ring("Shield / superconductive metallic ring",(u,-.04,.96),.34,.05,slate)
 cyl("Shield / focal optics visible disc",(u,-.04,.98),.27,.07,blue,n=32)
 for j in range(8):
  a=j*math.tau/8
  cyl("Shield / retaining fastener",
      (u+.405*math.cos(a),-.04+.405*math.sin(a),1.015),
      .026,.032,black,n=12)
 tube("Shield / mechanical armored cabling",
      (u,-.61,.68),(s*2.0,-1.15,.76),.07,black)

current=modules[4]
for s in (-1,1):
 u=s*3.45
 box("Drones / machined docking cradle",(u,-2,.55),(.87,1.58,.25),black,.085)
 box("Drones / retained armored launch rack",(u,-1.96,.75),(.96,1.26,.19),light,.075)
 box("Drones / guide rail center recess",(u,-1.95,.88),(.43,1.07,.10),charcoal,.03)
 for t in (-1,1):
  tube("Drones / tensile retention guide",(u+t*.4,-2.47,.88),
       (u+t*.4,-1.42,.88),.065,brushed)
 cyl("Drones / robotic charging socket",(u,-1.95,1.0),.22,.11,slate)
 ring("Drones / restrained induction rim",(u,-1.95,1.08),.16,.037,blue)
 box("Drones / folded mechanical craft",(u,-1.95,1.13),
     (.38,.54,.11),charcoal,.035)
 for t in (-1,1):
  panel("Drones / articulated folded winglet",[
   (u+t*.13,-1.72),(u+t*.33,-1.56),
   (u+t*.36,-1.98),(u+t*.13,-2.08)],1.18,.045,brushed,.013)

# Cinematic look-dev lights. All shots share these exact lights/materials.
current=extras
def area(name,position,power,color,size,target=(0,0,0)):
 bpy.ops.object.light_add(type="AREA",location=position)
 o=bpy.context.object;o.name=name
 o.data.energy=power;o.data.shape="DISK";o.data.size=size;o.data.color=color
 o.rotation_euler=(Vector(target)-o.location).to_track_quat("-Z","Y").to_euler()
 extras.append(o)
area("Hangar / massive left diffused softbox",(-9,5,15),2650,(.81,.9,1),8)
area("Hangar / right cooler bounce",(10,-1,12),1800,(.55,.75,1),6)
area("Hangar / orange engine rim",(0,-7,12),1280,(1,.76,.5),6)
area("Hangar / frontal detail",(0,10,11),740,(.7,.82,1),5)

def cam(name,where,lookat,size):
 bpy.ops.object.camera_add(location=where)
 o=bpy.context.object;o.name=name
 o.rotation_euler=(Vector(lookat)-o.location).to_track_quat("-Z","Y").to_euler()
 o.data.type="ORTHO";o.data.ortho_scale=size
 return o
game_cam=cam("ART-001 GAME / nose is +Y, rendered UP",(0,0,29),(0,0,0),14.9)
werft_cam=cam("ART-001 WERFT / cinematic 3-quarter",
              (11,15,19),(0,0,.15),16.9)
scene.camera=werft_cam
scene.world.use_nodes=True
bg=scene.world.node_tree.nodes.get("Background")
bg.inputs["Color"].default_value=(.1,.14,.19,1);bg.inputs["Strength"].default_value=.35

# Showroom hangar is a separate background, never baked into transparent layers.
current=room
box("Industrial drydock floor",(0,0,-2.6),(26,25,.44),black,.12)
for x in (-9,9):
 box("Hangar yellow maintenance path",(x,0,-2.34),(.11,24,.03),ochre,.01)
for y in [-10,-6,-2,2,6,10]:
 box("Drydock recessed floor seam",(0,y,-2.32),(24,.075,.045),charcoal,.009)
for x in (-7.7,7.7):
 for y in (-8,-4,0,4,8):
  box("Hangar deck flood lamp base",(x,y,-2.25),(.55,.3,.07),black,.02)
  box("Muted amber floor work light",(x,y,-2.21),(.39,.09,.025),warm,.004)
for x in (-11,11):
 box("Hangar structural bulkhead",(x,6,2),(.7,18,10),black,.12)
 for y in (-6,-2,2,6):
  box("Hangar reinforced support beam",(x,y,2.9),(.8,.21,7.8),charcoal,.04)
box("Hangar overhead gantry",(0,10.9,6),(24,1.1,.7),charcoal,.08)
for x in (-8,-4,0,4,8):
 tube("Overhead maintenance suspension cable",
      (x,10.2,5.7),(x*.94,7.9,3.3),.077,black)
for o in room:o.hide_render=True
# Save complete editable source before final render – groups and cameras intact.
bpy.ops.wm.save_as_mainfile(filepath=os.path.join(R,"hero_fighter.blend"))
def show(base_on,ids,hangar_on):
 for o in base:o.hide_render=not base_on
 for id,collection in modules.items():
  for o in collection:o.hide_render=(id not in ids)
 for o in room:o.hide_render=not hangar_on
def render(path,camera,dimension,base_on=True,ids=(),hangar_on=False):
 show(base_on,ids,hangar_on)
 scene.camera=camera
 scene.render.resolution_x,scene.render.resolution_y=dimension
 scene.render.film_transparent=not hangar_on
 scene.render.filepath=path
 print("ART-001 RENDER",path,flush=True)
 bpy.ops.render.render(write_still=True)
 if not os.path.isfile(path) or os.stat(path).st_size<500:
  raise RuntimeError("Empty/missing render: "+path)

render(os.path.join(P,"werft_before.png"),werft_cam,(1000,900),hangar_on=True)
render(os.path.join(P,"werft_weapons.png"),werft_cam,(1000,900),ids=(0,),hangar_on=True)
render(os.path.join(P,"werft_shield.png"),werft_cam,(1000,900),ids=(5,),hangar_on=True)
render(os.path.join(P,"werft_drones.png"),werft_cam,(1000,900),ids=(4,),hangar_on=True)
render(os.path.join(P,"game_nose_up.png"),game_cam,(1024,1024))
render(os.path.join(G,"fighter_base.png"),game_cam,(1024,1024))
for id in (0,4,5):
 render(os.path.join(G,"fighter_module_%s.png"%id),game_cam,(1024,1024),False,(id,))
render(os.path.join(W,"fighter_base.png"),werft_cam,(2048,2048))
for id in (0,4,5):
 render(os.path.join(W,"fighter_module_%s.png"%id),werft_cam,(2048,2048),False,(id,))
print("ART-001: ORIGINAL MASTER + THREE REAL MODULES + ALIGNED GAME/WERFT EXPORTS READY")
