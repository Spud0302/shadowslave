# The Spell calling a Carrier. Runs once every 30 seconds, as each player.
#
# Lore: "Once infected by the Nightmare Spell, that person will experience constant fatigue
# and sleepiness and eventually will fall into an unnatural slumber." The fatigue is the
# Spell pulling them under — so it is a Sleeper-only condition that stops at Awakening.

# Only Carriers hear it. The untouched are left alone, and the Awakened have answered.
execute unless entity @s[tag=ss_carrier] run return 0
execute if score @s ss_rank matches 1.. run return 0

effect give @s minecraft:nausea 4 0 true
title @s actionbar {"text":"Your eyelids are heavy. Something is calling.","color":"dark_gray","italic":true}
