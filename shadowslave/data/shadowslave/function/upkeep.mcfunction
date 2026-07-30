# Runs as and at each Sleeper player, once per second.

execute if entity @s[tag=ss_aspect_shadow] run function shadowslave:aspect/shadow
execute if entity @s[tag=ss_aspect_flame] run function shadowslave:aspect/flame
execute if entity @s[tag=ss_aspect_bone] run function shadowslave:aspect/bone
execute if entity @s[tag=ss_aspect_wind] run function shadowslave:aspect/wind

execute if entity @s[tag=ss_flaw_shadow_slave] run function shadowslave:flaw/shadow_slave
execute if entity @s[tag=ss_flaw_fragile] run function shadowslave:flaw/fragile
execute if entity @s[tag=ss_flaw_ravenous] run function shadowslave:flaw/ravenous
execute if entity @s[tag=ss_flaw_weightless] run function shadowslave:flaw/weightless

execute if entity @s[tag=ss_aspect_shadow] run advancement grant @s only shadowslave:test/aspect_live
execute if entity @s[tag=ss_aspect_flame] run advancement grant @s only shadowslave:test/aspect_live
execute if entity @s[tag=ss_aspect_bone] run advancement grant @s only shadowslave:test/aspect_live
execute if entity @s[tag=ss_aspect_wind] run advancement grant @s only shadowslave:test/aspect_live
execute if entity @s[tag=ss_flaw_shadow_slave] run advancement grant @s only shadowslave:test/flaw_live
execute if entity @s[tag=ss_flaw_fragile] run advancement grant @s only shadowslave:test/flaw_live
execute if entity @s[tag=ss_flaw_ravenous] run advancement grant @s only shadowslave:test/flaw_live
execute if entity @s[tag=ss_flaw_weightless] run advancement grant @s only shadowslave:test/flaw_live
