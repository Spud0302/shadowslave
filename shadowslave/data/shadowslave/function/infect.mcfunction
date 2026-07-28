# The Spell marks a Carrier. Runs as the player.

tag @s add ss_carrier

playsound minecraft:ambient.cave ambient @s ~ ~ ~ 0.6 0.6
effect give @s minecraft:nausea 6 0 true
tellraw @s [{"text":"\n"},{"text":"Something noticed you while you slept.","color":"dark_purple","italic":true},{"text":"\nYou will not rest easily again. Sneak on a bed, at any hour, and it will take you.\n","color":"gray"}]
