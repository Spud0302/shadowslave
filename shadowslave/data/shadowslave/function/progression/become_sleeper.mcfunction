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

# The datapack now generates a personal identity over a finite Dormant mechanical vocabulary and
# derives the Flaw family from strong trial observations when available. Keeping all generation behind
# this one call leaves the future Java/data model a single seam without polluting rank progression.
function shadowslave:prototype/roll_aspect_flaw

tellraw @s [{"text":"\n","color":"white"},{"text":"You are a Sleeper.","color":"light_purple","bold":true},{"text":"\nRun ","color":"gray"},{"text":"/trigger soul","color":"aqua"},{"text":" to read your soul.\n","color":"gray"}]

# Historical advancement id retained for save/test compatibility. Its display name is Sleeper.
advancement grant @s only shadowslave:test/awakened
