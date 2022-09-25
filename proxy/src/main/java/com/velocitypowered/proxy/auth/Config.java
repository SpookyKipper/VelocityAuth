/*
 * Copyright (C) 2018 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.velocitypowered.proxy.auth;

import com.osiris.dyml.Yaml;
import com.osiris.dyml.YamlSection;
import com.osiris.dyml.exceptions.*;
import com.velocitypowered.proxy.auth.database.Database;

import java.io.File;
import java.io.IOException;

public class Config extends Yaml {
    public YamlSection databaseRawUrl;
    public YamlSection databaseUrl;
    public YamlSection databaseUsername;
    public YamlSection databasePassword;
    public YamlSection whitelistMode;
    public YamlSection sessionMaxHours;
    public YamlSection minFailedLoginsForBan;
    public YamlSection failedLoginBanTime;
    public YamlSection minPasswordLength;

    public Config() throws YamlReaderException, YamlWriterException, IOException, DuplicateKeyException, IllegalListException, NotLoadedException, IllegalKeyException {
        super(new File(VelocityAuth.INSTANCE.authDirectory + "/config.yml"));
        this.load();
        databaseRawUrl = this.put("database", "raw-url").setDefValues(Database.rawUrl);
        databaseUrl = this.put("database", "url").setDefValues(Database.url);
        databaseUsername = this.put("database", "username");
        databasePassword = this.put("database", "password");

        whitelistMode = this.put("whitelist-mode").setCountTopLineBreaks(1)
                .setDefValues("false")
                .setComments("If true, not registered players will be blocked from joining the server (proxy).",
                        "Note that in this case your players will have to register themselves",
                        "over another platform, like a website for example.",
                        "If you proxy is in offline mode people are able to bypass this by naming themselves like registered users.");

        this.put("session").setCountTopLineBreaks(1).setComments("A session is created at successful player login and linked to the players ip.",
                "Players won't have to re-login every time they join,",
                " but only when their ip changes, or the session expires.");
        sessionMaxHours = this.put("session", "max-hours-valid").setDefValues("720").setComments("The maximum time (hours) a session is valid.",
                "Default is one month (30 days * 24h = 720h).");


        minFailedLoginsForBan = this.put("failed-logins").setCountTopLineBreaks(1)
                .setComments("The amount of failed logins required (within the last minute) before temporarily banning the player.",
                        "Default is 5 attempts.")
                .setDefValues("5");
        failedLoginBanTime = this.put("ban-time")
                .setComments("The time in seconds the user will be banned when failing the above count of logins.",
                        "Default is 10 seconds.")
                .setDefValues("10");
        minPasswordLength = this.put("min-password-length").setDefValues("10");


        this.put("debug").setCountTopLineBreaks(1).setComments("Options useful for debugging stuff.",
                "Could disappear in future releases without notice.",
                "Changing any of this values not advised.");
        this.save();
    }
}
