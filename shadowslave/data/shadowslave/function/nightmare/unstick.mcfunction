# Step to a free neighbouring block if we materialised somewhere unusable.
#
# The return position is captured while the player is IN the bed, so they come back inside
# the bed block. Beds are not full-collision, so this does not suffocate — but with a low
# ceiling the player is clipped and cannot move or jump out.
#
# Vanilla's own bed-exit search (BedBlock.findStandUpPosition) is not exposed to commands,
# so this is the manual version: find somewhere with two blocks of headroom and step into it.
# `#minecraft:replaceable` covers air, grass, ferns and snow — anything you can stand in.

# Already fine? Do nothing.
execute if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run return 0

# Otherwise take the first neighbour with headroom. `return run` stops at the first hit,
# so we never chain two moves.
execute positioned ~1 ~ ~ if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run return run tp @s ~1 ~ ~
execute positioned ~-1 ~ ~ if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run return run tp @s ~-1 ~ ~
execute positioned ~ ~ ~1 if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run return run tp @s ~ ~ ~1
execute positioned ~ ~ ~-1 if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run return run tp @s ~ ~ ~-1
execute positioned ~1 ~ ~1 if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run return run tp @s ~1 ~ ~1
execute positioned ~-1 ~ ~1 if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run return run tp @s ~-1 ~ ~1
execute positioned ~1 ~ ~-1 if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run return run tp @s ~1 ~ ~-1
execute positioned ~-1 ~ ~-1 if block ~ ~ ~ #minecraft:replaceable if block ~ ~1 ~ #minecraft:replaceable run return run tp @s ~-1 ~ ~-1

# Nothing within a block of the bed is usable — the player walled themselves in. Go up,
# which is always better than being stuck.
tp @s ~ ~1 ~
