# Burdened — distance bought during the trial follows you home.
#
# The fled/retreat family used to reduce safe_fall_distance. Minecraft 1.21.1 intermittently ignored
# that modifier even when the command executed successfully, so the prototype mechanic was not reliable
# enough for the completed datapack. The family contract matters more than that specific implementation:
# behaviour during the Nightmare earns a persistent, real drawback.
#
# Slowness I is deliberately simple and robust. Upkeep refreshes it once per second (1000ms), so milk can
# clear it only momentarily. Duration is 1200 ticks (60 seconds) to survive until the next refresh even if
# the player delays between upkeep cycles.
effect give @s minecraft:slowness 1200 0 true
