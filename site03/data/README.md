# data/

Skylight OS keeps its data here, **outside** the web root (`public/`), so the web server never serves it. The web server's user must be able to write to this folder.

| Path | Contents |
| --- | --- |
| `users.json` | Accounts: `{"alice": {"password": "<bcrypt hash>"}}`. Created by `php tools/add-user.php`. |
| `users/<name>/settings.json` | The user's background, notification time and desktop shortcuts. |
| `users/<name>/files/` | Files the user's apps saved with `App.writeFile`. |

Nothing here is needed to start: the files are created as they are used.
