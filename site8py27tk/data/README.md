# data/

Skylight OS keeps each user's data here. Everything is created as it is needed.

| Path | Contents |
| --- | --- |
| `users.json` | Accounts: `{"alice": {"password": "pbkdf2-sha256$20000$<salt>$<hash>"}}`. Created by `python add-user.py`. |
| `users/<name>/settings.json` | Background, notification time and desktop shortcuts |
| `users/<name>/files/` | Files the user's apps saved with `self.writeFile()` |

The formats are the same as the PHP-GTK2 version's, so a `data/` folder can be copied between the two.
