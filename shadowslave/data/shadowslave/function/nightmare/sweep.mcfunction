# Scheduled 5 ticks after a teardown, by which point the death drops exist.
#
# `schedule` cannot pass storage to a macro function, so this plain function exists purely to
# hand off to one that can.
function shadowslave:nightmare/sweep_move with storage shadowslave:ret
