# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Kotlin Multiplatform project named **AquaNest** targeting Android and iOS, with shared UI built on Compose Multiplatform. The shared package is `org.example.project`; the Android `applicationId` and iOS bundle ID are templated and rewritten by the publish pipeline (see below) before release.

## Build & run

- Android debug build: `./gradlew :androidApp:assembleDebug`
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run, or use `./build_unsigned_ipa.command` (double-click) to produce an unsigned `AquaNest.ipa` at the repo root via `xcodebuild archive` + manual `Payload/` zip.
- Tests:
  - `./gradlew :shared:testAndroidHostTest` (JVM host tests for `androidHostTest`)
  - `./gradlew :shared:iosSimulatorArm64Test` (iOS simulator tests)
  - `commonTest` runs as part of both target test suites.
- Dependency versions are centralized in `gradle/libs.versions.toml` (AGP `com.android.kotlin.multiplatform.library`, Kotlin `2.3.21`, Compose Multiplatform `1.11.0`, Material3 `1.11.0-alpha07`, Android min/target/compile SDK 24/36/36). Touch this catalog rather than individual `build.gradle.kts` files when bumping versions.

## Module layout & shared-code architecture

Two Gradle modules plus an Xcode project:

- `:shared` — the only place real UI/logic lives. Uses the `androidLibrary { ... }` DSL (not the legacy Android plugin) and exposes an `iosArm64` + `iosSimulatorArm64` static framework named `Shared`. Source sets: `commonMain`, `androidMain`, `iosMain`, plus `commonTest`, `androidHostTest`, `iosTest`. Compose resources live in `shared/src/commonMain/composeResources/` and are accessed via the generated `aquanest.shared.generated.resources.Res`.
- `:androidApp` — thin Compose Activity host (`MainActivity` → `setContent { App() }`). Also force-hides system bars in `onResume`/`onWindowFocusChanged`.
- `iosApp/` — Xcode project. `iOSApp.swift` is the SwiftUI entry; `ContentView.swift` wraps the Kotlin `MainViewController()` (defined in `shared/src/iosMain/.../MainViewController.kt`) in a `UIViewControllerRepresentable`. The framework is consumed as `import Shared`.

Shared composable entry point is `App()` in `shared/src/commonMain/.../App.kt`. It delegates everything to a `Gray` state-router composable with three slots: `loading`, `noInternet(onRetry)`, and `white` (the actual app, currently `AppNavGraph`).

### expect/actual contract

Two `expect` declarations drive the platform split, both following the same pattern (`commonMain` declares, `androidMain` + `iosMain` implement):

- `Gray(loading, noInternet, white)` — state router that chooses which slot to render. **Both `actual` implementations are currently empty stubs**, so the visible app tree is effectively nothing until they are filled in. Treat this as the central place to wire connectivity/loading state per platform.
- `PlatformWebView(url, modifier)` — Android impl uses `AndroidView { WebView }` with `javaScriptEnabled = true`; iOS impl uses `UIKitView { WKWebView }` loading an `NSURLRequest`.

When adding new platform-specific capability, follow this expect-in-commonMain / actual-in-each-platform pattern rather than reaching for interfaces + DI.

## Release pipeline (iOS-focused)

Releases are driven by Codemagic + a custom Python automation in `scripts/publish/`. Understanding this matters before editing `codemagic.yaml`, `iosApp/Configuration/Config.xcconfig`, `iosApp/fastlane/Fastfile`, `gradle.properties`, or `iosApp/iosApp.xcodeproj/project.pbxproj` — those files are programmatically rewritten on each release.

### Codemagic workflows (`codemagic.yaml`)

- `ios_kmp_release` — archive + sign IPA (`xcode-project build-ipa` against `iosApp/iosApp.xcodeproj`, scheme `iosApp`), submit to TestFlight via App Store Connect API key.
- `upload_ios_metadata` — generates `fastlane/metadata/` from a Google Sheet via `iosApp/scripts/import_fastlane_metadata_from_gsheet.py`, then `bundle exec fastlane ios upload_metadata`.

Both workflows read env vars from the Codemagic group named after the Jira ticket number (templated as `"9999"` in the committed YAML — the publish script swaps it).

### `scripts/publish/` (entry: `python -m scripts.publish`, optional `-y`)

End-to-end iOS publishing automation. The README at `scripts/publish/README.md` is the authoritative guide; key points for working in this repo:

- Requires `pip install -r scripts/requirements.txt` (incl. `claude-agent-sdk`, which calls `claude` CLI under `claude-sonnet-4-6` to generate App Store copy).
- Reads secrets from a **sibling** `../Utils/local.properties` (Jira creds + Google service account path). The location is hard-coded as `UTILS_LOCAL_PROPS` in `scripts/publish/config.py`.
- Derives the Jira ticket number from the leading digits of the project folder name (e.g. `1234-aqua-nest/` → ticket `1234`). The folder name `Aqua Nest` here has no leading digits, so the script will prompt.
- Cache file `scripts/.publish-cache.json` (gitignored) persists GitHub PAT, Codemagic token, and Telegraph token between runs.
- The script edits `Config.xcconfig` (TEAM_ID, bundle ID), `project.pbxproj`, `codemagic.yaml` (env group `"9999"` → real ticket number), `Fastfile`, and the `#Gradle` section of `gradle.properties`. **Avoid hand-editing these files in ways that the regex-based rewrites might not survive.**

## Known empty/stub state

These are intentionally minimal scaffolding, not bugs to "fix" unsolicited:

- `Gray.android.kt` / `Gray.ios.kt` — empty `actual` bodies.
- `AppNavGraph()` — single centered `Text("Home")`.
- `LoadingScreen()` / `NoInternetScreen()` — placeholder texts.
- `Config.xcconfig` has an empty `TEAM_ID=` line — populated by the publish script per release.
