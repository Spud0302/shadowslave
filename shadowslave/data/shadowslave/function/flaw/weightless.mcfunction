# Weightless — the ground is unkind.
attribute @s minecraft:generic.safe_fall_distance modifier remove shadowslave:flaw_weightless_fall
# -1, not -2. Vanilla safe fall distance is 3; -2 leaves 1, and a standing jump is 1.25
# blocks — so the player took damage simply for jumping on the spot. -1 leaves 2, which
# clears a jump but still bites on any real drop.
attribute @s minecraft:generic.safe_fall_distance modifier add shadowslave:flaw_weightless_fall -1 add_value
