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
# Health sampled each tick while in the nightmare
scoreboard objectives add ss_health dummy "Health"
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

tellraw @a {"text":"[Shadow Slave] The Spell stirs.","color":"dark_gray","italic":true}
