"""SHATTERWAKE Kettenwurm Mk-I. Original procedural Blender master; no external assets.
Usage: blender -b -t 4 --python art/bosses/kettenwurm/build_master.py -- <output-dir>
Produces editable .blend, genuine Cycles/Eevee renders and metadata. No APK edits.
"""
import bpy, math, sys, os, json, hashlib
from mathutils import Vector
from math import sin, cos, pi
out=os.path.abspath(sys.argv[sys.argv.index('--')+1]) if '--' in sys.argv else os.path.abspath('art/bosses/kettenwurm/output')
os.makedirs(out,exist_ok=True)
bpy.ops.object.select_all(action='SELECT');bpy.ops.object.delete(use_global=False)
sc=bpy.context.scene
sc.render.engine='CYCLES';sc.cycles.samples=24
sc.render.resolution_x=1024;sc.render.resolution_y=1024;sc.render.resolution_percentage=100
sc.render.film_transparent=True;sc.render.image_settings.file_format='PNG';sc.render.image_settings.color_mode='RGBA'
sc.render.image_settings.color_depth='8'
sc.world.color=(.04,.05,.07)
def mat(name,color,metal=0,rough=.4,emission=None):
 m=bpy.data.materials.new(name);m.diffuse_color=(*color,1);m.use_nodes=True
 p=m.node_tree.nodes.get('Principled BSDF');p.inputs['Base Color'].default_value=(*color,1)
 p.inputs['Metallic'].default_value=metal;p.inputs['Roughness'].default_value=rough
 if emission:
  p.inputs['Emission Color'].default_value=(*color,1);p.inputs['Emission Strength'].default_value=emission
 return m
steel=mat('gunmetal brushed titanium',(.105,.135,.16),.9,.35)
armor=mat('aged red ceramic armor',(.30,.058,.052),.68,.43)
dark=mat('recessed black graphite',(.023,.032,.04),.65,.48)
silver=mat('worn steel edges',(.38,.41,.43),.9,.29)
orange=mat('amber thermal cores',(.95,.20,.025),.2,.2,5)
cyan=mat('teal navigation sensor',(.035,.45,.56),.15,.18,3)
def cube(name,loc,scale,material,bevel=.06):
 bpy.ops.mesh.primitive_cube_add(size=1,location=loc);o=bpy.context.object;o.name=name
 o.dimensions=scale;bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
 o.data.materials.append(material)
 if bevel:
  mod=o.modifiers.new('machined edge radii','BEVEL');mod.width=bevel;mod.segments=3
  o.modifiers.new('weighted hard surface normals','WEIGHTED_NORMAL')
 return o
def cyl(name,loc,r,depth,material,vertices=32):
 bpy.ops.mesh.primitive_cylinder_add(vertices=vertices,radius=r,depth=depth,location=loc)
 o=bpy.context.object;o.name=name;o.data.materials.append(material)
 mod=o.modifiers.new('machined rim','BEVEL');mod.width=.045;mod.segments=2
 o.modifiers.new('weighted normals','WEIGHTED_NORMAL');return o
def cone(name,loc,r1,r2,depth,material):
 bpy.ops.mesh.primitive_cone_add(vertices=8,radius1=r1,radius2=r2,depth=depth,location=loc)
 o=bpy.context.object;o.name=name;o.data.materials.append(material);return o
