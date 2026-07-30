# Record strong First-Nightmare behavior signals for the eventual Flaw family.
# Runs every tick only while the Nightmare Creature exists.
#
# Do not infer personality from weak/noisy signals. Each tag below requires an event the pack can
# directly observe and explain.

# Entry refuses health <=9, while <=4 ejects. Reaching 5..8 therefore proves this run genuinely
# drove the player into the survivable near-collapse window.
execute if score @s ss_health matches 5..8 run tag @s add ss_trial_bloodied

# Compare current FoodLevel to the entry baseline stored in ss_roll. An absolute hunger threshold
# would punish somebody who arrived hungry before the Nightmare and falsely call that trial behavior.
scoreboard players reset @s ss_scratch_b
execute store result score @s ss_scratch_b run data get entity @s foodLevel
scoreboard players operation @s ss_scratch_b -= @s ss_roll
execute if score @s ss_scratch_b matches ..-6 run tag @s add ss_trial_hungry

# The state machine leashes the creature at 48 blocks. Observe at 40+ BEFORE that teleport so the
# player's decision to create a large gap is not erased by the safety mechanic itself.
execute if entity @e[tag=ss_creature,distance=40..] run tag @s add ss_trial_fled
