# Chat service

## Functionality description

 1. Logs a user in
 2. If a user writes a message, this message is retransmitted to the rest of connected users, unless it doesn't start with `$\`
 3. If the message starts with `$\`, this will be treated as a special command:
  3.1 `$\LOGIN <username>` to login
  3.2 `$\LOGOUT` to logout
  3.3 `$\PRIVMSG <dest-username>` to send a private message to a specific user
  3.4 `$\CONNECTED` to receive the list of connected users

