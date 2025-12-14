package net.nitrado.hytale.plugins.query;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.plugin.config.PluginIdentifier;
import net.nitrado.hytale.plugins.webserver.WebServerPlugin;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class Query extends JavaPlugin {

    public Query(@Nonnull JavaPluginInit init) {
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

        try {
            webServer
                    .createHandlerBuilder(this)
                    .requireAnyPermissionOf(
                            Permissions.VIEW_PLAYERS,
                            Permissions.VIEW_SERVER,
                            Permissions.VIEW_UNIVERSE
                    )
                    .addServlet(new QueryServlet(), "/")
                    .register();
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).log("Failed to register route: " + e.getMessage());
        }
    }

    @Override
    protected void start() {}
}
