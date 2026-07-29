# Macro. Moves everything the player dropped in the nightmare back to where they returned.
#
# The chained `in` clauses are the whole trick, and the reason the first two attempts failed:
#
#   in shadowslave:nightmare   -> @e resolves in the nightmare, so it finds the drops
#   as @e[type=item]           -> now acting as each dropped item
#   in minecraft:overworld     -> switch the execution dimension
#   tp @s <coords>             -> absolute coords, so the item lands in the Overworld
#
# Selectors are always scoped to the execution dimension. That killed the marker approach:
# `execute in shadowslave:nightmare run tp @e[type=item] @e[tag=marker]` looked for the marker
# in the NIGHTMARE, where it never was, so there was no destination and nothing moved.
#
# Sweeps every item in the nightmare rather than only those near the death position. Safe
# because the pack is single-player-at-a-time by design, and more robust than a radius —
# a mob can kick an item several blocks before this fires.
$execute in shadowslave:nightmare as @e[type=item] in minecraft:overworld run tp @s $(x) $(dy) $(z)
