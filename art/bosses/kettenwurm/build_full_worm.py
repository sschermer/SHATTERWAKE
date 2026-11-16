"""SHATTERWAKE Kettenwurm Mk-III / complete 12-node Blender review master.

Run: blender -b -t 4 --python art/bosses/kettenwurm/build_full_worm.py -- OUTPUT_DIR
Source: original procedural geometry extended from the accepted polished Mk-II.
Visual approval required before Android integration. Does NOT change hitboxes or gameplay.
"""
import bpy
import json
import math
import os
import random
import sys
from mathutils import Matrix, Vector

HERE = os.path.dirname(os.path.realpath(__file__))
OUT = os.path.abspath(sys.argv[sys.argv.index("--") + 1]) if "--" in sys.argv else os.path.join(HERE, "full_output")
os.makedirs(OUT, exist_ok=True)
random.seed(12216)

# The previous (approved, polished) generator is the authoritative mechanical source.
# Run its source once to construct the real head/segment prototype collections.
prototype_code = open(os.path.join(HERE, "build_mk2.py"), encoding="utf-8").read()
# Mk-II saves its own two isolated previews. This produces them in a temporary path;
# they are not part of the full-boss review deliverables.
proto_argv = list(sys.argv)
sys.argv = [sys.argv[0], "--", os.path.join(OUT, "_prototype")]
exec(compile(prototype_code, os.path.join(HERE, "build_mk2.py"), "exec"), {"__name__": "__main__"})
sys.argv = proto_argv

scene = bpy.context.scene
scene.render.engine = "CYCLES"
scene.cycles.samples = 96
scene.view_layers[0].cycles.use_denoising = True
scene.render.resolution_x = 1024
scene.render.resolution_y = 1024
scene.render.resolution_percentage = 100
scene.render.image_settings.color_mode = "RGBA"
scene.render.image_settings.color_depth = "8"
scene.render.film_transparent = True

head_seed = bpy.data.collections["MKII_HEAD"]
segment_seed = bpy.data.collections["MKII_SEGMENT"]
mat_red = bpy.data.materials["polished oxblood metallic armor"]
mat_steel = bpy.data.materials["bright precision-polished stainless steel edges"]
mat_dark = bpy.data.materials["smooth graphite gasket seals"]
mat_copper = bpy.data.materials["polished bronze hydraulic pistons"]
mat_glow = bpy.data.materials["orange heat ports"]
mat_optic = bpy.data.materials["cold sensor glass"]
mat_titanium = bpy.data.materials["PBR mirror-polished charcoal titanium"]

def new_mat(name, color, metal=0.9, rough=0.18, emission=0):
    m = bpy.data.materials.new(name)
    m.diffuse_color = (*color, 1)
    m.use_nodes = True
    bs = m.node_tree.nodes.get("Principled BSDF")
    bs.inputs["Base Color"].default_value = (*color, 1)
    bs.inputs["Metallic"].default_value = metal
    bs.inputs["Roughness"].default_value = rough
    if emission:
        bs.inputs["Emission Color"].default_value = (*color, 1)
        bs.inputs["Emission Strength"].default_value = emission
    return m

scorch = new_mat("thermally scorched graphite", (.019, .018, .018), .42, .53)
fracture = new_mat("exposed cherry-red heated fracture", (.95, .08, .015), .36, .22, 4.5)

# Keep exactly the existing type mapping from GameView.Boss.
types = [0] + [3 if i % 5 == 0 else 2 if i % 4 == 0 else 4 if i % 3 == 0 else 1 for i in range(1, 12)]
assert len(types) == 12 and set(types) == {0, 1, 2, 3, 4}

def boss_coordinates(t=1.0, phase=1):
    """Existing GameView world=0 sine trajectory; geometric scale only."""
    points = []
    for i in range(12):
        x = 210 + math.sin(t * 1.2 - i * .58) * (68 + phase * 10)
        y = 155 + i * 28 + math.sin(t * 1.4 - i * .42) * 7
        points.append(Vector(((x-210)/34.0, (155-y)/26.0 + 5.9, 0)))
    return points

def node_angle(points, i):
    # Source models face +Y; orient each to face its predecessor/head.
    v = points[0]-points[1] if i == 0 else points[i-1]-points[i]
    return -math.atan2(v.x, v.y)

def collection(name):
    col = bpy.data.collections.new(name)
    scene.collection.children.link(col)
    return col

# Scene-specific naming separates intact and damaged assembly variants.
intact_collection = collection("REVIEW / INTACT TWELVE NODES")
damage_collection = collection("REVIEW / DAMAGED TWELVE NODES")
all_boss_objects = set()

def new_obj_mesh(col, name, points, faces, material):
    mesh = bpy.data.meshes.new(name + "_mesh")
    mesh.from_pydata(points, [], faces)
    mesh.update()
    o = bpy.data.objects.new(name, mesh)
    col.objects.link(o)
    o.data.materials.append(material)
    all_boss_objects.add(o)
    return o

