# Wipes verification state, Aspect/Flaw tags and modifiers for a clean test run.

advancement revoke @s from shadowslave:test/root
advancement revoke @s only shadowslave:enter_nightmare

scoreboard players set @s ss_rank 0
scoreboard players set @s ss_timer 0
scoreboard players set @s ss_aspect 0
scoreboard players set @s ss_flaw 0

tag @s remove ss_in_nightmare
tag @s remove ss_creature_spawned
tag @s remove ss_aspect_shadow
tag @s remove ss_aspect_flame
tag @s remove ss_aspect_bone
tag @s remove ss_aspect_wind
tag @s remove ss_flaw_shadow_slave
tag @s remove ss_flaw_fragile
tag @s remove ss_flaw_ravenous
tag @s remove ss_flaw_weightless

attribute @s minecraft:generic.armor modifier remove shadowslave:aspect_bone_armor
attribute @s minecraft:generic.movement_speed modifier remove shadowslave:aspect_wind_speed
attribute @s minecraft:generic.max_health modifier remove shadowslave:flaw_fragile_health
attribute @s minecraft:generic.safe_fall_distance modifier remove shadowslave:flaw_weightless_fall

kill @e[tag=ss_creature]
bossbar set shadowslave:trial visible false

tellraw @s {"text":"[Shadow Slave] Verification state reset. You are a Sleeper again.","color":"gray","italic":true}
