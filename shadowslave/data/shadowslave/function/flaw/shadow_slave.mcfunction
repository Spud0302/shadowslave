# Shadow Slave — the sun is an enemy.
# Damage type is `magic`, not `on_fire`: the Flame Aspect grants fire resistance, and a
# Flame + Shadow Slave roll would otherwise cancel the Flaw entirely, leaving that player
# with a power and no price. Flaws are the price.
execute if predicate shadowslave:in_sunlight run damage @s 1 minecraft:magic
execute if predicate shadowslave:in_sunlight run effect give @s minecraft:weakness 2 0 true
