# /function shadowslave:test/awaken — historical command name; skip the trial and become a Sleeper.
# For testing generated Aspect/Flaw identities without fighting the creature every time.

execute if score @s ss_rank matches 1.. run tellraw @s {"text":"[Shadow Slave] Already a Sleeper. Run test/reset to roll again.","color":"red"}
execute if score @s ss_rank matches 1.. run return 0

# This helper explicitly skips the trial, so it must also discard trial observations from a previous
# failed attempt. Otherwise a supposed random generator test could silently inherit an earned family.
tag @s remove ss_trial_bloodied
tag @s remove ss_trial_hungry
tag @s remove ss_trial_fled
scoreboard players reset @s ss_roll

tag @s add ss_carrier
function shadowslave:awaken/roll
