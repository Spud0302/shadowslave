# /function shadowslave:test/infect — mark yourself as a Carrier without waiting to sleep.

execute if score @s ss_rank matches 1.. run tellraw @s {"text":"[Shadow Slave] You are already a Sleeper. Run test/reset first.","color":"red"}
execute if score @s ss_rank matches 1.. run return 0
execute if entity @s[tag=ss_carrier] run tellraw @s {"text":"[Shadow Slave] You are already a Carrier.","color":"gray","italic":true}
execute if entity @s[tag=ss_carrier] run return 0

function shadowslave:infect
