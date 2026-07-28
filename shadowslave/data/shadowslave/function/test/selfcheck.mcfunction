# Run with /function shadowslave:test/selfcheck — asserts the pack loaded correctly.

tellraw @s [{"text":"— Shadow Slave self-check —","color":"light_purple","bold":true}]

# Objectives exist: setting a value on a missing objective fails the command.
execute store success score $check ss_roll run scoreboard players set $probe ss_rank 0
execute if score $check ss_roll matches 1 run tellraw @s {"text":"PASS objectives registered","color":"green"}
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL objectives missing — did init run?","color":"red"}

# Dimension is registered.
execute store success score $check ss_roll run execute in shadowslave:nightmare run time query daytime
execute if score $check ss_roll matches 1 run tellraw @s {"text":"PASS dimension shadowslave:nightmare registered","color":"green"}
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL dimension missing","color":"red"}

# Bossbar exists.
execute store success score $check ss_roll run bossbar get shadowslave:trial max
execute if score $check ss_roll matches 1 run tellraw @s {"text":"PASS trial bossbar registered","color":"green"}
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL trial bossbar missing","color":"red"}

# Every Aspect and Flaw function resolves.
execute store success score $check ss_roll run function shadowslave:aspect/shadow
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL aspect/shadow missing","color":"red"}
# Must stay ahead of the flaw/shadow_slave probe: its fire_resistance is what stops that
# probe's `damage @s 1 minecraft:on_fire` from actually hurting whoever runs the self-check.
execute store success score $check ss_roll run function shadowslave:aspect/flame
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL aspect/flame missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:aspect/bone
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL aspect/bone missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:aspect/wind
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL aspect/wind missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:flaw/shadow_slave
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL flaw/shadow_slave missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:flaw/fragile
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL flaw/fragile missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:flaw/ravenous
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL flaw/ravenous missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:flaw/weightless
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL flaw/weightless missing","color":"red"}

# Clean up the effects the probe calls just applied.
attribute @s minecraft:generic.armor modifier remove shadowslave:aspect_bone_armor
attribute @s minecraft:generic.movement_speed modifier remove shadowslave:aspect_wind_speed
attribute @s minecraft:generic.max_health modifier remove shadowslave:flaw_fragile_health
attribute @s minecraft:generic.safe_fall_distance modifier remove shadowslave:flaw_weightless_fall
# The fragile probe clamped current health when it lowered the max; give it back.
effect give @s minecraft:instant_health 1 4 true
effect clear @s

scoreboard players reset $probe ss_rank
scoreboard players reset $check ss_roll

tellraw @s {"text":"— self-check complete —","color":"light_purple"}
