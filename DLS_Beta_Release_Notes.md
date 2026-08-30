# Dashboard Layout Studio — Beta Release Notes

---

## 1.9.572

- First 1.9 beta release.
- Added: Recovery available indicator to dashboard list
- Added: Detailed **Modified** edit history shows unsaved changes chronologically and allows restoring an earlier edit as a normal undoable action.
- Fixed: Beta preferences were reset or overwritten by stable preferences
- Added: Changes made in the Preview Dashboard can be pulled into the DLS
- Added: Dashboard background can be viewed in DLS.  Two-tone grid remains visible.
- Changed: External CSS uses shared `@import` parsing and validation with checks for duplicate/conflicting/consolidatable rules across both tile-scoped and global selectors.
- Added: External stylesheets can be imported into `customCSS` using **Preserve customCSS rules**, **Overwrite customCSS rules**, or **Add to customCSS rules** conflict handling.
- Fixed: CSS Cleanup did not perform final consolidation check.
- Fixed: Delete Rows/Columns did not check for overlap conflicts created by unselected tiles (include partial was unselected).  Behavior did not match CLI logic.  
- Changed: Insert Rows/Columns supports only **Default** and **Include Partial** selection behavior, matching CLI logic.
- Fixed: Paste with Push now works for cut tiles; Shift-click Paste and Ctrl/Cmd+Shift+V invoke Paste with Push directly.
- Fixed: Resize no longer changes the Push/Pull layout option when Shift is pressed after the Resize dialog is open.
- Fixed: Grid-resize CSS checks flagged font-size: 0 as becoming too small.
- Added: Dashboard loading progress allows Device Validation and initial Render Tiles steps to be skipped.
- Bug fixes and UX improvements.
## 1.8.543

- Changed: Recovered layouts were not removed after session resumed and saved.
- Changed: Imported CSS is checked separately for orphaned tile selectors, with a dedicated **External CSS Orphans** warning distinct from `customCSS` orphan warnings.
- Bug fixes and UX improvements.

## 1.8.538

- First 1.8 beta release.
- Changed: Failed external `@import` stylesheets are now reported through the External CSS warning instead of silently being omitted from CSS analysis.
- Changed: Imported CSS is checked separately for orphaned tile selectors, with a dedicated **External CSS Orphans** warning distinct from `customCSS` orphan warnings.
- Added: External CSS rules can be imported into `customCSS` and their `@import` statements removed, creating a normal undo point and preserving information needed to restore the original imports.
- Fixed: Refreshing the browser prompts to recover.
- Fixed: Use Preview loses setting in preferences.
- Fixed: Preview button showing when preview is disabled.
- Added: Operations rejected because **Overlaps = Warn** visibly flash the Overlaps behavior setting.
- Fixed: Dragging a tile that already overlapping another tile did not trigger warnings when moved to overlap other tiles.
- Bug fixes and UX improvements.

## 1.7.532

- Changed: Alt-click now toggles an entire overlap cluster. If no tiles in the cluster are selected, the cluster becomes the selection; if any tile in the cluster is selected, the complete cluster is removed from the selection.
- Changed: Ctrl/Cmd+Alt-click now adds or removes an entire overlap cluster from the current multi-selection using the same toggle logic while preserving selections outside that cluster.
- Added: Shift-assisted overlap-layer navigation allows buried tiles in an overlapping stack to be located without changing the current selection. Shift-click advances through the stack when a selection already exists, even when none of the tiles in that stack are selected.
- Added: Ctrl/Cmd+Shift-click toggles only the currently focused tile layer in an overlap stack and keeps focus on that layer instead of advancing, allowing individual tiles within a stack to be added to or removed from a multi-selection.
- Changed: Normal Click and Ctrl/Cmd-click act on the current overlap layer and then advance to the next layer; Shift has no special effect when no selection exists or when clicking a non-overlapping tile.
- Bug fixes and UX improvements.


## 1.7.527

- First 1.7 beta release
- Added: crash/session recovery - DLS preserves unsaved working layouts locally and can restore them after an accidental window close, browser crash, system restart, or other interruption.
- Changed: The same dashboard cannot be open in multiple browser windows
- Added: A directly opened temporary Preview dashboard can be manually revealed by clicking the corners UL, UR, LR, LL
- Added: Hub Variables can now be added to and removed from a dashboard alongside physical devices, with complete hub-variable discovery and Dashboard Variables / All Hub Variables filtering.
- Added: Add Tile can select variables from either the dashboard or the entire hub, automatically authorize newly selected variables.
- Fixed: DLS creates native Hubitat `global-variable` tiles incorrectly.
- Added: Existing Hub Variable tiles can have their assigned variable changed through the Expert → Tile Devices interface.
- Fixed: Temporary Preview dashboards did not carry the Hub Variable authorizations required by the working layout.
- Fixed: Cancelling Tile Configuration after opening it from Tile Information no longer leaves subsequent dialog controls disabled.
- Advanced the beta development line from 1.6 to 1.7.

## 1.6.520

