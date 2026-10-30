"""Builds the hand-made structures for the Nightmares "The Last Vigil" and "The Long Thorn".

Usage: python3 mod/tools/build_nightmare_structures.py [data/shadowslave directory]
Writes structure/nightmare/last_vigil.nbt (the Ember Watch as it stood, the
night the ash first fell; the Nightmare is played inside it) and
structure/nightmare/ember_watch_ruin.nbt (what is left of it today, raised at
its echo site in the Ashen Expanse). Both share one footprint so the ruin reads
as the same place.

It also writes structure/nightmare/long_thorn.nbt (the Briar Cloister in the
season the briars came, a Category 3 Long Nightmare) and
structure/nightmare/briar_cloister_ruin.nbt (the cloister today, swallowed by
black briar in the Thornwake Basin). Edit the layout below and rerun; no
third-party packages.

The point names used by data/shadowslave/nightmare_trial/nightmare/last_vigil.json
(arrival, lamps, breaches) are listed in POINTS, and those of long_thorn.json in
CLOISTER_POINTS; both must match the layout.
"""
import gzip
import random
import struct
import sys
from pathlib import Path

DATA = Path(sys.argv[1]) if len(sys.argv) > 1 else (
    Path(__file__).resolve().parent.parent / "src/main/resources/data/shadowslave")
DATA_VERSION = 3955  # Minecraft 1.21.1

SIZE = 61
FLOOR = 3  # ground level; three layers of foundation below it
WALL_LO, WALL_HI = 10, 50  # the curtain wall's outer square
TOWERS = {"nw": (8, 8), "ne": (48, 8), "sw": (8, 48), "se": (48, 48)}
LAMP_TOWERS = ("nw", "ne", "sw")
TOWER_TOP = FLOOR + 10
KEEP_LO, KEEP_HI = 26, 34
KEEP_TOP = FLOOR + 14

# Points the Nightmare pack refers to, in structure coordinates.
POINTS = {
    "arrival": (30, FLOOR + 1, 42),
    "lamp_nw": (10, TOWER_TOP + 1, 10),
    "lamp_ne": (50, TOWER_TOP + 1, 10),
    "lamp_sw": (10, TOWER_TOP + 1, 50),
    "ash_west": (3, FLOOR + 1, 30),
    "ash_east": (57, FLOOR + 1, 30),
    "ash_north": (30, FLOOR + 1, 3),
    "gate_south": (30, FLOOR + 1, 57),
    "ashfall": (30, FLOOR + 1, 55),
}


# ---- a tiny NBT writer ---------------------------------------------------------------------------------------------

def _name(name):
    data = name.encode("utf-8")
    return struct.pack(">H", len(data)) + data


class Int(int):
    pass


class IntList(list):
    pass


def _payload(value):
    if isinstance(value, Int) or (isinstance(value, int) and not isinstance(value, bool)):
        return 3, struct.pack(">i", value)
    if isinstance(value, str):
        return 8, _name(value)
    if isinstance(value, dict):
        out = b""
        for key, item in value.items():
            tag, body = _payload(item)
            out += bytes([tag]) + _name(key) + body
        return 10, out + b"\x00"
    if isinstance(value, IntList):
        return 9, bytes([3]) + struct.pack(">i", len(value)) + b"".join(struct.pack(">i", v) for v in value)
    if isinstance(value, list):
        if not value:
            return 9, bytes([0]) + struct.pack(">i", 0)
        tags = [_payload(item) for item in value]
        return 9, bytes([tags[0][0]]) + struct.pack(">i", len(tags)) + b"".join(body for _, body in tags)
    raise TypeError(value)


def write_nbt(path, root):
    tag, body = _payload(root)
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "wb") as raw, gzip.GzipFile(fileobj=raw, mode="wb", mtime=0) as out:
        out.write(bytes([tag]) + _name("") + body)


# ---- building ------------------------------------------------------------------------------------------------------

