package net.nitrado.hytale.plugins.query;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.protocol.ProtocolSettings;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.Universe;
import org.bson.Document;

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryResponseV1 {

    private BasicData basic;
    private ServerData server;
    private UniverseData universe;
    private List<PlayerData> players;
    private List<PluginData> plugins;
    private InetSocketAddress address;

    public QueryResponseV1(InetSocketAddress publicAddress) {
        this.address = publicAddress;
    }

    public BasicData getBasic() {
        return basic;
    }

    public ServerData getServer() {
        return server;
    }

    public UniverseData getUniverse() {
        return universe;
    }

    public List<PlayerData> getPlayers() {
        return players;
    }

    public List<PluginData> getPlugins() {
        return plugins;
    }

    public void addBasicData() {
        this.basic = new BasicData(
                HytaleServer.get().getServerName(),
                ManifestUtil.getImplementationVersion(),
                HytaleServer.get().getConfig().getMaxPlayers(),
                Universe.get().getPlayerCount(),
                address
        );
    }

    public void addServerData() {
        this.server = new ServerData(
                HytaleServer.get().getServerName(),
                ManifestUtil.getImplementationVersion(),
                ManifestUtil.getImplementationRevisionId(),
                ManifestUtil.getPatchline(),
                ProtocolSettings.PROTOCOL_VERSION,
                ProtocolSettings.PROTOCOL_HASH,
                HytaleServer.get().getConfig().getMaxPlayers(),
                address
        );
    }

    public void addUniverseData() {
        var defaultWorld = Universe.get().getDefaultWorld();
        this.universe = new UniverseData(
                Universe.get().getPlayerCount(),
                defaultWorld == null ? null : defaultWorld.getName()
        );
    }

    public void addPlayerData() {
        this.players = new ArrayList<>();
        for (var entry : Universe.get().getWorlds().entrySet()) {
            var world = entry.getValue();
            for (var ref : world.getPlayerRefs()) {
                players.add(new PlayerData(
                        ref.getUsername(),
                        ref.getUuid().toString(),
                        world.getName()
                ));
            }
        }
    }

    public void addPluginData() {
        this.plugins = new ArrayList<>();
        var pluginsList = PluginManager.get().getPlugins();
        var pluginMap = new HashMap<PluginIdentifier, PluginBase>(pluginsList.size());

        for (var plugin : pluginsList) {
            pluginMap.put(plugin.getIdentifier(), plugin);
        }

        for (var manifest : PluginManager.get().getAvailablePlugins().values()) {
            var pluginIdentifier = new PluginIdentifier(manifest);
            var plugin = pluginMap.get(pluginIdentifier);
            this.plugins.add(new PluginData(
                    pluginIdentifier.toString(),
                    manifest.getVersion().toString(),
                    plugin != null,
                    plugin != null ? plugin.isEnabled() : null,
                    plugin != null ? plugin.getState().toString() : null
            ));
        }
    }

    public Document toDocument() {
        Document doc = new Document();

        if (basic != null) {
            var basicDoc = new Document();
            basicDoc.append("Name", basic.name())
                    .append("Version", basic.version())
                    .append("MaxPlayers", basic.maxPlayers())
                    .append("CurrentPlayers", basic.currentPlayers());

            if (basic.address() != null) {
                basicDoc.append("Address", formatAddress(basic.address()));
            }

            doc.append("Basic", basicDoc);
        }

        if (server != null) {
            var serverDoc = new Document();
            serverDoc
                    .append("Name", server.name())
                    .append("Version", server.version())
                    .append("Revision", server.revision())
                    .append("Patchline", server.patchline())
                    .append("ProtocolVersion", server.protocolVersion())
                    .append("ProtocolHash", server.protocolHash())
                    .append("MaxPlayers", server.maxPlayers());

            if (server.address() != null) {
                serverDoc.append("Address", formatAddress(server.address()));
            }

            doc.append("Server", serverDoc);
        }

        if (universe != null) {
            doc.append("Universe", new Document()
                    .append("CurrentPlayers", universe.currentPlayers())
                    .append("DefaultWorld", universe.defaultWorld())
            );
        }

        if (players != null) {
            var playerDocs = new ArrayList<Document>();
            for (var player : players) {
                playerDocs.add(new Document()
                        .append("Name", player.name())
                        .append("UUID", player.uuid())
                        .append("World", player.world())
                );
            }
            doc.append("Players", playerDocs);
        }

        if (plugins != null) {
            var pluginDoc = new Document();
            for (var plugin : plugins) {
                var entry = new Document()
                        .append("Version", plugin.version())
                        .append("Loaded", plugin.loaded());
                if (plugin.loaded()) {
                    entry.append("Enabled", plugin.enabled())
                          .append("State", plugin.state());
                }
                pluginDoc.append(plugin.identifier(), entry);
            }
            doc.append("Plugins", pluginDoc);
        }

        return doc;
    }

    public record BasicData(
            String name,
            String version,
            int maxPlayers,
            int currentPlayers,
            InetSocketAddress address
    ) {
        public String formattedAddress() {
            return address == null ? null : formatAddress(address);
        }
    }

    public record ServerData(
            String name,
            String version,
            String revision,
            String patchline,
            int protocolVersion,
            String protocolHash,
            int maxPlayers,
            InetSocketAddress address
    ) {
        public String formattedAddress() {
            return address == null ? null : formatAddress(address);
        }
    }

    public record UniverseData(
            int currentPlayers,
            String defaultWorld
    ) {}

    public record PlayerData(
            String name,
            String uuid,
            String world
    ) {}

    public record PluginData(
            String identifier,
            String version,
            boolean loaded,
            Boolean enabled,
            String state
    ) {}

    private static String formatAddress(InetSocketAddress address) {
            String formatted;

            formatted = address.getHostString();
            if (formatted.contains(":")) {
                // IPv6
                formatted = "[" + formatted + "]";
            }

            return formatted + ":" + address.getPort();
    }
}
