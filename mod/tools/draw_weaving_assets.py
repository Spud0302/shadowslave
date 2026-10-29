"""Draws the Weaving assets: the Weaving Table block, the creature materials,
the woven Forging Memory icons and their worn armour layers.

Usage: python3 mod/tools/draw_weaving_assets.py [assets/shadowslave directory]
Icons are 16x16 grids; '.' is transparent and every other letter is a palette
colour. Armour layers are filled into the vanilla 64x32 armour UV regions.
Edit a grid or palette and rerun. Needs Pillow.
"""
import random
import sys
from pathlib import Path
from PIL import Image

ASSETS = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path(__file__).resolve().parent.parent / "src/main/resources/assets/shadowslave")

SOUL = {"k": (40, 34, 52), "d": (110, 112, 138), "m": (170, 176, 198), "l": (214, 222, 236), "w": (255, 255, 255)}
AQUA = {"q": (64, 176, 206), "Q": (160, 236, 248)}
WOOD = {"a": (92, 60, 36), "A": (138, 94, 56)}
IRON = {"b": (96, 96, 104), "B": (176, 176, 186)}

ICONS = {
    "shard_knife": ({**SOUL, **WOOD, **IRON}, [
        "................",
        "............kk..",
        "...........kwlk.",
        "..........kwlmk.",
        ".........kwlmk..",
        "........kwlmk...",
        ".......kwlmk....",
        "......kwlmk.....",
        "..kk.kwlmk......",
        "..kBkklmk.......",
        "...kBbkk........",
        "...kAbBk........",
        "..kAak.kk.......",
        ".kAak...........",
        ".kak............",
        "..k.............",
    ]),
    "sleepers_hood": ({"k": (24, 18, 34), "d": (52, 38, 74), "m": (82, 62, 112), "l": (120, 98, 156), "w": (176, 160, 210),
                       "q": (64, 176, 206)}, [
        "................",
        "................",
        ".....kkkkkk.....",
        "....kmmllmmk....",
        "...kmllwllmmk...",
        "...kmlmmmmlmk...",
        "..kmmkkkkkkmmk..",
        "..kmk......kmk..",
        "..kmk......kmk..",
        "..kmdk....kdmk..",
        "..kddk....kddk..",
        "..kdk..kq..kdk..",
        "...k...kk...k...",
        "................",
        "................",
        "................",
    ]),
    "wanderers_boots": ({"k": (36, 24, 16), "d": (84, 56, 34), "m": (124, 86, 52), "l": (164, 120, 78), "w": (196, 160, 110),
                         "q": (180, 186, 206)}, [
        "................",
        "................",
        "................",
        "...kkkk...kkkk..",
        "...kqqk...kqqk..",
        "...kllk...kllk..",
        "...kmmk...kmmk..",
        "...kmmk...kmmk..",
        "...kmmk...kmmk..",
        "..kmmmk..kmmmk..",
        ".kmmlmk.kmmlmk..",
        ".kmmmmk.kmmmmk..",
        ".kddddk.kddddk..",
        ".kkkkkk.kkkkkk..",
        "................",
        "................",
    ]),
    "ashen_hide_vest": ({"k": (30, 26, 24), "d": (70, 62, 58), "m": (110, 100, 94), "l": (150, 140, 132), "w": (190, 182, 174),
                         "e": (232, 112, 48)}, [
        "................",
        "................",
        "..kkk......kkk..",
        ".kmmmk....kmmmk.",
        ".kmlmmkkkkmmlmk.",
        ".kmmlmmmmmmlmmk.",
        "..kkmmlmmlmmkk..",
        "....kmmmemmk....",
        "....kmlmmmlk....",
        "....kmmmmmmk....",
        "....kmemmlmk....",
        "....kmmlmmek....",
        "....kddddddk....",
        "....kkkkkkkk....",
        "................",
        "................",
    ]),
    "linked_cleaver": ({"k": (28, 24, 26), "d": (80, 58, 46), "m": (124, 90, 64), "l": (170, 130, 94), "w": (214, 186, 150),
                        **IRON, **WOOD}, [
        "................",
        ".....kkkkkk.....",
        "....kBBBBBBk....",
        "....kBbbbbBk....",
        "....kBbbbbBk....",
        "....kBbbbbBk....",
        ".....kbbbbk.....",
        "......kddk......",
        ".......kk.......",
        "......kmmk......",
        "......kdlk......",
        ".......kk.......",
        "......kmmk......",
        "......kAak......",
        "......kAak......",
        ".......kk.......",
    ]),
    "chainback_mail": ({"k": (24, 22, 24), "d": (70, 52, 42), "m": (110, 86, 66), "l": (160, 128, 96), "w": (206, 176, 140),
                        "q": (64, 176, 206)}, [
        "................",
        "................",
        "..kkk......kkk..",
        ".kldlk....kldlk.",
        ".kdldlkkkkldldk.",
        ".kldldldldldldk.",
        "..kkldldldldkk..",
        "....kdldldlk....",
        "....kldqqldk....",
        "....kdldldlk....",
        "....kldldldk....",
        "....kdldldlk....",
        "....kmmmmmmk....",
        "....kkkkkkkk....",
        "................",
        "................",
    ]),
    "hollow_visage": ({"k": (26, 22, 30), "d": (150, 142, 128), "m": (196, 188, 172), "l": (226, 220, 206), "w": (250, 248, 240),
                       "x": (14, 10, 18), "q": (120, 220, 240)}, [
        "................",
        "................",
        ".....kkkkkk.....",
        "....kllwwllk....",
        "...klmmllmmlk...",
        "...kmmmmmmmmk...",
        "...kmxxmmxxmk...",
        "...kmxqmmxqmk...",
        "...kmmmmmmmmk...",
        "...kdmmmmmmdk...",
        "....kdmxxmdk....",
        "....kdmmmmdk....",
        ".....kdddddk....",
        "......kkkkk.....",
        "................",
        "................",
    ]),
    "hollow_edge": ({"k": (26, 22, 30), "d": (150, 142, 128), "m": (196, 188, 172), "l": (230, 224, 210), "w": (255, 255, 250),
                     "x": (30, 22, 40), **IRON, **WOOD}, [
        "................",
        "............kk..",
        "...........klwk.",
        "..........klxlk.",
        ".........klxlk..",
        "........klxlk...",
        ".......klxlk....",
        "......klxlk.....",
        ".....klxlk......",
        "..kk.kxlk.......",
        "..kBkklk........",
        "...kBbkk........",
        "...kAbBk........",
        "..kAak.kk.......",
        ".kAak...........",
        ".kk.............",
    ]),
    # A loose strand of spun essence, coiled: what a Forger pulls out of a piece.
    "woven_thread": ({"k": (30, 20, 46), "d": (92, 64, 140), "m": (150, 116, 210), "l": (200, 176, 246), "w": (246, 238, 255),
                      "q": (64, 176, 206), "Q": (160, 236, 248)}, [
        "................",
        "......kkkk......",
        "....kkmllmkk....",
        "...kmlwwwwlmk...",
        "..kmlk....klmk..",
        "..klk.kkkk.klk..",
        ".kmlkkmQQmkklmk.",
        ".klk.kQwwQk.klk.",
        ".klk.kQwwQk.klk.",
        ".kmlkkmQQmkklmk.",
        "..klk.kkkk.kldk.",
        "..kmlk....kldk..",
        "...kmlwwwwldk...",
        "....kkmlldkkqq..",
        "......kkkk...qQ.",
        ".............Qq.",
    ]),
    "ashen_hide": ({"k": (30, 26, 24), "d": (70, 62, 58), "m": (110, 100, 94), "l": (150, 140, 132), "w": (190, 182, 174),
                    "e": (232, 112, 48), "E": (255, 180, 90)}, [
        "................",
        "................",
        "...kkkk.........",
        "..kmmllkkk......",
        "..kmlwllmmkk....",
        ".kmmlllmemmmk...",
        ".kmmmlmmmEmmk...",
        ".kdmmmmmmmmmmk..",
        "..kdmmemmmlmmk..",
        "..kddmmmmmllmk..",
        "...kddmmmmmmk...",
        "....kkddmmdk....",
        "......kkddk.....",
        "........kk......",
        "................",
        "................",
    ]),
    "chainback_link": ({"k": (24, 22, 24), "d": (84, 54, 38), "m": (132, 86, 56), "l": (180, 124, 84), "w": (222, 176, 130)}, [
        "................",
        "................",
        "...kkkk.........",
        "..kllmmk........",
        ".klk..kmk.......",
        ".kmk..kmk.......",
        ".kmk.kkkkk......",
        ".kdkklwlmmk.....",
        "..kklk..kdmk....",
        "....kmk..kdk....",
        "....kmk..kdk....",
        "....kdkkkkdk....",
        ".....kddddk.....",
        "......kkkk......",
        "................",
        "................",
    ]),
    "mimic_husk": ({"k": (26, 22, 30), "d": (150, 142, 128), "m": (196, 188, 172), "l": (226, 220, 206), "w": (250, 248, 240),
                    "x": (14, 10, 18), "r": (120, 36, 48)}, [
        "................",
        "................",
        "......kkkk......",
        "....kkllwlkk....",
        "...klllllmmmk...",
        "..klmmmmmmmmdk..",
        "..kmmmkxxkmmdk..",
        "..kmmkxrrxkmdk..",
        "..kmmkxxxxkmdk..",
        "..kdmmkkkkmmdk..",
        "...kdmmmmmmdk...",
        "....kddmmddk....",
        ".....kkddkk.....",
        ".......kk.......",
        "................",
        "................",
    ]),
}