def beam(col, name, p0, p1, radius, material, sides=12):
    direction = Vector(p1)-Vector(p0)
    mid = (Vector(p0)+Vector(p1))*0.5
    bpy.ops.mesh.primitive_cylinder_add(vertices=sides, radius=radius, depth=max(direction.length, .01), location=mid)
    o = bpy.context.object
    o.name = name
    o.rotation_euler = direction.to_track_quat("Z", "Y").to_euler()
    for c in list(o.users_collection):
        c.objects.unlink(o)
    col.objects.link(o)
    o.data.materials.append(material)
    all_boss_objects.add(o)
    return o

def attach_local_beam(col, name, p, a, b, r, material):
    return beam(col, name, p @ Vector(a), p @ Vector(b), r, material)

def convert_transform(points, i, base_y):
    loc = points[i]
    return Matrix.Translation(loc) @ Matrix.Rotation(node_angle(points, i), 4, "Z") @ Matrix.Translation((0, -base_y, 0))

def clone_segment(col, points, i, mode, variant):
    """Each node retains its ID and independent material/mesh ownership."""
    prefix = ("DAMAGED" if mode else "INTACT") + "_NODE_%02d_TYPE_%d_" % (i, types[i])
    source = head_seed if i == 0 else segment_seed
    trans = convert_transform(points, i, 0 if i == 0 else -2.15)
    damaged = variant in ("cracked", "critical")
    for source_obj in source.objects:
        name = source_obj.name.lower()
        if damaged and "overlapping sloped armor tile" in name:
            # Remove selected discrete armor pieces to expose the mechanism.
            if (len([c for c in name if c.isdigit()])+i+len(name)) % 3 == 1:
                continue
        if variant == "destroyed":
            continue
        o = source_obj.copy()
        if source_obj.type == "MESH":
            o.data = source_obj.data.copy()
        o.name = prefix + source_obj.name
        col.objects.link(o)
        o.matrix_world = trans @ source_obj.matrix_world
        all_boss_objects.add(o)
        if damaged and source_obj.type == "MESH" and "armor" in name and i % 2 == 0:
            o.data.materials.clear()
            o.data.materials.append(scorch if variant == "critical" else mat_red)
    center = trans @ Vector((0, 0 if i == 0 else -2.15, 0.8))
    if variant in ("cracked", "critical"):
        # Cuts and fractured exposed ribs are true geometry, not an image overlay.
        for k in range(5 if variant == "critical" else 3):
            xx = -.42 + k * .19
            y = .17 * math.sin(k*2.4)
            mat = fracture if k % 2 == 0 else mat_steel
            attach_local_beam(col, prefix + "fractured plate seam", trans,
                              (xx, (0 if i == 0 else -2.15) + y, 1.04),
                              (xx + .13, (0 if i == 0 else -2.15) + y + .24, 1.08),
                              .015, mat, )
        if variant == "critical":
            attach_local_beam(col, prefix + "exposed hydraulic leak", trans,
                              (-.68,(0 if i == 0 else -2.15)-.30,.40),
                              (-.36,(0 if i == 0 else -2.15)+.24,.94), .046, mat_copper)
    if variant == "destroyed":
        # Wreckage floats clear of the gameplay node center, exposing chain break.
        for k in range(5):
            a = k * (2*math.pi/5)
            offset = Vector((math.cos(a)*(.40+k*.045), math.sin(a)*(.29+k*.02), .55))
            p = center + offset
            attach_local_beam(col, prefix+"detached shard", Matrix.Translation(p),
                              (-.11,-.04,0),(.11,.08,.08), .077,
                              mat_red if k%2 else mat_steel)
        return
    # Five actual, physically distinguishable node archetypes.
    base_y = 0 if i == 0 else -2.15
    if types[i] == 1:  # weapon carrier
        for side in [-1, 1]:
            attach_local_beam(col, prefix+"twin turret", trans,
                              (side*.73,base_y-.25,.63),(side*.73,base_y+.56,.63),
                              .11,mat_titanium)
            attach_local_beam(col, prefix+"weapon orange muzzle", trans,
                              (side*.73,base_y+.51,.63),(side*.73,base_y+.65,.63),
                              .056,mat_glow)
    elif types[i] == 2:  # split point
        for side in [-1, 1]:
            attach_local_beam(col, prefix+"fracture-release hydraulic ram", trans,
                              (side*.82,base_y-.45,.3),(side*.96,base_y+.37,.54),
                              .105,mat_copper)
            attach_local_beam(col, prefix+"released clasp", trans,
                              (side*.97,base_y-.19,.52),(side*.97,base_y+.18,.52),
                              .05,mat_steel)
    elif types[i] == 3:  # explosive core
        for side in [-1, 1]:
            attach_local_beam(col, prefix+"reactor guard rail", trans,
                              (side*.24,base_y-.35,1.10),(side*.24,base_y+.35,1.10),
                              .075,mat_steel)
        attach_local_beam(col, prefix+"exposed volatile core", trans,
                          (0,base_y-.20,1.14),(0,base_y+.20,1.14),
                          .18,mat_glow)
    elif types[i] == 4:  # EMP/energy equipment
        for side in [-1, 1]:
            attach_local_beam(col, prefix+"capacitor housing", trans,
                              (side*.48,base_y-.30,1.0),(side*.48,base_y+.30,1.0),
                              .12,mat_titanium)
            attach_local_beam(col, prefix+"capacitor lens", trans,
                              (side*.48,base_y-.16,1.13),(side*.48,base_y+.16,1.13),
                              .06,mat_optic)

