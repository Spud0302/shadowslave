"""Draws the 16x16 soul shard item icons, one shape recoloured per creature rank.

Usage: python3 mod/tools/draw_shard_icons.py [textures/item directory]
Grid letters: k outline, d shadow, m body, l light, w glint; '.' is transparent.
Edit the grid or a palette and rerun. Needs Pillow.
"""
import sys
from pathlib import Path
from PIL import Image

OUT = sys.argv[1] if len(sys.argv) > 1 else str(
    Path(__file__).resolve().parent.parent / "src/main/resources/assets/shadowslave/textures/item")

SHARD = [
    "................",
    "..........k.....",
    ".........kwk....",
    "........kwlmk...",
    ".......kwllmdk..",
    "......kwllmmdk..",
    ".....kwllmmdk...",
    "....kllllmmdk...",
    "....kdlllmddk...",
    "...kddmlmmdk....",
    "...kddmmmddk....",
    "..kdddmmddk.....",
    "..kdddmddk......",
    "...kkddddk......",
    ".....kkkk.......",
    "................",
]

PALETTES = {
    # Dormant: pale, almost colourless soul light.
    "dormant_soul_shard": {"k": (52, 50, 66), "d": (126, 130, 152), "m": (180, 186, 206), "l": (222, 228, 240), "w": (255, 255, 255)},
    # Awakened: clear aqua.
    "awakened_soul_shard": {"k": (14, 52, 70), "d": (30, 112, 140), "m": (64, 176, 206), "l": (138, 226, 242), "w": (232, 255, 255)},
    # Fallen: dark red.
    "fallen_soul_shard": {"k": (54, 10, 16), "d": (120, 22, 34), "m": (182, 42, 52), "l": (236, 102, 104), "w": (255, 214, 206)},
    # Corrupted: violet-black with a magenta glint.
    "corrupted_soul_shard": {"k": (16, 6, 26), "d": (52, 18, 78), "m": (96, 34, 138), "l": (174, 74, 210), "w": (255, 176, 255)},
}


def draw(name, palette):
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(SHARD):
        for x, cell in enumerate(row):
            if cell != ".":
                image.putpixel((x, y), palette[cell] + (255,))
    image.save(Path(OUT) / f"{name}.png")


if __name__ == "__main__":
    for shard, colours in PALETTES.items():
        draw(shard, colours)
        print("wrote", shard)
