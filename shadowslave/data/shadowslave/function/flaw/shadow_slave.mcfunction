# Historical INTERNAL id for the daylight-burden Flaw family.
#
# Do not expose "Shadow Slave" as this Flaw's formal name: canonically Shadow Slave is Sunny's
# Aspect. The function/tag path stays for save/mechanics compatibility while generated identities
# such as Nightbound or Pale Dawn provide the player-facing Flaw name.
#
# Damage type is `magic`, not `on_fire`: the ember-root Aspect grants fire resistance, and an
# ember-root + daylight-burden combination would otherwise cancel the Flaw entirely, leaving that
# player with a power and no price. Flaws are the price.
execute if predicate shadowslave:in_sunlight run damage @s 1 minecraft:magic
execute if predicate shadowslave:in_sunlight run effect give @s minecraft:weakness 2 0 true
