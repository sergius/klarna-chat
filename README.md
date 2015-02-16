# Chat service

## Initial requirements

 __Chat server__

Write a *robust* server in Erlang or Java that *listens for connecting clients on tcp port 6667*. *All characters* received from a client should be sent to *all other connected clients*, unless the sequence starts with the *character* `$\`. A sequence ends with carriage return. What the server does with sequences starting with $\ is up to you.
Your solution will be judged on *robustness, capacity, features, and the beauty of your code*.

## Solution

### Language

As I am applying for a role in the Functional Programming team - and I don't know to program in Erlang, yet - I decided to present my solution in Scala, as the closest to functional programming what I know. Also, as another intent of getting close to Erlang, I chose to use Akka, an implementation of actor model for Scala and Java, and remembering that it was Erlang the first language to have implemented the actors.

 I would like to mention that I avoided on purpose a trivial implementation in Java, as it could test just the elementary notions of Java and something on networking and concurrency, *at most*, and I believe this is not what the team would like to assess.

### Robustness & capacity

There's no more robust and capable tool for JVM-based systems than Akka, for implementations in a distributed and concurrent environment. Therefore it was an easy an natural solution for the proposed problem, being a direct and implicit answer to its requirements.

### Features

As a user of the chat system, all you need on your side is to have the chat client (for instance, the one implemented here) and a working node of chat server. Well, it's worth mentioning that your chat client should know the server's `ip` and the `port` it listens on.

When you start your client you're going to see the following menu (in a terminal):

```

        === Starting Chat Client App ===

        === Please use the following formats for commands: ===

        1. 'login Batman' - to login as Batman

        (*note: the rest of commands start with '$\'*)

        2. '$\help' - to see these instructions

        3. '$\logout' - to logout from chat

        4. '$\exit' - to exit this application

        5. '$\private John It's a secret' - to write a private message to John

        6. '$\connected' - to see all connected users

        7. '$\status' - to see a refreshed chat timeline

        Please remember: when logged-in, whatever you type,
        if it's not one of the mentioned commands, it will be treated
         as a message and will be published to the chat.

```

As you can see, the characters `$\` are reserved for special commands, i.e. for sentences that should not be treated as simple messages to be published to the chat. Anything else you type in when in your session, will be treated as message and will be published to the chat, so everybody will (be able to) read it.

__I would like to mention something regarding the special characters `$\`__

Though the requirements don't mention it explicitly, I strongly believe that dealing with special characters (and other special cases) is a pure client-side task. The server doesn't have to have a clue about this kind of stuff. What the server *__should have__* is a clear API to serve possible requests and whatever needs the system (client) might have. Then, it's gonna be client's task to deal with any special characters, cases, styles, etc. - depending on the kind of the environment the client is developed for - in a way that, under the hood, the client will actually invoke the nice and clear commands of server's API.

### Design, Structure & Functionality

I couldn't just write the server and not write at least some kind of client. Though I wrote the tests for the server in a kind of specification definition, there's no better testing for anything than using it. Therefore I wrote a client, too, and I can only confirm that it helped.

The project that I present consists of 3 modules:

 * Server
 * Client
 * API: A module that consists of just one file where are defined the messages that the server and the client should implement. It is actually a part of the server, but being a separate module, the client has just a small and light dependency to count with.

Perhaps the best way to explain the server functionality is based on the API commands:

 1. `Login(username: String, ref: ActorRef)`: The login request is satisfied with the following conditions:
    * The user is not trying to login with already existing username
    * The user is not already logged in (with a different username) only one session per user is permitted.

    If any of these conditions are not satisfied, a `LoginError(error: String)` message is received by the client (the app, not the user)

    If the conditions are respected, the user is logged in, which is confirmed with a `LoginAck(username: String)` message.

    *Note: In the current client implementation, the `login` command is the only one that doesn't require the use of `$\` prefix, because it is not actually necessary. As the user is not chatting yet, it doesn't have to be discriminated from the rest of the input. And when the user is chatting, the word doesn't make sense as a command (the user is already logged-in and double logging is not admitted), so it is treated as a message input.*

 2. `Logout(username: String)`: The user logs-out from the chat (but the application keeps running). The user can close the application with `$\exit` command.

 3. `ChatMessage(username: String, message: String)`: A message published by the user.

    *Note: this would be *anything* typed in without the `$\` prefix. (Note that `login` command is the only one that doesn't use this prefix)*

 4. `PrivateMessage(from: String, to: String, message: String)`: The user sends a private message to another user. These messages can only be seen by the sender and the receiver and they will figure in the chat timeline for only these users.

 5. `GetChatLog(username: String)`: A request for the chat timeline. Useful for newly connected users or for just a refresh.

    *Note: A useful thing would be to limit somehow the requested chatline (eg. by time or amount of lines). This feature is not implemented in the current version.*

 6. `Connected(username: String)`: Retrieves the list of all connected users. This command is accepted even when the user is not logged in. The idea is that a user (which is going to log-in) might first want to see if other certain users are connected.

 7. `ChatError(error: String)`: Response to some misuse or error during the chat. So far it was used when a user tries to chat without having logged-in.

 8. `ChatLog(messages: List[(String, String)])`: Response to the request for chat timeline.

 9. `OnLine(users: List[String])`: Response to the request for connected users.

#### Persistence

 A basic persistence to a text file was implemented for this example. The code structure admits adding any other implementation easily.

## How to run it

The necessary things to know in order to connect to the server are the `ip address` and the `port` where it is going to listen. Actually the word *listen* has a bit different sense in Akka applications. It is not the program listening for requests, it's the actor system listening to other actor systems in order to locate or create actors. Thus, the business logic, based on communication between actors stays totally transparent, whatever the ways of connection.

I used [Akka remoting](http://doc.akka.io/docs/akka/current/java/remoting.html), being the server the remote actor and here's the bit of configuration you'll need.

Both in server and client modules, you'll find the `application.conf` file in the resources directory (`src/main/resources`). In these files you should store the following details:

```

    netty.tcp {
      hostname = "127.0.0.1"
      port = 6667
    }

```

As you can see, currently the `hostname` is `localhost`. Change it as you need.
In the client's module you have to indicate the same `hostname` and `port`, so the client knows where to look for its remote actor.

One more thing. In the client module, in the file `src/main/scala/app/ChatClientApp.scala` you'll find this small object:

```

    object Conf {
      val path = "akka.tcp://chat-server-system@127.0.0.1:6667/user/chat-service-actor"
    }

```

You'll have to change the `path` to the corresponding values (the same as in `application.conf`).

That's it. You're ready to chat. Start the server running `src/main/scala/app/ChatServiceApp` in the server module. Then, you can connect as many clients as you wish, from the same machine or from different ones, running `src/main/scala/app/ChatClientApp` in the client module.

You'll probably have to do it from your IDE (I hope it's IntelliJ, as it has the necessary plugins for Scala and SBT). If you have machines with Scala installed, you can package the modules to `.jar` files and port them like that.

*Note: If you connect your clients from the same machine (using `localhost` for server), you should set the `port = 0` in the `application.conf` __(on the client side only)__ to let the system choose the port automatically, otherwise you'll get the exception of port already in use.*
