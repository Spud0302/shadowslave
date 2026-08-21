# Claude project entry point

Follow [AGENTS.md](AGENTS.md). It is the single operational protocol for every AI
tool in this repository, and it is deliberately not restated here — one file to
keep correct rather than three that drift.

Start every session with:

```bash
python brain/tools/agent_brief.py
```

Then load one bounded packet from [brain/ai/context/](brain/ai/context) and file
a claim before writing.

Claude's own capability card, including what it cannot verify, is at
[brain/ai/agents/claude-code.md](brain/ai/agents/claude-code.md).
