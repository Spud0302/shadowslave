"""Draws the Smithing assets: the Forge Hearth, Smithing Anvil and Quench
Trough block textures, the Smith's Hammer, and the billet and bundle icons
(the billet is drawn in greys and tinted by its heat in game).

Usage: python3 mod/tools/draw_smithing_assets.py [assets/shadowslave directory]
Icons are 16x16 grids; '.' is transparent and every other letter is a palette
colour. Block textures are drawn procedurally from a fixed seed, so a rerun
gives the same pixels. Needs Pillow.
"""
import random
import sys
from pathlib import Path
from PIL import Image

ASSETS = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path(__file__).resolve().parent.parent / "src/main/resources/assets/shadowslave")

IRON = {"k": (26, 26, 32), "d": (62, 62, 72), "m": (104, 104, 116), "l": (150, 150, 162), "w": (206, 206, 214)}
WOOD = {"a": (78, 50, 30), "A": (128, 86, 50), "h": (164, 116, 70)}
GREY = {"k": (40, 40, 40), "d": (120, 120, 120), "m": (170, 170, 170), "l": (215, 215, 215), "w": (255, 255, 255)}
HIDE = {"k": (34, 24, 18), "d": (88, 62, 44), "m": (128, 92, 62), "l": (168, 128, 90), "s": (214, 196, 160)}

ICONS = {
    "smiths_hammer": ({**IRON, **WOOD}, [
        "................",
        "......kkkk......",
        ".....kwllmk.....",
        "....kwlllmdk....",
        "....kllmmmdk....",
        "....kmmmmddk....",
        ".....kmddk.k....",
        "......kAak......",
        "......kAak......",
        "......khak......",
        "......kAak......",
        "......kAak......",
        "......khak......",
        "......kAak......",
        "......kaak......",
        ".......kk.......",
    ]),
    # Drawn in greys: the game tints it from iron grey to white heat.
    "billet": (GREY, [
        "................",
        "................",
        "................",
        "................",
        "................",
        "..kkkkkkkkkkkk..",
        ".kwwllllllllmmk.",
        ".kwlllllllllmdk.",
        ".klllmllllmmmdk.",
        ".kmmmmmmmmmmddk.",
        ".kddddddddddddk.",
        "..kkkkkkkkkkkk..",
        "................",
        "................",
        "................",
        "................",
    ]),
    "billet_bundle": (HIDE, [
        "................",
        "................",
        "................",
        "....kkkkkkkk....",
        "...kllllllmmk...",
        "..klllmmllmmdk..",
        "..kssssssssssk..",
        "..kmmmmlmmmmdk..",
        "..klllmmllmmdk..",
        "..kssssssssssk..",
        "..kmmmmmmmmddk..",
        "...kddddddddk...",
        "....kkkkkkkk....",
        "................",
        "................",
        "................",
    ]),
}


def icon(name, palette, grid):
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(grid):
        assert len(row) == 16, (name, y, len(row))
        for x, key in enumerate(row):
            if key != ".":
                image.putpixel((x, y), (*palette[key], 255))
    image.save(ASSETS / "textures/item" / f"{name}.png")


def shade(colour, amount):
    return tuple(max(0, min(255, int(c + amount))) for c in colour)


def bricks(rng, base, mortar):
    image = Image.new("RGBA", (16, 16))
    for y in range(16):
        row = y // 4
        for x in range(16):
            offset = 4 if row % 2 else 0
            edge = y % 4 == 3 or (x + offset) % 8 == 7
            colour = mortar if edge else shade(base, rng.randint(-14, 10) - (row % 2) * 6)
            image.putpixel((x, y), (*colour, 255))
    return image


def coals(rng, lit):
    image = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            if lit:
                heat = rng.random()
                colour = (255, 230, 140) if heat > 0.9 else (246, 150, 50) if heat > 0.6 else (180, 60, 24) if heat > 0.3 else (52, 30, 26)
            else:
                colour = shade((44, 40, 40), rng.randint(-10, 12))
            image.putpixel((x, y), (*colour, 255))
    return image


def hearth_textures():
    rng = random.Random(41)
    brick = (122, 64, 50)
    mortar = (70, 60, 58)
    side = bricks(rng, brick, mortar)
    side.save(ASSETS / "textures/block/forge_hearth_side.png")
    bricks(random.Random(42), shade(brick, -20), mortar).save(ASSETS / "textures/block/forge_hearth_bottom.png")
    for lit in (False, True):
        top = side.copy()
        inner = coals(random.Random(43), lit)
        for y in range(3, 13):
            for x in range(3, 13):
                top.putpixel((x, y), inner.getpixel((x, y)))
        for i in range(2, 14):
            for x, y in ((i, 2), (i, 13), (2, i), (13, i)):
                top.putpixel((x, y), (*IRON["d"], 255))
        top.save(ASSETS / "textures/block" / ("forge_hearth_top_lit.png" if lit else "forge_hearth_top.png"))
        front = side.copy()
        fire = coals(random.Random(44), lit)
        for y in range(6, 14):
            for x in range(3, 13):
                arch = y == 6 and x in (3, 12)
                front.putpixel((x, y), (*IRON["k"], 255) if arch else fire.getpixel((x, y)))
        for x in range(2, 14):
            front.putpixel((x, 5), (*IRON["m"], 255))
            front.putpixel((x, 14), (*IRON["d"], 255))
        front.save(ASSETS / "textures/block" / ("forge_hearth_front_lit.png" if lit else "forge_hearth_front.png"))


def anvil_textures():
    rng = random.Random(51)
    for name, base in (("smithing_anvil", (58, 58, 66)), ("smithing_anvil_top", (78, 78, 88))):
        image = Image.new("RGBA", (16, 16))
        for y in range(16):
            for x in range(16):
                colour = shade(base, rng.randint(-8, 8))
                if name.endswith("top") and 3 <= y <= 12 and rng.random() < 0.12:
                    colour = shade(base, 40)
                image.putpixel((x, y), (*colour, 255))
        if name.endswith("top"):
            for x in range(16):
                image.putpixel((x, 0), (*IRON["l"], 255))
                image.putpixel((x, 15), (*IRON["d"], 255))
        image.save(ASSETS / "textures/block" / f"{name}.png")


def trough_textures():
    rng = random.Random(61)
    side = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            plank = (y // 4) % 2
            colour = shade(WOOD["A"] if plank else WOOD["a"], rng.randint(-10, 10))
            if y % 4 == 3:
                colour = shade(WOOD["a"], -20)
            if y in (1, 12):
                colour = shade(IRON["m"], rng.randint(-8, 8))
            side.putpixel((x, y), (*colour, 255))
    side.save(ASSETS / "textures/block/quench_trough_side.png")
    water = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            ripple = 18 if (x + y * 2) % 7 == 0 else 0
            water.putpixel((x, y), (*shade((44, 92, 168), rng.randint(-8, 8) + ripple), 255))
    water.save(ASSETS / "textures/block/quench_trough_water.png")


def main():
    (ASSETS / "textures/item").mkdir(parents=True, exist_ok=True)
    (ASSETS / "textures/block").mkdir(parents=True, exist_ok=True)
    for name, (palette, grid) in ICONS.items():
        icon(name, palette, grid)
    hearth_textures()
    anvil_textures()
    trough_textures()


if __name__ == "__main__":
    main()
