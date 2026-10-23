"""Draws the Dream Realm assets: the Nightmare Seed's four growth stages.

Usage: python3 mod/tools/draw_dream_realm_assets.py [assets/shadowslave directory]
Writes textures/block/nightmare_seed_*.png, models/block/nightmare_seed_stage_*.json
and blockstates/nightmare_seed.json. The husk darkens and its veins burn
brighter as the Seed ripens; from stage 2 a crown of crystal thorns grows out
of it. Edit the palette or shapes and rerun. Needs Pillow.
"""
import json
import random
import sys
from pathlib import Path
from PIL import Image

ASSETS = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path(__file__).resolve().parent.parent / "src/main/resources/assets/shadowslave")

HUSK_LOW = (20, 13, 28)
HUSK_HIGH = (46, 30, 60)
VEIN = [(90, 44, 120), (128, 60, 176), (176, 90, 228), (226, 146, 255)]
CORE = (255, 220, 255)
STAGES = 4


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def husk(stage):
    """A smooth dark husk, lighter towards the top, split by thin veins that
    branch further and glow brighter each stage."""
    rng = random.Random(1000 + stage)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            t = 1.0 - y / 15.0
            shade = lerp(HUSK_LOW, HUSK_HIGH, 0.35 + 0.5 * t + rng.uniform(-0.08, 0.08))
            img.putpixel((x, y), shade + (255,))
    vein = VEIN[stage]
    for start in (2, 7, 12)[:1 + stage] + ((4, 10) if stage == 3 else ()):
        x = start
        for y in range(15, 15 - (6 + stage * 3), -1):
            img.putpixel((x, y), vein + (255,))
            if rng.random() < 0.35:
                x = max(0, min(15, x + rng.choice((-1, 1))))
                img.putpixel((x, y), vein + (255,))
    if stage == 3:
        for _ in range(4):
            img.putpixel((rng.randrange(16), rng.randrange(2, 8)), CORE + (255,))
    return img


def top(stage):
    """The crown of the husk: a split that opens wider as the Seed ripens."""
    img = husk(stage).copy()
    radius = 1.5 + stage
    for x in range(16):
        for y in range(16):
            d = abs(x - 7.5) + abs(y - 7.5)
            if d < radius:
                img.putpixel((x, y), (CORE if d < radius / 2 else VEIN[stage]) + (255,))
    return img


def thorns(stage):
    """Crystal thorns drawn on a cross plane, transparent around them."""
    rng = random.Random(2000 + stage)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for base in (3, 7, 11):
        height = 6 + stage * 2 + rng.randrange(3)
        for i in range(min(16, height)):
            y = 15 - i
            width = max(0, 2 - i // 4)
            for dx in range(-width, width + 1):
                x = base + dx + (i // 5) * (1 if base > 7 else -1 if base < 7 else 0)
                if 0 <= x < 16:
                    colour = CORE if dx == 0 and i > height - 4 else VEIN[min(3, stage + (1 if dx == 0 else 0))]
                    img.putpixel((x, y), colour + (230,))
    return img


def cube(lo, hi, side, top_tex, light):
    faces = {}
    for face in ("north", "south", "east", "west", "down", "up"):
        tex = "#top" if face == "up" else "#side"
        entry = {"texture": tex}
        if light:
            entry["neoforge_data"] = {"block_light": light}
        faces[face] = entry
    return {"from": lo, "to": hi, "faces": faces}


def cross(height, light):
    planes = []
    for angle, (lo, hi) in ((45, ([0.8, 0, 8], [15.2, height, 8])), (-45, ([8, 0, 0.8], [8, height, 15.2]))):
        faces = {f: {"texture": "#thorns", "uv": [0, 16 - height, 16, 16], "neoforge_data": {"block_light": light}}
                 for f in (("north", "south") if angle == 45 else ("east", "west"))}
        planes.append({"from": lo, "to": hi, "shade": False,
                       "rotation": {"origin": [8, 8, 8], "axis": "y", "angle": angle, "rescale": True},
                       "faces": faces})
    return planes


SIZES = [(5, 6), (4, 9), (3, 11), (2, 13)]


def model(stage):
    inset, height = SIZES[stage]
    light = 4 + stage * 3
    elements = [cube([inset, 0, inset], [16 - inset, height, 16 - inset], "#side", "#top", light)]
    # A narrower cap so the pod tapers.
    cap = min(7, inset + 2)
    elements.append(cube([cap, height, cap], [16 - cap, height + 1 + stage // 2, 16 - cap], "#side", "#top", light))
    textures = {
        "particle": f"shadowslave:block/nightmare_seed_husk_{stage}",
        "side": f"shadowslave:block/nightmare_seed_husk_{stage}",
        "top": f"shadowslave:block/nightmare_seed_top_{stage}",
    }
    if stage >= 2:
        textures["thorns"] = f"shadowslave:block/nightmare_seed_thorns_{stage}"
        elements += cross(min(16, height + 3 + stage), min(15, light + 3))
    return {"parent": "block/block", "render_type": "minecraft:cutout", "ambientocclusion": False,
            "textures": textures, "elements": elements}


def main():
    textures = ASSETS / "textures/block"
    models = ASSETS / "models/block"
    for stage in range(STAGES):
        husk(stage).save(textures / f"nightmare_seed_husk_{stage}.png")
        top(stage).save(textures / f"nightmare_seed_top_{stage}.png")
        if stage >= 2:
            thorns(stage).save(textures / f"nightmare_seed_thorns_{stage}.png")
        (models / f"nightmare_seed_stage_{stage}.json").write_text(json.dumps(model(stage), indent=1) + "\n")
    variants = {f"stage={stage}": {"model": f"shadowslave:block/nightmare_seed_stage_{stage}"} for stage in range(STAGES)}
    (ASSETS / "blockstates/nightmare_seed.json").write_text(json.dumps({"variants": variants}, indent=1) + "\n")


if __name__ == "__main__":
    main()
