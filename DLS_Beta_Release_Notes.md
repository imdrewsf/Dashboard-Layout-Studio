# Dashboard Layout Studio — Beta Release Notes

---

## 1.5.504

**Changes since beta 1.5.502**

- Fixed: Down arrows missing on light mode drop downs on edit actions toolbar

## 1.5.502

**Changes since beta 1.5.501**

- Fixed: Preview Dashboard lock releases when url changes in preview tab/window.  Lock restored on browser back.

## 1.5.501

**Changes since beta 1.5.499**

- Fixed: Copy To Other Window disabled UX when device was not valid in the destination dashboard.

## 1.5.499

**Changes since beta 1.4.479**

- First 1.5 beta release.
- Added: Hierarchical List and Tree views in the Selection panel, with separate cluster groups, nested-container relationships, z-index ordering, cluster-specific controls, and clearer active-tile identification.
- Added: Z-index report sorting and a selectable Z-index report field; improved nested-container classification in reports.
- Changed: Light mode is now the default for new installations, with additional interface contrast and selection-visibility improvements.
- Added: Release-note, GitHub, issue-reporting, and MIT License links to the Update and Help/About panels.
- Added: A conditional yellow **Beta** indicator below the version number when the application identifies itself as a beta build.
- Added: **Reset Settings** restores all preferences to beta defaults and re-enables dismissed notifications without clearing hub credentials or the current layout.
- Changed: Reorganized Reports into Report, Sorting, and Fields tabs; reorganized Preferences into logical groups and disabled Rendered View method when Rendered View is not enabled.
- Fixed: Removing an external `@import` immediately clears stale imported-CSS conflicts and reruns validation.
- Bug fixes and UX improvements.

## 1.4.479

**Changes since beta 1.4.476**

- Added: z-index can be edited Tile Information.
- Added: Overlap z-index conflict detection and automatic conflict resolution.
- Added: Preserved the complete existing stacking order when resolving z-index conflicts.  Tile ID breaks equal-z-index ties.
- Fixed: Tweaking the dashboard JSON file does not automatically become flagged as a full replacement / warning dialogs.
- Bug fixes and UX improvements.

## 1.4.476

**Changes since beta 1.4.455**

- Fixed: Improved light-mode contrast on all panels and dialogs
- Added: Shift key temporarily changes the Info button function to Reports
- Changed: Renamed the JSON toolbar control to **Expert** and updated Help to identify it as the Layout JSON Editor.
- Changed: Made Preview the default action, added Shift-to-Launch, and synchronized Preview busy-state protection across DLS windows.
- Added: Preview window warning suppression option.
- Fixed:  Preview warnings appear after window creation.
- Fixed: Light theme not applied to preview warnings, preparation pages, and progress dialogs.
- Fixed: Improved Compatibility Rendered View authorization and temporary-dashboard validation.
- Fixed: report page boundaries, dashboard-loading failures, Help-tab highlighting, and other bugs.
- Bug fixes and UX improvements.

## 1.4.455

**Changes since beta 1.4.448**

- Added: Generate preview dashboard support files for Simple CSS work around.
- Fixed: Dashboard switching so the first dashboard ID is not reused for later dashboards.
- Fixed: Tiles assigned to a nonexistent hub device caused the preview to hang on load.  Now auto set non-existent devices to `0`in temporary Preview copies.
- Fixed: Preview reliability.
- Bug fixes and UX improvements.

## 1.4.448

**Changes since beta 1.4.446**

- Added: Copy/Copy CSS to other windows actions now synchronize across all open DLS windows.
- Added: New Copy To Other Window action allows copying tiles between DLS windows.
- Added: New Copy CSS To Other Windows actions copying tile CSS between DLS windows.
- Fixed: Changed Copy CSS and Paste CSS alternate-action menus to vertical, full-width layouts.
- Bug fixes and UX improvements.

## 1.4.446

**Changes since beta 1.4.445**

- Improved light-theme contrast.
- General light-mode UX refinements.

## 1.4.445

**Changes since beta 1.4.442**

- Fixed: Light and dark themes were applied after interface load.
- Fixed: Rendered View and temporary-dashboard preparation from hanging when a dashboard contains no tiles.
- Bug fixes.

## 1.4.442

**Changes since beta 1.3.432**

- First 1.4 beta release
- Added: Copy CSS action allows copying a tile's CSS to multiple tiles.  CSS is automatically re-targeted to the destination tile.
- Fixed: Ctrl/Cmd+Alt-click cluster now adds to the current selection.
- Fixed: Corrected Launch and Preview button states for no dashboard, offline layouts, unchanged dashboards, and modified dashboards.
- Fixed: Delayed auto-connect progress and stable Starting up/Launch initialization states.
- Bug fixes and UX improvements.


## 1.3.432

**Changes since beta 1.3.430**

- Added: CSS pasted into the CSS edit text box in tile information can be automatically re-targeted to the current tile.  Option checkbox is remembered. 
- Fixed: Undo was not created for CSS edits in tile information.
- Bug fixes.

## 1.3.430

**Changes since beta 1.3.426**

- Added: Dashboard previews make temporary copies of Simple CSS files if Simple CSS is detected.  
- Added: Checks if Simple CSS Editor is in use in the current dashboard.
- Added: Preview warnings that external-editor changes affect temporary files and may be overwritten.  Warning supression opt-out in dialog.
- Bug fixes and UX improvements.

## 1.3.426

**Changes since beta 1.3.422**

- Added: vertical scrolling to beta dialogs and panels.
- Fixed: Excluded system, variable, text, navigation, static, and clock-style tiles / templates from invalid-device validation.
- Bug fixes and UX improvements.

## 1.3.422

**Changes since beta 1.2.412**

- First 1.3 beta release
- Added: printable tile reports corresponding to CLI report types, with selectable scope, columns, and sorting.
- Added: relationship, overlap, nested, hierarchy, and conflict reports.
- Added: Letter, Legal, and A4 page sizes, programmatic pagination, headers, footers, page numbers, and repeated table headings.
- Added: Readable splitting of oversized report groups across pages.
- Added: Optional fixed / actual proportional Simple View and Rendered View thumbnails with corrected aspect ratios.
- Changed: Initial rendering uses the loaded dashboard directly instead of updating the temporary dashboard.
- Added: Modified state-aware Launch and Preview behavior and a Preview dropdown for modified dashboards.
- Fixed: Disabled Tile Information when no tiles are selected.
- Bug fixes and UX improvements.

## 1.2.412

**Changes since beta 1.2.410**

- Version correction only.  Reissued beta 1.2.410 under build 412.
- No other changes.

## 1.2.410

- First beta release.  Forked from stable 1.1.404.
- Added: Temporary Preview dashboard allows dashboard edits to be viewed without requiring layouts be saved.
- Added the automatic stable/beta update system.
- Added Stable Only and Include Beta update channels
- Bug fixes and UX improvements.
