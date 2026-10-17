# Skylight's changes to Luxor XUL 1.0 beta 8

Luxor XUL 1.0 beta 8 (2003, by Gerald Bauer) is free software under the **GNU General Public License, version 2**. The seven files under `src/` are Luxor's own, changed for Skylight OS, and are under the same licence. `tools/get-libs` copies them over Luxor's downloaded source and compiles the result into `lib/luxor-1.0-b8-skylight.jar`.

`skylight.patch` shows the same changes as a diff against Luxor's source (`patch -p1 < skylight.patch` inside the unzipped `luxor-1.0-b8` folder). Every change is marked with a `Skylight:` comment.

| File | Change |
| --- | --- |
| `core/XulTag.java` | Colours as `#rgb`, `#rrggbb` or `rgb(r, g, b)`; any Java font family (`monospaced`, `Dialog`, `"DejaVu Sans"`), and a size or weight alone (`font: 13 bold`) gives a sans-serif font instead of an error; `padding` and `margin` (1 to 4 numbers, as in CSS), as empty borders inside and outside the element's border |
| `swing/AbstractBoxPeer.java` | A box's children are sized again when their text, icon or font changes, and so are the boxes around them, so labels no longer keep the width of their first text (even an empty one); `min-width`, `max-width`, `min-height` and `max-height` are honoured for every child, and a preferred size never exceeds the maximum (a text field drops its column count when it has to be narrower); a `spacing="6"` attribute puts gaps between children; every child is named after its `id` |
| `swing/AbstractComponent.java` | `getMaxWidth()` returned the maximum *height*, so `max-width` never worked anywhere |
| `swing/input/XLabel.java` | Labels apply their `padding`, `margin` and `border` styles |
| `swing/input/XButton.java` | `tooltip="..."` works on buttons (it was documented but ignored); a button without a `label` has no text, instead of showing its `id` |
| `swing/input/XTextArea.java` | Named after its `id`; `wrap="true"` wraps lines at word boundaries, `readonly="true"` makes it read-only |
| `swing/IFramePeer.java` | Named after its `id`, so a script can call `setPage`; `src` is optional (an empty page) |

Luxor's Jython console (`luxor/python/`) is left out of the build, since Skylight uses Tcl.
