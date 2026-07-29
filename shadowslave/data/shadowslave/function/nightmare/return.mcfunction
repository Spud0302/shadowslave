# Macro. Called from leave.mcfunction with the stored return coordinates, executing in the
# Overworld. Players cannot have their NBT written, so dynamic coordinates have to come
# through command storage and a macro like this one.

# Drops come home FIRST, and unconditionally — including when their owner died, which is
# precisely the case that matters. They fall inside the nightmare, and the player respawns
# in the Overworld with no route back: sleeping starts a fresh trial somewhere else and the
# items despawn in five minutes.
#
# Selectors are dimension-scoped, so coordinates cannot pull entities across a dimension
# boundary. Giving /tp a destination ENTITY can: a marker placed at the return position.
$summon minecraft:marker $(x) $(dy) $(z) {Tags:["ss_return_marker"]}
execute in shadowslave:nightmare run tp @e[tag=ss_drop] @e[tag=ss_return_marker,limit=1]
tag @e[tag=ss_drop] remove ss_drop
kill @e[tag=ss_return_marker]

# A corpse needs no teleport. A dead-but-not-yet-respawned player still ticks, so this used
# to run behind the death screen — they watched the portal warp and their own bed appear,
# then pressed respawn and vanilla sent them to the same place anyway. The state teardown in
# leave.mcfunction still has to happen on death; only this teleport is redundant.
# Reading player NBT is allowed — it is only writing that Minecraft refuses.
execute if entity @s[nbt={Health:0.0f}] run return 0

$tp @s $(x) $(y) $(z)

# We land inside the bed we slept in; step out of it.
execute at @s run function shadowslave:nightmare/unstick
