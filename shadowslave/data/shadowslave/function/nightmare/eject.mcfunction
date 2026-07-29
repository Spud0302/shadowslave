# Near-death: the Spell spits them out. Gear intact, no Awakening.

function shadowslave:nightmare/leave

# Death lands here too, and should not be dressed as an ejection.
#
# tick_player ejects at Health <= 4, and a real death is 0 — which is also <= 4. So dying ran
# this entire ceremony: the "Cast Out" title, the blindness and nausea, and the Cast Out
# advancement, all behind the vanilla death screen. Being cast out is surviving; dying is not.
# The teardown in leave.mcfunction above has already run, so the state cleanup and the item
# sweep still happen either way — only the presentation differs.
#
# A tellraw rather than a title, because a title shown to a dead player is drawn behind the
# death screen and gone by the time they respawn. Chat survives the respawn, and doubles as the
# hint that their belongings are waiting at the bed.
execute if entity @s[nbt={Health:0.0f}] run return run tellraw @s {"text":"The Nightmare took you. What fell there has come home with you.","color":"dark_red","italic":true}


# Shaken.
effect give @s minecraft:blindness 5 0 true
effect give @s minecraft:nausea 8 0 true

execute at @s run playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 0.4 0.5
title @s times 10 50 20
title @s subtitle {"text":"You were not ready.","color":"dark_gray"}
title @s title {"text":"Cast Out","color":"dark_red","bold":true}
tellraw @s {"text":"The Nightmare rejected you. Sleep again to face it.","color":"gray","italic":true}

advancement grant @s only shadowslave:test/cast_out