# Forward is +Y; camera views the +Z top. Twelve physical assemblies.
def segment(idx,typ):
 y=4.8-idx*.85
 root=bpy.data.collections.new('NODE_%02d_TYPE_%d'%(idx,typ));sc.collection.children.link(root)
 before=set(bpy.context.scene.objects)
 cyl('structural spine %02d'%idx,(0,y,.25),.38,.68,dark)
 cyl('overlapping segmented pressure shell %02d'%idx,(0,y,.38),.57,.62,steel)
 cyl('exposed joint ring %02d'%idx,(0,y-.35,.25),.35,.13,silver)
 for side in (-1,1):
  cube('red bolted armor shell %02d'%idx,(side*.37,y,.65),(.38,.57,.20),armor,.09)
  cube('titanium side spar %02d'%idx,(side*.59,y,.40),(.14,.52,.17),silver,.04)
  for offset in (-.20,.20):
   cyl('armor bolt %02d'%idx,(side*.38,y+offset,.78),.035,.045,silver,12)
  cube('thermal slot %02d'%idx,(side*.39,y,.78),(.08,.25,.035),orange,.02)
 for z in (.44,.72):
  cube('vent channel %02d'%idx,(0,y,z),(.19,.49,.055),dark,.025)
 if typ==0:
  cube('armored spearhead',(0,y+.25,.80),(.78,.93,.27),armor,.12)
  cone('forward crusher blade',(0,y+.83,.65),.43,.025,.95,silver).rotation_euler[0]=pi/2
  for side in (-1,1):
   cone('articulated mandible',(side*.51,y+.56,.56),.22,.03,.85,silver).rotation_euler[0]=pi/2
   cyl('targeting optics',(side*.25,y+.36,.98),.10,.08,cyan)
 elif typ==2:
  for side in (-1,1):cyl('reactor heat port',(side*.36,y,.88),.14,.06,orange)
 elif typ==3:
  cyl('critical reactor core',(0,y,.94),.27,.13,orange)
  for side in (-1,1):cube('reactor cage',(side*.28,y,.95),(.11,.55,.14),silver)
 elif typ==4:
  for side in (-1,1):
   cyl('plasma charge socket',(side*.43,y,.89),.13,.12,cyan)
 elif typ==1:
  for side in (-1,1):
   cyl('embedded turret barrel',(side*.37,y+.26,.82),.095,.46,dark)
   cyl('muzzle aperture',(side*.37,y+.51,.82),.067,.04,orange)
 for o in set(sc.objects)-before:
  for c in list(o.users_collection):c.objects.unlink(o)
  root.objects.link(o)
 return root
types=[0]+[3 if i%5==0 else 2 if i%4==0 else 4 if i%3==0 else 1 for i in range(1,12)]
for i,t in enumerate(types):segment(i,t)
def camera(name,location,look,ortho):
 bpy.ops.object.camera_add(location=location);o=bpy.context.object;o.name=name
 direction=Vector(look)-o.location;o.rotation_euler=direction.to_track_quat('-Z','Y').to_euler()
 o.data.type='ORTHO';o.data.ortho_scale=ortho;return o
game=camera('GAME camera / nose up',(0,0,25),(0,0,.2),13.8)
hero=camera('REVIEW three-quarter camera',(12,-15,17),(0,0,.3),17)
def light(name,loc,power,size):
 bpy.ops.object.light_add(type='AREA',location=loc);o=bpy.context.object;o.name=name
 o.data.energy=power;o.data.shape='DISK';o.data.size=size
 o.rotation_euler=(Vector((0,0,0))-o.location).to_track_quat('-Z','Y').to_euler()
light('large cool studio softbox',(-5,4,10),1900,9)
light('warm rim',(5,-5,7),1450,7)
light('cold rear fill',(2,7,8),850,6)
sc.view_settings.view_transform='AgX'
def render(name,cam):
 sc.camera=cam;sc.render.filepath=os.path.join(out,name);bpy.ops.render.render(write_still=True)
render('kettenwurm_game.png',game)
render('kettenwurm_review.png',hero)
sc.camera=game
blend=os.path.join(out,'kettenwurm_master.blend');bpy.ops.wm.save_as_mainfile(filepath=blend)
manifest={'version':'boss-kettenwurm-mk1-review','source':'original Blender procedural geometry','node_count':12,'node_types':types,'camera_game':'orthographic nose +Y','approval':'3D review pending, no APK integration'}
with open(os.path.join(out,'manifest.json'),'w') as f:json.dump(manifest,f,indent=2)
for name in ['kettenwurm_game.png','kettenwurm_review.png','kettenwurm_master.blend']:
 path=os.path.join(out,name);print('OUTPUT',name,os.path.getsize(path),hashlib.sha256(open(path,'rb').read()).hexdigest(),flush=True)
