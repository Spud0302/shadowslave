# Single teardown path shared by survive and eject. Runs as the player.

tag @s remove ss_in_nightmare
tag @s remove ss_creature_spawned
scoreboard players set @s ss_timer 0

# Clear the trial of anything left behind.
kill @e[tag=ss_creature]

bossbar set shadowslave:trial visible false
bossbar set shadowslave:trial players

# Put them back where they slept.
execute in minecraft:overworld run tp @s 0 0 0
execute store result entity @s Pos[0] double 1 run scoreboard players get @s ss_ret_x
execute store result entity @s Pos[1] double 1 run scoreboard players get @s ss_ret_y
execute store result entity @s Pos[2] double 1 run scoreboard players get @s ss_ret_z
