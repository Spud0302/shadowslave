# Shadow Slave — per-tick dispatch. Keep this file cheap: it runs 20x/second.

# Let every player use /trigger soul again this tick.
scoreboard players enable @a soul
execute as @a[scores={soul=1..}] run function shadowslave:soul

# Only players actually inside a nightmare cost anything.
# Tags survive death, so a player who died in the trial would otherwise stay tagged forever
# and drag the nightmare into the Overworld. Anyone tagged but no longer in the dimension
# gets torn down first.
execute as @a[tag=ss_in_nightmare] at @s unless data entity @s {Dimension:"shadowslave:nightmare"} run function shadowslave:nightmare/leave
execute as @a[tag=ss_in_nightmare] at @s run function shadowslave:nightmare/tick_player

# Aspect and Flaw upkeep — once a second, not every tick.
scoreboard players add $ss_clock ss_clock 1
execute if score $ss_clock ss_clock matches 20.. run scoreboard players set $ss_clock ss_clock 0
execute if score $ss_clock ss_clock matches 0 as @a[scores={ss_rank=1..}] at @s run function shadowslave:upkeep

# Succumbing to the Spell. Vanilla only lets you sleep at night, but the novel's Carriers
# fall under whenever the Spell takes them — so sneaking on a bed is the real entry, and it
# works at any hour. The vanilla slept_in_bed advancement still works as a second path.
# NOTE: `unless ... matches 1..`, not `matches 0` — a Sleeper has no ss_rank entry at all,
# and `matches` fails outright on an absent score.
execute if score $ss_clock ss_clock matches 0 as @a[tag=ss_carrier,scores={ss_cooldown=..0}] at @s unless score @s ss_rank matches 1.. if predicate shadowslave:is_sneaking if block ~ ~ ~ #minecraft:beds run function shadowslave:nightmare/enter

# Telegraph the hold. The check above polls once a second, so a quick tap falls between
# polls and reads as unresponsive — but a deliberate hold is the RIGHT interaction here:
# an accidental sneak near a bed should not drop you into a trial that can end your run.
# So keep the hold and say so. Polled every tick, unlike the entry itself, so the message
# appears the instant they crouch.
execute as @a[tag=ss_carrier] at @s unless score @s ss_rank matches 1.. unless entity @s[tag=ss_in_nightmare] if predicate shadowslave:is_sneaking if block ~ ~ ~ #minecraft:beds run title @s actionbar {"text":"The Spell reaches for you...","color":"dark_purple","italic":true}

# Cooldown ticks down once a second.
execute if score $ss_clock ss_clock matches 0 as @a[scores={ss_cooldown=1..}] run scoreboard players remove @s ss_cooldown 1

# The Spell calls its Carriers every 30 seconds until they answer it.
execute if score $ss_clock ss_clock matches 0 run scoreboard players add $ss_call ss_call 1
execute if score $ss_call ss_call matches 30.. run scoreboard players set $ss_call ss_call 0
execute if score $ss_call ss_call matches 0 if score $ss_clock ss_clock matches 0 as @a run function shadowslave:carrier

# Grant the verification tree's root so the tab renders before anything has been earned.
# Minecraft draws nothing for a tree with no completed advancement, so without this the tab
# looks broken on a fresh world. The selector only matches players who lack it.
execute if score $ss_clock ss_clock matches 0 run advancement grant @a[advancements={shadowslave:test/root=false}] only shadowslave:test/root
