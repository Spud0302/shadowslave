# /function shadowslave:test/nightmare — enter the trial immediately, no bed, no sleep.

execute if entity @s[tag=ss_in_nightmare] run tellraw @s {"text":"[Shadow Slave] You are already in a nightmare.","color":"red"}
execute if entity @s[tag=ss_in_nightmare] run return 0
execute if score @s ss_rank matches 1.. run tellraw @s {"text":"[Shadow Slave] A Sleeper cannot re-enter a First Nightmare. Run test/reset first.","color":"red"}
execute if score @s ss_rank matches 1.. run return 0

# Testing bypasses the cooldown and the weakness gate, deliberately and visibly. enter.mcfunction
# honours this tag and consumes it, so the exemption lasts exactly one entry.
tag @s add ss_test_bypass

tag @s add ss_carrier
function shadowslave:nightmare/enter
