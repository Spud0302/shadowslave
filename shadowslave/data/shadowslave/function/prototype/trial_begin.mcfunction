# Begin a fresh set of First-Nightmare observations for Aspect/Flaw generation.
# Runs only after nightmare/enter has accepted the player.
#
# A failed/ejected attempt must not contaminate the next successful one. These tags record only the
# trial that will actually produce the player's Sleeper identity.
tag @s remove ss_trial_bloodied
tag @s remove ss_trial_hungry
tag @s remove ss_trial_fled

# ss_roll is scratch outside the trial. During the trial it temporarily holds the FoodLevel at entry
# so observe_trial can detect hunger CONSUMED inside this Nightmare instead of punishing a player who
# arrived hungry. The generator overwrites it with random rolls and resets it after use.
scoreboard players reset @s ss_roll
execute store result score @s ss_roll run data get entity @s foodLevel
