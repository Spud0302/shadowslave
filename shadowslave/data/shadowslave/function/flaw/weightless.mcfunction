# Weightless — the ground is unkind.
attribute @s minecraft:generic.safe_fall_distance modifier remove shadowslave:flaw_weightless_fall
# -1, not -2. Vanilla safe fall distance is 3; -2 leaves 1, and a standing jump is 1.25
# blocks — so the player took damage simply for jumping on the spot. -1 leaves 2, which
# clears a jump but still bites on any real drop.
attribute @s minecraft:generic.safe_fall_distance modifier add shadowslave:flaw_weightless_fall -1 add_value

# Q4 diagnostic, inert in normal gameplay. Chat-based probes changed the timing of the intermittent
# harness failure, so record what the SERVER saw immediately after the modifier command instead.
# ss_scratch_a counts executions; ss_scratch_b stores safe_fall_distance * 1000 (expected 2000).
execute if entity @s[tag=ss_test_trace_weightless] run scoreboard players add @s ss_scratch_a 1
execute if entity @s[tag=ss_test_trace_weightless] store result score @s ss_scratch_b run attribute @s minecraft:generic.safe_fall_distance get 1000
