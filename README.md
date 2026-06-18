# ambient-link-google

Ambient Link surface for **Google Android XR** display glasses.

This is the Google sibling of [`ambient-link-meta`](https://github.com/maceip/ambient-link-meta).
It renders live coding-agent sessions (Cursor / Claude / Codex) from the Ambient Link
relay onto Android XR display glasses, using the **Jetpack XR SDK**.

> Status: **scaffold / placeholder**. The wiring targets the real Jetpack XR APIs
> (Developer Preview) but artifact versions and device behavior must be pinned
> against the SDK release you build with. Search markers: `TODO(xr)`.

## How Android XR glasses apps work

Unlike XR headsets (which run a full APK), **audio/display glasses use a dedicated
`Activity` that runs inside your phone app and is _projected_ to the glasses**.

- Declare the activity with `android:requiredDisplayCategory="android.hardware.display.category.XR_PROJECTED"`.
- Make it discoverable by Gemini with the `android.intent.category.XR_PROJECTED_LAUNCHER` category.
- Inspect the projected device via `ProjectedDeviceController` and check
  `CAPABILITY_VISUAL_UI` before drawing.
- Build the UI with **Jetpack Compose Glimmer** (transparent, glanceable, glasses-optimized).
- Launch with `ProjectedContext.createProjectedActivityOptions(...)`.

Docs: https://developer.android.com/develop/xr/jetpack-xr-sdk/glasses/first-activity

## Layout

```
app/src/main/
  AndroidManifest.xml                      # XR_PROJECTED projected activity
  java/com/ambientlink/glasses/
    GlassesMainActivity.kt                 # projected activity + capability gate
    ui/HomeScreen.kt                       # Glimmer session list UI
    data/RelayClient.kt                    # polls the Ambient Link relay
    data/Session.kt                        # session model
```

## Relay

Reuses the existing Ambient Link relay (same one the Meta app reads):

```
GET https://public.computer/ambient-link/status   ->  { sessions: [...] }
```

Override with `AMBIENT_LINK_RELAY` at build time if you run your own.

## Build

```bash
./gradlew :app:assembleDebug
```

Requires Android Studio with the Android XR / AI Glasses emulator (Developer
Preview 3+) or physical display glasses. Pin the Jetpack XR + Glimmer versions in
`app/build.gradle.kts` (see `TODO(xr)`).