class Build:
    def __init__(self, height):
        self.height = height
        self.blocks = {}

    def set(self, x, y, z, state):
        if 0 <= x < SIZE and 0 <= y < self.height and 0 <= z < SIZE:
            self.blocks[(x, y, z)] = state

    def get(self, x, y, z):
        return self.blocks.get((x, y, z))

    def box(self, x0, y0, z0, x1, y1, z1, state):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    self.set(x, y, z, state(x, y, z) if callable(state) else state)

    def save(self, path, air_above=None):
        """Writes the structure. Air is written inside the footprint so placing it clears what was there."""
        palette, index, blocks = [], {}, []
        for x in range(SIZE):
            for z in range(SIZE):
                for y in range(self.height):
                    state = self.blocks.get((x, y, z))
                    if state is None:
                        if air_above is None or y < air_above(x, z):
                            continue
                        state = "minecraft:air"
                    if state not in index:
                        index[state] = len(palette)
                        palette.append(state)
                    blocks.append({"pos": IntList([x, y, z]), "state": Int(index[state])})
        write_nbt(path, {
            "DataVersion": Int(DATA_VERSION),
            "size": IntList([SIZE, self.height, SIZE]),
            "palette": [_state_tag(state) for state in palette],
            "blocks": blocks,
            "entities": [],
        })
        print(f"wrote {path} ({len(blocks)} blocks, {len(palette)} states)")


def _state_tag(state):
    if "[" not in state:
        return {"Name": state}
    name, props = state[:-1].split("[", 1)
    return {"Name": name, "Properties": dict(pair.split("=") for pair in props.split(","))}


def ash(rng):
    return rng.choices(["minecraft:gravel", "minecraft:tuff", "minecraft:light_gray_concrete_powder",
                        "minecraft:gray_concrete_powder"], [3, 3, 2, 1])[0]


def brick(rng, worn=0.0):
    if rng.random() < worn:
        return rng.choice(["minecraft:cracked_deepslate_bricks", "minecraft:cobbled_deepslate"])
    return rng.choices(["minecraft:deepslate_bricks", "minecraft:cracked_deepslate_bricks",
                        "minecraft:polished_deepslate"], [8, 1, 1])[0]


def in_wall(x, z):
    """The curtain wall: two blocks thick around the courtyard."""
    inside = WALL_LO <= x <= WALL_HI and WALL_LO <= z <= WALL_HI
    core = WALL_LO + 2 <= x <= WALL_HI - 2 and WALL_LO + 2 <= z <= WALL_HI - 2
    return inside and not core


def in_tower(x, z):
    return any(tx <= x <= tx + 4 and tz <= z <= tz + 4 for tx, tz in TOWERS.values())


def in_gate(x, z):
    return 28 <= x <= 32 and z >= WALL_HI - 1


def in_keep(x, z):
    return KEEP_LO <= x <= KEEP_HI and KEEP_LO <= z <= KEEP_HI


def tower_ladder(name):
    """Where a tower's ladder runs: on its courtyard face."""
    tx, tz = TOWERS[name]
    if name == "nw":
        return (tx + 4, tz + 5, "south")
    if name == "ne":
        return (tx, tz + 5, "south")
    if name == "sw":
        return (tx + 4, tz - 1, "north")
    return (tx, tz - 1, "north")


def foundation(b, rng):
    b.box(0, 0, 0, SIZE - 1, FLOOR - 1, SIZE - 1, lambda x, y, z: "minecraft:deepslate" if y < 2 else "minecraft:tuff")
    for x in range(SIZE):
        for z in range(SIZE):
            courtyard = WALL_LO + 2 <= x <= WALL_HI - 2 and WALL_LO + 2 <= z <= WALL_HI - 2
            road = 28 <= x <= 32 and z > KEEP_HI
            if courtyard and road:
                b.set(x, FLOOR, z, "minecraft:polished_andesite" if rng.random() < 0.8 else "minecraft:andesite")
            elif courtyard:
                b.set(x, FLOOR, z, rng.choice(["minecraft:packed_mud", "minecraft:coarse_dirt", "minecraft:packed_mud",
                                               "minecraft:mud_bricks"]))
            else:
                b.set(x, FLOOR, z, ash(rng))


