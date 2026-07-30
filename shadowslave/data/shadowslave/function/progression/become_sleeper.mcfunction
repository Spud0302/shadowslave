# Canon transition after surviving the First Nightmare.
#
# IMPORTANT: this is NOT Awakening. The player becomes a Sleeper/Dreamer with a Dormant
# soul rank here. True Awakening requires the later first Dream Realm journey and return
# through a Gateway, which Phase 1 does not implement yet.
#
# Keep progression state separate from Aspect/Flaw generation. The old awaken/roll.mcfunction mixed
# those responsibilities together, which made the incorrect First Nightmare -> Awakened model harder
# to remove later.

scoreboard players set @s ss_rank 1

# The datapack generates a personal identity over a finite Dormant mechanical vocabulary and derives
# the Flaw family from strong trial observations when available. Keeping all generation behind this
# one call leaves the future Java/data model a single seam without polluting rank progression.
function shadowslave:prototype/roll_aspect_flaw

# Player-facing appraisal. The wording is project-authored Spell-style presentation, not a claim that
# canon exposes this exact classification algorithm. It makes the causal half of the Flaw legible:
# the burden was shaped by the successful trial; the formal name was randomized inside that family.
tellraw @s [{"text":"\n","color":"white"},{"text":"[Your trial is over.]","color":"light_purple","italic":true}]
execute if score @s ss_flaw matches 11..14 run tellraw @s {"text":"[You endured without yielding to one consuming weakness. The price will find you in the light.]","color":"gray","italic":true}
execute if score @s ss_flaw matches 21..24 run tellraw @s {"text":"[You survived at the edge of death. Your body remembers how close it came to breaking.]","color":"gray","italic":true}
execute if score @s ss_flaw matches 31..34 run tellraw @s {"text":"[You fed the body while the Nightmare consumed you. Hunger followed you home.]","color":"gray","italic":true}
execute if score @s ss_flaw matches 41..44 run tellraw @s {"text":"[You made distance your shield. Even the ground remembers your retreat.]","color":"gray","italic":true}

tellraw @s [{"text":"You are a Sleeper.","color":"light_purple","bold":true},{"text":"\nRun ","color":"gray"},{"text":"/trigger soul","color":"aqua"},{"text":" to learn the names the Spell left you.\n","color":"gray"}]

# Historical advancement id retained for save/test compatibility. Its display name is Sleeper.
advancement grant @s only shadowslave:test/awakened
