# /function shadowslave:test/nightmare — enter the trial immediately, no bed, no sleep.

execute if entity @s[tag=ss_in_nightmare] run tellraw @s {"text":"[Shadow Slave] You are already in a nightmare.","color":"red"}
execute if entity @s[tag=ss_in_nightmare] run return 0
execute if score @s ss_rank matches 1.. run tellraw @s {"text":"[Shadow Slave] The Awakened cannot re-enter a First Nightmare. Run test/reset first.","color":"red"}
execute if score @s ss_rank matches 1.. run return 0

# Testing overrides the cooldown deliberately, rather than slipping past it — the guard now
# lives in enter.mcfunction, so this has to clear it on purpose.
scoreboard players reset @s ss_cooldown

tag @s add ss_carrier
function shadowslave:nightmare/enter