def connect_live_nodes(col, points, states, prefix):
    for i in range(1, 12):
        if states[i] == "destroyed" or states[i-1] == "destroyed":
            continue
        a, b = points[i-1], points[i]
        direction = (a-b).normalized()
        tangent = Vector((-direction.y,direction.x,0))
        for side in (-1,1):
            offset = tangent*side*.41 + Vector((0,0,.43))
            start = b + (a-b)*.26 + offset
            end = a - (a-b)*.26 + offset
            beam(col, prefix+" articulated double hydraulic joint", start,end,.077,mat_copper,16)
            beam(col, prefix+" armored compression sleeve",
                 start.lerp(end,.28),start.lerp(end,.72),.105,mat_titanium,16)
        beam(col,prefix+" central flexible pressure conduit",
             b.lerp(a,.24)+Vector((0,0,.35)),b.lerp(a,.76)+Vector((0,0,.35)),.12,mat_dark,20)

def build(col, states, prefix):
    points = boss_coordinates()
    for i in range(12):
        clone_segment(col, points, i, states[i] != "intact", states[i])
    connect_live_nodes(col, points, states, prefix)
    return points

intact_states = ["intact"]*12
damaged_states = ["intact","cracked","intact","critical","destroyed",
                  "critical","intact","cracked","cracked","intact","critical","intact"]
points = build(intact_collection, intact_states, "INTACT")
build(damage_collection, damaged_states, "DAMAGED")

# Hide the original prototypes; preserve them in the editable Blender master.
for col in (head_seed, segment_seed):
    for o in col.objects:
        o.hide_render = True

# All templates' original cameras remain, but are superseded by the main cameras.
def camera(name, location, target, ortho):
    bpy.ops.object.camera_add(location=location)
    o = bpy.context.object
    o.name=name
    o.rotation_euler=(Vector(target)-o.location).to_track_quat("-Z","Y").to_euler()
    o.data.type="ORTHO"
    o.data.ortho_scale=ortho
    return o

game_cam = camera("BOSS GAME / orthographic, +Y nose upward",(0,0,36),(0,0,0),17.3)
hero_cam = camera("BOSS REVIEW / shallow three-quarter",(12,-13,38),(0,0,.1),18.0)
detail_cam = camera("BOSS DAMAGE DETAIL",(3,-3,13),(points[5].x, points[5].y,.7),5.6)

def set_variant(name):
    for o in intact_collection.objects:
        o.hide_render = name == "damaged"
    for o in damage_collection.objects:
        o.hide_render = name != "damaged"

def export(filename, camera_obj, variant):
    set_variant(variant)
    scene.camera=camera_obj
    scene.render.filepath=os.path.join(OUT,filename)
    bpy.ops.render.render(write_still=True)
    print("RENDER_OK",filename,flush=True)

export("full_worm_game_intact.png",game_cam,"intact")
export("full_worm_game_damaged.png",game_cam,"damaged")
export("full_worm_hero_intact.png",hero_cam,"intact")
export("full_worm_hero_damaged.png",hero_cam,"damaged")
export("segment_damage_detail.png",detail_cam,"damaged")

# Save the entire editable assembly (both mutually exclusive variants).
set_variant("intact")
scene.camera=game_cam
bpy.ops.wm.save_as_mainfile(filepath=os.path.join(OUT,"kettenwurm_full_master.blend"))

manifest={
    "model": "SHATTERWAKE Kettenwurm / complete twelve-node Mk-II polished source",
    "version": "boss-full-preview-01",
    "source": "Original procedural Blender geometry, polished Mk-II materials",
    "base_main_commit": "3eb83b7880b4a4b3111bd252e21d176726eca56a",
    "node_ids": list(range(12)),
    "node_types": types,
    "state_intact": intact_states,
    "state_damaged": damaged_states,
    "trajectory_reference": "GameView.positionBossNodes: world==0",
    "viewport": {"game_camera":"orthographic +Y up","resolution":[1024,1024],"alpha":True},
    "android_integration": False,
    "visual_approval": "PENDING",
    "credits": "Original procedural Blender geometry; no third-party model, texture, music or font",
}
with open(os.path.join(OUT,"manifest.json"),"w",encoding="utf-8") as f:
    json.dump(manifest,f,ensure_ascii=False,indent=2)
print("COMPLETE: 12 actual segment IDs, five types, intact/damage render collections",flush=True)
