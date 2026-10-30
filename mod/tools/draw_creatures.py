"""Builds the Nightmare Creature models: GeckoLib geometry, animations and textures.

Usage: python3 mod/tools/draw_creatures.py [assets/shadowslave directory]

Each creature is a list of bones made of boxes. Every box names a material;
the script packs the boxes into a texture with box UVs and paints each face
from its material (base colour, shading, noise and a pattern), so a model
change is an edit here and a rerun. Glowing materials also go into
<name>_glowmask.png, which GeckoLib draws at full brightness.

Animations are written as sampled curves (catmull-rom between samples) from
small functions of time, so a loop is a few lines of maths.

It also draws the Gate's rift (an entity, built like a creature) and the
Gate Lure block's charge models and textures.

Coordinates are Bedrock model units: 16 per block, y up from the feet,
the creature faces -z. Needs Pillow.
"""
import json
import math
import random
import sys
from pathlib import Path

from PIL import Image

ASSETS = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path(__file__).resolve().parent.parent / "src/main/resources/assets/shadowslave")

# ---------------------------------------------------------------- materials
# name: (base rgb, pattern, glows)
MATERIALS = {
    "ash": ((84, 76, 72), "cracks", False),
    "ash_dark": ((52, 46, 44), "cracks", False),
    "ash_plate": ((96, 88, 82), "plates", False),
    "ember_seam": ((70, 60, 56), "ember_cracks", True),
    "ember": ((255, 128, 36), "ember", True),
    "bone": ((214, 204, 182), "bone", False),
    "claw": ((40, 34, 32), "smooth", False),
    "iron": ((64, 66, 72), "rivets", False),
    "iron_plate": ((74, 74, 80), "rust_plates", False),
    "rust": ((128, 64, 30), "rust", False),
    "chain": ((92, 92, 98), "chain", False),
    "skin_pale": ((196, 190, 180), "ribs", False),
    "skin_grey": ((150, 144, 138), "smooth", False),
    "mask": ((232, 227, 216), "mask", False),
    "shroud": ((34, 31, 38), "cloth", False),
    "hollow": ((12, 10, 14), "smooth", False),
    "eye_ember": ((255, 170, 60), "ember", True),
    "rift": ((150, 70, 240), "rift", False),
    "rift_core": ((235, 215, 255), "rift", False),
    "rift_edge": ((46, 18, 70), "cracks", False),
    "rift_shard": ((24, 14, 34), "shard", False),
    "bark": ((62, 44, 38), "cracks", False),
    "bark_pale": ((150, 132, 116), "mask", False),
    "briar": ((36, 42, 34), "thorns", False),
    "thorn": ((200, 182, 156), "bone", False),
    "bloom": ((214, 36, 58), "petal", True),
}

SHADE = {"up": 1.12, "down": 0.62, "north": 1.0, "south": 0.82, "east": 0.9, "west": 0.9}


def box(origin, size, material, inflate=0.0, rotation=None, pivot=None, front=None):
    """A model box. front overrides the material on the -z face (faces, masks)."""
    return {"origin": origin, "size": size, "material": material, "inflate": inflate,
            "rotation": rotation, "pivot": pivot, "front": front}


def bone(name, pivot, cubes=(), parent=None, rotation=None):
    return {"name": name, "pivot": pivot, "cubes": list(cubes), "parent": parent, "rotation": rotation}


def mirror(bones, prefix_from="left_", prefix_to="right_"):
    """Copies bones named left_* to right_*, flipped across x."""
    out = []
    for b in bones:
        if not b["name"].startswith(prefix_from):
            continue
        nb = dict(b)
        nb["name"] = prefix_to + b["name"][len(prefix_from):]
        if b["parent"] and b["parent"].startswith(prefix_from):
            nb["parent"] = prefix_to + b["parent"][len(prefix_from):]
        nb["pivot"] = [-b["pivot"][0], b["pivot"][1], b["pivot"][2]]
        if b["rotation"]:
            nb["rotation"] = [b["rotation"][0], -b["rotation"][1], -b["rotation"][2]]
        nb["cubes"] = []
        for c in b["cubes"]:
            nc = dict(c)
            nc["origin"] = [-c["origin"][0] - c["size"][0], c["origin"][1], c["origin"][2]]
            if c["pivot"]:
                nc["pivot"] = [-c["pivot"][0], c["pivot"][1], c["pivot"][2]]
            if c["rotation"]:
                nc["rotation"] = [c["rotation"][0], -c["rotation"][1], -c["rotation"][2]]
            nb["cubes"].append(nc)
        out.append(nb)
    return bones + out


# ---------------------------------------------------------------- creatures

def ash_burrower():
    b = [
        bone("root", [0, 0, 0]),
        bone("body", [0, 6, 0], parent="root", cubes=[
            box([-5, 3, -6], [10, 6, 13], "ash"),
            box([-5, 7, -5], [10, 3, 4], "ash_plate", 0.25),
            box([-5, 7, -1], [10, 3, 4], "ash_plate", 0.25),
            box([-4.5, 6.5, 3], [9, 3, 4], "ash_plate", 0.25),
            box([-4, 3, -6], [8, 1, 12], "ember_seam", 0.05),
        ]),
        bone("ridge", [0, 10, 0], parent="body", cubes=[
            box([-0.5, 9, -4], [1, 4, 1], "bone", rotation=[-20, 0, 0], pivot=[0, 9, -3.5]),
            box([-0.5, 9, -1], [1, 5, 1], "bone", rotation=[-25, 0, 0], pivot=[0, 9, -0.5]),
            box([-0.5, 9, 2], [1, 4, 1], "bone", rotation=[-30, 0, 0], pivot=[0, 9, 2.5]),
            box([-0.5, 9, 5], [1, 3, 1], "bone", rotation=[-35, 0, 0], pivot=[0, 9, 5.5]),
            box([-3.5, 9, -2], [1, 3, 1], "bone", rotation=[-25, 0, 20], pivot=[-3, 9, -1.5]),
            box([2.5, 9, -2], [1, 3, 1], "bone", rotation=[-25, 0, -20], pivot=[3, 9, -1.5]),
            box([-3.5, 9, 2], [1, 2, 1], "bone", rotation=[-30, 0, 20], pivot=[-3, 9, 2.5]),
            box([2.5, 9, 2], [1, 2, 1], "bone", rotation=[-30, 0, -20], pivot=[3, 9, 2.5]),
        ]),
        bone("head", [0, 6, -6], parent="body", cubes=[
            box([-3.5, 3, -11], [7, 5, 6], "ash_plate"),
            box([-2, 3, -15], [4, 3, 4], "ash", front="snout"),
            box([-2.5, 5.5, -11.5], [1, 1, 1], "eye_ember"),
            box([1.5, 5.5, -11.5], [1, 1, 1], "eye_ember"),
            box([-3, 7.5, -10], [1, 2, 1], "bone", rotation=[-30, 0, 15], pivot=[-2.5, 7.5, -9.5]),
            box([2, 7.5, -10], [1, 2, 1], "bone", rotation=[-30, 0, -15], pivot=[2.5, 7.5, -9.5]),
        ]),
        bone("jaw", [0, 3.5, -10], parent="head", cubes=[
            box([-2.5, 2, -15], [5, 1, 5], "ash_dark"),
            box([-2, 3, -15], [1, 1, 1], "bone"),
            box([1, 3, -15], [1, 1, 1], "bone"),
        ]),
        bone("left_front_leg", [4, 5, -4], parent="body", cubes=[
            box([3, 0, -5.5], [3, 5, 3], "ash_dark"),
            box([2.5, 0, -8], [4, 1, 3], "claw"),
        ]),
        bone("left_back_leg", [4, 5, 4], parent="body", cubes=[
            box([3, 0, 3], [3, 5, 3], "ash_dark"),
            box([3, 0, 2], [3, 1, 1], "claw"),
        ]),
        bone("tail", [0, 6, 7], parent="body", cubes=[
            box([-1.5, 4.5, 7], [3, 2, 6], "ash"),
        ]),
        bone("tail_tip", [0, 5.5, 13], parent="tail", cubes=[
            box([-1, 5, 13], [2, 1, 4], "ember_seam"),
            box([-0.5, 5.5, 16], [1, 1, 2], "bone"),
        ]),
    ]
    return mirror(b)


