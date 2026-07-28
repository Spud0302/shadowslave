# One-time transition: Sleeper -> Awakened, with an Aspect and a Flaw.

scoreboard players set @s ss_rank 1

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
