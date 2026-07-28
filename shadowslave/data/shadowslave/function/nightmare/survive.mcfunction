# The creature is dead. Runs as the player.

function shadowslave:nightmare/leave
function shadowslave:awaken/roll

# Same stale-context trap as eject: `leave` teleported the player, so `at @s` is required.
execute at @s run playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1 1
title @s times 20 80 30
title @s subtitle {"text":"You are Awakened.","color":"gray"}
title @s title {"text":"The Nightmare Ends","color":"light_purple","bold":true}