def chainback():
    b = [
        bone("root", [0, 0, 0]),
        bone("body", [0, 10, 0], parent="root", cubes=[
            box([-8, 6, -9], [16, 6, 18], "iron"),
            box([-10, 11, -10], [20, 5, 8], "iron_plate", 0.2),
            box([-11, 12, -3], [22, 6, 9], "iron_plate", 0.2),
            box([-10, 11, 5], [20, 5, 7], "iron_plate", 0.2),
            box([-6, 17, -2], [12, 3, 6], "rust", 0.1),
            box([-1, 19, -4], [2, 5, 2], "bone", rotation=[-20, 0, 0], pivot=[0, 19, -3]),
            box([-1, 19, 1], [2, 6, 2], "bone", rotation=[-25, 0, 0], pivot=[0, 19, 2]),
            box([-5, 17, 6], [2, 4, 2], "bone", rotation=[-30, 0, 20], pivot=[-4, 17, 7]),
            box([3, 17, 6], [2, 4, 2], "bone", rotation=[-30, 0, -20], pivot=[4, 17, 7]),
            box([-8, 16, -8], [2, 3, 2], "bone", rotation=[-15, 0, 25], pivot=[-7, 16, -7]),
            box([6, 16, -8], [2, 3, 2], "bone", rotation=[-15, 0, -25], pivot=[7, 16, -7]),
        ]),
        bone("head", [0, 9, -9], parent="body", cubes=[
            box([-4, 5, -15], [8, 6, 6], "iron_plate", front="chainback_face"),
            box([-3, 4, -14], [6, 1, 5], "rust"),
            box([-3, 9, -15.5], [1, 1, 1], "eye_ember"),
            box([2, 9, -15.5], [1, 1, 1], "eye_ember"),
        ]),
        bone("left_mandible", [2.5, 6, -14], parent="head", rotation=[0, -15, 0], cubes=[
            box([2, 5, -19], [2, 2, 5], "bone"),
            box([1, 5, -20], [2, 2, 1], "claw"),
        ]),
        bone("left_chain_front", [10, 13, -6], parent="body", cubes=[
            box([9.5, 3, -6.5], [1, 10, 1], "chain"),
            box([9, 1, -7], [2, 2, 2], "rust"),
        ]),
        bone("left_chain_back", [10, 13, 4], parent="body", cubes=[
            box([9.5, 5, 3.5], [1, 8, 1], "chain"),
            box([9, 3, 3], [2, 2, 2], "rust"),
        ]),
        bone("tail_chain", [0, 12, 11], parent="body", cubes=[
            box([-0.5, 11.5, 11], [1, 1, 10], "chain"),
            box([-1.5, 11, 20], [3, 2, 2], "rust"),
        ]),
    ]
    for i, z in enumerate((-6, 0, 6)):
        b.append(bone(f"left_leg_{i}", [8, 9, z], parent="body", rotation=[0, (i - 1) * -18, 0], cubes=[
            box([8, 8, z - 1], [8, 3, 2], "iron"),
        ]))
        b.append(bone(f"left_shin_{i}", [15, 9, z], parent=f"left_leg_{i}", cubes=[
            box([14, 0, z - 1], [2, 9, 2], "iron_plate"),
            box([14.5, -0.5, z - 0.5], [1, 1, 1], "claw"),
            box([15.5, 9, z - 0.5], [1, 3, 1], "bone", rotation=[0, 0, -20], pivot=[16, 9, z]),
        ]))
    return mirror(b)


def hollow_mimic():
    b = [
        bone("root", [0, 0, 0]),
        bone("left_leg", [2, 15, 0], parent="root", cubes=[
            box([1, 7, -1], [2, 8, 2], "skin_grey"),
            box([1.25, 0, -0.75], [1.5, 7, 1.5], "skin_pale"),
            box([1, 0, -2], [2, 1, 3], "claw"),
        ]),
        bone("body", [0, 15, 0], parent="root", cubes=[
            box([-3, 15, -1.5], [6, 3, 3], "shroud"),
        ]),
        bone("chest", [0, 18, 0], parent="body", rotation=[12, 0, 0], cubes=[
            box([-3.5, 18, -2], [7, 10, 4], "skin_pale"),
            box([-4.5, 26, -2.5], [9, 3, 5], "shroud", 0.2),
            box([-4, 19, 1.5], [8, 9, 1], "shroud"),
            box([-0.5, 27, -0.5], [1, 3, 1], "skin_grey"),
        ]),
        bone("head", [0, 29, 0], parent="chest", cubes=[
            box([-3, 29, -3.5], [6, 9, 6], "mask", front="mimic_face"),
            box([-2.5, 37.5, -3], [5, 1, 5], "mask"),
        ]),
        bone("jaw", [0, 31, -1], parent="head", cubes=[
            box([-2, 29, -3.6], [4, 2, 1], "hollow"),
        ]),
        bone("left_arm", [4.5, 27, 0], parent="chest", cubes=[
            box([4, 15, -1], [2, 13, 2], "skin_pale"),
            box([4.25, 6, -0.75], [1.5, 9, 1.5], "skin_grey"),
        ]),
        bone("left_hand", [5, 6, 0], parent="left_arm", cubes=[
            box([4, 1, -1.5], [1, 5, 1], "claw"),
            box([5, 0, -1.5], [1, 6, 1], "claw"),
            box([4.5, 1, 0], [1, 5, 1], "claw"),
        ]),
    ]
    return mirror(b)


