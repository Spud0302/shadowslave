# Sunscorched — the sun is an enemy.
#
# PROTOTYPE-LIMIT: the function/id keeps the legacy `shadow_slave` name for save and test
# compatibility. That is NOT the Flaw's player-facing name: Shadow Slave is Sunny's canon
# Divine Aspect. The release displays this invented placeholder as Sunscorched until Flaws
# are generated personally rather than selected from a fixed list.
#
# Damage type is `magic`, not `on_fire`: the Flame Aspect grants fire resistance, and a
# Flame + Sunscorched roll would otherwise cancel the Flaw entirely, leaving that player
# with a power and no price. Flaws are the price.
execute if predicate shadowslave:in_sunlight run damage @s 1 minecraft:magic
execute if predicate shadowslave:in_sunlight run effect give @s minecraft:weakness 2 0 true
