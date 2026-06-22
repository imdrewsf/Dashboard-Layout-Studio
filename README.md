# Dashboard Layout Studio

**Dashboard Layout Studio** is a visual layout editor for **Hubitat Dashboard v1** layouts.

<img width="3013" height="1695" alt="image" src="https://github.com/user-attachments/assets/bc827bbe-fbec-4021-a873-f8c23f540348" />

## Main Features

### Visual Layout Editing

Tiles can be selected by:

- Clicking a single tile
- Shift-clicking multiple tiles
- Alt-clicking a connected tile cluster
- Dragging a rectangular selection range
- Selecting one or more rows
- Selecting one or more columns

Selected tiles can be moved, copied, resized, cleared, deleted, spaced, or adjusted as a group.

---

### Move, Copy, Paste, and Duplicate Tiles

Dashboard Layout Studio supports operations that are difficult or unavailable in the standard Hubitat dashboard editor:

- Move groups of tiles
- Drag tiles to new locations
- Copy tiles within the same dashboard
- Copy tiles between dashboards
- Paste tiles into another dashboard layout
- Clear tiles while leaving empty space
- Insert rows or columns
- Delete rows or columns
- Push tiles right or down
- Pull tiles left or up
- Choose how conflicts with existing tiles should be handled

When tiles are copied, the tool assigns new tile IDs that do not conflict with existing tiles or tile-scoped CSS rules.

---

### Copy Tiles with Their CSS

For dashboards that use custom CSS, Dashboard Layout Studio can duplicate tile-scoped CSS when tiles are copied.

For example, CSS rules targeting a copied tile can be replicated and retargeted to the new tile ID.

This allows you to duplicate styled tiles without manually finding and editing every `#tile-123` rule.

---

### Merge Tiles from Other Dashboards

Dashboard Layout Studio can help reuse sections of one dashboard inside another.

When copying or importing tiles from another dashboard, it can:

- Assign safe new tile IDs
- Preserve related tile-scoped CSS
- Retarget copied CSS to the new tile IDs
- Check assigned devices against the destination dashboard
- Warn when copied tiles use devices that are not selected for the destination dashboard

---

### Tile Spacing and Layout Cleanup

Dashboard Layout Studio includes tools for cleaning up and reorganizing dashboard layouts:

- Set uniform spacing between tiles or groups
- Increase existing spacing
- Decrease existing spacing
- Preserve clusters and nested groups
- Unstack overlapping tiles and spread them out
- Enlarge or reduce tiles while preserving layout and spacing
- Resize selected tiles to a uniform size
- Resize tiles by dragging tile corners
- Resize container-style tile groups while preserving their internal layout
- Trim unused rows and columns

These tools are useful when a dashboard has grown over time and needs to be aligned, enlarged, spaced out, or reorganized.

---

### Workspace Editing with Undo/Redo

Dashboard Layout Studio provides a workspace separate from the live Hubitat dashboard.

You can:

- Make layout changes without immediately changing the hub dashboard
- Undo and redo individual steps
- Undo all changes back to the loaded layout
- Review changes before saving back to the hub

The hub dashboard is not changed until you explicitly save the updated layout.

---

## Online Mode and Offline Mode

Dashboard Layout Studio does not require or use any cloud resources.  It can be used in two modes.

### Online Mode

Online Mode is available when the HTML file is hosted on the Hubitat hub and opened from the hub.

Online Mode can:

- Load layouts directly from the hub
- Save layouts directly back to the hub
- Capture rendered snapshots of actual dashboard tiles
- Show hub device names
- Check tile device assignments against the dashboard’s selected devices
- Add missing devices to the dashboard’s selected device list
- Remove unused devices from the dashboard’s selected device list
- Use all Offline Mode editing capabilities

Online Mode is the most powerful mode because the tool can communicate with the hub and validate the layout against real dashboard and device information.

### Offline Mode

Offline Mode is available when working with local files or clipboard JSON.

Offline Mode can:

- Load layouts from files
- Save layouts to files
- Load layout JSON from the clipboard
- Copy layout JSON to the clipboard
- Edit layouts using placeholder tiles
- Use the main layout, spacing, resize, undo, JSON, and CSS tools

Offline Mode is useful for working from backups, experimenting, or editing layouts without connecting to the hub.

---

## Device Management

Dashboard Layout Studio includes device-management tools for layouts.

It can:

- Show the device assigned to each tile
- Show device names from the hub
- Identify missing, blank, invalid, unknown, or unselected device IDs
- Highlight invalid device assignments
- Highlight valid devices assigned to tiles but not selected for the dashboard
- Reassign tile devices
- Add devices to the dashboard’s selected device list
- Remove unused devices from the dashboard’s selected device list
- Warn before saving if tile devices are not selected for the dashboard

This is especially useful when copying tiles between dashboards, importing JSON, or cleaning up older dashboards.

---

## CSS Tools

Dashboard Layout Studio includes CSS tools for dashboards that use custom styling.

It can:

- View tile-specific CSS
- Edit CSS rules associated with a tile
- Replicate tile-scoped CSS when duplicating tiles
- Remove tile-scoped CSS when tiles are removed
- Detect duplicate CSS
- Detect conflicting CSS
- Find orphaned tile CSS
- Consolidate repeated selector rules
- Optionally preserve removed or changed CSS as comments

The CSS Cleanup tool checks for:

- **Conflicts** — the same selector and property has different values
- **Duplicates** — repeated declarations with the same value
- **Consolidation** — repeated selector rules that can be combined
- **Orphans** — tile-scoped rules that reference tile IDs no longer present in the layout

For dashboards with a lot of hand-written CSS, this can save substantial cleanup time.

---

## JSON Editor

Dashboard Layout Studio includes a JSON Editor for users who want direct access to the dashboard JSON.

It supports:

- Backing up layouts
- Importing layouts
- Full JSON editing
- Viewing and editing `customHTML` separately
- Viewing and editing `customJS` separately
- Viewing and editing `customCSS` separately
- Formatting and validating JSON
- Basic validation and formatting for `customHTML` and `customJS`
- Enhanced validation and formatting for `customCSS`
- Tile Devices review
- Dashboard Devices review

The visual editor and JSON editor are designed to work together.---

## Installation

Dashboard Layout Studio is distributed as a single HTML file.

### Online Mode Installation

1. Download the latest `dashboard-layout-studio-v###.html` file.
2. Upload it to the Hubitat hub’s file storage so it is available under `/local/`.
3. Open the file from the hub in a browser.

Example URL format:

```text
http://<hub-ip>/local/dashboard-layout-studio.html
```

Replace `<hub-ip>` with your hub’s IP address.

### Offline Use

1. Download the latest HTML file.
2. Open it directly in a browser.
3. Load dashboard JSON from a file or the clipboard.
4. Save edited layouts back to a file or clipboard.

Offline Mode does not provide direct hub load/save, rendered snapshots, or hub device validation.

---

---

