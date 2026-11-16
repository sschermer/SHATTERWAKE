"""SHATTERWAKE Kettenwurm Mk-II head + one armor unit. Original procedural Blender.
Render-only quality gate; does not alter Android gameplay. Blender 4.3+.
"""
import bpy, math, os, sys, json, random
from mathutils import Vector
from math import pi
out=os.path.abspath(sys.argv[sys.argv.index('--')+1]) if '--' in sys.argv else os.path.abspath('art/bosses/kettenwurm/mk2_output')
os.makedirs(out,exist_ok=True)
random.seed(27011)
bpy.ops.object.select_all(action='SELECT');bpy.ops.object.delete(use_global=False)
sc=bpy.context.scene
sc.render.engine='CYCLES';sc.cycles.samples=48
sc.render.resolution_x=1024;sc.render.resolution_y=1024;sc.render.resolution_percentage=100
sc.render.film_transparent=True;sc.render.image_settings.file_format='PNG';sc.render.image_settings.color_mode='RGBA'
sc.view_settings.view_transform='AgX'
def material(name,c,metal=0.8,rough=.35,emit=0):
 m=bpy.data.materials.new(name);m.diffuse_color=(*c,1);m.use_nodes=True
 bs=m.node_tree.nodes.get('Principled BSDF')
 bs.inputs['Base Color'].default_value=(*c,1);bs.inputs['Metallic'].default_value=metal;bs.inputs['Roughness'].default_value=rough
 if emit:bs.inputs['Emission Color'].default_value=(*c,1);bs.inputs['Emission Strength'].default_value=emit
 if metal>.5 and not emit:
  tex=m.node_tree.nodes.new('ShaderNodeTexNoise');tex.inputs['Scale'].default_value=95;tex.inputs['Detail'].default_value=3
  bump=m.node_tree.nodes.new('ShaderNodeBump');bump.inputs['Strength'].default_value=.13;bump.inputs['Distance'].default_value=.028
  m.node_tree.links.new(tex.outputs['Fac'],bump.inputs['Height']);m.node_tree.links.new(bump.outputs['Normal'],bs.inputs['Normal'])
 return m
titanium=material('PBR charcoal titanium / micro-machining',(.075,.091,.105),.92,.29)
red=material('scarred oxblood ceramic armor',(.29,.045,.035),.64,.38)
edge=material('brushed silver steel exposed edges',(.35,.37,.38),.94,.28)
carbon=material('carbon fiber seals',(.017,.021,.025),.42,.63)
copper=material('aged bronze hydraulics',(.32,.19,.09),.85,.3)
amber=material('orange heat ports',(.95,.19,.02),.18,.2,5)
optic=material('cold sensor glass',(.025,.32,.48),.26,.14,3)
def assign(o,name,m):
 o.name=name;o.data.materials.append(m);return o
def bevel(o,w=.055):
 mod=o.modifiers.new('CNC bevels','BEVEL');mod.width=w;mod.segments=3
 o.modifiers.new('weighted normals','WEIGHTED_NORMAL');return o
def box(name,xyz,dim,mat,w=.05):
 bpy.ops.mesh.primitive_cube_add(size=1,location=xyz);o=bpy.context.object
 o.dimensions=dim;bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
 assign(o,name,mat);return bevel(o,w) if w else o
def cyl(name,xyz,r,depth,mat,verts=32,axis='Z'):
 bpy.ops.mesh.primitive_cylinder_add(vertices=verts,radius=r,depth=depth,location=xyz);o=bpy.context.object
 assign(o,name,mat)
 if axis=='Y':o.rotation_euler[0]=pi/2
 elif axis=='X':o.rotation_euler[1]=pi/2
 return bevel(o,.018)
def sphere(name,xyz,scale,mat):
 bpy.ops.mesh.primitive_uv_sphere_add(segments=24,ring_count=12,location=xyz);o=bpy.context.object;o.scale=scale
 return assign(o,name,mat)
def blade(name,pts,thick,mat):
 verts=[(x,y,z) for x,y,z in pts]+[(x,y,z-thick) for x,y,z in pts]
 n=len(pts);faces=[tuple(range(n)),tuple(range(2*n-1,n-1,-1))]
 faces += [(i,(i+1)%n,(i+1)%n+n,i+n) for i in range(n)]
 mesh=bpy.data.meshes.new(name);mesh.from_pydata(verts,[],faces);mesh.update()
 o=bpy.data.objects.new(name,mesh);sc.collection.objects.link(o);o.data.materials.append(mat)
 return bevel(o,.015)
def rod(name,a,b,r,mat):
 v=Vector(b)-Vector(a);mid=(Vector(a)+Vector(b))/2
 o=cyl(name,mid,r,v.length,mat,16)
 o.rotation_euler=v.to_track_quat('Z','Y').to_euler();return o
