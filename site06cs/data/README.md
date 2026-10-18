# data\

Skylight OS keeps each user's data here. Everything is created as it is needed.

| Path | Contents |
| --- | --- |
| `users.json` | Accounts: `{"alice": "pbkdf2-sha256$20000$<salt>$<hash>"}`. Created by `AddUser.exe`. |
| `users\<name>\settings.json` | Background, notification time and desktop shortcuts |
| `users\<name>\files\` | Files the user's apps saved with `WriteFile()` |
