# Phase 1 Nightmare objective seam. Runs as and at the player once per tick while they are inside.
#
# Canon Nightmares are historical scenarios built around a central conflict; "wait for one creature,
# then kill it" is this prototype scenario, not the universal definition of a Nightmare. Keep the
# scenario-specific win machinery behind this function so the Java port grows a Nightmare objective /
# scenario abstraction instead of baking boss-kill assumptions into entry, teardown or player state.
#
# This file intentionally preserves 0.5.0 behaviour. It is an architectural seam plus presentation,
# not a balance redesign.

# Atmosphere during the 90-second approach. Exact score matches make each beat fire once; test runs
# that skip the countdown by setting ss_timer=1 simply skip these beats as intended.
execute unless entity @s[tag=ss_creature_spawned] if score @s ss_timer matches 1200 run title @s actionbar {"text":"The dark is listening.","color":"dark_gray","italic":true}
execute unless entity @s[tag=ss_creature_spawned] if score @s ss_timer matches 600 run playsound minecraft:ambient.cave ambient @s ~ ~ ~ 0.8 0.45
execute unless entity @s[tag=ss_creature_spawned] if score @s ss_timer matches 600 run title @s actionbar {"text":"Something is drawing near.","color":"dark_purple","italic":true}
execute unless entity @s[tag=ss_creature_spawned] if score @s ss_timer matches 200 run playsound minecraft:entity.warden.heartbeat ambient @s ~ ~ ~ 0.7 0.7
execute unless entity @s[tag=ss_creature_spawned] if score @s ss_timer matches 200 run title @s actionbar {"text":"It has found you.","color":"dark_red","italic":true}

# Timer expired and no creature yet: summon this scenario's conflict entity.
execute if score @s ss_timer matches ..0 unless entity @s[tag=ss_creature_spawned] run function shadowslave:nightmare/spawn_creature

# Once the creature exists, the shared bossbar tracks this objective's current threat instead of time.
execute if entity @s[tag=ss_creature_spawned] store result bossbar shadowslave:trial value run data get entity @e[tag=ss_creature,limit=1] Health

# Observe strong First-Nightmare behaviour while the creature is genuinely present. This MUST run
# before the 48-block leash below; otherwise the flee signal is teleported away before it can be seen.
execute if entity @e[tag=ss_creature] run function shadowslave:prototype/observe_trial

# You cannot outrun this prototype conflict. The Java scenario system should replace this global
# creature selector with explicit instance/entity ownership rather than extending the tag trick.
execute if entity @s[tag=ss_creature_spawned] run tp @e[tag=ss_creature,distance=48..] ~ ~ ~

# Creature was summoned and is now gone: this scenario's central conflict is resolved. Require the
# absence to hold for two seconds so a rejoin/chunk-deserialisation gap cannot be misread as victory.
execute if entity @e[tag=ss_creature] run scoreboard players set @s ss_gone 0
execute if entity @s[tag=ss_creature_spawned] unless entity @e[tag=ss_creature] run scoreboard players add @s ss_gone 1
execute if score @s ss_gone matches 40.. run function shadowslave:nightmare/survive