def ashen_tyrant():
    b = [
        bone("root", [0, 0, 0]),
        bone("left_leg", [5, 18, 0], parent="root", cubes=[
            box([2, 6, -3], [6, 12, 6], "ash"),
            box([2.5, 0, -3.5], [5, 6, 6], "ash_dark", 0.3),
            box([2, 0, -6], [6, 2, 3], "claw"),
        ]),
        bone("body", [0, 18, 0], parent="root", cubes=[
            box([-7, 18, -4], [14, 6, 8], "ash_dark"),
        ]),
        bone("chest", [0, 24, 2], parent="body", rotation=[18, 0, 0], cubes=[
            box([-9, 24, -5], [18, 16, 10], "ember_seam", front="tyrant_chest"),
            box([-11, 36, -6], [22, 6, 12], "ash_plate", 0.4),
            box([-8, 32, 4], [16, 6, 3], "ash_plate"),
            box([-3, 41, 2], [2, 6, 2], "bone", rotation=[-30, 0, 10], pivot=[-2, 41, 3]),
            box([1, 41, 2], [2, 6, 2], "bone", rotation=[-30, 0, -10], pivot=[2, 41, 3]),
            box([-7, 40, 3], [2, 4, 2], "bone", rotation=[-40, 0, 25], pivot=[-6, 40, 4]),
            box([5, 40, 3], [2, 4, 2], "bone", rotation=[-40, 0, -25], pivot=[6, 40, 4]),
        ]),
        bone("head", [0, 40, -5], parent="chest", cubes=[
            box([-4, 37, -12], [8, 8, 8], "ash", front="tyrant_face"),
            box([-3, 35, -12.5], [6, 2, 6], "ash_dark"),
        ]),
        bone("crown", [0, 45, -8], parent="head", cubes=[
            box([-0.5, 45, -9], [1, 7, 1], "bone", rotation=[-10, 0, 0], pivot=[0, 45, -8.5]),
            box([-3, 45, -9], [1, 6, 1], "bone", rotation=[-10, 0, 22], pivot=[-2.5, 45, -8.5]),
            box([2, 45, -9], [1, 6, 1], "bone", rotation=[-10, 0, -22], pivot=[2.5, 45, -8.5]),
            box([-4.5, 44, -7], [1, 5, 1], "bone", rotation=[0, 0, 45], pivot=[-4, 44, -6.5]),
            box([3.5, 44, -7], [1, 5, 1], "bone", rotation=[0, 0, -45], pivot=[4, 44, -6.5]),
        ]),
        bone("left_arm", [11, 38, 0], parent="chest", cubes=[
            box([9, 22, -3], [6, 16, 6], "ash"),
            box([8.5, 32, -3.5], [7, 6, 7], "ash_plate", 0.3),
            box([9.5, 14, -2.5], [5, 8, 5], "ember_seam"),
        ]),
        bone("left_fist", [12, 14, 0], parent="left_arm", cubes=[
            box([8.5, 6, -4], [7, 8, 8], "ash_dark"),
            box([9, 5, -4.5], [1, 3, 1], "claw"),
            box([11, 5, -4.5], [1, 3, 1], "claw"),
            box([13, 5, -4.5], [1, 3, 1], "claw"),
        ]),
    ]
    return mirror(b)


def thorn_matron():
    """Thorn Matron: a tall bark-skinned mother of briars, a bloom where her heart should be."""
    b = [
        bone("root", [0, 0, 0]),
        bone("skirt", [0, 14, 0], parent="root", cubes=[
            box([-6, 2, -5], [12, 12, 10], "briar"),
            box([-5, 0, -4], [10, 2, 8], "bark"),
            box([-7, 0, -6], [2, 6, 2], "briar", rotation=[0, 0, 25], pivot=[-6, 2, -5]),
            box([5, 0, -6], [2, 6, 2], "briar", rotation=[0, 0, -25], pivot=[6, 2, -5]),
            box([-1, 0, 4], [2, 7, 4], "briar", rotation=[-30, 0, 0], pivot=[0, 4, 5]),
        ]),
        bone("body", [0, 14, 0], parent="root", cubes=[
            box([-4.5, 14, -3], [9, 14, 6], "bark"),
            box([-5, 24, -3.5], [10, 4, 7], "briar", 0.3),
            box([-1.5, 19, -3.6], [3, 3, 1], "bloom"),
            box([-2.5, 18, -3.3], [5, 5, 1], "briar"),
            box([-4, 27, 2], [1, 6, 1], "thorn", rotation=[-35, 0, 20], pivot=[-3.5, 27, 2.5]),
            box([3, 27, 2], [1, 6, 1], "thorn", rotation=[-35, 0, -20], pivot=[3.5, 27, 2.5]),
            box([-0.5, 25, 2.5], [1, 5, 1], "thorn", rotation=[-50, 0, 0], pivot=[0, 25, 3]),
        ]),
        bone("head", [0, 28, 0], parent="body", cubes=[
            box([-3, 28, -3.5], [6, 8, 6], "bark_pale", front="matron_face"),
            box([-3.5, 35, -4], [7, 2, 7], "briar"),
        ]),
        bone("crown", [0, 37, 0], parent="head", cubes=[
            box([-0.5, 36, -2], [1, 7, 1], "thorn", rotation=[-10, 0, 0], pivot=[0, 36, -1.5]),
            box([-3, 36, -2], [1, 6, 1], "thorn", rotation=[-10, 0, 25], pivot=[-2.5, 36, -1.5]),
            box([2, 36, -2], [1, 6, 1], "thorn", rotation=[-10, 0, -25], pivot=[2.5, 36, -1.5]),
            box([-3.5, 36, 1], [1, 5, 1], "thorn", rotation=[20, 0, 35], pivot=[-3, 36, 1.5]),
            box([2.5, 36, 1], [1, 5, 1], "thorn", rotation=[20, 0, -35], pivot=[3, 36, 1.5]),
            box([-1, 36.5, -4.2], [2, 2, 1], "bloom"),
        ]),
        bone("left_arm", [5.5, 26, 0], parent="body", cubes=[
            box([4.5, 13, -1.5], [3, 13, 3], "briar"),
            box([4.2, 22, -1.8], [3.6, 4, 3.6], "bark"),
            box([7, 18, -0.5], [1, 3, 1], "thorn", rotation=[0, 0, -40], pivot=[7, 18, 0]),
        ]),
        bone("left_hand", [6, 13, 0], parent="left_arm", cubes=[
            box([4.5, 9, -1.5], [3, 4, 3], "bark"),
            box([4.5, 1, -2], [1, 8, 1], "thorn"),
            box([6.5, 0, -2], [1, 9, 1], "thorn"),
            box([5.5, 2, 0.5], [1, 7, 1], "thorn"),
        ]),
    ]
    return mirror(b)


def gate_rift():
    # A tall lens-shaped tear standing a little off the ground: an outer violet
    # body, a brighter seam down its middle, ragged dark splinters along both
    # edges, and four shards of the other side drifting around it. The renderer
    # draws the whole texture translucent and full-bright.
    widths = [3, 6, 10, 13, 16, 18, 19, 19, 18, 16, 13, 10, 6, 3]
    seg = 4
    base = 4
    tear, seam, edge = [], [], []
    for i, w in enumerate(widths):
        y = base + i * seg
        tear.append(box([-w / 2, y, -0.5], [w, seg, 1], "rift"))
        sw = max(1, round(w * 0.35))
        seam.append(box([-sw / 2, y, -0.75], [sw, seg, 1.5], "rift_core"))
        if 0 < i < len(widths) - 1:
            tilt = 18 if i < len(widths) / 2 else -18
            edge.append(box([w / 2 - 0.5, y, -1], [1.5, seg + 1, 2], "rift_edge",
                            rotation=[0, 0, -tilt], pivot=[w / 2, y, 0]))
            if i % 3 == 1:
                edge.append(box([w / 2 + 0.5, y + 1, -0.5], [2, 1, 1], "rift_shard",
                                rotation=[0, 0, 25 if i % 2 else -25], pivot=[w / 2 + 0.5, y + 1, 0]))
    top = base + len(widths) * seg
    mid = base + len(widths) * seg / 2
    b = [
        bone("root", [0, 0, 0]),
        bone("tear", [0, mid, 0], parent="root", cubes=tear + seam),
        bone("left_edge", [5, mid, 0], parent="tear", cubes=edge),
        bone("shard_1", [12, 40, 0], parent="root", cubes=[
            box([11, 39, -1], [2, 3, 2], "rift_shard", rotation=[20, 30, 15], pivot=[12, 40, 0])]),
        bone("shard_2", [-12, 22, 2], parent="root", cubes=[
            box([-13, 21, 1], [2, 2, 2], "rift_shard", rotation=[40, 10, 0], pivot=[-12, 22, 2])]),
        bone("shard_3", [9, 14, -4], parent="root", cubes=[
            box([8.5, 13, -4.5], [1, 3, 1], "rift_shard", rotation=[0, 45, 25], pivot=[9, 14, -4])]),
        bone("shard_4", [-10, top - 10, -3], parent="root", cubes=[
            box([-11, top - 11, -4], [2, 2, 2], "rift_shard", rotation=[15, 60, 35], pivot=[-10, top - 10, -3])]),
    ]
    return mirror(b)


