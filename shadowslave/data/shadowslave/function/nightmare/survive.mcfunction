# The creature is dead. Runs as the player.

function shadowslave:nightmare/leave

# Surviving the First Nightmare makes the player a Sleeper/Dreamer with a Dormant soul rank.
# Do not route normal gameplay through the historical awaken/roll compatibility alias.
function shadowslave:progression/become_sleeper

# Same stale-context trap as eject: `leave` teleported the player, so `at @s` is required.
execute at @s run playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1 1
title @s times 20 80 30
title @s subtitle {"text":"You are a Sleeper.","color":"gray"}
title @s title {"text":"The Nightmare Ends","color":"light_purple","bold":true}

advancement grant @s only shadowslave:test/slayer
