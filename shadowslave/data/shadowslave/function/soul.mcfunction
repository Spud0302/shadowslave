# /trigger soul — the Spell's status page, such as it is without a GUI.

scoreboard players set @s soul 0

tellraw @s [{"text":"\n"},{"text":"— Soul —","color":"light_purple","bold":true}]

# The ladder, corrected in 1.4.8 against the lore research in docs/lore-research/:
#
#   Mundane -> Carrier -> Sleeper (Dormant) -> Awakened
#
# Two things were wrong before. A Carrier was labelled a Sleeper, but being marked is not yet
# a rank — you hold Dormant only after surviving a First Nightmare. And ss_rank 1 was labelled
# Awakened, which is a rank further on: canon puts Awakening after a first Dream Realm journey,
# not at the end of the First Nightmare. Phase 1 cannot reach Awakened at all.
#
# "Mundane" is descriptive in the novel rather than a formal rank name, but it is accurate.
# Only the labels moved here — ss_rank 1 still means "survived the First Nightmare" everywhere.
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

# Attributes — the spec promised these and Phase 1 shipped without them. Aspects and Flaws
# express themselves as attribute modifiers, so this is where a player sees their Flaw
# actually costing them something.
# Dedicated scratch objectives. This readout used to borrow ss_health, which the ejection
# check compares against its threshold — so reading your soul with low armour could poison
# that score and eject you the moment you entered a nightmare. Two objectives cost nothing.
execute store result score @s ss_scratch_a run attribute @s minecraft:generic.max_health get
execute store result score @s ss_scratch_b run attribute @s minecraft:generic.armor get
execute if score @s ss_rank matches 1 run tellraw @s [{"text":"Vitality: ","color":"gray"},{"score":{"name":"@s","objective":"ss_scratch_a"}},{"text":"   Endurance: ","color":"gray"},{"score":{"name":"@s","objective":"ss_scratch_b"}}]

execute unless score @s ss_rank matches 1.. unless entity @s[tag=ss_carrier] run tellraw @s {"text":"The Spell has not noticed you yet. Sleep, and it may.","color":"dark_gray","italic":true}
execute unless score @s ss_rank matches 1.. if entity @s[tag=ss_carrier] run tellraw @s {"text":"The Spell has marked you. Sneak on a bed, at any hour, and it will take you.","color":"dark_gray","italic":true}
tellraw @s {"text":""}