def icon(name, palette, grid):
    assert len(grid) == 16 and all(len(row) == 16 for row in grid), name
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(grid):
        for x, cell in enumerate(row):
            if cell != ".":
                image.putpixel((x, y), palette[cell] + (255,))
    image.save(ASSETS / "textures/item" / f"{name}.png")


def shade(colour, amount):
    return tuple(max(0, min(255, c + amount)) for c in colour)


def planks(rng, base):
    image = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            seam = y % 4 == 3
            image.putpixel((x, y), shade(base, -34 if seam else rng.randint(-10, 8)) + (255,))
    return image


def table_textures():
    rng = random.Random(7)
    wood = (118, 82, 52)
    frame = (72, 48, 32)
    thread = [(160, 236, 248), (214, 179, 255), (122, 92, 170)]

    top = planks(rng, (96, 66, 44))
    for x in range(16):
        for y in (0, 15):
            top.putpixel((x, y), frame + (255,))
            top.putpixel((y, x), frame + (255,))
    # Warp threads run north to south, a weft crosses them halfway.
    for i, x in enumerate(range(3, 14, 2)):
        for y in range(1, 15):
            top.putpixel((x, y), thread[i % 2] + (255,))
    for x in range(1, 15):
        if x % 2 == 0:
            top.putpixel((x, 7), thread[2] + (255,))
            top.putpixel((x, 8), thread[0] + (255,))
    top.save(ASSETS / "textures/block/weaving_table_top.png")

    side = Image.new("RGBA", (16, 16), (34, 24, 20, 255))
    for y in range(16):
        for x in range(16):
            leg = x <= 2 or x >= 13
            if y <= 4 or leg:
                side.putpixel((x, y), shade(wood if y > 0 else frame, rng.randint(-12, 6)) + (255,))
            elif 6 <= y <= 13:
                # The loom's beam with threads hanging from it.
                colour = thread[(x // 2) % 3] if x % 2 == 1 else shade(frame, -10)
                side.putpixel((x, y), (colour if y > 6 else frame) + (255,))
    for x in range(16):
        side.putpixel((x, 4), frame + (255,))
        side.putpixel((x, 14), frame + (255,))
    side.save(ASSETS / "textures/block/weaving_table_side.png")

    planks(rng, wood).save(ASSETS / "textures/block/weaving_table_bottom.png")


ARMOUR = {
    "woven_cloth": ((82, 62, 112), (120, 98, 156)),
    "ashen_hide": ((110, 100, 94), (150, 140, 132)),
    "chainback": ((110, 86, 66), (160, 128, 96)),
    "hollow": ((196, 188, 172), (226, 220, 206)),
}


def armour_layer(name, colours, rng):
    dark, light = colours
    image = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def fill(x0, y0, x1, y1, pattern=None):
        for y in range(y0, y1):
            for x in range(x0, x1):
                base = light if (pattern and pattern(x, y)) else dark
                image.putpixel((x, y), shade(base, rng.randint(-8, 8)) + (255,))

    links = (lambda x, y: (x + y) % 2 == 0) if name == "chainback" else None
    # Helmet: the head box, leaving the face open below the brim for cloth.
    fill(8, 0, 24, 8)
    fill(0, 8, 32, 16, links)
    if name in ("woven_cloth",):
        for y in range(10, 16):
            for x in range(9, 15):
                image.putpixel((x, y), (0, 0, 0, 0))
    if name == "hollow":
        for x, y in ((10, 11), (11, 11), (13, 11), (14, 11), (11, 14), (12, 14)):
            image.putpixel((x, y), (14, 10, 18, 255))
        image.putpixel((11, 12), (120, 220, 240, 255))
        image.putpixel((13, 12), (120, 220, 240, 255))
    # Chest: body box and short sleeves.
    fill(20, 16, 36, 20)
    fill(16, 20, 40, 32, links)
    fill(44, 16, 52, 20)
    fill(40, 20, 56, 26, links)
    if name == "ashen_hide":
        for x, y in ((22, 23), (25, 27), (34, 25), (18, 29)):
            image.putpixel((x, y), (232, 112, 48, 255))
    # Boots: the bottom rows of the legs.
    fill(8, 16, 12, 20)
    fill(0, 26, 16, 32)
    image.save(ASSETS / "textures/models/armor" / f"{name}_layer_1.png")


def main():
    (ASSETS / "textures/models/armor").mkdir(parents=True, exist_ok=True)
    for name, (palette, grid) in ICONS.items():
        icon(name, palette, grid)
    table_textures()
    rng = random.Random(11)
    for name, colours in ARMOUR.items():
        armour_layer(name, colours, rng)


if __name__ == "__main__":
    main()
