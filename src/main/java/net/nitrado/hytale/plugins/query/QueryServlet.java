package net.nitrado.hytale.plugins.query;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.protocol.ProtocolSettings;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.Universe;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.nitrado.hytale.plugins.webserver.authentication.HytaleUserPrincipal;
import net.nitrado.hytale.plugins.webserver.authorization.RequirePermissions;
import net.nitrado.hytale.plugins.webserver.util.RequestUtils;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;

public class QueryServlet extends HttpServlet {

    private static final String JSON_V1 = "application/x.hytale.nitrado.query+json;version=1";
    private static final String TEXT_HTML = "text/html";

    @Override
    @RequirePermissions(
            mode = RequirePermissions.Mode.ANY,
            value = {
                Permissions.WEB_READ_PLAYERS,
                Permissions.WEB_READ_SERVER,
                Permissions.WEB_READ_UNIVERSE,
                Permissions.WEB_READ_PLUGINS
            }
    )
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        var contentType = RequestUtils.negotiateContentType(
                req,
                JSON_V1,
                TEXT_HTML
        );

        if  (contentType == null) {
            resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }

        switch (contentType) {
            case JSON_V1:
                handleJsonV1(req, resp);
                break;
            case TEXT_HTML:
                handleHtml(req, resp);
                break;
        }
    }

    private void handleHtml(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(TEXT_HTML);
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println("HTML output not implemented yet. Append ?output=json query parameter for JSON response.");

        // TODO implement
    }

    protected void handleJsonV1(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON_V1);

        Document doc = new Document();

        var principal = req.getUserPrincipal();
        if (principal instanceof HytaleUserPrincipal user) {
            if (user.hasPermission(Permissions.WEB_READ_SERVER)) {
                this.addServerData(doc);
            }

            if (user.hasPermission(Permissions.WEB_READ_PLAYERS)) {
                this.addPlayerData(doc);
            }

            if (user.hasPermission(Permissions.WEB_READ_UNIVERSE)) {
                this.addUniverseData(doc);
            }

            if (user.hasPermission(Permissions.WEB_READ_PLUGINS)) {
                this.addPluginData(doc);
            }
        }

        resp.getWriter().println(doc.toJson(JsonWriterSettings.builder().indent(true).build()));
    }

    /**
     * Sets a Deprecation header used to signal that the negotiated content type has been deprecated
     * @param resp The HttpServletResponse to set the header on
     */
    private void setDeprecationHeader(HttpServletResponse resp) {
        resp.setHeader("Deprecation", "true");
    }

    protected void addServerData(Document doc) {
        doc.append("Server", new Document()
                .append("Name", HytaleServer.get().getServerName())
                .append("Version", ManifestUtil.getImplementationVersion())
                .append("Revision", ManifestUtil.getImplementationRevisionId())
                .append("Patchline", ManifestUtil.getPatchline())
                .append("ProtocolVersion", ProtocolSettings.PROTOCOL_VERSION)
                .append("ProtocolHash", ProtocolSettings.PROTOCOL_HASH)
                .append("MaxPlayers", HytaleServer.get().getConfig().getMaxPlayers())
        );
    }

    protected void addUniverseData(Document doc) {
        var defaultWorld = Universe.get().getDefaultWorld();
        doc.append("Universe", new Document()
            .append("CurrentPlayers", Universe.get().getPlayerCount())
            .append("DefaultWorld", defaultWorld == null ? null : defaultWorld.getName())
        );
    }

    protected void addPlayerData(Document doc) {
        var players = new Document();
        for (var entry : Universe.get().getWorlds().entrySet()) {
            var world = entry.getValue();
            for (var ref : world.getPlayerRefs()) {

                players.append(ref.getUuid().toString(), new Document()
                        .append("Name", ref.getUsername())
                        .append("UUID", ref.getUuid().toString())
                        .append("World", world.getName())
                );
            }
        }

        doc.append("Players", players);
    }

    protected void addPluginData(Document doc) {
        var pluginDoc =  new Document();
        var plugins = PluginManager.get().getPlugins();
        var pluginMap = new HashMap<PluginIdentifier, PluginBase>(plugins.size());

        for (var plugin : plugins) {
            pluginMap.put(plugin.getIdentifier(), plugin);
        }

        for (var manifest : PluginManager.get().getAvailablePlugins().values()) {
            var pluginIdentifier = new PluginIdentifier(manifest);
            pluginDoc.append(pluginIdentifier.toString(),
                    pluginToDoc(manifest, pluginMap.get(pluginIdentifier)));
        }

        doc.append("Plugins", pluginDoc);
    }

    protected Document pluginToDoc(@Nonnull PluginManifest manifest, @Nullable PluginBase plugin) {
        var result = new Document();

        result.append("Version", manifest.getVersion().toString());
        result.append("Loaded", plugin != null);
        if (plugin != null) {
            result.append("Enabled", plugin.isEnabled());
            result.append("State", plugin.getState().toString());
        }

        return result;
    }
}