def past(rng):
    """The Ember Watch on its last night: whole walls, three unlit watch lamps, the keep's fire burning."""
    b = Build(FLOOR + 18)
    foundation(b, rng)
    top = FLOOR + 6
    for x in range(SIZE):
        for z in range(SIZE):
            if in_wall(x, z) and not in_tower(x, z):
                for y in range(FLOOR + 1, top + 1):
                    if in_gate(x, z) and y <= FLOOR + 4:
                        continue
                    b.set(x, y, z, brick(rng))
                outer = x in (WALL_LO, WALL_HI) or z in (WALL_LO, WALL_HI)
                if outer and (x + z) % 2 == 0:
                    b.set(x, top + 1, z, brick(rng))
    for name, (tx, tz) in TOWERS.items():
        b.box(tx, FLOOR + 1, tz, tx + 4, TOWER_TOP, tz + 4, lambda x, y, z: brick(rng))
        for x in range(tx, tx + 5):
            for z in range(tz, tz + 5):
                if x in (tx, tx + 4) or z in (tz, tz + 4):
                    if (x + z) % 2 == 0:
                        b.set(x, TOWER_TOP + 1, z, "minecraft:deepslate_brick_wall")
        lx, lz, facing = tower_ladder(name)
        for y in range(FLOOR + 1, TOWER_TOP + 1):
            b.set(lx, y, lz, f"minecraft:ladder[facing={facing},waterlogged=false]")
        if name in LAMP_TOWERS:
            px, py, pz = POINTS["lamp_" + name]
            b.set(px, py, pz, "minecraft:campfire[facing=north,lit=false,signal_fire=false,waterlogged=false]")
        else:
            b.set(tx + 2, TOWER_TOP + 1, tz + 2, "minecraft:lantern[hanging=false,waterlogged=false]")
    # Ladders up to the wall walk inside the gate.
    for x in (27, 33):
        for y in range(FLOOR + 1, top + 1):
            b.set(x, y, WALL_HI - 2, f"minecraft:ladder[facing=north,waterlogged=false]")
    # The keep: hollow, a door to the south, a ladder to its roof, and the watch fire burning on top.
    for x in range(KEEP_LO, KEEP_HI + 1):
        for z in range(KEEP_LO, KEEP_HI + 1):
            edge = x in (KEEP_LO, KEEP_HI) or z in (KEEP_LO, KEEP_HI)
            for y in range(FLOOR + 1, KEEP_TOP + 1):
                if edge:
                    window = y in (FLOOR + 7, FLOOR + 8) and (x == 30 or z == 30)
                    b.set(x, y, z, "minecraft:air" if window else brick(rng))
            b.set(x, KEEP_TOP + 1, z, "minecraft:polished_deepslate")
            if edge and (x + z) % 2 == 0:
                b.set(x, KEEP_TOP + 2, z, "minecraft:deepslate_brick_wall")
            b.set(x, FLOOR, z, "minecraft:polished_deepslate")
    for y in (FLOOR + 1, FLOOR + 2):
        b.set(30, y, KEEP_HI, "minecraft:air")
    for y in range(FLOOR + 1, KEEP_TOP + 2):
        b.set(30, y, KEEP_LO + 1, "minecraft:ladder[facing=south,waterlogged=false]")
    b.set(30, KEEP_TOP + 1, KEEP_LO + 1, "minecraft:ladder[facing=south,waterlogged=false]")
    b.set(30, KEEP_TOP + 2, 30, "minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]")
    for x, z in ((27, 27), (33, 27), (27, 33), (33, 33)):
        b.set(x, FLOOR + 3, z, "minecraft:lantern[hanging=false,waterlogged=false]")
    # Huts of the watch's slaves along the west wall, and a store along the east.
    for hz in (16, 24, 36):
        b.box(13, FLOOR + 1, hz, 17, FLOOR + 3, hz + 4, "minecraft:spruce_planks")
        b.box(14, FLOOR + 1, hz + 1, 16, FLOOR + 2, hz + 3, "minecraft:air")
        b.box(13, FLOOR + 4, hz, 17, FLOOR + 4, hz + 4, "minecraft:dark_oak_slab[type=bottom,waterlogged=false]")
        b.set(17, FLOOR + 1, hz + 2, "minecraft:air")
        b.set(17, FLOOR + 2, hz + 2, "minecraft:air")
    b.box(42, FLOOR + 1, 16, 47, FLOOR + 3, 22, "minecraft:spruce_planks")
    b.box(43, FLOOR + 1, 17, 46, FLOOR + 2, 21, "minecraft:air")
    b.set(42, FLOOR + 1, 19, "minecraft:air")
    b.set(42, FLOOR + 2, 19, "minecraft:air")
    b.box(42, FLOOR + 4, 16, 47, FLOOR + 4, 22, "minecraft:dark_oak_slab[type=bottom,waterlogged=false]")
    for x, z in ((44, 18), (45, 20)):
        b.set(x, FLOOR + 1, z, "minecraft:barrel[facing=up,open=false]")
    # Lamp posts in the courtyard and on the wall walk, so the watch is never quite dark.
    for x, z in ((20, 20), (40, 20), (20, 40), (40, 40), (30, 46)):
        b.set(x, FLOOR + 1, z, "minecraft:spruce_fence[east=false,north=false,south=false,waterlogged=false,west=false]")
        b.set(x, FLOOR + 2, z, "minecraft:lantern[hanging=false,waterlogged=false]")
    for x in range(WALL_LO + 6, WALL_HI - 5, 8):
        for z in (WALL_LO + 1, WALL_HI - 1):
            if not in_gate(x, z):
                b.set(x, top + 1, z, "minecraft:lantern[hanging=false,waterlogged=false]")
    # Dead trees and ash drifts out on the plain.
    for _ in range(26):
        x, z = rng.randrange(SIZE), rng.randrange(SIZE)
        if not (WALL_LO - 2 <= x <= WALL_HI + 2 and WALL_LO - 2 <= z <= WALL_HI + 2):
            b.set(x, FLOOR + 1, z, rng.choice(["minecraft:dead_bush", "minecraft:light_gray_concrete_powder"]))
    return b


