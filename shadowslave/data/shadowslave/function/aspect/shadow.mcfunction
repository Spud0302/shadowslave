# Shadow — sees in the dark, and moves faster within it.
effect give @s minecraft:night_vision 15 0 true
execute if predicate shadowslave:in_darkness run effect give @s minecraft:speed 2 1 true
