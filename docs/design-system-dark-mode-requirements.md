# Design-system dark-mode requirements (for `co.onestep:design-system-kmp`)

**Context.** `onestep-uikit-kmp` added a global light/dark switch
(`OneStepUiKit.setThemeMode(Light|Dark|System)`, default Light). The uikit provides
`LocalOSColors` from `Themes.osLightTheme()` / `Themes.osDarkTheme()` per mode and lets the
design-system components render themselves. On testing (Galaxy S21, dark selected), dark mode
renders as **light-with-pale-text**: white/near-white backgrounds, dark-navy titles, and
brand buttons whose label goes dark. The root cause is in `design-system-kmp` (verified against
1.3.0), not the uikit. This document specifies what the design system must change so the
uikit's switch "just works".

## Evidence (design-system-kmp 1.3.0, `Themes.os*Theme()`, neutral ramp)

Dumped at runtime (`OSColors.neutral_*.value`, ARGB):

```
LIGHT: p3=3e3d3b p2=716d69 p1=8c8884 0=b3b0ad m1=d2d0cf m2=e7e6e6 m3=f0f0ef m4=fbfbfb m5=ffffff
DARK : p3=fbfbfb p2=e7e6e6 p1=d2d0cf 0=b3b0ad m1=8c8884 m2=716d69 m3=f0f0ef m4=fbfbfb m5=ffffff
```

Two defects:

### 1. Dark surface neutrals are still white
In the dark theme the foreground end inverts correctly (`neutral_p3` 3e3d3b→fbfbfb), but the
**surface end does not**: `neutral_m3=f0f0ef`, `neutral_m4=fbfbfb`, `neutral_m5=ffffff` are
identical to light. Since screen/card backgrounds resolve to `neutral_m4`/`neutral_m5`, dark
mode has **no dark background**. The ramp is also non-monotonic in dark (…m2=716d69 then
m3 jumps back to f0f0ef).

**Required:** in the dark theme, `neutral_m3/m4/m5` must be dark so the neutral scale is a
single monotonic ramp (light foreground `p3` → dark surface `m5`). Suggested targets
(tune to brand): `m3 ≈ #333331`, `m4 ≈ #262625`, `m5 ≈ #1A1A19`.

### 2. Surface and "on-primary/on-dark foreground" share the same fields
`OSButton` (design-system) sources its label/on-color from `neutral_m4` / `neutral_p2`
(confirmed in `OSButtonKt` bytecode), and the uikit's brand-button/title styling reads
`neutral_m4` and `primary_p3_main`. But `neutral_m4`/`neutral_m5` are **also** the light
surface colors. So the same token must be simultaneously "light text on a navy button" and
"light card surface". Darkening it for surfaces (defect 1) turns button labels dark; keeping
it light leaves white cards. **No consumer-side palette patch can satisfy both.**

**Required (pick one):**
- **(a) Preferred — add explicit on-color roles:** introduce dedicated tokens for foreground
  on filled/brand surfaces (e.g. `on_primary`, `on_surface`, `on_brand`) that are theme-correct
  independent of the neutral surface scale, and have `OSButton`/`OSText` title styles read them.
  Then surfaces (`neutral_m*`) can be darkened freely.
- **(b) Minimum:** make the dark neutral ramp monotonic (defect 1) **and** change `OSButton`
  (and any title/brand-foreground usage) to derive its on-color from the *foreground* end
  (`neutral_p3`, which is light in dark) rather than the surface end (`neutral_m4`).

### 3. Brand foreground (`primary_p3_main`) stays dark navy in dark
Titles styled with `primary_p3_main` (e.g. permission screen title) are dark navy on a dark
background. The dark theme needs a light-on-dark brand foreground (e.g. shift brand text to
`primary_0`/`primary_m1` in dark, or expose an adaptive `brand_text` role).

## Acceptance
With the above, and **no further uikit changes**, switching `OneStepUiKit.setThemeMode(Dark)`
must yield: dark screen/card backgrounds, light body + title text, and brand buttons with
legible (light) labels — across permission, recording, summary, and care-log flows. Light mode
must be byte-for-byte unchanged.

## uikit side (already done, forward-compatible)
- `OneStepUiKit.setThemeMode` + reactive `OneStepUiKitTheme(mode = …)`; previews render both modes.
- Hardcoded `Color.White` screen backgrounds replaced with `LocalOSColors.current.neutral_m5`
  (identical in light; auto-darkens once defect 1 is fixed).
- `adaptBakedNeutral(...)` remaps light neutral literals baked into data factories.
No uikit color values were forced to fight the design system; the dark mapping is symmetric
with light and simply consumes `osDarkTheme()`.
