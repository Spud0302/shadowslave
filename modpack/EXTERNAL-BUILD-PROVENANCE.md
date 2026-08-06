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

1. resolves the source repository and commit that the statement will claim;
2. checks out that exact repository and commit rather than relying on the pull-request merge-ref default;
3. builds and verifies the deterministic package;
4. uploads that package with a named `actions/upload-artifact` step;
5. reads the action's returned `artifact-id` output;
6. creates and immediately verifies the external statement using that real ID;
7. uploads the statement as a separate artifact.

For pull-request runs, checkout and the statement use the pull request head repository plus head SHA. This matters for fork pull requests: `github.repository` names the base repository, while the bytes under review can come from a different head repository. Recording the base repository while checking out a fork head would bind the artifact to a repository/commit pair that does not identify the source that was built. Push and manual runs use `github.repository` and `github.sha`.

The workflow never invents a placeholder artifact ID. Repository and commit alignment are both required for correctness: recording a head SHA while packaging bytes from the default pull-request merge ref, or recording the base repository while packaging a fork head, would misidentify the checked-out source.

The workflow also pins every third-party action to a complete commit SHA. Human-readable release comments remain beside those pins, but mutable major-version tags are not execution identities. The pinned revisions are:

- `actions/checkout` commit `11bd71901bbe5b1630ceea73d27597364c9af683` (`v4.2.2`);
- `actions/upload-artifact` commit `ea165f8d65b6e75b540449e92b4886f43607fa02` (`v4.6.2`).

Pinning closes the bounded risk that the same repository source commit and workflow text could later execute different action code after an upstream tag moves. Updating an action is therefore an explicit reviewed source change.

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

- **DESIGN:** schema fields, checkout/source alignment, fork-source identity, immutable action revisions, publication order, strict validation and external expected-value verification.
- **COMPATIBILITY:** the packaged Java core remains the sole canonical Shadow Slave state owner.
- No lore-sensitive runtime mechanic changes; no new **CANON**, **INFERRED** or **UNKNOWN** claim is introduced.

## Deliberate limits

A matching unsigned statement proves that the supplied files agree with the supplied repository/build identity. It does not prove that the expected values came from a trusted source, that GitHub executed reviewed source, that the pinned action commits or their transitive runtime were uncompromised, or that the statement was not replaced together with the files.

The separate provenance upload has its own GitHub artifact ID, which schema 1 does not record. The package artifact ID remains the identity being bound. The workflow also packages a fixture core JAR; it is CI evidence for the publication contract, not a public modpack release.

Pull-request artifacts, including artifacts built from forks, are review evidence only. They are not releases and do not imply that the base repository owner endorses or published the head repository's code.

Cryptographic signing or GitHub artifact attestations, protected release environments, immutable release publication and independently reproducible core builds remain separate work. Until those exist, this statement is a precise provenance contract, not a signature or authenticity claim.