def ruin(rng):
    """The same place today: broken walls, tower stumps, a gutted keep, all of it half-buried in ash."""
    b = Build(FLOOR + 12)
    foundation(b, rng)
    for x in range(SIZE):
        for z in range(SIZE):
            if in_wall(x, z) and not in_tower(x, z) and not in_gate(x, z):
                height = max(0, int(rng.gauss(1.6, 1.4)))
                for y in range(FLOOR + 1, FLOOR + 1 + height):
                    b.set(x, y, z, brick(rng, worn=0.5))
    for name, (tx, tz) in TOWERS.items():
        stump = rng.randint(3, 7)
        for x in range(tx, tx + 5):
            for z in range(tz, tz + 5):
                for y in range(FLOOR + 1, FLOOR + 1 + stump - rng.randint(0, 2)):
                    b.set(x, y, z, brick(rng, worn=0.4))
    for x in range(KEEP_LO, KEEP_HI + 1):
        for z in range(KEEP_LO, KEEP_HI + 1):
            if x in (KEEP_LO, KEEP_HI) or z in (KEEP_LO, KEEP_HI):
                height = 8 - (abs(x - 30) + abs(z - 26)) // 2 + rng.randint(-1, 1)
                for y in range(FLOOR + 1, FLOOR + 1 + max(1, height)):
                    b.set(x, y, z, brick(rng, worn=0.5))
            b.set(x, FLOOR, z, "minecraft:polished_deepslate")
    for y in (FLOOR + 1, FLOOR + 2):
        b.set(30, y, KEEP_HI, None)
        b.blocks.pop((30, y, KEEP_HI), None)
    # The keep's fire went out long ago.
    b.set(30, FLOOR + 1, 30, "minecraft:campfire[facing=north,lit=false,signal_fire=false,waterlogged=false]")
    # Rubble and ash drifts everywhere.
    for _ in range(420):
        x, z = rng.randrange(SIZE), rng.randrange(SIZE)
        y = FLOOR + 1
        while b.get(x, y, z) is not None and y < b.height - 1:
            y += 1
        b.set(x, y, z, rng.choice([ash(rng), ash(rng), "minecraft:cobbled_deepslate", "minecraft:dead_bush"]))
    return b


# ---- the Briar Cloister ----------------------------------------------------------------------------------------------

C_WALL_LO, C_WALL_HI = 8, 52  # the outer wall's square
C_WALL_TOP = FLOOR + 5
ARCADE_LO, ARCADE_HI = 18, 42  # the covered walk around the garden
GARDEN_LO, GARDEN_HI = 22, 38
CHAPEL = (24, 10, 36, 17)  # x0, z0, x1, z1
BELL_TOWER = (28, 11, 32, 15)
BELL_TOP = FLOOR + 16
TURRETS = {"west": (10, 26), "east": (46, 26)}
TURRET_TOP = FLOOR + 12

CLOISTER_POINTS = {
    "arrival": (30, FLOOR + 1, 46),
    "brazier_bell": (30, BELL_TOP + 1, 13),
    "brazier_west": (12, TURRET_TOP + 1, 28),
    "brazier_east": (48, TURRET_TOP + 1, 28),
    "briar_north": (30, FLOOR + 1, 3),
    "briar_east": (57, FLOOR + 1, 30),
    "briar_west": (3, FLOOR + 1, 30),
    "briar_south": (30, FLOOR + 1, 57),
    "garden": (30, FLOOR + 1, 30),
}

