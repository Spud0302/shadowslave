# /function shadowslave:test/awaken — skip the trial and Awaken now.
# For testing Aspects and Flaws without fighting the creature every time.

execute if score @s ss_rank matches 1.. run tellraw @s {"text":"[Shadow Slave] Already a Sleeper. Run test/reset to roll again.","color":"red"}
execute if score @s ss_rank matches 1.. run return 0

tag @s add ss_carrier
function shadowslave:awaken/roll
