# Runs as and at the player. The timer has expired; the Nightmare shows its face.

# Spawned overhead, then placed on the surface at a distance.
#
# Local coords (^ ^1 ^12) follow pitch, so looking downhill buried it in terrain and
# looking down near the world floor put it outside the dimension entirely. The player's
# own column is always clear, so summon there — but landing on their head hands it free
# hits, hence the spread below. See ISSUES.md for the full history.
summon minecraft:ravager ~ ~10 ~ {Tags:["ss_creature"],CustomName:'{"text":"Nightmare Creature","color":"dark_purple","bold":true}',CustomNameVisible:1b,PersistenceRequired:1b,attributes:[{id:"minecraft:generic.max_health",base:60},{id:"minecraft:generic.attack_damage",base:4},{id:"minecraft:generic.knockback_resistance",base:0.8},{id:"minecraft:generic.follow_range",base:64}],Health:60f,active_effects:[{id:"minecraft:fire_resistance",amplifier:0b,duration:-1,show_particles:0b},{id:"minecraft:speed",amplifier:0b,duration:-1,show_particles:0b}]}

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
execute if entity @s[tag=ss_creature_spawned] run bossbar set shadowslave:trial max 60
execute if entity @s[tag=ss_creature_spawned] run bossbar set shadowslave:trial value 60

execute if entity @s[tag=ss_creature_spawned] run playsound minecraft:entity.ravager.roar hostile @s ~ ~ ~ 2 0.6
execute if entity @s[tag=ss_creature_spawned] run title @s times 10 50 20
execute if entity @s[tag=ss_creature_spawned] run title @s subtitle {"text":"Kill it, or be killed.","color":"dark_gray"}
execute if entity @s[tag=ss_creature_spawned] run title @s title {"text":"It Has Found You","color":"dark_red","bold":true}

execute if entity @s[tag=ss_creature_spawned] run advancement grant @s only shadowslave:test/endured
