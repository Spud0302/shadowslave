# Modpack archive verification

Run the verifier after building or downloading a repository package:

```bash
python3 modpack/tools/verify_package.py build/nightmare-spell-modpack-dev.zip
```

The verifier fails closed unless all of the following hold:

- the input is a valid ZIP with unique, safe, canonical entry names;
- entries are lexicographically ordered;
- every entry uses the fixed timestamp, Unix creator metadata, regular-file permissions and deflate compression required by `deterministic-zip-v1`;
- `provenance.json` is valid UTF-8 JSON using schema version 1;
- provenance paths are unique and sorted;
- provenance describes every payload entry and no absent entry;
- every payload byte length and SHA-256 matches its provenance record;
- provenance does not recursively claim to hash itself.

A successful result proves that the archive is internally consistent with its embedded provenance and deterministic package-format contract. It does **not** prove that the supplied Java core JAR was independently reviewed, compiled from a particular source commit, signed by the project owner, free of malicious code, or reproducible across toolchains. Those require an external trusted digest/signature and build provenance in a later slice.

This verification tooling is Minecraft **DESIGN** and build infrastructure. It changes no lore-sensitive behavior and makes no CANON, INFERRED or UNKNOWN claim. Preserving the Java core as sole canonical state owner remains a COMPATIBILITY architecture rule.
