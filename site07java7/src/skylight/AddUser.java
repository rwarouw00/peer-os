package skylight;

/** Adds a user, or changes a password: java -cp skylight.jar skylight.AddUser <username> <password> */
public final class AddUser {

	public static void main(String[] args) {
		if (args.length != 2 || !Storage.isValidUsername(args[0]) || args[1].isEmpty()) {
			System.err.println("Usage: java -cp skylight.jar skylight.AddUser <username> <password>");
			System.err.println("Usernames may use letters, digits, '-' and '_' (up to 32 characters).");
			System.exit(1);
		}
		boolean existed = Storage.readProperties(Storage.DATA.resolve("users.properties")).containsKey(args[0]);
		if (!Storage.setPassword(args[0], args[1])) {
			System.err.println("Could not write " + Storage.DATA.resolve("users.properties"));
			System.exit(1);
		}
		System.out.println((existed ? "Changed the password for " : "Added user ") + args[0]);
	}
}
