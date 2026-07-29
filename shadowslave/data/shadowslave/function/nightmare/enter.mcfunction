# Runs as and at the player, from the slept_in_bed advancement reward.

# Revoke immediately so the trigger can fire again on the next sleep.
advancement revoke @s only shadowslave:enter_nightmare

# Only Sleepers are Chosen. Awakened players sleep normally.
execute if score @s ss_rank matches 1.. run advancement grant @s only shadowslave:test/bypass
execute if score @s ss_rank matches 1.. run return 0
# Guard against re-entry if something fires twice.
execute if entity @s[tag=ss_in_nightmare] run return 0

# The Spell is still spent.
#
# This guard used to live only in the callers — sleep.mcfunction and the sneak check — which
# meant any other path in bypassed it entirely. Both player routes were covered, so it was
# invisible in play, but it is the same fragility as guarding in callers instead of at the
# choke point. Every entry passes through here.
execute if score @s ss_cooldown matches 1.. run tellraw @s {"text":"The Spell is spent. It will come for you again.","color":"dark_gray","italic":true}
execute if score @s ss_cooldown matches 1.. run return 0

# Too weak to be taken.
#
# This replaces the free instant_health that used to fire on arrival. That heal existed to
# stop an entry loop — walk in at 2 hearts, get ejected on the first tick, repeat — but it
# also meant ejection cost nothing at all: thrown out at 2 hearts, straight back in at full.
# Refusing entry instead makes failure expensive. You have to actually recover.
#
# 14 (7 hearts) leaves a couple of hits of margin above the ejection threshold of 8, so you
# cannot enter and be ejected again immediately.
# ponytail: own scratch objective, NOT ss_health — the ejection check reads that one
scoreboard players reset @s ss_scratch_a
execute store result score @s ss_scratch_a run data get entity @s Health
execute if score @s ss_scratch_a matches ..13 run tellraw @s {"text":"You are too weak. The Spell has no use for you yet — recover, and it will come.","color":"dark_gray","italic":true}
execute if score @s ss_scratch_a matches ..13 run return 0

# Remember where to put them back.
execute store result score @s ss_ret_x run data get entity @s Pos[0]
execute store result score @s ss_ret_y run data get entity @s Pos[1]
execute store result score @s ss_ret_z run data get entity @s Pos[2]

tag @s add ss_in_nightmare
scoreboard players set @s ss_timer 6000
scoreboard players set @s ss_gone 0

# Pull them in. Teleporting wakes the player out of the bed.
execute in shadowslave:nightmare run tp @s 0 120 0
execute in shadowslave:nightmare run spreadplayers 0 0 200 400 false @s


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

advancement grant @s only shadowslave:test/root
advancement grant @s only shadowslave:test/chosen
