# Runs as and at each player inside a nightmare, every tick.

# Sample health so we can eject before a real death drops their gear.
# ponytail: NBT reads on players are expensive, but this only runs for players
# ponytail: actually inside a nightmare — a handful at most
execute store result score @s ss_health run data get entity @s Health
execute if score @s ss_health matches ..4 run function shadowslave:nightmare/eject
execute if entity @s[tag=!ss_in_nightmare] run return 0

# Count down.
scoreboard players remove @s ss_timer 1
execute store result bossbar shadowslave:trial value run scoreboard players get @s ss_timer

# Timer expired and no creature yet: summon it.
execute if score @s ss_timer matches ..0 unless entity @s[tag=ss_creature_spawned] run function shadowslave:nightmare/spawn_creature

# Once the creature exists, the bar tracks its health instead of the timer.
execute if entity @s[tag=ss_creature_spawned] store result bossbar shadowslave:trial value run data get entity @e[tag=ss_creature,limit=1] Health

# Creature was summoned and is now gone: the trial is won.
execute if entity @s[tag=ss_creature_spawned] unless entity @e[tag=ss_creature,limit=1] run function shadowslave:nightmare/survive