BRIAR = "shadowslave:black_briar[withering=false]"


def stone(rng, worn=0.0):
    if rng.random() < worn:
        return rng.choice(["minecraft:cracked_stone_bricks", "minecraft:mossy_cobblestone", "minecraft:cobblestone"])
    return rng.choices(["minecraft:stone_bricks", "minecraft:mossy_stone_bricks", "minecraft:cracked_stone_bricks"], [7, 2, 1])[0]


def mud(rng):
    return rng.choices(["minecraft:packed_mud", "minecraft:mud", "minecraft:rooted_dirt", "minecraft:podzol"], [4, 2, 2, 1])[0]


def in_c_wall(x, z):
    inside = C_WALL_LO <= x <= C_WALL_HI and C_WALL_LO <= z <= C_WALL_HI
    core = C_WALL_LO + 2 <= x <= C_WALL_HI - 2 and C_WALL_LO + 2 <= z <= C_WALL_HI - 2
    return inside and not core


def in_c_gate(x, z):
    return 28 <= x <= 32 and z >= C_WALL_HI - 1


def in_turret(x, z):
    return any(tx <= x <= tx + 4 and tz <= z <= tz + 4 for tx, tz in TURRETS.values())


def in_garden(x, z):
    return GARDEN_LO <= x <= GARDEN_HI and GARDEN_LO <= z <= GARDEN_HI


def in_arcade(x, z):
    return ARCADE_LO <= x <= ARCADE_HI and ARCADE_LO <= z <= ARCADE_HI and not in_garden(x, z)


def cloister_ground(b, rng):
    b.box(0, 0, 0, SIZE - 1, FLOOR - 1, SIZE - 1, lambda x, y, z: "minecraft:deepslate" if y < 2 else "minecraft:packed_mud")
    for x in range(SIZE):
        for z in range(SIZE):
            inner = C_WALL_LO + 2 <= x <= C_WALL_HI - 2 and C_WALL_LO + 2 <= z <= C_WALL_HI - 2
            if in_garden(x, z):
                b.set(x, FLOOR, z, "minecraft:moss_block")
            elif in_arcade(x, z) or (28 <= x <= 32 and z > ARCADE_HI and inner):
                b.set(x, FLOOR, z, "minecraft:polished_andesite" if rng.random() < 0.85 else "minecraft:andesite")
            elif inner:
                b.set(x, FLOOR, z, rng.choice(["minecraft:packed_mud", "minecraft:coarse_dirt", "minecraft:mud_bricks"]))
            else:
                b.set(x, FLOOR, z, mud(rng))


