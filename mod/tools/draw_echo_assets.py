"""Draws the Echo item icons: the echo remnant a slain creature leaves and
the bound Echo it becomes at the Weaving Table.

Usage: python3 mod/tools/draw_echo_assets.py [assets/shadowslave directory]
Icons are 16x16 grids; '.' is transparent and every other letter is a palette
colour. Edit a grid or palette and rerun. Needs Pillow.
"""
import sys
from pathlib import Path
from PIL import Image

ASSETS = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path(__file__).resolve().parent.parent / "src/main/resources/assets/shadowslave")

SOUL = {"k": (18, 30, 44), "d": (32, 86, 112), "m": (64, 160, 196), "l": (128, 222, 242), "w": (232, 252, 255)}
BOND = {"v": (58, 34, 92), "p": (132, 92, 196), "P": (196, 160, 240)}

ICONS = {
    # A wisp of soul fire torn loose: the echo, not yet bound.
    "echo_remnant": (SOUL, [
        "................",
        ".......l........",
        "......lw........",
        "......lwl...m...",
        ".....mlwl..ml...",
        "....mmlwwl.ml...",
        "....mllwwlml....",
        "...dmlwwwwlm....",
        "...dmlwwwwlmd...",
        "..kdmllwwllmdk..",
        "..kddmllllmddk..",
        "...kddmmmmddk...",
        "....kkddddkk....",
        "......kkkk......",
        "................",
        "................",
    ]),
    # The same soul fire held inside a woven ring: an Echo, bound to someone.
    "bound_echo": ({**SOUL, **BOND}, [
        "................",
        ".....vvvvvv.....",
        "...vvpPPPPpvv...",
        "..vpPv....vPpv..",
        "..vPv..l...vPv..",
        ".vpv..lwl...vpv.",
        ".vPv.mlwwl..vPv.",
        ".vPv.mlwwlm.vPv.",
        ".vPv.dlwwwm.vPv.",
        ".vpv.dmllmd.vpv.",
        "..vPv.kddk.vPv..",
        "..vpPv....vPpv..",
        "...vvpPPPPpvv...",
        ".....vvvvvv.....",
        "................",
        "................",
    ]),
}


def draw(name, palette, grid):
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(grid):
        for x, cell in enumerate(row):
            if cell != ".":
                image.putpixel((x, y), palette[cell] + (255,))
    path = ASSETS / "textures/item" / (name + ".png")
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)
    print("wrote", path)


for name, (palette, grid) in ICONS.items():
    assert len(grid) == 16 and all(len(row) == 16 for row in grid), name
    draw(name, palette, grid)
