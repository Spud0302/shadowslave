# Compatibility entry point for the Phase 1 transition.
#
# Historical name: this used to mean "First Nightmare -> Awakened". Canon research established
# that surviving the First Nightmare produces a Sleeper/Dreamer with a Dormant soul rank instead.
# Keep this function as a thin alias for now because existing test commands and documentation may
# still call it; new gameplay code should call progression/become_sleeper directly.
#
# PROTOTYPE-LIMIT: remove this alias once callers have migrated and the compatibility value is gone.

function shadowslave:progression/become_sleeper
