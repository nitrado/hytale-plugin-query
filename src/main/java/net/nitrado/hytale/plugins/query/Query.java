package net.nitrado.hytale.plugins.query;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.plugin.config.PluginIdentifier;
import net.nitrado.hytale.plugins.webserver.WebServer;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class Query extends JavaPlugin {

    public Query(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getLogger().atInfo().log("query setup");
        this.registerHandlers();
    }

    private void registerHandlers() {
        var plugin = PluginManager.get().getPlugin(new PluginIdentifier("Nitrado", "WebServer"));

        if (!(plugin instanceof WebServer webServer)) {
            return;
        }

        try {
            webServer
                    .createHandlerBuilder(this)
                    .requireAnyPermissionOf(
                            Permissions.PERMISSION_VIEW_PLAYERS,
                            Permissions.PERMISSION_VIEW_SERVER,
                            Permissions.PERMISSION_VIEW_UNIVERSE
                    )
                    .addServlet(new QueryServlet(), "/")
                    .register();
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).log("Failed to register route: " + e.getMessage());
        }
    }

    @Override
    protected void start() {
        getLogger().atInfo().log("query");
    }
}