# ---------------------------------------------------------------- animations

def lerp_track(fn, length, samples, mode="catmullrom"):
    """Samples fn(t) -> [x, y, z] over one loop into GeckoLib keyframes."""
    track = {}
    for i in range(samples + 1):
        t = length * i / samples
        v = [round(c, 2) for c in fn(t)]
        track[f"{t:.4g}"] = {"post": v, "lerp_mode": mode}
    return track


def wave(t, length, amp, phase=0.0, freq=1.0):
    return amp * math.sin((t / length * freq + phase) * 2 * math.pi)


def anim(length, loop, bones, samples=12):
    """bones: name -> {"rotation": fn, "position": fn}."""
    out = {"animation_length": length, "bones": {}}
    if loop:
        out["loop"] = True
    for name, chans in bones.items():
        out["bones"][name] = {ch: lerp_track(fn, length, samples) for ch, fn in chans.items()}
    return out


def keys(points, mode="catmullrom"):
    """Explicit keyframes: [(t, [x, y, z]), ...] -> a fixed track."""
    return lambda: {f"{t:.4g}": {"post": v, "lerp_mode": mode} for t, v in points}


def anim_keys(length, bones, loop=False):
    out = {"animation_length": length, "bones": {}}
    if loop:
        out["loop"] = True
    for name, chans in bones.items():
        out["bones"][name] = {ch: track() for ch, track in chans.items()}
    return out


def ash_burrower_anims():
    L = 2.0
    W = 0.5
    legs = {}
    for side, s in (("left", 1), ("right", -1)):
        legs[f"{side}_front_leg"] = {"rotation": lambda t, s=s: [wave(t, W, 35, 0 if s > 0 else 0.5), 0, 0]}
        legs[f"{side}_back_leg"] = {"rotation": lambda t, s=s: [wave(t, W, 35, 0.5 if s > 0 else 0), 0, 0]}
    return {
        "idle": anim(L, True, {
            "body": {"position": lambda t: [0, wave(t, L, 0.25), 0]},
            "head": {"rotation": lambda t: [wave(t, L, 3, 0.25), wave(t, L, 8), 0]},
            "jaw": {"rotation": lambda t: [max(0, wave(t, L, 10, 0, 2)) * -1, 0, 0]},
            "tail": {"rotation": lambda t: [0, wave(t, L, 12), 0]},
            "tail_tip": {"rotation": lambda t: [0, wave(t, L, 16, 0.2), 0]},
        }),
        "walk": anim(W, True, dict(legs, **{
            "body": {"position": lambda t: [0, abs(wave(t, W, 0.6)), 0],
                     "rotation": lambda t: [0, 0, wave(t, W, 3)]},
            "head": {"rotation": lambda t: [wave(t, W, 4, 0, 2), 0, 0]},
            "tail": {"rotation": lambda t: [0, wave(t, W, 18), 0]},
            "tail_tip": {"rotation": lambda t: [0, wave(t, W, 22, 0.25), 0]},
        }), samples=8),
        "burrowed": anim(1.0, True, {
            "root": {"position": lambda t: [0, -8.5 + wave(t, 1.0, 0.4), 0]},
            "body": {"rotation": lambda t: [0, 0, wave(t, 1.0, 4)]},
            "ridge": {"rotation": lambda t: [wave(t, 1.0, 6, 0.25), 0, 0]},
        }, samples=8),
        "emerge": anim_keys(0.55, {
            "root": {"position": keys([(0, [0, -8.5, 0]), (0.2, [0, 3, 0]), (0.4, [0, 0.5, 0]), (0.55, [0, 0, 0])])},
            "head": {"rotation": keys([(0, [-40, 0, 0]), (0.25, [-35, 0, 0]), (0.55, [0, 0, 0])])},
            "jaw": {"rotation": keys([(0, [0, 0, 0]), (0.2, [35, 0, 0]), (0.55, [0, 0, 0])])},
        }),
        "bite": anim_keys(0.35, {
            "head": {"rotation": keys([(0, [0, 0, 0]), (0.1, [-20, 0, 0]), (0.2, [15, 0, 0]), (0.35, [0, 0, 0])])},
            "jaw": {"rotation": keys([(0, [0, 0, 0]), (0.1, [40, 0, 0]), (0.2, [0, 0, 0]), (0.35, [0, 0, 0])])},
        }),
    }


def chainback_anims():
    def legs(length, amp_y, amp_z, speed=1.0):
        out = {}
        for side, s in (("left", 1), ("right", -1)):
            for i in range(3):
                phase = (i % 2) * 0.5 + (0 if s > 0 else 0.5)
                out[f"{side}_leg_{i}"] = {"rotation": lambda t, p=phase, s=s: [
                    0, wave(t, length, amp_y, p, speed), s * min(0, wave(t, length, amp_z, p + 0.25, speed))]}
        return out
    W = 0.8
    return {
        "idle": anim(3.0, True, {
            "body": {"position": lambda t: [0, wave(t, 3.0, 0.35), 0]},
            "head": {"rotation": lambda t: [wave(t, 3.0, 3, 0.3), 0, 0]},
            "left_mandible": {"rotation": lambda t: [0, wave(t, 3.0, 6, 0, 2), 0]},
            "right_mandible": {"rotation": lambda t: [0, -wave(t, 3.0, 6, 0, 2), 0]},
            "left_chain_front": {"rotation": lambda t: [wave(t, 3.0, 6), 0, wave(t, 3.0, 4, 0.3)]},
            "right_chain_front": {"rotation": lambda t: [wave(t, 3.0, 6, 0.4), 0, wave(t, 3.0, 4, 0.1)]},
            "left_chain_back": {"rotation": lambda t: [wave(t, 3.0, 5, 0.2), 0, 0]},
            "right_chain_back": {"rotation": lambda t: [wave(t, 3.0, 5, 0.7), 0, 0]},
            "tail_chain": {"rotation": lambda t: [wave(t, 3.0, 4), wave(t, 3.0, 8, 0.25), 0]},
        }),
        "walk": anim(W, True, dict(legs(W, 18, 14), **{
            "body": {"position": lambda t: [0, abs(wave(t, W, 0.5, 0, 2)), 0],
                     "rotation": lambda t: [0, 0, wave(t, W, 2)]},
            "left_chain_front": {"rotation": lambda t: [wave(t, W, 14), 0, 0]},
            "right_chain_front": {"rotation": lambda t: [wave(t, W, 14, 0.5), 0, 0]},
            "left_chain_back": {"rotation": lambda t: [wave(t, W, 12, 0.25), 0, 0]},
            "right_chain_back": {"rotation": lambda t: [wave(t, W, 12, 0.75), 0, 0]},
            "tail_chain": {"rotation": lambda t: [0, wave(t, W, 14), 0]},
        }), samples=8),
        "climb": anim(0.5, True, dict(legs(0.5, 24, 22), **{
            "body": {"rotation": lambda t: [-6, 0, wave(t, 0.5, 3)]},
        }), samples=8),
        "slam": anim_keys(0.7, {
            "body": {"rotation": keys([(0, [0, 0, 0]), (0.25, [-18, 0, 0]), (0.4, [10, 0, 0]), (0.7, [0, 0, 0])]),
                     "position": keys([(0, [0, 0, 0]), (0.25, [0, 2, 1]), (0.4, [0, -1, -2]), (0.7, [0, 0, 0])])},
            "head": {"rotation": keys([(0, [0, 0, 0]), (0.25, [-15, 0, 0]), (0.4, [20, 0, 0]), (0.7, [0, 0, 0])])},
        }),
        "chain_throw": anim_keys(0.8, {
            "left_chain_front": {"rotation": keys([(0, [0, 0, 0]), (0.3, [40, 0, -30]), (0.5, [-110, 0, -20]),
                                                   (0.8, [0, 0, 0])])},
            "right_chain_front": {"rotation": keys([(0, [0, 0, 0]), (0.3, [40, 0, 30]), (0.5, [-110, 0, 20]),
                                                    (0.8, [0, 0, 0])])},
            "body": {"rotation": keys([(0, [0, 0, 0]), (0.3, [-8, 0, 0]), (0.5, [6, 0, 0]), (0.8, [0, 0, 0])])},
        }),
    }


