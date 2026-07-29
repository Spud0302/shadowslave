# Runs as and at the player, from the slept_in_bed advancement reward.
#
# Three states, not two. A fresh player is untouched — no nausea, no calling, nothing until
# the Spell has actually noticed them. Being a Carrier from the moment you spawn is both
# unplayable (nausea before you can even build a bed) and wrong: in the novel, infection is
# an event that happens to you.
#
#   uninfected  -> sleeps once, normally. The Spell marks them.
#   Carrier     -> feels the calling; sleeping or sneaking on a bed takes them under.
#   Awakened    -> has already answered it; sleeps normally forever after.

# Re-arm the trigger for next time.
advancement revoke @s only shadowslave:enter_nightmare

# The Awakened sleep like anyone else.
execute if score @s ss_rank matches 1.. run advancement grant @s only shadowslave:test/bypass
execute if score @s ss_rank matches 1.. run return 0

# Spent Spell — sleep normally. This is the whole point of the cooldown: after it throws you
# out you get your game back, and it comes for you again later.
execute if score @s ss_cooldown matches 1.. run title @s actionbar {"text":"You sleep, and nothing reaches for you.","color":"dark_gray","italic":true}
execute if score @s ss_cooldown matches 1.. run return 0

# First sleep: the Spell notices. Let them wake normally this once.
#
# `return run` matters. `infect` adds the ss_carrier tag, so a separate
# `execute unless entity @s[tag=ss_carrier] run return 0` on the next line would
# already be false and fall straight through into the nightmare. Running the call
# and the return as one command is what makes the branch actually branch.
execute unless entity @s[tag=ss_carrier] run return run function shadowslave:infect

# Already a Carrier — this is the night it takes them.
function shadowslave:nightmare/enter
