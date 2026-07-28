# Shadow Slave — the sun is an enemy.
execute if predicate shadowslave:in_sunlight run damage @s 1 minecraft:on_fire
execute if predicate shadowslave:in_sunlight run effect give @s minecraft:weakness 2 0 true