def hollow_mimic_anims():
    L = 4.0
    W = 1.2
    return {
        "idle": anim(L, True, {
            "chest": {"rotation": lambda t: [wave(t, L, 2), 0, wave(t, L, 3, 0.25)]},
            "head": {"rotation": lambda t: [wave(t, L, 4, 0.1), wave(t, L, 10, 0, 0.5), 18 + wave(t, L, 6)]},
            "left_arm": {"rotation": lambda t: [wave(t, L, 3, 0.2), 0, wave(t, L, 2) - 4]},
            "right_arm": {"rotation": lambda t: [wave(t, L, 3, 0.7), 0, -wave(t, L, 2) + 4]},
            "left_hand": {"rotation": lambda t: [wave(t, L, 8, 0, 2), 0, 0]},
            "right_hand": {"rotation": lambda t: [wave(t, L, 8, 0.5, 2), 0, 0]},
        }),
        "walk": anim(W, True, {
            "left_leg": {"rotation": lambda t: [wave(t, W, 28), 0, 0]},
            "right_leg": {"rotation": lambda t: [wave(t, W, 28, 0.5), 0, 0]},
            "left_arm": {"rotation": lambda t: [wave(t, W, 20, 0.5), 0, -4]},
            "right_arm": {"rotation": lambda t: [wave(t, W, 20), 0, 4]},
            "chest": {"rotation": lambda t: [8, wave(t, W, 6), 0]},
            "body": {"position": lambda t: [0, abs(wave(t, W, 0.8)), 0]},
            "head": {"rotation": lambda t: [0, 0, 10 + wave(t, W, 4)]},
        }, samples=10),
        "speak": anim_keys(1.2, {
            "head": {"rotation": keys([(0, [0, 0, 10]), (0.3, [-10, 0, 38]), (0.9, [-10, 0, 40]), (1.2, [0, 0, 10])])},
            "jaw": {"position": keys([(0, [0, 0, 0]), (0.3, [0, -2, 0]), (0.5, [0, -0.5, 0]), (0.7, [0, -2, 0]),
                                      (0.9, [0, -0.5, 0]), (1.2, [0, 0, 0])])},
        }),
        "claw": anim_keys(0.5, {
            "right_arm": {"rotation": keys([(0, [0, 0, 0]), (0.2, [-150, 20, 10]), (0.32, [-20, -30, 0]),
                                            (0.5, [0, 0, 0])])},
            "chest": {"rotation": keys([(0, [12, 0, 0]), (0.2, [0, -15, 0]), (0.32, [24, 15, 0]), (0.5, [12, 0, 0])])},
        }),
    }


def ashen_tyrant_anims():
    L = 3.0
    W = 1.4
    return {
        "idle": anim(L, True, {
            "chest": {"rotation": lambda t: [wave(t, L, 2.5), 0, 0], "position": lambda t: [0, wave(t, L, 0.4), 0]},
            "head": {"rotation": lambda t: [wave(t, L, 3, 0.3), wave(t, L, 6, 0, 0.5), 0]},
            "left_arm": {"rotation": lambda t: [wave(t, L, 3, 0.2), 0, -4 + wave(t, L, 2)]},
            "right_arm": {"rotation": lambda t: [wave(t, L, 3, 0.7), 0, 4 - wave(t, L, 2)]},
        }),
        "walk": anim(W, True, {
            "left_leg": {"rotation": lambda t: [wave(t, W, 22), 0, 0]},
            "right_leg": {"rotation": lambda t: [wave(t, W, 22, 0.5), 0, 0]},
            "left_arm": {"rotation": lambda t: [wave(t, W, 16, 0.5), 0, -4]},
            "right_arm": {"rotation": lambda t: [wave(t, W, 16), 0, 4]},
            "body": {"position": lambda t: [0, -abs(wave(t, W, 1.2)), 0],
                     "rotation": lambda t: [0, wave(t, W, 4), wave(t, W, 3)]},
            "head": {"rotation": lambda t: [0, -wave(t, W, 4), 0]},
        }, samples=10),
        "slam": anim_keys(1.5, {
            "left_arm": {"rotation": keys([(0, [0, 0, 0]), (0.8, [-165, 0, -10]), (1.05, [-170, 0, -12]),
                                           (1.15, [-30, 0, 0]), (1.5, [0, 0, 0])])},
            "right_arm": {"rotation": keys([(0, [0, 0, 0]), (0.8, [-165, 0, 10]), (1.05, [-170, 0, 12]),
                                            (1.15, [-30, 0, 0]), (1.5, [0, 0, 0])])},
            "chest": {"rotation": keys([(0, [0, 0, 0]), (0.8, [-22, 0, 0]), (1.05, [-25, 0, 0]),
                                        (1.15, [30, 0, 0]), (1.5, [0, 0, 0])])},
            "body": {"position": keys([(0, [0, 0, 0]), (0.8, [0, 1, 0]), (1.15, [0, -3, 0]), (1.5, [0, 0, 0])])},
        }),
        "swipe": anim_keys(0.6, {
            "right_arm": {"rotation": keys([(0, [0, 0, 0]), (0.2, [-70, -40, 30]), (0.35, [-60, 50, -10]),
                                            (0.6, [0, 0, 0])])},
            "chest": {"rotation": keys([(0, [0, 0, 0]), (0.2, [0, 20, 0]), (0.35, [0, -25, 0]), (0.6, [0, 0, 0])])},
        }),
        "brood": anim_keys(1.5, {
            "left_arm": {"rotation": keys([(0, [0, 0, 0]), (0.5, [-40, 0, -60]), (1.2, [-45, 0, -65]), (1.5, [0, 0, 0])])},
            "right_arm": {"rotation": keys([(0, [0, 0, 0]), (0.5, [-40, 0, 60]), (1.2, [-45, 0, 65]), (1.5, [0, 0, 0])])},
            "chest": {"rotation": keys([(0, [0, 0, 0]), (0.5, [-25, 0, 0]), (1.2, [-28, 0, 0]), (1.5, [0, 0, 0])])},
            "head": {"rotation": keys([(0, [0, 0, 0]), (0.5, [-30, 0, 0]), (1.2, [-35, 0, 0]), (1.5, [0, 0, 0])])},
        }),
    }


