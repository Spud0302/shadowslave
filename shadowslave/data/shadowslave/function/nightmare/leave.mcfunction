# Single teardown path shared by survive and eject. Runs as the player.

tag @s remove ss_in_nightmare
tag @s remove ss_creature_spawned
scoreboard players set @s ss_timer 0
scoreboard players set @s ss_gone 0

# Kills every creature in the nightmare, not just this player's. Phase 1 is single-player
# at a time — same limitation as the shared bossbar. Per-player creature ownership would
# need owner tags; deferred with it.
execute in shadowslave:nightmare run kill @e[tag=ss_creature]

bossbar set shadowslave:trial visible false
bossbar set shadowslave:trial players

# Put them back where they slept.
# Players cannot have their NBT written, so the return position goes through storage
# and a macro function — the only way to feed dynamic coordinates to /tp.
execute store result storage shadowslave:ret x int 1 run scoreboard players get @s ss_ret_x
execute store result storage shadowslave:ret y int 1 run scoreboard players get @s ss_ret_y
execute store result storage shadowslave:ret z int 1 run scoreboard players get @s ss_ret_z
execute in minecraft:overworld run function shadowslave:nightmare/return with storage shadowslave:ret
