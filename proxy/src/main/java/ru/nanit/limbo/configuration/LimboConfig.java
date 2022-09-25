/*
 * Copyright (C) 2020 Nan1t
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.nanit.limbo.configuration;

import com.osiris.dyml.Yaml;
import com.osiris.dyml.exceptions.DuplicateKeyException;
import com.osiris.dyml.exceptions.IllegalListException;
import com.osiris.dyml.exceptions.YamlReaderException;
import ru.nanit.limbo.server.data.BossBar;
import ru.nanit.limbo.server.data.InfoForwarding;
import ru.nanit.limbo.server.data.PingData;
import ru.nanit.limbo.server.data.Title;
import ru.nanit.limbo.util.Colors;
import ru.nanit.limbo.world.Location;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class LimboConfig extends Yaml {
    public final Path root;

    public IpAddressWrapper address;
    public int maxPlayers;
    public PingData pingData;

    public String dimensionType;
    public Location spawnPosition;
    public int gameMode;

    public boolean useBrandName;
    public boolean useJoinMessage;
    public boolean useBossBar;
    public boolean useTitle;
    public boolean usePlayerList;
    public boolean useHeaderAndFooter;

    public String brandName;
    public String joinMessage;
    public BossBar bossBar;
    public Title title;

    public String playerListUsername;
    public String playerListHeader;
    public String playerListFooter;

    public InfoForwarding infoForwarding;
    public long readTimeout;
    public int debugLevel;

    public boolean useEpoll;
    public int bossGroupSize;
    public int workerGroupSize;

    public LimboConfig(Path root) {
        super(Paths.get(root.toString(), "settings.yml").toFile());
        this.root = root;
    }

    @Override
    public Yaml load() throws IOException, YamlReaderException, IllegalListException, DuplicateKeyException {
        try {
            writeDefaultsIfNeeded();
            super.load();

            address = put("bind").as(IpAddressWrapper.class);
            maxPlayers = put("maxPlayers").asInt();
            pingData = put("ping").as(PingData.class);
            dimensionType = put("dimension").asString();
            if (dimensionType.equalsIgnoreCase("nether")) {
                dimensionType = "the_nether";
            }
            if (dimensionType.equalsIgnoreCase("end")) {
                dimensionType = "the_end";
            }
            spawnPosition = put("spawnPosition").as(Location.class);
            gameMode = put("gameMode").asInt();
            useBrandName = put("brandName", "enable").asBoolean();
            useJoinMessage = put("joinMessage", "enable").asBoolean();
            useBossBar = put("bossBar", "enable").asBoolean();
            useTitle = put("title", "enable").asBoolean();
            usePlayerList = put("playerList", "enable").asBoolean();
            playerListUsername = put("playerList", "username").asString();
            useHeaderAndFooter = put("headerAndFooter", "enable").asBoolean();

            if (useBrandName)
                brandName = put("brandName", "content").asString();

            if (useJoinMessage)
                joinMessage = Colors.of(put("joinMessage", "text").asString());

            if (useBossBar)
                bossBar = put("bossBar").as(BossBar.class);

            if (useTitle)
                title = put("title").as(Title.class);

            if (useHeaderAndFooter) {
                playerListHeader = Colors.of(put("headerAndFooter", "header").asString());
                playerListFooter = Colors.of(put("headerAndFooter", "footer").asString());
            }

            infoForwarding = put("infoForwarding").as(InfoForwarding.class);
            readTimeout = put("readTimeout").asLong();
            debugLevel = put("debugLevel").asInt();

            useEpoll = put("netty", "useEpoll").setDefValues("true").asBoolean();
            bossGroupSize = put("netty", "threads", "bossGroup").setDefValues("1").asInt();
            workerGroupSize = put("netty", "threads", "workerGroup").setDefValues("4").asInt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private void writeDefaultsIfNeeded() throws IOException {
        if (!getFile().exists() || getFile().length() == 0) {
            InputStream stream = getClass().getResourceAsStream("/" + getFile().getName());

            if (stream == null)
                throw new FileNotFoundException("Cannot find settings resource file");

            Files.copy(stream, getFile().toPath());
        }
    }

    public SocketAddress getAddress() {
        return address.toInetSocketAddress();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public PingData getPingData() {
        return pingData;
    }

    public String getDimensionType() {
        return dimensionType;
    }

    public Location getSpawnPosition() {
        return spawnPosition;
    }

    public int getGameMode() {
        return gameMode;
    }

    public InfoForwarding getInfoForwarding() {
        return infoForwarding;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public int getDebugLevel() {
        return debugLevel;
    }

    public boolean isUseBrandName() {
        return useBrandName;
    }

    public boolean isUseJoinMessage() {
        return useJoinMessage;
    }

    public boolean isUseBossBar() {
        return useBossBar;
    }

    public boolean isUseTitle() {
        return useTitle;
    }

    public boolean isUsePlayerList() {
        return usePlayerList;
    }

    public boolean isUseHeaderAndFooter() {
        return useHeaderAndFooter;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getJoinMessage() {
        return joinMessage;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public Title getTitle() {
        return title;
    }

    public String getPlayerListUsername() {
        return playerListUsername;
    }

    public String getPlayerListHeader() {
        return playerListHeader;
    }

    public String getPlayerListFooter() {
        return playerListFooter;
    }

    public boolean isUseEpoll() {
        return useEpoll;
    }

    public int getBossGroupSize() {
        return bossGroupSize;
    }

    public int getWorkerGroupSize() {
        return workerGroupSize;
    }
}
