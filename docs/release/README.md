# Release

Two independent tag driven releases. Both create a draft GitHub release.

| Component   | Tag              | Workflow          | Artifact                                       |
| ----------- | ---------------- | ----------------- | ---------------------------------------------- |
| Android app | `android-vX.Y.Z` | `release-app.yml` | signed `aruco-mapper-X.Y.Z.apk`                |
| API         | `api-vX.Y.Z`     | `release-api.yml` | source archive `aruco-mapper-api-X.Y.Z.tar.gz` |

## New release

1. Merge your changes to `main` with passing CI.
2. Add the version and changes to `CHANGELOG.md`.
3. API only: set the same version in `api/pyproject.toml`.
4. Tag and push. E. g.:

   ```bash
   git tag -a android-v0.1.1 -m "Android 0.1.1" && git push origin android-v0.1.1
   git tag -a api-v0.1.1 -m "API 0.1.1" && git push origin api-v0.1.1
   ```

5. Open the GitBub workflow run and publish the draft release.
