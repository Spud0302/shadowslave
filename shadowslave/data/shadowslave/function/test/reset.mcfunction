# Full test-state reset. After this function, the player should behave like a fresh Mundane
# player regardless of where the reset was invoked or what the previous test touched.
#
# BEST-PRACTICE: test reset helpers must clear hidden/transient state as well as visible state.
# Leaving health below an entry threshold, a cooldown, observation tag, scratch score, modifier or
# short-lived pack effect behind makes later tests order-dependent and can turn a correct feature into
# a false failure (or vice versa).

# Get out of the nightmare first if we are in it. This must happen before state is cleared:
# nightmare/leave owns teardown of the bossbar, creature and return teleport.
execute if entity @s[tag=ss_in_nightmare] run function shadowslave:nightmare/leave

advancement revoke @s from shadowslave:test/root
advancement revoke @s only shadowslave:enter_nightmare

# Persistent/progression prototype state.
scoreboard players set @s ss_rank 0
scoreboard players set @s ss_timer 0
scoreboard players set @s ss_aspect 0
scoreboard players set @s ss_flaw 0

# Transient state. nightmare/leave deliberately sets ss_cooldown, so reset MUST clear it after
# calling leave or a reset performed inside the Nightmare contaminates the next run. ss_roll is also
# the per-trial hunger baseline before it returns to random-generation scratch at progression time.
scoreboard players reset @s ss_cooldown
scoreboard players reset @s ss_gone
scoreboard players reset @s ss_health
scoreboard players reset @s ss_roll
scoreboard players reset @s ss_scratch_a
scoreboard players reset @s ss_scratch_b
scoreboard players reset @s ss_ret_x
scoreboard players reset @s ss_ret_y
scoreboard players reset @s ss_ret_z
scoreboard players reset @s soul

tag @s remove ss_in_nightmare
tag @s remove ss_carrier
tag @s remove ss_creature_spawned
tag @s remove ss_test_bypass
tag @s remove ss_trial_bloodied
tag @s remove ss_trial_hungry
tag @s remove ss_trial_fled
tag @s remove ss_aspect_shadow
tag @s remove ss_aspect_flame
tag @s remove ss_aspect_bone
tag @s remove ss_aspect_wind
tag @s remove ss_flaw_shadow_slave
tag @s remove ss_flaw_fragile
tag @s remove ss_flaw_ravenous
tag @s remove ss_flaw_weightless

# Upkeep effects expire naturally in ordinary gameplay, but a verification reset promises a genuinely
# fresh observation window. Clear every transient effect this pack can apply so a test cannot inherit
# the previous identity or an ejection presentation for even a few seconds.
effect clear @s minecraft:night_vision
effect clear @s minecraft:speed
effect clear @s minecraft:fire_resistance
effect clear @s minecraft:jump_boost
effect clear @s minecraft:hunger
effect clear @s minecraft:slowness
effect clear @s minecraft:weakness
effect clear @s minecraft:blindness
effect clear @s minecraft:nausea

# Tags alone are not enough: persistent attribute modifiers survive after their source tag is
# removed, so every modifier owned by the finite Dormant mechanical vocabulary is stripped explicitly.
attribute @s minecraft:generic.armor modifier remove shadowslave:aspect_bone_armor
attribute @s minecraft:generic.movement_speed modifier remove shadowslave:aspect_wind_speed
attribute @s minecraft:generic.max_health modifier remove shadowslave:flaw_fragile_health
# Migration cleanup for 0.7.2 and older worlds. The fled family no longer uses this modifier.
attribute @s minecraft:generic.safe_fall_distance modifier remove shadowslave:flaw_weightless_fall

# Restore an enterable verification baseline after removing the max-health modifier. Entry refuses
# below 14 HP, so leaving low health behind makes a reset-then-enter test exercise only the refusal path.
effect give @s minecraft:instant_health 1 4 true

# Defensive cleanup for tests that spawned a creature without completing normal teardown.
# Single-player-at-a-time limitation still applies; multiplayer instance ownership must replace
# this global selector before simultaneous Nightmares are supported.
execute in shadowslave:nightmare run kill @e[tag=ss_creature]
bossbar set shadowslave:trial visible false
bossbar set shadowslave:trial players

# Untouched, not a Sleeper: Carrier is a pre-First-Nightmare infection state; Sleeper begins
# only after surviving that Nightmare.
tellraw @s {"text":"[Shadow Slave] Verification state reset. You are Mundane again.","color":"gray","italic":true}
