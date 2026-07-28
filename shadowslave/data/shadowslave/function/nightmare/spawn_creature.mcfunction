# Runs as and at the player. The timer has expired; the Nightmare shows its face.

tag @s add ss_creature_spawned

# Attribute format is 1.20.5+: lowercase `attributes`, with `id` and `base`.
summon minecraft:ravager ^ ^1 ^12 {Tags:["ss_creature"],CustomName:'{"text":"Nightmare Creature","color":"dark_purple","bold":true}',CustomNameVisible:1b,PersistenceRequired:1b,attributes:[{id:"minecraft:generic.max_health",base:160},{id:"minecraft:generic.attack_damage",base:4},{id:"minecraft:generic.movement_speed",base:0.32},{id:"minecraft:generic.knockback_resistance",base:0.8},{id:"minecraft:generic.follow_range",base:64}],Health:160f,ActiveEffects:[{id:"minecraft:fire_resistance",amplifier:0b,duration:-1,show_particles:0b}]}

bossbar set shadowslave:trial name {"text":"Nightmare Creature","color":"dark_red"}
bossbar set shadowslave:trial color red
bossbar set shadowslave:trial max 160
bossbar set shadowslave:trial value 160

playsound minecraft:entity.ravager.roar hostile @s ~ ~ ~ 2 0.6
title @s times 10 50 20
title @s subtitle {"text":"Kill it, or be killed.","color":"dark_gray"}
title @s title {"text":"It Has Found You","color":"dark_red","bold":true}

advancement grant @s only shadowslave:test/endured
