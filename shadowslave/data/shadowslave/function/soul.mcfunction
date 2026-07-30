# /trigger soul — the Spell's status page, such as it is without a GUI.

scoreboard players set @s soul 0

tellraw @s [{"text":"\n"},{"text":"— Soul —","color":"light_purple","bold":true}]

# The ladder, corrected in 1.4.8 against the lore research in docs/lore-research/:
#
#   Mundane -> Carrier -> Sleeper (Dormant) -> Awakened
#
# A Carrier is marked by the Spell but has not yet earned the Dormant/Sleeper rank. Surviving
# the First Nightmare produces a Sleeper. True Awakening comes later, after the first Dream
# Realm journey and successful return through a Gateway.
#
# "Mundane" is descriptive rather than a formal Soul Rank, but it is accurate for untouched
# players. ss_rank remains a Phase 1 prototype score: value 1 means "survived First Nightmare".
execute unless score @s ss_rank matches 1.. unless entity @s[tag=ss_carrier] run tellraw @s [{"text":"Rank: ","color":"gray"},{"text":"Mundane","color":"dark_gray"}]
execute unless score @s ss_rank matches 1.. if entity @s[tag=ss_carrier] run tellraw @s [{"text":"Rank: ","color":"gray"},{"text":"Carrier","color":"dark_gray"},{"text":"  (marked)","color":"dark_purple","italic":true}]
execute if score @s ss_rank matches 1 run tellraw @s [{"text":"Rank: ","color":"gray"},{"text":"Sleeper","color":"aqua"},{"text":"  (Dormant)","color":"dark_gray","italic":true}]

execute if entity @s[tag=ss_aspect_shadow] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Shadow","color":"dark_purple"},{"text":" — sight in darkness, speed within it","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_flame] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Flame","color":"gold"},{"text":" — fire cannot touch you","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_bone] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Bone","color":"white"},{"text":" — the body hardens","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_wind] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Wind","color":"green"},{"text":" — light on the feet","color":"dark_gray","italic":true}]

execute if entity @s[tag=ss_flaw_shadow_slave] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Shadow Slave","color":"red"},{"text":" — the sun burns you","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_fragile] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Fragile","color":"red"},{"text":" — three hearts were never returned","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_ravenous] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Ravenous","color":"red"},{"text":" — the soul burns through the body","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_weightless] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Weightless","color":"red"},{"text":" — the ground is unkind","color":"dark_gray","italic":true}]

# PROTOTYPE-LIMIT: these are ordinary Minecraft combat stats, NOT Shadow Slave Attributes.
# Canon Attributes are named supernatural traits such as [Fated] or [Child of Shadows]. Keep
# the numbers because they make Phase 1's placeholder powers legible, but label them honestly
# until a real Attribute system exists.
#
# Dedicated scratch objectives are intentional. This readout once borrowed ss_health, which
# the ejection check reads; a failed cross-dimension health refresh could then eject a player
# because merely opening the Soul readout had poisoned the score.
execute store result score @s ss_scratch_a run attribute @s minecraft:generic.max_health get
execute store result score @s ss_scratch_b run attribute @s minecraft:generic.armor get
execute if score @s ss_rank matches 1 run tellraw @s [{"text":"Body: ","color":"gray"},{"text":"Max Health ","color":"dark_gray"},{"score":{"name":"@s","objective":"ss_scratch_a"}},{"text":"   Armor ","color":"dark_gray"},{"score":{"name":"@s","objective":"ss_scratch_b"}}]

execute unless score @s ss_rank matches 1.. unless entity @s[tag=ss_carrier] run tellraw @s {"text":"The Spell has not noticed you yet. Sleep, and it may.","color":"dark_gray","italic":true}
execute unless score @s ss_rank matches 1.. if entity @s[tag=ss_carrier] run tellraw @s {"text":"The Spell has marked you. Sneak on a bed, at any hour, and it will take you.","color":"dark_gray","italic":true}
tellraw @s {"text":""}
