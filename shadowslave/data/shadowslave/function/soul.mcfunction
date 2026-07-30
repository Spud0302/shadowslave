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

# Generated Aspect identities. Nature and archetype are independent vocabularies encoded into one
# score: Veiled/Ashen/Pale/Restless × Witness/Bearer/Warden/Wanderer. Canon does not give a universal
# naming grammar or generation formula; these are fan-created supernatural archetypes built over four
# finite Dormant mechanics the datapack can actually execute.
execute if score @s ss_aspect matches 11 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Veiled Witness","color":"dark_purple"},{"text":" — darkness lends sight and a little speed","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 12 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Veiled Bearer","color":"dark_purple"},{"text":" — darkness lends sight and a little speed","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 13 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Veiled Warden","color":"dark_purple"},{"text":" — darkness lends sight and a little speed","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 14 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Veiled Wanderer","color":"dark_purple"},{"text":" — darkness lends sight and a little speed","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 21 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Ashen Witness","color":"gold"},{"text":" — fire recoils from you; your blows carry embers","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 22 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Ashen Bearer","color":"gold"},{"text":" — fire recoils from you; your blows carry embers","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 23 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Ashen Warden","color":"gold"},{"text":" — fire recoils from you; your blows carry embers","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 24 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Ashen Wanderer","color":"gold"},{"text":" — fire recoils from you; your blows carry embers","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 31 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Pale Witness","color":"white"},{"text":" — the body hardens beneath the skin","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 32 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Pale Bearer","color":"white"},{"text":" — the body hardens beneath the skin","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 33 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Pale Warden","color":"white"},{"text":" — the body hardens beneath the skin","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 34 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Pale Wanderer","color":"white"},{"text":" — the body hardens beneath the skin","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 41 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Restless Witness","color":"green"},{"text":" — movement becomes unnaturally light and quick","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 42 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Restless Bearer","color":"green"},{"text":" — movement becomes unnaturally light and quick","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 43 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Restless Warden","color":"green"},{"text":" — movement becomes unnaturally light and quick","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 44 run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Restless Wanderer","color":"green"},{"text":" — movement becomes unnaturally light and quick","color":"dark_gray","italic":true}]

# Legacy fallback for worlds that rolled the old 1..4 score values before this rework. Mechanics
# already live on tags, so those players continue working until reset/re-roll instead of losing their
# status page. "Legacy" is explicit so the old class label is not mistaken for the new identity model.
execute if score @s ss_aspect matches 1 if entity @s[tag=ss_aspect_shadow] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Shadow","color":"dark_purple"},{"text":"  (legacy prototype)","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 2 if entity @s[tag=ss_aspect_flame] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Flame","color":"gold"},{"text":"  (legacy prototype)","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 3 if entity @s[tag=ss_aspect_bone] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Bone","color":"white"},{"text":"  (legacy prototype)","color":"dark_gray","italic":true}]
execute if score @s ss_aspect matches 4 if entity @s[tag=ss_aspect_wind] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Wind","color":"green"},{"text":"  (legacy prototype)","color":"dark_gray","italic":true}]

# Generated Flaw identities. The family comes from strong First-Nightmare observations when one was
# recorded; randomness changes the personal identity inside that family. Internal historical tags
# remain mechanics ids only — especially ss_flaw_shadow_slave, whose old display name was canonically
# wrong because Shadow Slave is an Aspect, not a Flaw.
execute if score @s ss_flaw matches 11 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Nightbound","color":"red"},{"text":" — direct sunlight hurts and weakens you","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 12 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Pale Dawn","color":"red"},{"text":" — direct sunlight hurts and weakens you","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 13 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Sunshy","color":"red"},{"text":" — direct sunlight hurts and weakens you","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 14 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Dusk's Debt","color":"red"},{"text":" — direct sunlight hurts and weakens you","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 21 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Brittle Vessel","color":"red"},{"text":" — maximum health is reduced","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 22 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Cracked Heart","color":"red"},{"text":" — maximum health is reduced","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 23 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Borrowed Blood","color":"red"},{"text":" — maximum health is reduced","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 24 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Thin Thread","color":"red"},{"text":" — maximum health is reduced","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 31 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Hollow Maw","color":"red"},{"text":" — hunger drains faster","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 32 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Bottomless Hunger","color":"red"},{"text":" — hunger drains faster","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 33 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Empty Feast","color":"red"},{"text":" — hunger drains faster","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 34 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Gnawing Soul","color":"red"},{"text":" — hunger drains faster","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 41 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Leadbound","color":"red"},{"text":" — movement is slowed; distance is harder won","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 42 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Heavy Step","color":"red"},{"text":" — movement is slowed; distance is harder won","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 43 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Shackled Pace","color":"red"},{"text":" — movement is slowed; distance is harder won","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 44 run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Burdened Road","color":"red"},{"text":" — movement is slowed; distance is harder won","color":"dark_gray","italic":true}]

# Legacy Flaw scores retain the current family mechanics but use neutral replacement identities rather
# than exposing internal ids. A reset/re-roll assigns the full generated identity.
execute if score @s ss_flaw matches 1 if entity @s[tag=ss_flaw_shadow_slave] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Nightbound","color":"red"},{"text":"  (legacy prototype)","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 2 if entity @s[tag=ss_flaw_fragile] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Brittle Vessel","color":"red"},{"text":"  (legacy prototype)","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 3 if entity @s[tag=ss_flaw_ravenous] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Hollow Maw","color":"red"},{"text":"  (legacy prototype)","color":"dark_gray","italic":true}]
execute if score @s ss_flaw matches 4 if entity @s[tag=ss_flaw_weightless] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Leadbound","color":"red"},{"text":"  (legacy prototype)","color":"dark_gray","italic":true}]

# PROTOTYPE-LIMIT: these are ordinary Minecraft combat stats, NOT Shadow Slave Attributes.
# Canon Attributes are named supernatural traits such as [Fated] or [Child of Shadows]. Keep
# the numbers because they make Phase 1's Dormant mechanics legible, but label them honestly
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
