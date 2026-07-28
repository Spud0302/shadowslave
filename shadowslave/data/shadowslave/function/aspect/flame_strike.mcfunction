# "Strikes burn" — the half of the Flame Aspect the spec promised and Phase 1 shipped without.
# Runs as and at the player, from the player_hurt_entity advancement reward.

# Revoke immediately so the trigger re-arms for the next hit.
advancement revoke @s only shadowslave:flame_strike

# Only Flame Awakened. Everyone else hits things without setting them alight.
execute unless entity @s[tag=ss_aspect_flame] run return 0

# The trigger does not hand us the victim, so take the nearest non-player entity in front
# of the player. Approximate, but it only fires on a real hit, which is what matters.
# ponytail: nearest-in-front heuristic; if it ever mis-targets in a crowd, the fix is an
# ponytail: interaction entity or a marker, not a better selector
execute anchored eyes positioned ^ ^ ^2 run data merge entity @e[type=!player,distance=..3,sort=nearest,limit=1] {Fire:100s}
