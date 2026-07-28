# Shadow Slave — per-tick dispatch. Keep this file cheap: it runs 20x/second.

# Let every player use /trigger soul again this tick.
scoreboard players enable @a soul
execute as @a[scores={soul=1..}] run function shadowslave:soul

# Only players actually inside a nightmare cost anything.
execute as @a[tag=ss_in_nightmare] at @s run function shadowslave:nightmare/tick_player

# Aspect and Flaw upkeep — once a second, not every tick.
scoreboard players add $ss_clock ss_clock 1
execute if score $ss_clock ss_clock matches 20.. run scoreboard players set $ss_clock ss_clock 0
execute if score $ss_clock ss_clock matches 0 as @a[scores={ss_rank=1..}] at @s run function shadowslave:upkeep
