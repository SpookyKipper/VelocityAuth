# Velocity

A Minecraft server proxy with unparalleled server support, scalability,
and flexibility. This fork includes new commands and better support for offline-mode players.

## Usage
1. Download the velocity jar from the latest release [here](https://github.com/Osiris-Team/Velocity/releases).
2. Stop your proxy and replace the old velocity jar with the new/downloaded one.
3. Make sure all your minecraft servers have `online-mode=false` in their `server.properties`
   (also set it in `velocity.toml`).
4. Done! Start your velocity proxy from a terminal with `java -jar velocity.jar` or via a script.

It's recommended to also install the [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/) 
and [SkinsRestorer](https://www.spigotmc.org/resources/skinsrestorer.2124/)
plugins on your proxy (to add support for more Minecraft versions and 
restore the players skins). Also don't forget to secure your Minecraft servers via a firewall and/or
by using Velocitys' modern-forwarding, read more [here](https://velocitypowered.com/wiki/deployment/security/).

## Features
Regular players will **not** have to register/login, only
offline-mode players must.

This velocity fork makes it possible for you
to disable online mode and not fear about
offline-mode (cracked) players causing you trouble. Your server will be available to a much larger player-base,
and you should see a fast increase in player count.

- **Basics**
  - SQL database **required** (normally pre-installed on your system).
  - Ban system, to block players from joining your proxy.
  - Whitelist mode to completely block not registered players from joining.

- **Security: offline-mode players**
  - Register/Login **required** (session based authentication, players only need to login once).
  - Connections to other servers are blocked, if the player is not logged in, and  get automatically forwarded to the limbo auth-server (in spectator mode).
  - Blocks all proxy command execution for not logged in players (except the /register and /login commands)
    , by changing the permissions function of the player.
  - Prevents kicking of already connected players.
  - Prevents join blocking.
  - Note that client offline/online mode detection works fine for players joining with 1.19.1 and above. On older versions
  offline-mode players using the username of an online-mode account will get kicked with "Invalid Session" (there is
  nothing I can do about this, tell those players to use another username or an hacked client like wurst).

- **Security: online-mode players**
  - Register/Login **is not required**
  - If a offline-mode player was playing on the server before
  and had the same name as the current online-mode player that joined, the
  offline-mode player loses access to the account.

- **Registration/Login**
  - Players get forwarded to a virtual limbo server for authentication.
  - Secured against password timing attacks.
  - Secured against password spamming attacks, by temp-banning those players (configurable).
  - Secured against SQL injection.

- **Other changes**
  - `help or ?` will display a list of all available velocity commands.
  - `plugins or pl` will display a list of all installed velocity plugins.
  - `servers` will display a list of all registered Minecraft servers.
  - `player <name>` will display details about the connected player.

## Player commands

#### /register _password_ _confirm-password_
- `velocityauth.register`
- Players have this permission by default when not logged in.
- Registers the player.

#### /login _password_
- `velocityauth.login`
- Players have this permission by default when not logged in.
- The minimum password length is 10 (configurable).
- Logins the player. On success, forwards the player to the first server, restores permissions, and creates a session
  so this player can rejoin without needing to login again.
  Failed logins get saved to a table, together with
  the UUID and IP of the player. If there are more than 5 failed attempts (for the same UUID OR IP)
  in the last hour, the player gets banned for 10 seconds on each
  following failed attempt.

## Admin commands

#### /a_register _username_ _password_
- `velocityauth.admin.register`
- Registers the provided player.

#### /a_unregister _username_
- `velocityauth.admin.unregister`
- Unregisters the provided player.

#### /a_login _username_ _password_
- `velocityauth.admin.login`
- Logins the provided player.

#### /ban _username_ (_hours_) (_reason_)
- `velocityauth.ban`
- Bans the player for 24h, with default reason: Your behavior violated our community guidelines and/or terms of service.
  The UUID and IP of the player gets added to
  the banned players table. On each player join that table gets
  checked and if there is a match for the UUID OR IP,
  the connection is aborted.

#### /unban _username_
- `velocityauth.unban`
- Unbans the player, by setting the ban expires timestamp to the current time.

#### /list_sessions _(username)_
- `velocityauth.list.sessions`
- Lists all sessions, or the sessions for a specific player.

#### /clear_sessions _(username)_
- `velocityauth.clear.sessions`
- Removes/Clears all sessions from the database, or the sessions for a specific player.


## Developers

#### Goals

* A codebase that is easy to dive into and consistently follows best practices
  for Java projects as much as reasonably possible.
* High performance: handle thousands of players on one proxy.
* A new, refreshing API built from the ground up to be flexible and powerful
  whilst avoiding design mistakes and suboptimal designs from other proxies.
* First-class support for Paper, Sponge, and Forge. (Other implementations
  may work, but we make every endeavor to support these server implementations
  specifically.)
  
#### Building

Velocity is built with [Gradle](https://gradle.org). We recommend using the
wrapper script (`./gradlew`) as our CI builds using it.

It is sufficient to run `./gradlew build` to run the full build cycle.
Due to my changes you should run
`./gradlew build -x checkLicenseMain -x checkstyleMain -x checkLicenseTest -x checkstyleTest` instead.

#### Running

Once you've built Velocity, you can copy and run the `-all` JAR from
`proxy/build/libs`. Velocity will generate a default configuration file
and you can configure it from there.

Alternatively, you can get the proxy JAR from the [downloads](https://papermc.io/downloads#Velocity)
page.
