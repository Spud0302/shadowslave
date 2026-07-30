# /function shadowslave:test/cure — remove the Spell's mark. Back to untouched.

# You cannot use the lightweight cure after becoming a Sleeper. A Flaw is permanent in the novel,
# and `reset` is the explicit full test-state wipe. Refusing beats silently removing a Carrier tag
# that no longer represents the player's progression state.
execute if score @s ss_rank matches 1.. run tellraw @s {"text":"[Shadow Slave] You are a Sleeper. The Spell cannot lose interest now — use shadowslave:test/reset.","color":"red"}
execute if score @s ss_rank matches 1.. run return 0
execute unless entity @s[tag=ss_carrier] run tellraw @s {"text":"[Shadow Slave] The Spell has not noticed you in the first place.","color":"gray","italic":true}
execute unless entity @s[tag=ss_carrier] run return 0

tag @s remove ss_carrier
effect clear @s minecraft:nausea
tellraw @s {"text":"[Shadow Slave] The Spell has lost interest in you. Sleep once to be marked again.","color":"gray","italic":true}
