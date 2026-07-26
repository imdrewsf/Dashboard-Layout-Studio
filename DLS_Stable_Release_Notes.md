# Dashboard Layout Studio — Stable Release Notes

---

## 1.4.498

**Changes since stable 1.2.441**

- First 1.4 stable release.
- Added: Light and Dark interface themes
- Changed: Shift key temporarily changes the Info button to Reports; renamed the JSON toolbar control to **Expert** and updated Help to identify it as the Layout JSON Editor.
- Fixed: Clearer active-tile identification in the grid and Selection panel, including stronger selection visibility and cluster-grouped tile lists.
- Added: Release-note, GitHub, issue-reporting, and MIT License links to the Update and Help/About panels.
- Added: **Reset Settings** restores all preferences to stable defaults and re-enables dismissed notifications without clearing hub credentials or the current layout.
- Changed: Reorganized Reports into Report, Sorting, and Fields tabs; reorganized Preferences into logical groups and disabled Rendered View method when Rendered View is not enabled.
- Fixed: Rendered View no longer hangs on dashboards with no tiles, and removing an external `@import` immediately clears stale imported-CSS conflicts.
- Bug fixes and UX improvements.

## 1.2.441

**Changes since stable 1.2.425**

- Added: Ctrl/Cmd+Alt-click selects / adds an entire tile cluster to the existing selection.
- Added: Automatic hub login option
- Fixed: Startup waits until initialization is complete.
- Bug fixes and UX improvements.

## 1.2.425

**Changes since stable 1.2.411**

- Added: Vertical scrolling to dialogs and panels
- Fixed: Excluded system, variable, text, navigation, static, and clock-style tiles / templates from invalid-device validation.
- Bug fixes and UX improvements.

## 1.2.411

**Changes since stable 1.1.404**

- First 1.2 stable release
- Added: Automatic self update
- Added: Stable and Beta update channels
- Bug fixes and updater UX improvements.

## 1.1.404

**Changes since stable 1.1.398**

- Added: External `@import` stylesheet loading, imported tile-rule display, and CSS duplication support.
- Added: Extended CSS conflict, duplicate, and consolidation analysis across customCSS and imported styles while keeping imported rules read-only.
- Fixed: Modified-state handling and orphaned tile CSS comments.

## 1.1.398

**Changes since stable 1.1.394**

- Added: templateExtra` text can be edited for TextTile (only) tiles in Tile Information.
- Added: Skip device checks option added to preferences.
- Improved Rendered View invalidation and refresh behavior.
- Fixed: Updated Insert Rows and Insert Columns to honor Include Partial selection behavior.

## 1.1.394

**Changes since stable 1.1.389**

- Added: Style Copy rendering recreates dashboard tiles by extracting styles from the dashbaord DOM instead of images.
- Added: Rendering method in preferences
- Changed: Device selectors now sort by device name.

## 1.1.389

**Changes since stable 1.0.353**

- Added: Grid Settings for dashboard dimensions, cell sizes, grid gaps, zoom, Auto Fit presets, and grid serialization.
- Added: Dynamic-grid support, including independent dynamic sizing by axis and zero-width, zero-height, and zero-gap settings.
- Added: Trim Grid, automatic bounds sizing, and toolbar grid/zoom controls.
- Added: Preserve Layout (experimental) conversion with shared grid-line scaling, connected-group handling, container preservation, and a recommended-grid-size calculator.
- Added: Device presets for desktop, tablet, phone, kiosk, and Fire tablet layouts.
- Improved pending-action toolbar lockout, Escape handling, dashboard-launch warnings, and workspace fitting.
- Bug fixes and Grid Settings UX improvements.

## 1.0.353

**Changes since stable 0.9.350**

- First Dashboard Layout Studio 1.0 stable.
- Updated and expanded Help content.
- Bug fixes and documentation improvements.