def thorn_matron_anims():
    L = 4.0
    W = 1.6
    return {
        "idle": anim(L, True, {
            "body": {"rotation": lambda t: [wave(t, L, 2), wave(t, L, 3, 0.2, 0.5), 0]},
            "head": {"rotation": lambda t: [wave(t, L, 4, 0.1), wave(t, L, 12, 0, 0.5), wave(t, L, 5, 0.3)]},
            "crown": {"rotation": lambda t: [wave(t, L, 3, 0.5), 0, 0]},
            "left_arm": {"rotation": lambda t: [wave(t, L, 4, 0.2), 0, -6 + wave(t, L, 3)]},
            "right_arm": {"rotation": lambda t: [wave(t, L, 4, 0.7), 0, 6 - wave(t, L, 3)]},
            "left_hand": {"rotation": lambda t: [wave(t, L, 10, 0, 2), 0, 0]},
            "right_hand": {"rotation": lambda t: [wave(t, L, 10, 0.5, 2), 0, 0]},
            "skirt": {"rotation": lambda t: [0, wave(t, L, 4), 0]},
        }),
        "walk": anim(W, True, {
            "skirt": {"rotation": lambda t: [wave(t, W, 6), wave(t, W, 8, 0.25), 0],
                      "position": lambda t: [0, abs(wave(t, W, 0.6)), 0]},
            "body": {"rotation": lambda t: [10, wave(t, W, 6), wave(t, W, 3)]},
            "left_arm": {"rotation": lambda t: [wave(t, W, 18, 0.5), 0, -6]},
            "right_arm": {"rotation": lambda t: [wave(t, W, 18), 0, 6]},
            "head": {"rotation": lambda t: [-6, -wave(t, W, 6), 0]},
        }, samples=10),
        "lash": anim_keys(0.6, {
            "right_arm": {"rotation": keys([(0, [0, 0, 0]), (0.22, [-160, 0, 20]), (0.36, [-10, -20, -10]),
                                            (0.6, [0, 0, 0])])},
            "body": {"rotation": keys([(0, [0, 0, 0]), (0.22, [-10, -15, 0]), (0.36, [18, 15, 0]), (0.6, [0, 0, 0])])},
        }),
        "grow": anim_keys(1.4, {
            "left_arm": {"rotation": keys([(0, [0, 0, 0]), (0.5, [-100, 0, -50]), (0.9, [-20, 0, -30]), (1.4, [0, 0, 0])])},
            "right_arm": {"rotation": keys([(0, [0, 0, 0]), (0.5, [-100, 0, 50]), (0.9, [-20, 0, 30]), (1.4, [0, 0, 0])])},
            "body": {"rotation": keys([(0, [0, 0, 0]), (0.5, [-18, 0, 0]), (0.9, [25, 0, 0]), (1.4, [0, 0, 0])])},
            "head": {"rotation": keys([(0, [0, 0, 0]), (0.5, [-25, 0, 0]), (0.9, [15, 0, 0]), (1.4, [0, 0, 0])])},
        }),
    }


def gate_rift_anims():
    L = 4.0
    shards = {}
    for n, (radius, height, phase) in enumerate(((12, 40, 0.0), (12, 22, 0.25), (9.8, 14, 0.5), (10.4, 50, 0.75)), 1):
        def orbit(t, radius=radius, phase=phase):
            a = (t / L + phase) * 2 * math.pi
            b = phase * 2 * math.pi
            return [radius * (math.cos(a) - math.cos(b)), 1.5 * math.sin(a * 2), radius * (math.sin(a) - math.sin(b))]
        shards[f"shard_{n}"] = {"position": orbit,
                                "rotation": lambda t, phase=phase: [0, 360 * t / L + phase * 360, wave(t, L, 20, phase)]}
    open_loop = {
        "tear": {"scale": lambda t: [1 + wave(t, L, 0.08, 0, 2), 1 + wave(t, L, 0.02, 0.25), 1]},
        "left_edge": {"rotation": lambda t: [0, 0, wave(t, L, 3, 0, 2)]},
        "right_edge": {"rotation": lambda t: [0, 0, -wave(t, L, 3, 0.3, 2)]},
    }
    open_loop.update(shards)
    hidden = {f"shard_{n}": {"scale": keys([(0, [0, 0, 0])])} for n in range(1, 5)}
    forming = anim(1.0, True, {
        "tear": {"scale": lambda t: [0.18 + wave(t, 1.0, 0.05, 0, 3), 0.6 + wave(t, 1.0, 0.03), 1]},
        "left_edge": {"position": lambda t: [-6, 0, 0]},
        "right_edge": {"position": lambda t: [6, 0, 0]},
    }, samples=12)
    forming["bones"].update({k: {ch: tr() for ch, tr in v.items()} for k, v in hidden.items()})
    return {
        "forming": forming,
        "opening": anim_keys(1.2, {
            "tear": {"scale": keys([(0, [0.18, 0.6, 1]), (0.5, [0.5, 1.05, 1]), (0.9, [1.25, 1.02, 1]), (1.2, [1, 1, 1])])},
            "left_edge": {"position": keys([(0, [-6, 0, 0]), (0.9, [0.8, 0, 0]), (1.2, [0, 0, 0])])},
            "right_edge": {"position": keys([(0, [6, 0, 0]), (0.9, [-0.8, 0, 0]), (1.2, [0, 0, 0])])},
            **{f"shard_{n}": {"scale": keys([(0, [0, 0, 0]), (0.8, [0, 0, 0]), (1.2, [1, 1, 1])])} for n in range(1, 5)},
        }),
        "open": anim(L, True, open_loop, samples=16),
        "closing": anim_keys(1.5, {
            "tear": {"scale": keys([(0, [1, 1, 1]), (0.4, [1.2, 1.05, 1]), (1.2, [0.05, 0.6, 1]), (1.5, [0, 0, 0])])},
            "left_edge": {"position": keys([(0, [0, 0, 0]), (1.2, [-6, 0, 0])]),
                          "scale": keys([(1.2, [1, 1, 1]), (1.5, [0, 0, 0])])},
            "right_edge": {"position": keys([(0, [0, 0, 0]), (1.2, [6, 0, 0])]),
                           "scale": keys([(1.2, [1, 1, 1]), (1.5, [0, 0, 0])])},
            **{f"shard_{n}": {"scale": keys([(0, [1, 1, 1]), (0.6, [0, 0, 0])])} for n in range(1, 5)},
        }),
    }


# ---------------------------------------------------------------- texture painting

def clamp(v):
    return max(0, min(255, int(v)))


def mul(c, f):
    return tuple(clamp(x * f) for x in c)


def mix(a, b, t):
    return tuple(clamp(a[i] + (b[i] - a[i]) * t) for i in range(3))


