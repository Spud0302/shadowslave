# /trigger soul — the Spell's status page, such as it is without a GUI.

scoreboard players set @s soul 0

tellraw @s [{"text":"\n"},{"text":"— Soul —","color":"light_purple","bold":true}]

execute if score @s ss_rank matches 0 run tellraw @s [{"text":"Rank: ","color":"gray"},{"text":"Sleeper","color":"dark_gray"}]
execute if score @s ss_rank matches 1 run tellraw @s [{"text":"Rank: ","color":"gray"},{"text":"Awakened","color":"aqua"}]

execute if entity @s[tag=ss_aspect_shadow] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Shadow","color":"dark_purple"},{"text":" — sight in darkness, speed within it","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_flame] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Flame","color":"gold"},{"text":" — fire cannot touch you","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_bone] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Bone","color":"white"},{"text":" — the body hardens","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_wind] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Wind","color":"green"},{"text":" — light on the feet","color":"dark_gray","italic":true}]

execute if entity @s[tag=ss_flaw_shadow_slave] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Shadow Slave","color":"red"},{"text":" — the sun burns you","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_fragile] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Fragile","color":"red"},{"text":" — three hearts were never returned","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_ravenous] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Ravenous","color":"red"},{"text":" — the soul burns through the body","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_weightless] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Weightless","color":"red"},{"text":" — the ground is unkind","color":"dark_gray","italic":true}]

execute if score @s ss_rank matches 0 run tellraw @s {"text":"The Spell has not yet tested you. Sleep.","color":"dark_gray","italic":true}
tellraw @s {"text":""}
