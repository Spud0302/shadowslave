# Single teardown path shared by survive and eject. Runs as the player.

tag @s remove ss_in_nightmare
tag @s remove ss_creature_spawned
scoreboard players set @s ss_timer 0
scoreboard players set @s ss_gone 0

# The Spell is spent for a while, however you left.
#
# This lived in eject.mcfunction, which meant DEATH set no cooldown at all: dying reaches the
# teardown by a different route — the dimension-mismatch cleanup calls this function directly,
# never touching eject. Every exit passes through here, so this is where it belongs. Harmless
# on the survive path, since a Sleeper cannot re-enter a First Nightmare anyway.
scoreboard players set @s ss_cooldown 600

# Kills every creature in the nightmare, not just this player's. Phase 1 is single-player
# at a time — same limitation as the shared bossbar. Per-player creature ownership would
# need owner tags; deferred with it.
execute in shadowslave:nightmare run kill @e[tag=ss_creature]

bossbar set shadowslave:trial visible false
bossbar set shadowslave:trial players

# Put them back where they slept.
# Players cannot have their NBT written, so the return position goes through storage
# and a macro function — the only way to feed dynamic coordinates to /tp.
# 1.6 — drops are swept a few ticks later, not here.
#
# Tagging them at this moment matched nothing: on the tick the player's health hits 0 the
# teardown runs, but Minecraft has not spawned the death drops yet. Proven with a probe —
# no tagged item entity existed at +0, +300, +800 or +2000ms. The sweep is scheduled from
# return.mcfunction instead.

execute store result storage shadowslave:ret x int 1 run scoreboard players get @s ss_ret_x
execute store result storage shadowslave:ret y int 1 run scoreboard players get @s ss_ret_y
execute store result storage shadowslave:ret z int 1 run scoreboard players get @s ss_ret_z

# Drops land one block higher than the player does. The stored position is the bed itself, and
# items teleported into it can end up buried; a playtester respawned wedged between a block
# and the bed, which is the same geometry. A block of clearance lets them fall to the floor.
scoreboard players operation $drop_y ss_scratch_a = @s ss_ret_y
scoreboard players add $drop_y ss_scratch_a 1
execute store result storage shadowslave:ret dy int 1 run scoreboard players get $drop_y ss_scratch_a
scoreboard players reset $drop_y ss_scratch_a
execute in minecraft:overworld run function shadowslave:nightmare/return with storage shadowslave:ret
