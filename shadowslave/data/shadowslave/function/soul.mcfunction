# /trigger soul — the Spell's status page, such as it is without a GUI.

scoreboard players set @s soul 0

tellraw @s [{"text":"\n"},{"text":"— Soul —","color":"light_purple","bold":true}]

execute unless score @s ss_rank matches 1.. unless entity @s[tag=ss_carrier] run tellraw @s [{"text":"Rank: ","color":"gray"},{"text":"Sleeper","color":"dark_gray"}]
execute unless score @s ss_rank matches 1.. if entity @s[tag=ss_carrier] run tellraw @s [{"text":"Rank: ","color":"gray"},{"text":"Sleeper","color":"dark_gray"},{"text":"  (Carrier)","color":"dark_purple","italic":true}]
execute if score @s ss_rank matches 1 run tellraw @s [{"text":"Rank: ","color":"gray"},{"text":"Awakened","color":"aqua"}]

execute if entity @s[tag=ss_aspect_shadow] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Shadow","color":"dark_purple"},{"text":" — sight in darkness, speed within it","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_flame] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Flame","color":"gold"},{"text":" — fire cannot touch you","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_bone] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Bone","color":"white"},{"text":" — the body hardens","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_wind] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Wind","color":"green"},{"text":" — light on the feet","color":"dark_gray","italic":true}]

execute if entity @s[tag=ss_flaw_shadow_slave] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Shadow Slave","color":"red"},{"text":" — the sun burns you","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_fragile] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Fragile","color":"red"},{"text":" — three hearts were never returned","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_ravenous] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Ravenous","color":"red"},{"text":" — the soul burns through the body","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_weightless] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Weightless","color":"red"},{"text":" — the ground is unkind","color":"dark_gray","italic":true}]

# Attributes — the spec promised these and Phase 1 shipped without them. Aspects and Flaws
# express themselves as attribute modifiers, so this is where a player sees their Flaw
# actually costing them something.
# ponytail: ss_roll and ss_health are both scratch and are overwritten before any real read
execute store result score @s ss_roll run attribute @s minecraft:generic.max_health get
execute store result score @s ss_health run attribute @s minecraft:generic.armor get
execute if score @s ss_rank matches 1 run tellraw @s [{"text":"Vitality: ","color":"gray"},{"score":{"name":"@s","objective":"ss_roll"}},{"text":"   Endurance: ","color":"gray"},{"score":{"name":"@s","objective":"ss_health"}}]

execute unless score @s ss_rank matches 1.. unless entity @s[tag=ss_carrier] run tellraw @s {"text":"The Spell has not noticed you yet. Sleep, and it may.","color":"dark_gray","italic":true}
execute unless score @s ss_rank matches 1.. if entity @s[tag=ss_carrier] run tellraw @s {"text":"The Spell has marked you. Sneak on a bed, at any hour, and it will take you.","color":"dark_gray","italic":true}
tellraw @s {"text":""}
