"""Draws the Field Note item icon: a loose, slightly curled parchment page
with a few lines of ink and a small gold rune where a True Name is written.

Usage: python3 mod/tools/draw_field_note_assets.py [assets/shadowslave directory]
The icon is a 16x16 grid; '.' is transparent and every other letter is a
palette colour. Edit the grid or palette and rerun. Needs Pillow.
"""
import sys
from pathlib import Path
from PIL import Image

ASSETS = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path(__file__).resolve().parent.parent / "src/main/resources/assets/shadowslave")

PALETTE = {
    "k": (74, 56, 36),     # outline
    "s": (176, 156, 118),  # page shadow
    "p": (226, 212, 176),  # page
    "P": (244, 234, 206),  # page light
    "i": (58, 50, 66),     # ink
    "g": (170, 120, 40),   # gold shadow
    "G": (236, 190, 86),   # gold rune
}

ICON = [
    "................",
    "...kkkkkkkkk....",
    "..kPPPPPPPPpk...",
    "..kPpiipiiipk...",
    "..kPppppppppk...",
    "..kPpiiipiipsk..",
    "..kPppppppppsk..",
    "..kPpipiiippsk..",
    "..kPppppppppsk..",
    "..kPppppGpppsk..",
    "..kPpppGgGppsk..",
    "..kPppppGpppsk..",
    "..kPpiippppsk...",
    "...kppppppssk...",
    "...kkkssskkk....",
    "......kkk.......",
]


def main():
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(ICON):
        for x, cell in enumerate(row):
            if cell != ".":
                image.putpixel((x, y), PALETTE[cell] + (255,))
    out = ASSETS / "textures/item/field_note.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    image.save(out)
    print("wrote", out)


if __name__ == "__main__":
    main()