- First 1.6 beta release.
- Added: New Add Tile toolbar action allows tiles to be created.
- Added: Tile Configuration supports Hubitat device, variable, system, and static-content templates and can also edit supported settings for existing tiles through Tile Information.
- Added: Complete Hubitat template catalog, dashboard/all-hub device filtering, automatic dashboard device authorization, custom device attributes, and searchable Hubitat and Material icon selection.
- Fixed: Preview opens a window before DLS finishes creating the preview dashboard.
- Added: A deep-red **Preview Open** indicator appears in the upper status bar while the Preview is being prepared or remains open.
- Added: Double-clicking a selected tile opens Tile Information while preserving the complete current selection. Double-clicking an unselected tile retains the normal two-click selection and overlap-layer cycling behavior.
- Added: Beta-to-stable downgrade notices identify the major beta-only features that will no longer be available after installing the stable release.
- Changed: Light-mode dialogs and Preview progress use the standard high-contrast DLS dialog styling.
- Fixed: Help tabs missing new features.
- Bug fixes and UX improvements.


## 1.5.510

- Changed: Temporary Preview dashboard can only be opened by DLS.
- Changed: Temporary Preview dashboards have Hubitat navigation disabled.
- Fixed: Preview window lock fails to release when navigating away in the Preview tab/window.
- Changed: DLS now warns when closing or navigating away while its Preview dashboard remains open and closes the owned Preview after the user leaves.
- Fixed: Grid Columns and Grid Rows appear manually editable while Auto-size dashboard bounds controls them. 
- Fixed: Abort does not cancel the complete Save to Hub verification process, including the Checking current hub layout stage, token refresh, response reading, and retries.
- Changed: Save-verification requests now have a finite network timeout, preventing DLS from remaining indefinitely stuck after a VPN, network, routing, or hub failure.
- Bug fixes and UX improvements.


## 1.5.504

- Fixed: Down arrows missing on light mode drop downs on edit actions toolbar


## 1.5.502

- Fixed: Preview Dashboard lock releases when url changes in preview tab/window.  Lock restored on browser back.


## 1.5.501

- Fixed: Copy To Other Window disabled UX when device was not valid in the destination dashboard.


## 1.5.499

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

- Added: z-index can be edited Tile Information.
- Added: Overlap z-index conflict detection and automatic conflict resolution.
- Added: Preserved the complete existing stacking order when resolving z-index conflicts.  Tile ID breaks equal-z-index ties.
- Fixed: Tweaking the dashboard JSON file does not automatically become flagged as a full replacement / warning dialogs.
- Bug fixes and UX improvements.


## 1.4.476

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

- Added: Generate preview dashboard support files for Simple CSS work around.
- Fixed: Dashboard switching so the first dashboard ID is not reused for later dashboards.
- Fixed: Tiles assigned to a nonexistent hub device caused the preview to hang on load.  Now auto set non-existent devices to `0`in temporary Preview copies.
- Fixed: Preview reliability.
- Bug fixes and UX improvements.


## 1.4.448

- Added: Copy/Copy CSS to other windows actions now synchronize across all open DLS windows.
- Added: New Copy To Other Window action allows copying tiles between DLS windows.
- Added: New Copy CSS To Other Windows actions copying tile CSS between DLS windows.
- Fixed: Changed Copy CSS and Paste CSS alternate-action menus to vertical, full-width layouts.
- Bug fixes and UX improvements.


## 1.4.446

- Improved light-theme contrast.
- General light-mode UX refinements.


## 1.4.445

- Fixed: Light and dark themes were applied after interface load.
- Fixed: Rendered View and temporary-dashboard preparation from hanging when a dashboard contains no tiles.
- Bug fixes.


## 1.4.442

- First 1.4 beta release
- Added: Copy CSS action allows copying a tile's CSS to multiple tiles.  CSS is automatically re-targeted to the destination tile.
- Fixed: Ctrl/Cmd+Alt-click cluster now adds to the current selection.
- Fixed: Corrected Launch and Preview button states for no dashboard, offline layouts, unchanged dashboards, and modified dashboards.
- Fixed: Delayed auto-connect progress and stable Starting up/Launch initialization states.
- Bug fixes and UX improvements.


## 1.3.432

- Added: CSS pasted into the CSS edit text box in tile information can be automatically re-targeted to the current tile.  Option checkbox is remembered. 
- Fixed: Undo was not created for CSS edits in tile information.
- Bug fixes.


## 1.3.430

- Added: Dashboard previews make temporary copies of Simple CSS files if Simple CSS is detected.  
- Added: Checks if Simple CSS Editor is in use in the current dashboard.
- Added: Preview warnings that external-editor changes affect temporary files and may be overwritten.  Warning supression opt-out in dialog.
- Bug fixes and UX improvements.


## 1.3.426

- Added: vertical scrolling to beta dialogs and panels.
- Fixed: Excluded system, variable, text, navigation, static, and clock-style tiles / templates from invalid-device validation.
- Bug fixes and UX improvements.


## 1.3.422

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

- Version correction only.  Reissued beta 1.2.410 under build 412.
- No other changes.


## 1.2.410

- First beta release.  Forked from stable 1.1.404.
- Added: Temporary Preview dashboard allows dashboard edits to be viewed without requiring layouts be saved.
- Added the automatic stable/beta update system.
- Added Stable Only and Include Beta update channels
- Bug fixes and UX improvements.
