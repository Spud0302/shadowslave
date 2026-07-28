# One-time transition: Sleeper -> Awakened, with an Aspect and a Flaw.

scoreboard players set @s ss_rank 1

# Clear any previous roll before applying the new one.
#
# Removing an Aspect tag stops the upkeep REAPPLYING its modifier, but never removes the
# one already on the player — so a re-roll used to leave you carrying the armour of an
# Aspect you no longer had and the health penalty of a Flaw you no longer had. Tags and
# modifiers both have to go.
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

# Aspect and Flaw roll independently, so Shadow + Shadow Slave can occur.
execute store result score @s ss_aspect run random value 1..4
execute store result score @s ss_flaw run random value 1..4

execute if score @s ss_aspect matches 1 run tag @s add ss_aspect_shadow
execute if score @s ss_aspect matches 2 run tag @s add ss_aspect_flame
execute if score @s ss_aspect matches 3 run tag @s add ss_aspect_bone
execute if score @s ss_aspect matches 4 run tag @s add ss_aspect_wind

execute if score @s ss_flaw matches 1 run tag @s add ss_flaw_shadow_slave
execute if score @s ss_flaw matches 2 run tag @s add ss_flaw_fragile
execute if score @s ss_flaw matches 3 run tag @s add ss_flaw_ravenous
execute if score @s ss_flaw matches 4 run tag @s add ss_flaw_weightless

tellraw @s [{"text":"\n","color":"white"},{"text":"You have Awakened.","color":"light_purple","bold":true},{"text":"\nRun ","color":"gray"},{"text":"/trigger soul","color":"aqua"},{"text":" to read your soul.\n","color":"gray"}]

advancement grant @s only shadowslave:test/awakened
