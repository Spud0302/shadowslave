# /function shadowslave:test/awaken — historical command name; skip the trial and become a Sleeper.
# Kept for compatibility with existing test instructions. For Phase 1 this grants the same
# Dormant/Sleeper state as surviving the First Nightmare, including a placeholder Aspect and Flaw.

execute if score @s ss_rank matches 1.. run tellraw @s {"text":"[Shadow Slave] Already a Sleeper. Run test/reset to roll again.","color":"red"}
execute if score @s ss_rank matches 1.. run return 0

tag @s add ss_carrier
function shadowslave:awaken/roll
