# Near-death: the Spell spits them out. Gear intact, no Awakening.

function shadowslave:nightmare/leave

# Half a heart, and shaken.
execute store result entity @s Health float 1 run scoreboard players set @s ss_health 1
effect give @s minecraft:blindness 5 0 true
effect give @s minecraft:nausea 8 0 true

playsound minecraft:entity_wither_spawn master @s ~ ~ ~ 0.4 0.5
title @s times 10 50 20
title @s subtitle {"text":"You were not ready.","color":"dark_gray"}
title @s title {"text":"Cast Out","color":"dark_red","bold":true}
tellraw @s {"text":"The Nightmare rejected you. Sleep again to face it.","color":"gray","italic":true}
