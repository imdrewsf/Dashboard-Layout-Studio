# Publishing Dashboard Layout Studio Launcher 1.0.13 / build 014

Repository:
https://github.com/imdrewsf/Dashboard-Layout-Studio

## Files that must change

Replace these two files in the repository:

- `apps/dashboard-layout-studio-launcher.groovy`
- `hpm/packageManifest.json`

`hpm/repository.json` is included in this package for reference, but it is unchanged and does not need to be replaced.
The icon files are also unchanged.

## Recommended sequence

1. Update your local clone of the repository.
2. Copy the build-014 launcher and package manifest into the paths above.
3. Review the diff.
4. Commit both files together.
5. Push to `main`.
6. Verify the raw GitHub app file reports `Version: 1.0.13` and `Build: 014`.
7. Verify the raw package manifest reports `"version": "1.0.13"`.
8. Only after both raw files are correct, run HPM's update check on the test hub.

## Windows PowerShell commands

From inside your local `Dashboard-Layout-Studio` repository:

```powershell
git switch main
git pull --ff-only origin main

Copy-Item "PATH-TO-EXTRACTED-BUILD\apps\dashboard-layout-studio-launcher.groovy" ".\apps\dashboard-layout-studio-launcher.groovy" -Force
Copy-Item "PATH-TO-EXTRACTED-BUILD\hpm\packageManifest.json" ".\hpm\packageManifest.json" -Force

git diff -- apps/dashboard-layout-studio-launcher.groovy hpm/packageManifest.json
git status

git add apps/dashboard-layout-studio-launcher.groovy hpm/packageManifest.json
git commit -m "Launcher 1.0.13 build 014: compact Hubitat UI"
git push origin main
```

## Raw URLs to verify after push

Launcher:
https://raw.githubusercontent.com/imdrewsf/Dashboard-Layout-Studio/main/apps/dashboard-layout-studio-launcher.groovy

Package manifest:
https://raw.githubusercontent.com/imdrewsf/Dashboard-Layout-Studio/main/hpm/packageManifest.json

Repository manifest:
https://raw.githubusercontent.com/imdrewsf/Dashboard-Layout-Studio/main/hpm/repository.json

## Updating the test hub through HPM

If the custom repository is already configured, do not add it again.

1. Open Hubitat Package Manager.
2. Run an update/check-for-updates operation.
3. Select Dashboard Layout Studio when HPM reports version 1.0.13.
4. Complete the update.
5. Open Apps > Dashboard Layout Studio.
6. Confirm the launcher reports `Launcher 1.0.13 · build 014`.
7. Confirm DLS itself is still installed and Launch opens `/local/dashboard-layout-studio.html`.
8. Test Remove DLS > Confirm Removal > Install Dashboard Layout Studio once before considering the launcher update complete.

## HPM custom repository URL

https://raw.githubusercontent.com/imdrewsf/Dashboard-Layout-Studio/main/hpm/repository.json

No GitHub Release is required for this launcher update because HPM points directly at the files on the repository's `main` branch.
The DLS HTML release/update process remains separate and unchanged.
