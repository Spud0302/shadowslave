# Runs as and at the player, from the slept_in_bed advancement reward.

# Revoke immediately so the trigger can fire again on the next sleep.
advancement revoke @s only shadowslave:enter_nightmare

# Only Sleepers are Chosen. Awakened players sleep normally.
execute if score @s ss_rank matches 1.. run advancement grant @s only shadowslave:test/bypass
execute if score @s ss_rank matches 1.. run return 0
# Guard against re-entry if something fires twice.
execute if entity @s[tag=ss_in_nightmare] run return 0

# Remember where to put them back.
execute store result score @s ss_ret_x run data get entity @s Pos[0]
execute store result score @s ss_ret_y run data get entity @s Pos[1]
execute store result score @s ss_ret_z run data get entity @s Pos[2]

tag @s add ss_in_nightmare
scoreboard players set @s ss_timer 6000

# Pull them in. Teleporting wakes the player out of the bed.
execute in shadowslave:nightmare run tp @s 0 120 0
execute in shadowslave:nightmare run spreadplayers 0 0 200 400 false @s
execute in shadowslave:nightmare at @s run tp @s ~ 150 ~

bossbar set shadowslave:trial max 6000
bossbar set shadowslave:trial value 6000
bossbar set shadowslave:trial name {"text":"The Nightmare Spell","color":"light_purple"}
bossbar set shadowslave:trial color purple
bossbar set shadowslave:trial visible true
bossbar set shadowslave:trial players @s

execute at @s run playsound minecraft:ambient.cave ambient @s ~ ~ ~ 1 0.5
title @s times 20 60 20
title @s subtitle {"text":"Survive.","color":"gray"}
title @s title {"text":"The Nightmare Spell","color":"dark_purple","bold":true}

advancement grant @s only shadowslave:test/chosen
