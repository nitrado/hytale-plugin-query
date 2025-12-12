package net.nitrado.hytale.plugins.query;

import com.hypixel.hytale.protocol.Settings;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.Universe;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.nitrado.hytale.plugins.webserver.auth.HytaleUserPrincipal;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import java.io.IOException;

public class QueryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");

        Document doc = new Document();

        var principal = req.getUserPrincipal();
        if (principal instanceof HytaleUserPrincipal user) {
            if (user.hasPermission(Permissions.VIEW_SERVER)) {
                this.addServerData(doc);
            }

            if (user.hasPermission(Permissions.VIEW_PLAYERS)) {
                this.addPlayerData(doc);
            }

            if (user.hasPermission(Permissions.VIEW_UNIVERSE)) {
                this.addUniverseData(doc);
            }
        }

        resp.getWriter().println(doc.toJson(JsonWriterSettings.builder().indent(true).build()));
    }

    protected void addServerData(Document doc) {
        doc.append("Server", new Document()
                .append("Name", HytaleServer.get().getServerName())
                .append("ProtocolVersion", Settings.VERSION_HASH)
                .append("MaxPlayers", HytaleServer.get().getConfig().getMaxPlayers())
        );
    }

    protected void addUniverseData(Document doc) {
        doc.append("Universe", new Document()
                .append("CurrentPlayers", Universe.get().getPlayerCount())
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
}
