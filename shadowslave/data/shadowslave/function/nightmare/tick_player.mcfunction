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

# Count down. Stops once the scenario's conflict is active — otherwise the score runs unboundedly
# negative for the rest of the fight, and the bar it writes is overwritten anyway.
execute unless entity @s[tag=ss_creature_spawned] run scoreboard players remove @s ss_timer 1
execute unless entity @s[tag=ss_creature_spawned] store result bossbar shadowslave:trial value run scoreboard players get @s ss_timer
# Self-healing after a /reload or restart, which hides the bar via init.
bossbar set shadowslave:trial visible true

# Scenario-specific conflict, presentation and completion live behind one seam. Do not put future
# Nightmare win conditions back into this player lifecycle function; the Java port needs objectives /
# scenarios to vary independently from entry, failure and teardown.
function shadowslave:nightmare/objective_tick
