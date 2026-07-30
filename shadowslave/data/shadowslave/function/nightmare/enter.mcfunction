# Single First-Nightmare entry choke point. Runs as and at the player from sleep, sneak entry,
# testing wrappers, or any future caller. Eligibility belongs here rather than being trusted to
# every caller independently.

# Revoke immediately so the slept_in_bed trigger can fire again on a later sleep.
advancement revoke @s only shadowslave:enter_nightmare

# A Sleeper has already survived a First Nightmare and cannot enter another one. Phase 1 does
# not implement the later Dream Realm journey yet, so ss_rank 1 is the terminal progression state.
execute if score @s ss_rank matches 1.. run advancement grant @s only shadowslave:test/bypass
execute if score @s ss_rank matches 1.. run return 0

# Guard against duplicate entry while an existing trial is active.
execute if entity @s[tag=ss_in_nightmare] run return 0

# The normal First Nightmare requires infection first. Historically this invariant lived only in
# sleep.mcfunction and the sneak selector, so a direct call to this function could put an untouched
# player into a First Nightmare. Keep eligibility at the choke point. test/nightmare explicitly adds
# ss_carrier before calling here, so tests do not need a special exception.
execute unless entity @s[tag=ss_carrier] run tellraw @s {"text":"The Spell has not marked you yet.","color":"dark_gray","italic":true}
execute unless entity @s[tag=ss_carrier] run return 0

# The Spell is still spent.
#
# Both this and the weakness gate below are skipped for ss_test_bypass. A testing command
# that gets refused by the system it exists to test is useless, and duplicating the entry
# logic in test/ to dodge the guards would be worse — the guards stay here, the override is
# explicit and single-use.
#
# This guard used to live only in the callers — sleep.mcfunction and the sneak check — which
# meant any other path bypassed it entirely. Both player routes were covered, so it was
# invisible in play, but it is the same fragility as guarding in callers instead of at the
# choke point. Every entry passes through here.
execute unless entity @s[tag=ss_test_bypass] if score @s ss_cooldown matches 1.. run tellraw @s {"text":"The Spell is spent. It will come for you again.","color":"dark_gray","italic":true}
execute unless entity @s[tag=ss_test_bypass] if score @s ss_cooldown matches 1.. run return 0

# Too weak to be taken.
#
# This replaces the free instant_health that used to fire on arrival. That heal existed to
# stop an entry loop — walk in at 2 hearts, get ejected on the first tick, repeat — but it
# also meant ejection cost nothing at all: thrown out at 2 hearts, straight back in at full.
# Refusing entry instead makes failure expensive. You have to actually recover.
#
# 10 (5 hearts) leaves a hit of margin above the ejection threshold of 4, so you cannot enter
# and be ejected again immediately. Both dropped in 1.4.5 on Andrew's call: the fight wanted
# to run longer. The trade is real deaths — from 5 hearts a full creature hit kills outright
# rather than ejecting, so death is now the ordinary failure and ejection the near miss.
# Item recovery on death is confirmed working, which is what makes that acceptable.
# ponytail: own scratch objective, NOT ss_health — the ejection check reads that one
scoreboard players reset @s ss_scratch_a
execute store result score @s ss_scratch_a run data get entity @s Health
execute unless entity @s[tag=ss_test_bypass] if score @s ss_scratch_a matches ..9 run tellraw @s {"text":"You are too weak. The Spell has no use for you yet — recover, and it will come.","color":"dark_gray","italic":true}
execute unless entity @s[tag=ss_test_bypass] if score @s ss_scratch_a matches ..9 run return 0

# Bypass consumed. It only ever survives one entry, so a stray tag cannot silently disable
# the gates for the rest of a session — which is exactly the kind of thing that has bitten
# this pack before.
tag @s remove ss_test_bypass

# Remember where to put them back.
execute store result score @s ss_ret_x run data get entity @s Pos[0]
execute store result score @s ss_ret_y run data get entity @s Pos[1]
execute store result score @s ss_ret_z run data get entity @s Pos[2]

tag @s add ss_in_nightmare
# 1800 ticks = 90 seconds. Was 6000 (five real minutes), which read as waiting rather than
# dread — the dark has said everything it has to say well before then, and the trial does not
# actually begin until the creature lands. Ticked down once per tick at 20 tps, so this value
# and the bossbar max below must stay in step or the bar drains at the wrong rate.
scoreboard players set @s ss_timer 1800
scoreboard players set @s ss_gone 0

# Pull them in. Teleporting wakes the player out of the bed.
execute in shadowslave:nightmare run tp @s 0 120 0
execute in shadowslave:nightmare run spreadplayers 0 0 200 400 false @s

bossbar set shadowslave:trial max 1800
bossbar set shadowslave:trial value 1800
bossbar set shadowslave:trial name {"text":"The Nightmare Spell","color":"light_purple"}
bossbar set shadowslave:trial color purple
bossbar set shadowslave:trial visible true
bossbar set shadowslave:trial players @s

execute at @s run playsound minecraft:ambient.cave ambient @s ~ ~ ~ 1 0.5
title @s times 20 60 20
title @s subtitle {"text":"Survive.","color":"gray"}
title @s title {"text":"The Nightmare Spell","color":"dark_purple","bold":true}

advancement grant @s only shadowslave:test/root
advancement grant @s only shadowslave:test/chosen
