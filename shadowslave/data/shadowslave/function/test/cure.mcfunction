# /function shadowslave:test/cure — remove the Spell's mark. Back to untouched.

tag @s remove ss_carrier
effect clear @s minecraft:nausea
tellraw @s {"text":"[Shadow Slave] The Spell has lost interest in you. Sleep once to be marked again.","color":"gray","italic":true}
