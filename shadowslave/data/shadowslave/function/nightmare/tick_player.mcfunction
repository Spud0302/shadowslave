# Runs as and at each player inside a nightmare, every tick.

# Sample health so we can eject before a real death drops their gear.
# ponytail: NBT reads on players are expensive, but this only runs for players
# ponytail: actually inside a nightmare — a handful at most
# Reset BEFORE the read, then compare. If the read fails the score stays absent, and
# `matches` fails outright on an absent score — which is the same behaviour that caused
# the /trigger soul lockout, used deliberately this time.
#
# The previous attempt at this guarded with `matches 1..8` to exclude a zero reading.
# That also excluded a real 0, which is exactly what /kill and any lethal hit produce —
# so death stopped triggering the teardown, and with it the item recovery. The range has
# to include 0; the staleness has to be handled by the reset.
scoreboard players reset @s ss_health
execute store result score @s ss_health run data get entity @s Health
execute if score @s ss_health matches ..4 run function shadowslave:nightmare/eject
execute if entity @s[tag=!ss_in_nightmare] run return 0

# Count down. Stops once the creature is up — otherwise the score runs unboundedly
# negative for the rest of the fight, and the bar it writes is overwritten anyway.
execute unless entity @s[tag=ss_creature_spawned] run scoreboard players remove @s ss_timer 1
execute unless entity @s[tag=ss_creature_spawned] store result bossbar shadowslave:trial value run scoreboard players get @s ss_timer
# Self-healing after a /reload or restart, which hides the bar via init.
bossbar set shadowslave:trial visible true

# Timer expired and no creature yet: summon it.
execute if score @s ss_timer matches ..0 unless entity @s[tag=ss_creature_spawned] run function shadowslave:nightmare/spawn_creature

# Once the creature exists, the bar tracks its health instead of the timer.
execute if entity @s[tag=ss_creature_spawned] store result bossbar shadowslave:trial value run data get entity @e[tag=ss_creature,limit=1] Health

# You cannot outrun the Nightmare. Leashing the creature also makes the absence test below
# a genuine "it died" signal rather than "I walked away".
execute if entity @s[tag=ss_creature_spawned] run tp @e[tag=ss_creature,distance=48..] ~ ~ ~

# Creature was summoned and is now gone: the trial is won. Require the absence to
# hold for two seconds so a rejoin (chunk not yet deserialized) can't be misread
# as a kill.
execute if entity @e[tag=ss_creature] run scoreboard players set @s ss_gone 0
execute if entity @s[tag=ss_creature_spawned] unless entity @e[tag=ss_creature] run scoreboard players add @s ss_gone 1
execute if score @s ss_gone matches 40.. run function shadowslave:nightmare/survive