def cloister_past(rng):
    """The Briar Cloister in the season the briars came: walls whole, the garden in red bloom, three cold braziers."""
    b = Build(BELL_TOP + 4)
    cloister_ground(b, rng)
    # The outer wall with a walk on top, a gate to the south.
    for x in range(SIZE):
        for z in range(SIZE):
            if in_c_wall(x, z) and not in_turret(x, z):
                for y in range(FLOOR + 1, C_WALL_TOP + 1):
                    if in_c_gate(x, z) and y <= FLOOR + 4:
                        continue
                    b.set(x, y, z, stone(rng))
                outer = x in (C_WALL_LO, C_WALL_HI) or z in (C_WALL_LO, C_WALL_HI)
                if outer and (x + z) % 2 == 0:
                    b.set(x, C_WALL_TOP + 1, z, "minecraft:stone_brick_wall")
    for x in (27, 33):
        for y in range(FLOOR + 1, C_WALL_TOP + 1):
            b.set(x, y, C_WALL_HI - 2, "minecraft:ladder[facing=north,waterlogged=false]")
    # Two turrets on the east and west walls, each with a cold brazier on top.
    for name, (tx, tz) in TURRETS.items():
        b.box(tx, FLOOR + 1, tz, tx + 4, TURRET_TOP, tz + 4, lambda x, y, z: stone(rng))
        for x in range(tx, tx + 5):
            for z in range(tz, tz + 5):
                if (x in (tx, tx + 4) or z in (tz, tz + 4)) and (x + z) % 2 == 0:
                    b.set(x, TURRET_TOP + 1, z, "minecraft:stone_brick_wall")
        lx = tx + 5 if name == "west" else tx - 1
        facing = "east" if name == "west" else "west"
        for y in range(FLOOR + 1, TURRET_TOP + 1):
            b.set(lx, y, tz + 2, f"minecraft:ladder[facing={facing},waterlogged=false]")
        px, py, pz = CLOISTER_POINTS["brazier_" + name]
        b.set(px, py, pz, "minecraft:campfire[facing=north,lit=false,signal_fire=false,waterlogged=false]")
    # The chapel to the north, its bell tower above the nave, the third brazier at the top.
    x0, z0, x1, z1 = CHAPEL
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            edge = x in (x0, x1) or z in (z0, z1)
            for y in range(FLOOR + 1, FLOOR + 9):
                if edge:
                    window = y in (FLOOR + 4, FLOOR + 5) and (x - x0) % 3 == 1 and z in (z0, z1)
                    b.set(x, y, z, "minecraft:red_stained_glass_pane[east=false,north=false,south=false,waterlogged=false,west=false]"
                          if window else stone(rng))
            b.set(x, FLOOR + 9, z, "minecraft:dark_oak_planks")
            b.set(x, FLOOR, z, "minecraft:polished_deepslate")
    for y in (FLOOR + 1, FLOOR + 2, FLOOR + 3):
        b.set(30, y, z1, "minecraft:air")
    tx0, tz0, tx1, tz1 = BELL_TOWER
    b.box(tx0, FLOOR + 9, tz0, tx1, BELL_TOP, tz1, lambda x, y, z: stone(rng))
    b.box(tx0 + 1, FLOOR + 1, tz0 + 1, tx1 - 1, BELL_TOP - 1, tz1 - 1, "minecraft:air")
    for y in range(FLOOR + 1, BELL_TOP + 1):
        b.set(tx0 + 1, y, tz0 + 2, "minecraft:ladder[facing=east,waterlogged=false]")
    b.set(tx0 + 2, BELL_TOP - 2, tz0 + 2, "minecraft:bell[attachment=ceiling,facing=north,powered=false]")
    for x in range(tx0, tx1 + 1):
        for z in range(tz0, tz1 + 1):
            if (x in (tx0, tx1) or z in (tz0, tz1)) and (x + z) % 2 == 0:
                b.set(x, BELL_TOP + 1, z, "minecraft:stone_brick_wall")
    bx, by, bz = CLOISTER_POINTS["brazier_bell"]
    b.set(bx, by, bz, "minecraft:campfire[facing=north,lit=false,signal_fire=false,waterlogged=false]")
    for x in range(x0 + 2, x1 - 1, 2):
        b.set(x, FLOOR + 1, z0 + 3, "minecraft:dark_oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")
        b.set(x, FLOOR + 1, z0 + 5, "minecraft:dark_oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")
    b.set(30, FLOOR + 1, z0 + 1, "minecraft:lectern[facing=south,has_book=false,powered=false]")
    for x in (x0 + 1, x1 - 1):
        b.set(x, FLOOR + 3, z0 + 1, "minecraft:lantern[hanging=false,waterlogged=false]")
    # The covered walk around the garden: columns and a slab roof.
    for x in range(ARCADE_LO, ARCADE_HI + 1):
        for z in range(ARCADE_LO, ARCADE_HI + 1):
            if not in_arcade(x, z):
                continue
            b.set(x, FLOOR + 5, z, "minecraft:dark_oak_slab[type=bottom,waterlogged=false]")
            ring = x in (GARDEN_LO - 1, GARDEN_HI + 1) or z in (GARDEN_LO - 1, GARDEN_HI + 1)
            if ring and (x + z) % 3 == 0:
                for y in range(FLOOR + 1, FLOOR + 5):
                    b.set(x, y, z, "minecraft:stone_brick_wall" if y < FLOOR + 4 else "minecraft:chiseled_stone_bricks")
    # The garden in bloom, with a pale tree at its heart.
    for x in range(GARDEN_LO, GARDEN_HI + 1):
        for z in range(GARDEN_LO, GARDEN_HI + 1):
            if abs(x - 30) <= 1 and abs(z - 30) <= 1:
                continue
            roll = rng.random()
            if roll < 0.28:
                b.set(x, FLOOR + 1, z, "minecraft:poppy")
            elif roll < 0.36:
                b.set(x, FLOOR + 1, z, "minecraft:rose_bush[half=lower]")
                b.set(x, FLOOR + 2, z, "minecraft:rose_bush[half=upper]")
            elif roll < 0.42:
                b.set(x, FLOOR + 1, z, "minecraft:flowering_azalea")
    for y in range(FLOOR + 1, FLOOR + 6):
        b.set(30, y, 30, "minecraft:stripped_birch_log[axis=y]")
    for dx in range(-2, 3):
        for dz in range(-2, 3):
            for y in (FLOOR + 6, FLOOR + 7):
                if abs(dx) + abs(dz) <= (3 if y == FLOOR + 6 else 2):
                    b.set(30 + dx, y, 30 + dz, "minecraft:flowering_azalea_leaves[distance=1,persistent=true,waterlogged=false]")
    # The sisters' dormitory along the west wall, the infirmary along the east.
    for hz in (34, 42):
        b.box(12, FLOOR + 1, hz, 16, FLOOR + 3, hz + 5, "minecraft:spruce_planks")
        b.box(13, FLOOR + 1, hz + 1, 15, FLOOR + 2, hz + 4, "minecraft:air")
        b.box(12, FLOOR + 4, hz, 16, FLOOR + 4, hz + 5, "minecraft:dark_oak_slab[type=bottom,waterlogged=false]")
        b.set(16, FLOOR + 1, hz + 2, "minecraft:air")
        b.set(16, FLOOR + 2, hz + 2, "minecraft:air")
    b.box(43, FLOOR + 1, 34, 48, FLOOR + 3, 46, "minecraft:spruce_planks")
    b.box(44, FLOOR + 1, 35, 47, FLOOR + 2, 45, "minecraft:air")
    b.box(43, FLOOR + 4, 34, 48, FLOOR + 4, 46, "minecraft:dark_oak_slab[type=bottom,waterlogged=false]")
    b.set(43, FLOOR + 1, 40, "minecraft:air")
    b.set(43, FLOOR + 2, 40, "minecraft:air")
    for z in (36, 39, 42, 45):
        b.set(46, FLOOR + 1, z, "minecraft:white_bed[facing=east,occupied=false,part=foot]")
        b.set(47, FLOOR + 1, z, "minecraft:white_bed[facing=east,occupied=false,part=head]")
    b.set(44, FLOOR + 1, 35, "minecraft:brewing_stand[has_bottle_0=false,has_bottle_1=false,has_bottle_2=false]")
    for x, z in ((20, 46), (40, 46), (20, 14), (40, 14)):
        b.set(x, FLOOR + 1, z, "minecraft:spruce_fence[east=false,north=false,south=false,waterlogged=false,west=false]")
        b.set(x, FLOOR + 2, z, "minecraft:lantern[hanging=false,waterlogged=false]")
    # Outside, the briars are coming: thickets creeping towards the walls.
    for _ in range(9):
        cx, cz = rng.randrange(SIZE), rng.randrange(SIZE)
        if C_WALL_LO - 2 <= cx <= C_WALL_HI + 2 and C_WALL_LO - 2 <= cz <= C_WALL_HI + 2:
            continue
        for _ in range(14):
            x, z = cx + rng.randint(-2, 2), cz + rng.randint(-2, 2)
            if 0 <= x < SIZE and 0 <= z < SIZE and not (C_WALL_LO - 1 <= x <= C_WALL_HI + 1 and C_WALL_LO - 1 <= z <= C_WALL_HI + 1):
                b.set(x, FLOOR + 1, z, BRIAR)
                if rng.random() < 0.35:
                    b.set(x, FLOOR + 2, z, BRIAR)
    return b