class Painter:
    def __init__(self, w, h, seed):
        self.img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        self.glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        self.rand = random.Random(seed)

    def px(self, x, y, c, glow=False):
        self.img.putpixel((x, y), c + (255,))
        if glow:
            self.glow.putpixel((x, y), c + (255,))

    def face(self, x0, y0, w, h, material, face, overlay=None):
        base, pattern, glows = MATERIALS[material]
        r = self.rand
        shade = SHADE[face]
        for j in range(h):
            for i in range(w):
                c = mul(base, shade * (1 + r.uniform(-0.09, 0.09)))
                edge = i == 0 or j == 0 or i == w - 1 or j == h - 1
                glow = False
                if pattern == "cracks":
                    if r.random() < 0.06:
                        c = mul(c, 0.7)
                    if edge:
                        c = mul(c, 0.82)
                elif pattern == "plates":
                    if edge or (face not in ("up", "down") and j % 3 == 0):
                        c = mul(c, 0.72)
                    elif j % 3 == 1:
                        c = mul(c, 1.12)
                elif pattern == "ember_cracks":
                    if r.random() < 0.08 or (abs((i * 3 + j * 5) % 11 - 5) == 0 and r.random() < 0.6):
                        c = mix((255, 110, 30), (255, 190, 80), r.random())
                        glow = True
                    elif edge:
                        c = mul(c, 0.8)
                elif pattern == "ember":
                    centre = (0 < i < w - 1 and 0 < j < h - 1) or w <= 2 or h <= 2
                    c = mix(base, (255, 225, 120), 0.6 if centre else 0.0)
                    glow = True
                elif pattern == "bone":
                    c = mul(c, 1.0 - 0.25 * (j / max(1, h - 1)) if face != "down" else 0.8)
                    if r.random() < 0.05:
                        c = mul(c, 0.85)
                elif pattern == "rivets":
                    if (i % 4 == 1 and j % 4 == 1):
                        c = mul(base, 1.45)
                    elif r.random() < 0.07:
                        c = mix(c, MATERIALS["rust"][0], 0.6)
                    if edge:
                        c = mul(c, 0.78)
                elif pattern == "rust_plates":
                    n = math.sin(i * 0.9 + j * 0.4) + math.sin(j * 1.3 - i * 0.2)
                    if n > 1.1 or r.random() < 0.05:
                        c = mix(c, MATERIALS["rust"][0], 0.7)
                    if edge or (face not in ("up", "down") and j % 4 == 0):
                        c = mul(c, 0.7)
                    elif face in ("up",) and (i + j) % 6 == 0:
                        c = mul(c, 1.2)
                elif pattern == "rust":
                    c = mix(c, (90, 40, 20), r.random() * 0.4)
                elif pattern == "chain":
                    link = j % 3
                    if face in ("north", "south", "east", "west"):
                        c = mul(base, 1.25 if link == 1 else (0.55 if link == 0 else 0.9))
                elif pattern == "ribs":
                    if face == "north" and j % 2 == 0 and 0 < i < w - 1 and j < h * 0.7:
                        c = mul(c, 0.78)
                    if edge:
                        c = mul(c, 0.86)
                elif pattern == "cloth":
                    if i % 2 == 0:
                        c = mul(c, 0.85)
                    if j == h - 1 and r.random() < 0.5:
                        c = None
                elif pattern == "mask":
                    if edge:
                        c = mul(c, 0.9)
                elif pattern == "rift":
                    # bright at the seam, deepening to violet at the sides, with drifting streaks
                    centre = 1.0 - abs((i + 0.5) / w - 0.5) * 2
                    c = mix(mul(base, 0.55), (236, 210, 255), centre ** 1.5)
                    if r.random() < 0.08:
                        c = mix(c, (255, 255, 255), 0.5)
                elif pattern == "shard":
                    if r.random() < 0.18:
                        c = mix(c, (150, 70, 240), 0.7)
                    if edge:
                        c = mul(c, 1.4)
                elif pattern == "thorns":
                    # tangled stems with pale thorn points
                    if (i + 2 * j) % 5 == 0 or (2 * i - j) % 7 == 0:
                        c = mul(c, 1.35)
                    if r.random() < 0.07:
                        c = MATERIALS["thorn"][0]
                    elif edge:
                        c = mul(c, 0.8)
                elif pattern == "petal":
                    centre = (0 < i < w - 1 and 0 < j < h - 1) or w <= 2 or h <= 2
                    c = mix(base, (255, 150, 160), 0.55 if centre else 0.0)
                    glow = True
                elif pattern == "smooth":
                    pass
                if c is None:
                    continue
                self.px(x0 + i, y0 + j, c, glow or (glows and pattern not in ("ember_cracks",)))
        if overlay:
            overlay(self, x0, y0, w, h)


