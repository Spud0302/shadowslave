"""Draws the Citadel Gateway: a carved stone frame around a veil of light.

Usage: python3 mod/tools/draw_citadel_assets.py [assets/shadowslave directory]
Writes textures/block/citadel_gateway_*.png (+ .mcmeta for the animated
veils), models/block/citadel_gateway_*.json and blockstates/citadel_gateway.json.
The veil shows who holds the Citadel: sealed (a dim ember-red murk while the
Guardian lives), open (a swirling violet once it falls) and claimed (a
steady pale soul-light). Edit the palette or shapes and rerun. Needs Pillow.
"""
import json
import math
import random
import sys
from pathlib import Path
from PIL import Image

ASSETS = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path(__file__).resolve().parent.parent / "src/main/resources/assets/shadowslave")

STONE_LOW = (30, 30, 36)
STONE_HIGH = (66, 64, 74)
RUNE = (150, 110, 210)
FRAMES = 8
VEILS = {
    # state: (dark, bright, core, light level)
    "sealed": ((26, 6, 8), (110, 28, 22), (190, 70, 40), 6),
    "open": ((34, 10, 62), (140, 70, 220), (240, 200, 255), 13),
    "claimed": ((14, 36, 52), (90, 200, 220), (230, 255, 255), 15),
}


def lerp(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def stone(seed, runes):
    """Worn dark stone in rough courses, with a thin carved rune line."""
    rng = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            shade = lerp(STONE_LOW, STONE_HIGH, 0.45 + rng.uniform(-0.18, 0.18))
            if y % 5 == 4 or (x + (y // 5) * 5) % 8 == 7:
                shade = lerp(STONE_LOW, (0, 0, 0), 0.4)
            img.putpixel((x, y), shade + (255,))
    if runes:
        for y in range(2, 14):
            if y % 3 != 1:
                img.putpixel((7, y), RUNE + (255,))
                img.putpixel((8, y), lerp(RUNE, STONE_LOW, 0.5) + (255,))
        for x in (5, 10):
            img.putpixel((x, 4), RUNE + (255,))
            img.putpixel((x, 10), RUNE + (255,))
    return img


def veil(state):
    """An animated strip: light swirling around a bright core."""
    dark, bright, core, _ = VEILS[state]
    img = Image.new("RGBA", (16, 16 * FRAMES))
    for frame in range(FRAMES):
        phase = frame / FRAMES * math.tau
        for y in range(16):
            for x in range(16):
                dx = x - 7.5
                dy = y - 7.5
                r = math.hypot(dx, dy) / 8.0
                angle = math.atan2(dy, dx)
                if state == "sealed":
                    swirl = 0.5 + 0.5 * math.sin(angle * 2 + phase * 0.5 + r * 3)
                    t = (1 - r) * 0.55 + swirl * 0.2
                elif state == "open":
                    swirl = 0.5 + 0.5 * math.sin(angle * 3 - phase + r * 9)
                    t = (1 - r) * 0.6 + swirl * 0.45
                else:
                    swirl = 0.5 + 0.5 * math.sin(r * 10 - phase)
                    t = (1 - r) * 0.75 + swirl * 0.25
                colour = lerp(dark, bright, t)
                if r < 0.18 + (0.05 if state != "sealed" else 0) * math.sin(phase):
                    colour = lerp(bright, core, 1 - r / 0.25)
                img.putpixel((x, frame * 16 + y), colour + (255,))
    return img


def face(texture, uv=None, light=None):
    entry = {"texture": texture}
    if uv:
        entry["uv"] = uv
    if light:
        entry["neoforge_data"] = {"block_light": light, "sky_light": light}
    return entry


def box(lo, hi, texture="#stone", top="#top"):
    faces = {f: face(top if f in ("up", "down") else texture) for f in ("north", "south", "east", "west", "up", "down")}
    return {"from": lo, "to": hi, "faces": faces}


def model(state):
    light = VEILS[state][3]
    elements = [
        # A stepped plinth.
        box([0, 0, 2], [16, 2, 14]),
        box([1, 2, 4], [15, 4, 12]),
        # Two posts carrying a lintel, open along the north-south axis.
        box([1, 4, 5], [4, 14, 11], "#runes"),
        box([12, 4, 5], [15, 14, 11], "#runes"),
        box([0, 14, 4], [16, 16, 12]),
        # The veil between them, lit from within.
        {"from": [4, 4, 7.5], "to": [12, 14, 8.5], "shade": False,
         "faces": {"north": face("#veil", [0, 1, 16, 16], light), "south": face("#veil", [0, 1, 16, 16], light),
                   "up": face("#veil", [0, 7, 16, 8], light), "down": face("#veil", [0, 7, 16, 8], light)}},
    ]
    textures = {
        "particle": "shadowslave:block/citadel_gateway_stone",
        "stone": "shadowslave:block/citadel_gateway_stone",
        "runes": "shadowslave:block/citadel_gateway_runes",
        "top": "shadowslave:block/citadel_gateway_top",
        "veil": f"shadowslave:block/citadel_gateway_veil_{state}",
    }
    return {"parent": "block/block", "ambientocclusion": False, "textures": textures, "elements": elements}


def main():
    textures = ASSETS / "textures/block"
    models = ASSETS / "models/block"
    stone(31, False).save(textures / "citadel_gateway_stone.png")
    stone(37, True).save(textures / "citadel_gateway_runes.png")
    top = stone(41, False)
    for x in range(3, 13):
        top.putpixel((x, 7), RUNE + (255,))
        top.putpixel((x, 8), lerp(RUNE, STONE_LOW, 0.5) + (255,))
    top.save(textures / "citadel_gateway_top.png")
    for state in VEILS:
        name = f"citadel_gateway_veil_{state}"
        veil(state).save(textures / f"{name}.png")
        (textures / f"{name}.png.mcmeta").write_text(json.dumps(
            {"animation": {"frametime": 3 if state == "open" else 4, "interpolate": True}}, indent=1) + "\n")
        (models / f"citadel_gateway_{state}.json").write_text(json.dumps(model(state), indent=1) + "\n")
    variants = {f"state={state}": {"model": f"shadowslave:block/citadel_gateway_{state}"} for state in VEILS}
    (ASSETS / "blockstates/citadel_gateway.json").write_text(json.dumps({"variants": variants}, indent=1) + "\n")


if __name__ == "__main__":
    main()
