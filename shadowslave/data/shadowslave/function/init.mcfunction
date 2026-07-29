# Shadow Slave — one-time setup, runs on every /reload and world load.

# Rank: 0 = Sleeper, 1 = Awakened
scoreboard objectives add ss_rank dummy "Soul Rank"
# Nightmare countdown, in ticks
scoreboard objectives add ss_timer dummy "Nightmare Timer"
# Rolled Aspect and Flaw, 1-4 each
scoreboard objectives add ss_aspect dummy "Aspect"
scoreboard objectives add ss_flaw dummy "Flaw"
# Scratch space for /random rolls
scoreboard objectives add ss_roll dummy "Roll"
# Global once-a-second counter, held on the fake player $ss_clock
scoreboard objectives add ss_clock dummy "Clock"
# Counts seconds between the Spell's calls to its Carriers
scoreboard objectives add ss_call dummy "Spell Call"
# Seconds until the Spell will take a Carrier again. While this is running they sleep
# normally — the mod stops hijacking every night, and an ejected player cannot bounce
# straight back in at the low health that ejected them.
scoreboard objectives add ss_cooldown dummy "Spell Cooldown"
# Scratch for the soul readout. Deliberately separate from ss_health, which the ejection
# check reads — sharing them once made reading your soul lock you out of the mod.
scoreboard objectives add ss_scratch_a dummy "Scratch A"
scoreboard objectives add ss_scratch_b dummy "Scratch B"
# Health sampled each tick while in the nightmare
scoreboard objectives add ss_health dummy "Health"
# Consecutive ticks the creature has been absent — guards against a rejoin, where the
# entity's chunk has not deserialized yet, being misread as "the creature died".
scoreboard objectives add ss_gone dummy "Creature Absent"
# Overworld return position
scoreboard objectives add ss_ret_x dummy "Return X"
scoreboard objectives add ss_ret_y dummy "Return Y"
scoreboard objectives add ss_ret_z dummy "Return Z"
# Player-facing readout, enabled per-player in tick
scoreboard objectives add soul trigger "Soul"

# The trial bossbar. Global, which is fine for singleplayer.
# ponytail: one shared bossbar — per-player bars need macro-generated ids, add in Phase 6 multiplayer
bossbar add shadowslave:trial {"text":"The Nightmare Spell","color":"dark_purple"}
bossbar set shadowslave:trial color purple
bossbar set shadowslave:trial style notched_10
# Bossbars survive /reload. tick_player.mcfunction re-shows this for anyone
# still mid-trial, since a server restart fires this before any player joins.
bossbar set shadowslave:trial visible false

# Announce the version on every load and /reload. Datapacks have no variables, so this
# literal and the one in pack.mcmeta are maintained by hand — validate.py asserts they
# agree, because a version string that lies is worse than none at all.
tellraw @a [{"text":"[Shadow Slave] ","color":"dark_gray"},{"text":"v1.4.6","color":"light_purple","bold":true},{"text":" — The Spell stirs.","color":"dark_gray","italic":true},{"text":"  (testing commands: ","color":"dark_gray"},{"text":"shadowslave:test/help","color":"aqua"},{"text":")","color":"dark_gray"}]
