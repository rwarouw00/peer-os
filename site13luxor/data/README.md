# data/

Skylight OS keeps each user's data here. Everything is created as it is needed.

| Path | Contents |
| --- | --- |
| `users.properties` | Accounts: `alice=pbkdf2-sha256$20000$<salt>$<hash>`. Created by `skylight.AddUser`. |
| `users/<name>/settings.properties` | Background, notification time and desktop shortcuts |
| `users/<name>/files/` | Files the user's apps saved with `writeFile()` |
| `jython-cache/` | Jython's list of Java packages, made on the first start; safe to delete |
