# External build provenance

`provenance.json` inside the deterministic archive is self-reported by that archive. It is useful for integrity checks, but it cannot authenticate the publisher or bind bytes to a repository build by itself.

`modpack/tools/build_provenance.py` defines a separate schema-1 statement that binds:

- the exact `owner/repository`;
- a full 40-character source commit SHA;
- GitHub Actions workflow run ID and attempt;
- uploaded artifact ID and name;
- archive file name and SHA-256;
- input Shadow Slave core JAR SHA-256.

The statement is intentionally outside the archive it describes. Verification requires the caller to supply the expected repository, commit, workflow run ID and attempt, plus artifact ID and name from an independently trusted GitHub page or API response. The verifier recomputes both file digests and fails closed on unknown fields, malformed identifiers or any mismatch.

## Create

```bash
python3 modpack/tools/build_provenance.py create \
  --archive nightmare-spell-modpack-dev.zip \
  --core-jar shadowslave-core.jar \
  --repository Spud0302/shadowslave \
  --commit-sha "$GITHUB_SHA" \
  --workflow-run-id "$GITHUB_RUN_ID" \
  --workflow-run-attempt "$GITHUB_RUN_ATTEMPT" \
  --artifact-id 123456789 \
  --artifact-name nightmare-spell-modpack \
  --output build-provenance.json
```

The artifact ID is assigned only after GitHub accepts an upload. The shell workflow therefore:

1. builds and verifies the deterministic package;
2. uploads that package with a named `actions/upload-artifact` step;
3. reads the action's returned `artifact-id` output;
4. creates and immediately verifies the external statement using that real ID;
5. uploads the statement as a separate artifact.

For pull-request runs, the statement records the pull request head SHA rather than GitHub's synthetic merge commit. Push and manual runs use `github.sha`. The workflow never invents a placeholder artifact ID.

The provenance artifact is deliberately separate from the package artifact. Uploading the statement changes neither the already-created package artifact nor the package ID recorded by the statement.

## Verify

```bash
python3 modpack/tools/build_provenance.py verify \
  --statement build-provenance.json \
  --archive nightmare-spell-modpack-dev.zip \
  --core-jar shadowslave-core.jar \
  --expected-repository Spud0302/shadowslave \
  --expected-commit <40-character-commit> \
  --expected-run-id <workflow-run-id> \
  --expected-run-attempt <workflow-run-attempt> \
  --expected-artifact-id <artifact-id> \
  --expected-artifact-name <artifact-name>
```

The run attempt and artifact name are part of the externally checked identity. Recording them without requiring independent expected values would allow a statement to describe a different rerun or a different artifact label while still passing verification.

## Evidence classification

- **DESIGN:** schema fields, publication order, strict validation and external expected-value verification.
- **COMPATIBILITY:** the packaged Java core remains the sole canonical Shadow Slave state owner.
- No lore-sensitive runtime mechanic changes; no new **CANON**, **INFERRED** or **UNKNOWN** claim is introduced.

## Deliberate limits

A matching unsigned statement proves that the supplied files agree with the supplied repository/build identity. It does not prove that the expected values came from a trusted source, that GitHub executed reviewed source, that dependencies were uncompromised, or that the statement was not replaced together with the files.

The separate provenance upload has its own GitHub artifact ID, which schema 1 does not record. The package artifact ID remains the identity being bound. The workflow also packages a fixture core JAR; it is CI evidence for the publication contract, not a public modpack release.

Cryptographic signing or GitHub artifact attestations, protected release environments, immutable release publication and independently reproducible core builds remain separate work. Until those exist, this statement is a precise provenance contract, not a signature or authenticity claim.
