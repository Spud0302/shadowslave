# The Spell calling a Carrier. Runs once every 30 seconds, as each player.
#
# Lore: once infected by the Nightmare Spell, a Carrier experiences increasing fatigue and
# sleepiness until the First Nightmare takes them. This is a pre-Sleeper condition: surviving
# that Nightmare is what produces the Dormant Sleeper/Dreamer state.

# Only Carriers hear the call. Untouched players are left alone; Sleepers have already completed
# their First Nightmare and Phase 1 has no later Dream Realm sleep behavior yet.
execute unless entity @s[tag=ss_carrier] run return 0
execute if score @s ss_rank matches 1.. run return 0

# Already inside a trial — they have answered the call. Nausea during the boss fight is a real
# handicap, and calling someone who is already inside their Nightmare makes no sense.
execute if entity @s[tag=ss_in_nightmare] run return 0

effect give @s minecraft:nausea 4 0 true
title @s actionbar {"text":"Your eyelids are heavy. Something is calling.","color":"dark_gray","italic":true}
