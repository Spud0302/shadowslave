# Runs as and at the player. The timer has expired; the Nightmare shows its face.

# Attribute format is 1.20.5+: lowercase `attributes`, with `id` and `base`.
summon minecraft:ravager ^ ^1 ^12 {Tags:["ss_creature"],CustomName:'{"text":"Nightmare Creature","color":"dark_purple","bold":true}',CustomNameVisible:1b,PersistenceRequired:1b,attributes:[{id:"minecraft:generic.max_health",base:160},{id:"minecraft:generic.attack_damage",base:4},{id:"minecraft:generic.movement_speed",base:0.32},{id:"minecraft:generic.knockback_resistance",base:0.8},{id:"minecraft:generic.follow_range",base:64}],Health:160f,ActiveEffects:[{id:"minecraft:fire_resistance",amplifier:0b,duration:-1,show_particles:0b}]}

# Only claim the creature spawned if it actually did. `^ ^1 ^12` inherits the player's pitch,
# so looking straight down near y=-64 (or up near the ceiling) targets a position outside the
# dimension and the summon fails. Tagging unconditionally there would leave the player marked
# as fighting a creature that does not exist — which the win condition reads as a victory.
# Guarded this way, a failed summon simply retries next tick once their pitch moves.
execute if entity @e[tag=ss_creature] run tag @s add ss_creature_spawned

# Everything below is the arrival fanfare — gated on the same condition so a retrying
# summon does not roar and flash a title every tick while it keeps failing.
execute if entity @s[tag=ss_creature_spawned] run bossbar set shadowslave:trial name {"text":"Nightmare Creature","color":"dark_red"}
execute if entity @s[tag=ss_creature_spawned] run bossbar set shadowslave:trial color red
execute if entity @s[tag=ss_creature_spawned] run bossbar set shadowslave:trial max 160
execute if entity @s[tag=ss_creature_spawned] run bossbar set shadowslave:trial value 160

execute if entity @s[tag=ss_creature_spawned] run playsound minecraft:entity.ravager.roar hostile @s ~ ~ ~ 2 0.6
execute if entity @s[tag=ss_creature_spawned] run title @s times 10 50 20
execute if entity @s[tag=ss_creature_spawned] run title @s subtitle {"text":"Kill it, or be killed.","color":"dark_gray"}
execute if entity @s[tag=ss_creature_spawned] run title @s title {"text":"It Has Found You","color":"dark_red","bold":true}

execute if entity @s[tag=ss_creature_spawned] run advancement grant @s only shadowslave:test/endured