def cloister_ruin(rng):
    """The cloister today: wall stumps, a broken bell tower, the garden one black thicket."""
    b = Build(FLOOR + 12)
    cloister_ground(b, rng)
    for x in range(SIZE):
        for z in range(SIZE):
            if in_c_wall(x, z) and not in_turret(x, z) and not in_c_gate(x, z):
                height = max(0, int(rng.gauss(1.8, 1.3)))
                for y in range(FLOOR + 1, FLOOR + 1 + height):
                    b.set(x, y, z, stone(rng, worn=0.5))
    for tx, tz in TURRETS.values():
        stump = rng.randint(3, 6)
        for x in range(tx, tx + 5):
            for z in range(tz, tz + 5):
                for y in range(FLOOR + 1, FLOOR + 1 + stump - rng.randint(0, 2)):
                    b.set(x, y, z, stone(rng, worn=0.5))
    x0, z0, x1, z1 = CHAPEL
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if x in (x0, x1) or z in (z0, z1):
                height = 7 - (abs(x - 30) + abs(z - 13)) // 3 + rng.randint(-1, 1)
                for y in range(FLOOR + 1, FLOOR + 1 + max(1, height)):
                    b.set(x, y, z, stone(rng, worn=0.5))
            b.set(x, FLOOR, z, "minecraft:polished_deepslate")
    for y in (FLOOR + 1, FLOOR + 2, FLOOR + 3):
        b.blocks.pop((30, y, z1), None)
    tx0, tz0, tx1, tz1 = BELL_TOWER
    for x in range(tx0, tx1 + 1):
        for z in range(tz0, tz1 + 1):
            if x in (tx0, tx1) or z in (tz0, tz1):
                for y in range(FLOOR + 1, FLOOR + 1 + rng.randint(7, 10)):
                    b.set(x, y, z, stone(rng, worn=0.4))
    b.set(30, FLOOR + 1, 13, "minecraft:bell[attachment=floor,facing=east,powered=false]")
    # Arcade columns, most of them fallen.
    for x in range(ARCADE_LO, ARCADE_HI + 1):
        for z in range(ARCADE_LO, ARCADE_HI + 1):
            ring = x in (GARDEN_LO - 1, GARDEN_HI + 1) or z in (GARDEN_LO - 1, GARDEN_HI + 1)
            if in_arcade(x, z) and ring and (x + z) % 3 == 0:
                for y in range(FLOOR + 1, FLOOR + 1 + rng.choice((0, 1, 1, 2, 4))):
                    b.set(x, y, z, "minecraft:stone_brick_wall" if y < FLOOR + 4 else "minecraft:chiseled_stone_bricks")
    # The garden is a thicket now, around a clear heart where the old tree stood: the Seed grows there.
    for x in range(GARDEN_LO - 1, GARDEN_HI + 2):
        for z in range(GARDEN_LO - 1, GARDEN_HI + 2):
            if abs(x - 30) <= 2 and abs(z - 30) <= 2:
                continue
            if rng.random() < 0.7 and b.get(x, FLOOR + 1, z) is None:
                b.set(x, FLOOR + 1, z, BRIAR)
                if rng.random() < 0.5:
                    b.set(x, FLOOR + 2, z, BRIAR)
                    if rng.random() < 0.3:
                        b.set(x, FLOOR + 3, z, BRIAR)
    b.set(30, FLOOR, 30, "minecraft:rooted_dirt")
    b.set(29, FLOOR + 1, 31, "minecraft:stripped_birch_log[axis=x]")
    b.set(31, FLOOR + 1, 31, "minecraft:stripped_birch_log[axis=x]")
    # Briars everywhere else, rubble, and a few red flowers that never stopped growing.
    for _ in range(520):
        x, z = rng.randrange(SIZE), rng.randrange(SIZE)
        if abs(x - 30) <= 2 and abs(z - 30) <= 2:
            continue
        y = FLOOR + 1
        while b.get(x, y, z) is not None and y < b.height - 1:
            y += 1
        roll = rng.random()
        b.set(x, y, z, BRIAR if roll < 0.55 else ("minecraft:poppy" if roll < 0.6 and y == FLOOR + 1
                                                   else rng.choice(["minecraft:mossy_cobblestone", "minecraft:dead_bush", BRIAR])))
    return b


def main():
    structures = DATA / "structure" / "nightmare"
    past(random.Random(4242)).save(structures / "last_vigil.nbt", air_above=lambda x, z: FLOOR + 1)
    footprint = lambda x, z: FLOOR + 1 if WALL_LO - 3 <= x <= WALL_HI + 3 and WALL_LO - 3 <= z <= WALL_HI + 3 else 10 ** 6
    ruin(random.Random(1717)).save(structures / "ember_watch_ruin.nbt", air_above=footprint)
    for name, point in POINTS.items():
        print(f"  {name}: {list(point)}")
    cloister_past(random.Random(5151)).save(structures / "long_thorn.nbt", air_above=lambda x, z: FLOOR + 1)
    cloister_footprint = lambda x, z: (FLOOR + 1 if C_WALL_LO - 3 <= x <= C_WALL_HI + 3 and C_WALL_LO - 3 <= z <= C_WALL_HI + 3
                                       else 10 ** 6)
    cloister_ruin(random.Random(2626)).save(structures / "briar_cloister_ruin.nbt", air_above=cloister_footprint)
    for name, point in CLOISTER_POINTS.items():
        print(f"  {name}: {list(point)}")


if __name__ == "__main__":
    main()
