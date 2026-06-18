# ambient-link-google

Ambient Link surface for **Google Android XR** display glasses.

This is the Google sibling of [`ambient-link-meta`](https://github.com/maceip/ambient-link-meta).
It renders live coding-agent sessions (Cursor / Claude / Codex) from the Ambient Link
relay onto Google wearables:

- **`:app`** — Android XR display **glasses** (projected activity + Jetpack Compose Glimmer).
- **`:wear`** — **Wear OS watch** (standalone, Wear Compose Material3) — the glanceable
  "an agent needs you" companion on the wrist.

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
app/src/main/                              # Android XR glasses
  AndroidManifest.xml                      # XR_PROJECTED projected activity
  java/com/ambientlink/glasses/
    GlassesMainActivity.kt                 # projected activity + capability gate
    ui/HomeScreen.kt                       # Glimmer session list UI
    data/RelayClient.kt                    # polls the Ambient Link relay
    data/Session.kt                        # session model

wear/src/main/                             # Wear OS watch
  AndroidManifest.xml                      # standalone watch app
  java/com/ambientlink/watch/
    WatchActivity.kt                       # ComponentActivity + relay lifecycle
    ui/SessionListScreen.kt                # Wear Compose ScalingLazyColumn + TitleCard
    data/RelayClient.kt                    # polls the relay (longer interval for battery)
    data/Session.kt                        # session model
```

> Both modules currently carry their own `RelayClient`/`Session` copies. These are
> identical and tagged `TODO(shared)` — they should collapse into a shared
> `:core-android` library (tracked in `ambient-link-core`) so glasses, watch, and
> phone consume one relay implementation.

## Wear OS watch

The watch is a **standalone** Wear app (`com.google.android.wearable.standalone=true`)
so it works without the phone app: it polls the relay directly over the watch's own
Wi-Fi/LTE. UI is **Wear Compose Material3** — `ScalingLazyColumn` for the curved,
rotary-scrollable list and `TitleCard` rows with a per-agent accent dot (matching the
glasses + web accent palette). Build it as a separate run config:

```bash
./gradlew :wear:assembleDebug
```

Pin the Wear Compose versions in `wear/build.gradle.kts` (`TODO(wear)`). For
phone-tethered watches, the Wearable Data Layer path (proxy through the phone) is
stubbed but not wired.

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
