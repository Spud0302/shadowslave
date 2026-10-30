"""Draws the assets of Nightmares slice two: the Thornwake Basin's black briar
and the Nightmare Scar's effect icon.

Usage: python3 mod/tools/draw_thornwake_assets.py [assets/shadowslave directory]
Writes textures/block/black_briar.png with its model and blockstate (a cutout
thicket: stems, thorns and a few red buds, see-through between them) and
textures/mob_effect/nightmare_scar.png (a soul split by a red scar). Edit the
palette and rerun. Needs Pillow.
"""
import json
import math
import random
import sys
from pathlib import Path

from PIL import Image

ASSETS = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path(__file__).resolve().parent.parent / "src/main/resources/assets/shadowslave")

STEM_DARK = (22, 26, 20)
STEM = (40, 48, 34)
STEM_LIGHT = (66, 74, 52)
THORN = (196, 180, 152)
BUD = (196, 30, 52)


def black_briar():
    """Three tangled stems wind across the tile; thorns jut from them; the gaps stay clear."""
    rng = random.Random(3131)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    def put(x, y, c):
        img.putpixel((x % 16, y % 16), c + (255,))
    for n, (phase, slope) in enumerate(((0.0, 0.6), (2.1, -0.8), (4.0, 0.25), (1.2, -0.3), (5.3, 1.1))):
        for t in range(48):
            x = t / 3.0
            y = (7.5 + 5.5 * math.sin(x * 0.75 + phase) + slope * (x - 8)) % 16
            xi, yi = int(x), int(y)
            put(xi, yi, STEM if n % 2 == 0 else STEM_DARK)
            put(xi, yi + 1, STEM_DARK)
            if rng.random() < 0.25:
                put(xi, yi - 1, STEM_LIGHT)
            if rng.random() < 0.12:
                put(xi + rng.choice((-1, 1)), yi + rng.choice((-1, 2)), THORN)
    for _ in range(3):
        x, y = rng.randrange(16), rng.randrange(16)
        if img.getpixel((x, y))[3]:
            put(x, y, BUD)
    return img


def scar_icon():
    """An 18x18 pale soul flame with a red tear running down it."""
    img = Image.new("RGBA", (18, 18), (0, 0, 0, 0))
    for y in range(18):
        for x in range(18):
            dx, dy = (x - 8.5) / 6.5, (y - 10.0) / 7.5
            flame = dx * dx + dy * dy * (0.8 if y > 9 else 1.6)
            if flame <= 1.0:
                t = 1.0 - flame
                c = tuple(int(70 + (210 - 70) * t * a) for a in (0.8, 0.75, 1.0))
                img.putpixel((x, y), c + (255,))
    for y, x in ((3, 10), (4, 10), (5, 9), (6, 9), (7, 8), (8, 9), (9, 9), (10, 8), (11, 8), (12, 9), (13, 9), (14, 8)):
        img.putpixel((x, y), (200, 24, 48, 255))
        img.putpixel((x + 1, y), (120, 10, 30, 255))
    return img


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=1) + "\n")


def main():
    path = ASSETS / "textures/block/black_briar.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    black_briar().save(path)
    write_json(ASSETS / "models/block/black_briar.json", {
        "parent": "minecraft:block/cube_all",
        "render_type": "minecraft:cutout",
        "textures": {"all": "shadowslave:block/black_briar"},
    })
    write_json(ASSETS / "blockstates/black_briar.json", {"variants": {
        "withering=false": {"model": "shadowslave:block/black_briar"},
        "withering=true": {"model": "shadowslave:block/black_briar"},
    }})
    icon = ASSETS / "textures/mob_effect/nightmare_scar.png"
    icon.parent.mkdir(parents=True, exist_ok=True)
    scar_icon().save(icon)
    write_json(ASSETS / "models/item/thorn_matron_spawn_egg.json", {"parent": "minecraft:item/template_spawn_egg"})
    print("black_briar, nightmare_scar icon, thorn_matron spawn egg")


if __name__ == "__main__":
    main()
