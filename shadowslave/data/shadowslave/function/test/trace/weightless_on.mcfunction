# Arm the Q4 Weightless trace. Diagnostic only — inert until this tag exists.
#
# Exists so the tag is applied from INSIDE the pack. The harness previously ran `/tag @s add` itself,
# which meant validate.py correctly reported ss_test_trace_weightless as "tested but never applied":
# the pack referenced a tag nothing in the pack ever set. That check has real value — it catches
# misspelled and dead tags, which fail silently in Minecraft — so the fix is to give the trace a
# legitimate entry point rather than to add a validator exception.
#
# ss_scratch_a counts how many times flaw/weightless executed; ss_scratch_b holds the resulting
# safe_fall_distance * 1000, so the harness can ask what the SERVER saw rather than inferring it from
# chat round-trips that perturb the timing being measured.
#
# ponytail: shared scratch objectives, deliberately, to avoid adding two more for a temporary probe.
# ponytail: the ceiling is real though — nightmare/enter and soul.mcfunction both write @s ss_scratch_a
# ponytail: and ss_scratch_b, so entering a Nightmare or reading the soul will clobber these counters.
# ponytail: harmless while tracing a single isolated Flaw application; if this trace ever needs to
# ponytail: survive a full trial, give it its own objective. Reusing a scratch score across systems is
# ponytail: exactly what caused 1.7 (the soul readout poisoning the ejection check).
scoreboard players reset @s ss_scratch_a
scoreboard players reset @s ss_scratch_b
tag @s add ss_test_trace_weightless