# Face overlays painted over the -z (front) face of a box.
def overlay_mimic_face(p, x0, y0, w, h):
    hollow = (8, 6, 10)
    # two tall hollow eyes and a long vertical mouth: nothing behind them
    for j in range(2, 5):
        p.px(x0 + 1, y0 + j, hollow)
        p.px(x0 + w - 2, y0 + j, hollow)
    for j in range(5, h - 1):
        p.px(x0 + w // 2 - 1, y0 + j, hollow)
        p.px(x0 + w // 2, y0 + j, hollow)
    for i in range(w):
        p.px(x0 + i, y0 + 1, mul(MATERIALS["mask"][0], 0.82))
    # a hairline crack across the brow
    for i, j in ((0, 3), (1, 2), (2, 2), (3, 1)):
        p.px(x0 + i + 1, y0 + j - 1, mul(MATERIALS["mask"][0], 0.6))


def overlay_tyrant_chest(p, x0, y0, w, h):
    # five cores in a crown shape on the chest, each an ember with a dark rim
    cores = [(w // 2, 4), (w // 2 - 5, 6), (w // 2 + 4, 6), (w // 2 - 3, 10), (w // 2 + 2, 10)]
    for cx, cy in cores:
        for dx in range(-1, 2):
            for dy in range(-1, 2):
                p.px(x0 + cx + dx, y0 + cy + dy, (40, 20, 14))
        p.px(x0 + cx, y0 + cy, (255, 236, 160), True)
        for dx, dy in ((1, 0), (0, 1), (1, 1)):
            p.px(x0 + cx + dx - 1 + 1, y0 + cy + dy - 1 + 1, (255, 150, 50), True)
        p.px(x0 + cx, y0 + cy, (255, 236, 160), True)


def overlay_tyrant_face(p, x0, y0, w, h):
    dark = (20, 14, 12)
    for i in range(1, w - 1):
        p.px(x0 + i, y0 + h - 2, dark)
        if i % 2:
            p.px(x0 + i, y0 + h - 3, (200, 190, 170))
    for ex in (2, w - 3):
        p.px(x0 + ex, y0 + 3, (255, 200, 90), True)
        p.px(x0 + ex - 1 if ex < w // 2 else x0 + ex + 1, y0 + 3, (255, 120, 40), True)
    for i in range(1, w - 1):
        p.px(x0 + i, y0 + 1, mul(MATERIALS["ash"][0], 0.6))


def overlay_matron_face(p, x0, y0, w, h):
    eye = (255, 70, 90)
    dark = (24, 14, 16)
    # two thin slanted eyes that glow, a long split for a mouth, and bark grain
    for i, j in ((1, 3), (2, 3), (2, 2)):
        p.px(x0 + i, y0 + j, eye, glow=True)
        p.px(x0 + w - 1 - i, y0 + j, eye, glow=True)
    for j in range(5, h - 1):
        p.px(x0 + w // 2, y0 + j, dark)
    for j in range(h):
        if j % 3 == 0:
            p.px(x0, y0 + j, mul(MATERIALS["bark_pale"][0], 0.7))
            p.px(x0 + w - 1, y0 + j, mul(MATERIALS["bark_pale"][0], 0.7))


def overlay_snout(p, x0, y0, w, h):
    p.px(x0 + 1, y0 + 1, (20, 16, 14))
    p.px(x0 + w - 2, y0 + 1, (20, 16, 14))


def overlay_chainback_face(p, x0, y0, w, h):
    for i in range(w):
        if i % 2 == 0:
            p.px(x0 + i, y0 + h - 1, (200, 190, 170))
        p.px(x0 + i, y0 + h - 2, (24, 18, 16))
    for j in range(h - 2):
        p.px(x0 + w // 2, y0 + j, mul(MATERIALS["iron_plate"][0], 0.6))


OVERLAYS = {
    "mimic_face": ("mask", overlay_mimic_face),
    "tyrant_chest": ("ember_seam", overlay_tyrant_chest),
    "tyrant_face": ("ash", overlay_tyrant_face),
    "snout": ("ash", overlay_snout),
    "matron_face": ("bark_pale", overlay_matron_face),
    "chainback_face": ("iron_plate", overlay_chainback_face),
}


# ---------------------------------------------------------------- packing and output

def cube_uv_size(c):
    w, h, d = (max(1, math.ceil(s)) for s in c["size"])
    return w, h, d, 2 * (d + w), d + h


def pack(bones, width):
    cubes = [c for b in bones for c in b["cubes"]]
    order = sorted(cubes, key=lambda c: -cube_uv_size(c)[4])
    x = y = row = 0
    for c in order:
        _, _, _, uw, uh = cube_uv_size(c)
        if x + uw > width:
            x, y, row = 0, y + row, 0
        c["uv"] = [x, y]
        x += uw
        row = max(row, uh)
    height = y + row
    h = 16
    while h < height:
        h *= 2
    return h


def paint(bones, width, height, seed):
    p = Painter(width, height, seed)
    for b in bones:
        for c in b["cubes"]:
            w, h, d, _, _ = cube_uv_size(c)
            u, v = c["uv"]
            mat = c["material"]
            front = c.get("front")
            front_mat, front_overlay = OVERLAYS[front] if front else (mat, None)
            p.face(u + d, v, w, d, mat, "up")
            p.face(u + d + w, v, w, d, mat, "down")
            p.face(u, v + d, d, h, mat, "east")
            p.face(u + d, v + d, w, h, front_mat, "north", front_overlay)
            p.face(u + d + w, v + d, d, h, mat, "west")
            p.face(u + 2 * d + w, v + d, w, h, mat, "south")
    return p


def geo_json(name, bones, width, height):
    out_bones = []
    for b in bones:
        ob = {"name": b["name"], "pivot": b["pivot"]}
        if b["parent"]:
            ob["parent"] = b["parent"]
        if b["rotation"]:
            ob["rotation"] = b["rotation"]
        cubes = []
        for c in b["cubes"]:
            oc = {"origin": c["origin"], "size": c["size"], "uv": c["uv"]}
            if c["inflate"]:
                oc["inflate"] = c["inflate"]
            if c["rotation"]:
                oc["rotation"] = c["rotation"]
                oc["pivot"] = c["pivot"]
            cubes.append(oc)
        if cubes:
            ob["cubes"] = cubes
        out_bones.append(ob)
    xs = [abs(c["origin"][0]) + c["size"][0] for b in bones for c in b["cubes"]]
    ys = [c["origin"][1] + c["size"][1] for b in bones for c in b["cubes"]]
    return {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": f"geometry.shadowslave.{name}",
                "texture_width": width,
                "texture_height": height,
                "visible_bounds_width": math.ceil(max(xs) * 2 / 16) + 2,
                "visible_bounds_height": math.ceil(max(ys) / 16) + 2,
                "visible_bounds_offset": [0, max(ys) / 32, 0],
            },
            "bones": out_bones,
        }],
    }


CREATURES = {
    "ash_burrower": (ash_burrower, ash_burrower_anims, 64, 11),
    "chainback": (chainback, chainback_anims, 128, 12),
    "hollow_mimic": (hollow_mimic, hollow_mimic_anims, 64, 13),
    "ashen_tyrant": (ashen_tyrant, ashen_tyrant_anims, 128, 14),
    "thorn_matron": (thorn_matron, thorn_matron_anims, 64, 17),
    # Not a creature: the tear a Gate opens. Drawn the same way.
    "gate_rift": (gate_rift, gate_rift_anims, 64, 15),
}


# ---------------------------------------------------------------- Gate Lure block

LURE_STONE = (36, 22, 48)
LURE_RUNE_DARK = (20, 10, 28)
LURE_RUNE_LIT = [(120, 60, 200), (150, 80, 235), (180, 110, 255), (215, 160, 255)]
# Four runes down the pillar side, one lit per charge level (pixel strokes on a 16x16 face).
LURE_RUNES = [
    [(3, 2), (3, 3), (3, 4), (4, 3), (5, 2), (5, 4)],
    [(10, 3), (11, 2), (12, 3), (11, 4), (11, 5)],
    [(3, 9), (4, 10), (5, 11), (5, 9), (3, 11)],
    [(10, 9), (11, 9), (12, 9), (11, 10), (10, 11), (12, 11)],
]


def lure_textures(seed=16):
    """The Gate Lure's pillar side and top for each charge 0-4: runes and the socket light up."""
    out = {}
    for charge in range(5):
        r = random.Random(seed)
        side = Image.new("RGBA", (16, 16))
        top = Image.new("RGBA", (16, 16))
        for y in range(16):
            for x in range(16):
                c = mul(LURE_STONE, 1 + r.uniform(-0.15, 0.15))
                if r.random() < 0.05:
                    c = mix(c, (110, 40, 170), 0.5)
                side.putpixel((x, y), c + (255,))
                d = math.hypot(x - 7.5, y - 7.5)
                t = mul(LURE_STONE, (1.1 if (x + y) % 5 else 0.8) * (1 + r.uniform(-0.1, 0.1)))
                if d < 3.2:
                    t = mix(LURE_RUNE_DARK, (230, 190, 255), 0.0 if charge == 0 else 0.25 + 0.18 * charge)
                elif d < 4.2:
                    t = mul(LURE_STONE, 0.6)
                top.putpixel((x, y), t + (255,))
        for n, rune in enumerate(LURE_RUNES):
            colour = LURE_RUNE_LIT[charge - 1] if n < charge else LURE_RUNE_DARK
            for x, y in rune:
                side.putpixel((x, y), colour + (255,))
        out[f"gate_lure_side_{charge}"] = side
        out[f"gate_lure_top_{charge}"] = top
    return out


def lure_model(charge):
    """A plinth: a crying-obsidian base and cap around a rune pillar; the pillar glows with its charge."""
    glow = {"neoforge_data": {"block_light": 3 * charge + 3}} if charge else {}

    def faces(side, top, bottom, light=False):
        f = {d: {"texture": side} for d in ("north", "south", "east", "west")}
        f["up"] = {"texture": top}
        f["down"] = {"texture": bottom}
        if light:
            for face in f.values():
                face.update(glow)
        return f
    return {
        "parent": "block/block",
        "textures": {"particle": "minecraft:block/crying_obsidian", "stone": "minecraft:block/crying_obsidian",
                     "side": f"shadowslave:block/gate_lure_side_{charge}",
                     "top": f"shadowslave:block/gate_lure_top_{charge}"},
        "elements": [
            {"from": [2, 0, 2], "to": [14, 3, 14], "faces": faces("#stone", "#stone", "#stone")},
            {"from": [4, 3, 4], "to": [12, 12, 12], "faces": faces("#side", "#side", "#side", light=True)},
            {"from": [3, 12, 3], "to": [13, 15, 13], "faces": faces("#stone", "#top", "#stone", light=True)},
        ],
    }


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=1) + "\n")


def main():
    for name, (model, anims, width, seed) in CREATURES.items():
        bones = model()
        height = pack(bones, width)
        painter = paint(bones, width, height, seed)
        write_json(ASSETS / f"geo/entity/{name}.geo.json", geo_json(name, bones, width, height))
        write_json(ASSETS / f"animations/entity/{name}.animation.json",
                   {"format_version": "1.8.0", "animations": anims()})
        tex = ASSETS / f"textures/entity/{name}.png"
        tex.parent.mkdir(parents=True, exist_ok=True)
        painter.img.save(tex)
        if painter.glow.getbbox():
            painter.glow.save(ASSETS / f"textures/entity/{name}_glowmask.png")
        print(f"{name}: {len(bones)} bones, texture {width}x{height}")
    for name, img in lure_textures().items():
        path = ASSETS / f"textures/block/{name}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path)
    for charge in range(5):
        write_json(ASSETS / f"models/block/gate_lure_{charge}.json", lure_model(charge))
    write_json(ASSETS / "blockstates/gate_lure.json", {"variants": {
        f"charge={charge}": {"model": f"shadowslave:block/gate_lure_{charge}"} for charge in range(5)}})
    write_json(ASSETS / "models/item/gate_lure.json", {"parent": "shadowslave:block/gate_lure_2"})
    print("gate_lure: 5 charge models")


if __name__ == "__main__":
    main()
