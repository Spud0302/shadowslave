# Force the distance-opened Flaw family, then perform the normal Sleeper transition.
execute if score @s ss_rank matches 1.. run return run tellraw @s {"text":"[Shadow Slave] Already a Sleeper. Run test/reset first.","color":"red"}
function shadowslave:test/flaw/_prepare
tag @s add ss_trial_fled
function shadowslave:progression/become_sleeper
