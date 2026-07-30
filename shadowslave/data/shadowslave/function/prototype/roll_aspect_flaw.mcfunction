# Phase 1 placeholder Aspect/Flaw generator.
#
# PROTOTYPE-LIMIT: canon does not give everyone one of four reusable Aspects, and Flaws are
# not independent random penalties. This file deliberately contains ONLY the temporary
# generator so the eventual procedural system can replace it without also touching rank
# progression or Nightmare completion.

# Clear any previous roll before applying the new one.
# Removing a tag stops upkeep from REAPPLYING its modifier, but does not remove a modifier
# already on the player, so tags and persistent modifiers both have to be stripped.
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

# Placeholder behavior only: Aspect and Flaw currently roll independently.
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
