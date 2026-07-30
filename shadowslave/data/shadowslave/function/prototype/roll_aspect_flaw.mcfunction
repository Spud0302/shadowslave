# Phase 1 generated Aspect/Flaw identity.
#
# Canon gives constraints, influences and examples — not a formula. This is a GAME generation
# system, not a claim about the Nightmare Spell's canonical algorithm. Aspect identity is composed
# from independent nature + archetype vocabularies while the executable Dormant effects remain four
# finite roots, because a datapack cannot invent a new function at runtime.

# Clear the previous mechanical roots before applying a new identity. Removing a tag stops upkeep
# from REAPPLYING a modifier, but it does not remove a modifier already on the player; both layers
# have to be stripped or re-rolling can leave an old Aspect/Flaw behind.
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

# --- Aspect -----------------------------------------------------------------
# First roll chooses the nature / finite Dormant mechanical root. Keep the tags as compatibility
# mechanics ids; the player-facing first word is Veiled/Ashen/Pale/Restless rather than literally
# "Shadow", "Flame", "Bone" or "Wind".
execute store result score @s ss_aspect run random value 1..4
execute if score @s ss_aspect matches 1 run tag @s add ss_aspect_shadow
execute if score @s ss_aspect matches 2 run tag @s add ss_aspect_flame
execute if score @s ss_aspect matches 3 run tag @s add ss_aspect_bone
execute if score @s ss_aspect matches 4 run tag @s add ss_aspect_wind

# Second roll independently chooses the archetype Witness/Bearer/Warden/Wanderer. The final value is
# nature*10+archetype, so every nature can recombine with every archetype instead of choosing one
# prewritten whole Aspect name from a list.
execute store result score @s ss_roll run random value 1..4
execute if score @s ss_aspect matches 1 if score @s ss_roll matches 1 run scoreboard players set @s ss_aspect 11
execute if score @s ss_aspect matches 1 if score @s ss_roll matches 2 run scoreboard players set @s ss_aspect 12
execute if score @s ss_aspect matches 1 if score @s ss_roll matches 3 run scoreboard players set @s ss_aspect 13
execute if score @s ss_aspect matches 1 if score @s ss_roll matches 4 run scoreboard players set @s ss_aspect 14
execute if score @s ss_aspect matches 2 if score @s ss_roll matches 1 run scoreboard players set @s ss_aspect 21
execute if score @s ss_aspect matches 2 if score @s ss_roll matches 2 run scoreboard players set @s ss_aspect 22
execute if score @s ss_aspect matches 2 if score @s ss_roll matches 3 run scoreboard players set @s ss_aspect 23
execute if score @s ss_aspect matches 2 if score @s ss_roll matches 4 run scoreboard players set @s ss_aspect 24
execute if score @s ss_aspect matches 3 if score @s ss_roll matches 1 run scoreboard players set @s ss_aspect 31
execute if score @s ss_aspect matches 3 if score @s ss_roll matches 2 run scoreboard players set @s ss_aspect 32
execute if score @s ss_aspect matches 3 if score @s ss_roll matches 3 run scoreboard players set @s ss_aspect 33
execute if score @s ss_aspect matches 3 if score @s ss_roll matches 4 run scoreboard players set @s ss_aspect 34
execute if score @s ss_aspect matches 4 if score @s ss_roll matches 1 run scoreboard players set @s ss_aspect 41
execute if score @s ss_aspect matches 4 if score @s ss_roll matches 2 run scoreboard players set @s ss_aspect 42
execute if score @s ss_aspect matches 4 if score @s ss_roll matches 3 run scoreboard players set @s ss_aspect 43
execute if score @s ss_aspect matches 4 if score @s ss_roll matches 4 run scoreboard players set @s ss_aspect 44

# --- Flaw -------------------------------------------------------------------
# Start with a random family for a run where the pack observed no strong behavior. Then let actual
# First-Nightmare signals override it in increasing specificity. "Fled" wins over "hungry", which
# wins over "bloodied": almost any close fight can become bloody, while consuming six food points
# or opening a 40-block gap are more distinctive choices/outcomes.
execute store result score @s ss_flaw run random value 1..4
execute if entity @s[tag=ss_trial_bloodied] run scoreboard players set @s ss_flaw 2
execute if entity @s[tag=ss_trial_hungry] run scoreboard players set @s ss_flaw 3
execute if entity @s[tag=ss_trial_fled] run scoreboard players set @s ss_flaw 4

# Apply the compatibility/mechanics tag while ss_flaw still holds the family id.
# The historical ss_flaw_shadow_slave id stays internal for save/function compatibility; its old
# player-facing name was lore-wrong because Shadow Slave is an Aspect, not a Flaw.
execute if score @s ss_flaw matches 1 run tag @s add ss_flaw_shadow_slave
execute if score @s ss_flaw matches 2 run tag @s add ss_flaw_fragile
execute if score @s ss_flaw matches 3 run tag @s add ss_flaw_ravenous
execute if score @s ss_flaw matches 4 run tag @s add ss_flaw_weightless

# Randomness lives INSIDE the earned family. Two players who trigger the same strong behavior can
# therefore carry the same burden mechanic without necessarily receiving the same formal identity.
execute store result score @s ss_roll run random value 1..4
execute if score @s ss_flaw matches 1 if score @s ss_roll matches 1 run scoreboard players set @s ss_flaw 11
execute if score @s ss_flaw matches 1 if score @s ss_roll matches 2 run scoreboard players set @s ss_flaw 12
execute if score @s ss_flaw matches 1 if score @s ss_roll matches 3 run scoreboard players set @s ss_flaw 13
execute if score @s ss_flaw matches 1 if score @s ss_roll matches 4 run scoreboard players set @s ss_flaw 14
execute if score @s ss_flaw matches 2 if score @s ss_roll matches 1 run scoreboard players set @s ss_flaw 21
execute if score @s ss_flaw matches 2 if score @s ss_roll matches 2 run scoreboard players set @s ss_flaw 22
execute if score @s ss_flaw matches 2 if score @s ss_roll matches 3 run scoreboard players set @s ss_flaw 23
execute if score @s ss_flaw matches 2 if score @s ss_roll matches 4 run scoreboard players set @s ss_flaw 24
execute if score @s ss_flaw matches 3 if score @s ss_roll matches 1 run scoreboard players set @s ss_flaw 31
execute if score @s ss_flaw matches 3 if score @s ss_roll matches 2 run scoreboard players set @s ss_flaw 32
execute if score @s ss_flaw matches 3 if score @s ss_roll matches 3 run scoreboard players set @s ss_flaw 33
execute if score @s ss_flaw matches 3 if score @s ss_roll matches 4 run scoreboard players set @s ss_flaw 34
execute if score @s ss_flaw matches 4 if score @s ss_roll matches 1 run scoreboard players set @s ss_flaw 41
execute if score @s ss_flaw matches 4 if score @s ss_roll matches 2 run scoreboard players set @s ss_flaw 42
execute if score @s ss_flaw matches 4 if score @s ss_roll matches 3 run scoreboard players set @s ss_flaw 43
execute if score @s ss_flaw matches 4 if score @s ss_roll matches 4 run scoreboard players set @s ss_flaw 44

# Observation belongs to the trial that produced this identity, not to the player forever.
tag @s remove ss_trial_bloodied
tag @s remove ss_trial_hungry
tag @s remove ss_trial_fled
scoreboard players reset @s ss_roll
