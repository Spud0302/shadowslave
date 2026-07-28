# Runs as and at the player. The timer has expired; the Nightmare shows its face.

# Spawning is two steps, and both are needed.
#
# Local coords (`^ ^1 ^12`) follow pitch as well as yaw, so looking downhill buried the
# creature inside terrain, and near the world floor or ceiling it fell outside the dimension
# and the summon failed outright. So: summon directly overhead, where the player's own
# column is definitionally clear at any terrain and any angle.
#
# But landing on the player's head hands it free hits before they can react. So immediately
# spread it out — `spreadplayers` is the only vanilla primitive that places an entity on the
# surface via the heightmap, which is exactly the safe-placement guarantee we need. It ends
# up on solid ground, at a distance, in view.
# Attribute format is 1.20.5+: lowercase `attributes`, with `id` and `base`.
summon minecraft:ravager ~ ~10 ~ {Tags:["ss_creature"],CustomName:'{"text":"Nightmare Creature","color":"dark_purple","bold":true}',CustomNameVisible:1b,PersistenceRequired:1b,attributes:[{id:"minecraft:generic.max_health",base:160},{id:"minecraft:generic.attack_damage",base:4},{id:"minecraft:generic.movement_speed",base:0.32},{id:"minecraft:generic.knockback_resistance",base:0.8},{id:"minecraft:generic.follow_range",base:64}],Health:160f,active_effects:[{id:"minecraft:fire_resistance",amplifier:0b,duration:-1,show_particles:0b}]}

# Put it on the surface at a distance, rather than on top of the player.
#
# spreadplayers' distance argument is the minimum gap BETWEEN targets, so with a single
# creature it does nothing and the range was uniform 0-14 blocks — sometimes in your face.
# Centring the spread 12 blocks ahead instead puts it 8-16 blocks out, every time.
# `rotated ~ 0` keeps the player's yaw but zeroes pitch, so looking up or down cannot
# throw the centre point into the sky or the ground.
execute at @s rotated ~ 0 positioned ^ ^ ^12 run spreadplayers ~ ~ 0 4 false @e[tag=ss_creature]

# Only claim the creature spawned if it actually did. A summon can still fail — a version
# bump renaming the attribute ids would do it — and tagging unconditionally would leave the
# player marked as fighting a creature that does not exist, which the win condition reads as
# a victory. Guarded this way, a failed summon simply retries on the next tick.
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
