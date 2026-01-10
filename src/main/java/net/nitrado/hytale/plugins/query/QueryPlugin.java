package net.nitrado.hytale.plugins.query;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import net.nitrado.hytale.plugins.webserver.WebServerPlugin;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class QueryPlugin extends JavaPlugin {

    private WebServerPlugin webServerPlugin;

    public QueryPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.registerHandlers();
    }

    private void registerHandlers() {
        var plugin = PluginManager.get().getPlugin(new PluginIdentifier("Nitrado", "WebServer"));

        if (!(plugin instanceof WebServerPlugin webServer)) {
            return;
        }

        this.webServerPlugin = webServer;

        try {
            webServerPlugin.addServlet(this, "", new QueryServlet());
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to register route.");
        }
    }

    @Override
    protected void shutdown() {
        webServerPlugin.removeServlets(this);
    }
}
