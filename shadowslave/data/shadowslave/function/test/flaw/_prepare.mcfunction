# Shared setup for deterministic Flaw-family verification.
#
# Runtime gameplay must never call this path. It exists so each family can be exercised without a
# Mineflayer bot having to fight the Nightmare Creature. Classification from real behaviour remains a
# human/integration concern; these hooks verify what happens AFTER a family has been selected.

tag @s remove ss_trial_bloodied
tag @s remove ss_trial_hungry
tag @s remove ss_trial_fled
scoreboard players reset @s ss_roll
tag @s add ss_carrier
