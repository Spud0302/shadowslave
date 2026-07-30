# Burdened — distance bought during the trial follows you home.
#
# The fled/retreat family used to reduce safe_fall_distance. Minecraft 1.21.1 intermittently ignored
# that modifier even when the command executed successfully, so the prototype mechanic was not reliable
# enough for the completed datapack. The family contract matters more than that specific implementation:
# behaviour during the Nightmare earns a persistent, real drawback.
#
# Slowness I is deliberately simple and robust. Upkeep refreshes it once per second, so milk can clear it
# only momentarily. Java is expected to replace this finite datapack family with a proper FlawInstance.
effect give @s minecraft:slowness 2 0 true
