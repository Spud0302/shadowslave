"""Draws the Field Guide item icon: a strapped leather field book with a
brass compass rose on its cover and parchment page edges.

Usage: python3 mod/tools/draw_field_guide_assets.py [assets/shadowslave directory]
The icon is a 16x16 grid; '.' is transparent and every other letter is a
palette colour. Edit the grid or palette and rerun. Needs Pillow.
"""
import sys
from pathlib import Path
from PIL import Image

ASSETS = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path(__file__).resolve().parent.parent / "src/main/resources/assets/shadowslave")

PALETTE = {
    "k": (38, 24, 14),     # outline
    "d": (78, 50, 29),     # leather shadow
    "m": (112, 72, 40),    # leather
    "l": (146, 98, 56),    # leather highlight
    "p": (234, 223, 194),  # page edge
    "P": (200, 184, 150),  # page shadow
    "g": (150, 112, 40),   # brass shadow
    "G": (214, 172, 76),   # brass
    "Y": (250, 226, 140),  # brass glint
    "r": (142, 43, 43),    # ribbon
}

ICON = [
    "................",
    "..kkkkkkkkkkk...",
    ".kllllllllllmk..",
    ".klmmmmmmmmmdpk.",
    ".klmmmmGmmmmdpk.",
    ".klmmmGYGmmmdpk.",
    ".klmmgGYGgmmdPk.",
    ".klmGYYgYYGmdpk.",
    ".klmmgGgGgmmdpk.",
    ".klmmmGgGmmmdPk.",
    ".kkkkkkGkkkkkpk.",
    ".klmmmmmmmmmdpk.",
    ".klmmmmmmmmmdPk.",
    ".kdddddddddddkk.",
    "..kkkkkkkkrkk...",
    "..........r.....",
]


def draw(grid, palette):
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(grid):
        for x, cell in enumerate(row):
            if cell != ".":
                image.putpixel((x, y), palette[cell] + (255,))
    return image


def main():
    out = ASSETS / "textures/item/field_guide.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    draw(ICON, PALETTE).save(out)
    print("wrote", out)


if __name__ == "__main__":
    main()
