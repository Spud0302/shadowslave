# /function shadowslave:test/help — list the testing commands.
#
# Note: each id is written whole in a single text component. Splitting the namespace from
# the name reads the same in chat but leaves a bare `function shadowslave:test/` in the
# file, which the validator correctly flags as an unresolvable reference.

tellraw @s [{"text":"\n"},{"text":"— Shadow Slave: testing commands —","color":"light_purple","bold":true}]
tellraw @s [{"text":"shadowslave:test/help","color":"aqua"},{"text":"        this list","color":"gray"}]
tellraw @s [{"text":"shadowslave:test/selfcheck","color":"aqua"},{"text":"   assert the pack loaded correctly","color":"gray"}]
tellraw @s [{"text":"shadowslave:test/infect","color":"aqua"},{"text":"      become a Carrier now","color":"gray"}]
tellraw @s [{"text":"shadowslave:test/cure","color":"aqua"},{"text":"        back to untouched","color":"gray"}]
tellraw @s [{"text":"shadowslave:test/nightmare","color":"aqua"},{"text":"   enter the trial immediately","color":"gray"}]
# Historical command id retained for compatibility: Phase 1 completion is Sleeper/Dormant, not Awakening.
tellraw @s [{"text":"shadowslave:test/awaken","color":"aqua"},{"text":"      skip to Sleeper + placeholder roll","color":"gray"}]
tellraw @s [{"text":"shadowslave:test/reset","color":"aqua"},{"text":"       wipe everything, start over","color":"gray"}]
tellraw @s [{"text":"\nRun them with ","color":"gray"},{"text":"/function <id>","color":"white"},{"text":".  Skip the countdown mid-trial with ","color":"gray"},{"text":"/scoreboard players set @s ss_timer 1","color":"white"},{"text":"\n","color":"gray"}]