def create(kind,cy):
 before=set(sc.objects)
 # Root mechanism: toroidal bearing, pressure core, multiple nested rings
 cyl('pressurized graphite spinal chassis',(0,cy,.38),.62,.74,carbon,48)
 for dy,r in [(-.31,.63),(-.17,.72),(.18,.72),(.33,.63)]:
  cyl('machined circumferential armor collar',(0,cy+dy,.41),r,.065,edge,48,'Y')
 for x in [-.46,.46]:
  for z in [.18,.48,.77]:
   rod('segmented hydraulic feed',(x,cy-.40,z),(x,cy+.38,z+.03),.052,copper)
   for dy in [-.27,.26]:
    cyl('joint bearing fastener',(x,cy+dy,z),.088,.09,titanium,16,'Y')
 for side in [-1,1]:
  # Overlapping individual armor plates, angular edges, seams and bolts
  for k in range(3):
   y=cy-.25+k*.25
   x=side*(.40+(.04 if k==1 else 0))
   plate=box('overlapping sloped armor tile',(x,y,.79),(.46,.27,.17),red,.043)
   plate.rotation_euler[1]=side*.22
   box('recessed armor edge trim',(x,y+.09,.865),(.42,.023,.028),edge,.008)
   for yy in [-.085,.075]:
    cyl('countersunk bolt',(x+side*.13,y+yy,.89),.026,.026,edge,12)
  box('longitudinal outer exoskeletal rib',(side*.72,cy,.50),(.14,.73,.14),titanium,.035)
  for dy in [-.21,.19]:
   cyl('side actuator piston',(side*.72,cy+dy,.53),.085,.12,copper,20,'Y')
  box('embedded reactor grille',(side*.45,cy,.91),(.15,.32,.036),carbon,.018)
  for j in range(5):
   box('illuminated vent slit',(side*.45,cy-.125+j*.062,.936),(.095,.027,.012),amber,.005)
 # Centerline dorsal segmented plate
 for dy in [-.23,.02,.27]:
  box('dorsal central titanium spine',(0,cy+dy,.97),(.26,.20,.11),titanium,.03)
  for side in [-1,1]:
   cyl('spine mounting pin',(side*.11,cy+dy,.995),.026,.02,edge,12)
 if kind=='head':
  # Wide jaw mechanism: sharply faceted wedge, independently hinged pincer arms
  box('central sensor skull',(0,cy+.16,1.05),(.88,.87,.32),titanium,.13)
  blade('faceted crimson skull armor',[(-.42,cy-.17,1.22),(.42,cy-.17,1.22),(.29,cy+.52,1.25),(0,cy+.85,1.03),(-.29,cy+.52,1.25)],.14,red)
  blade('steel nose splitting wedge',[(-.18,cy+.50,1.24),(.18,cy+.50,1.24),(0,cy+1.17,.86)],.11,edge)
  for side in [-1,1]:
   x=side*.49
   cyl('mandible hinge pivot',(x,cy+.18,.76),.21,.22,edge,32,'X')
   rod('exposed jaw hydraulic ram',(side*.62,cy-.27,.69),(side*.72,cy+.45,.72),.075,copper)
   blade('articulated curved crushing jaw',[(side*.58,cy+.08,.90),(side*1.04,cy+.33,.92),(side*.91,cy+.91,.74),(side*.37,cy+1.43,.52),(side*.62,cy+.71,.70)],.16,edge)
   blade('outer red mandibular armor',[(side*.70,cy+.19,1.04),(side*1.09,cy+.37,1.02),(side*.84,cy+.83,.90)],.09,red)
   cyl('targeting sensor housing',(side*.36,cy+.38,1.26),.17,.11,carbon)
   cyl('recessed blue optical lens',(side*.36,cy+.38,1.33),.09,.04,optic)
   rod('coaxial weapon barrel',(side*.76,cy-.08,.74),(side*.76,cy+.76,.74),.105,titanium)
   cyl('orange weapon muzzle',(side*.76,cy+.80,.74),.085,.05,amber,24,'Y')
  cyl('open thermal intake throat',(0,cy+.53,.78),.32,.12,carbon)
  cyl('inner active heat reactor',(0,cy+.53,.86),.21,.08,amber)
 else:
  for side in [-1,1]:
   rod('underplate flexible drive linkage',(side*.61,cy-.42,.22),(side*.61,cy+.43,.23),.085,edge)
   cyl('service port',(side*.48,cy,.99),.11,.06,carbon)
   cyl('service indicator',(side*.48,cy,1.035),.065,.015,optic)
 created=list(set(sc.objects)-before)
 coll=bpy.data.collections.new('MKII_'+kind.upper());sc.collection.children.link(coll)
 for o in created:
  for c in list(o.users_collection):c.objects.unlink(o)
  coll.objects.link(o)
 return coll
head=create('head',0)
seg=create('segment',-2.15)
def camera(name,xyz,target,scale):
 bpy.ops.object.camera_add(location=xyz);o=bpy.context.object;o.name=name
 o.rotation_euler=(Vector(target)-o.location).to_track_quat('-Z','Y').to_euler()
 o.data.type='ORTHO';o.data.ortho_scale=scale;return o
cam=camera('three quarter quality-review camera',(6,-8,11),(0,-.9,.45),5.8)
top=camera('GAME ortho nose +Y',(0,-.5,12),(0,-.5,.4),5.6)
def light(name,xyz,power,size,color):
 bpy.ops.object.light_add(type='AREA',location=xyz);o=bpy.context.object;o.name=name
 o.data.energy=power;o.data.shape='DISK';o.data.size=size;o.data.color=color
 o.rotation_euler=(-o.location).to_track_quat('-Z','Y').to_euler()
light('large cool softbox',(-4,3,8),1600,6,(.70,.85,1))
light('warm edge light',(4,-3,6),1100,4,(1,.60,.35))
light('rear silver rim',(1,5,7),1900,3,(.65,.85,1))
def render(name,cam):
 sc.camera=cam;sc.render.filepath=os.path.join(out,name);bpy.ops.render.render(write_still=True)
render('mk2_head_segment_review.png',cam)
render('mk2_head_segment_game.png',top)
sc.camera=cam
bpy.ops.wm.save_as_mainfile(filepath=os.path.join(out,'kettenwurm_mk2_master.blend'))
with open(os.path.join(out,'manifest.json'),'w') as f:
 json.dump({'purpose':'visual review only','units':['head','segment'],'engine':'Cycles','resolution':[1024,1024],'source':'100% procedural original Blender geometry','android_integration':False},f,indent=2)
