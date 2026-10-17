# data/

Skylight OS keeps each user's data here. Everything is created as it is needed.

| Path | Contents |
| --- | --- |
| `users.json` | Accounts: `{"alice": {"password": "pbkdf2-sha256$20000$<salt>$<hash>"}}`. Created by `php tools/add-user.php`. |
| `users/<name>/settings.json` | Background, notification time and desktop shortcuts |
| `users/<name>/files/` | Files the user's apps saved with `$app->writeFile()` |
