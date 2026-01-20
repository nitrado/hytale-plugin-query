package net.nitrado.hytale.plugins.query;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.util.Config;
import net.nitrado.hytale.plugins.webserver.WebServerPlugin;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.logging.Level;

public class QueryPlugin extends JavaPlugin {

    private WebServerPlugin webServerPlugin;
    private final Config<QueryConfig> _config = withConfig(QueryConfig.CODEC);
    private InetSocketAddress publicAddress = null;

    public QueryPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        var config = this._config.get();
        this.publicAddress = config.getPublicAddress();

        if (config.hasPublicAddress() && this.publicAddress == null) {
            getLogger().atWarning().log("invalid public address: %s", config.getPublicAddressString());
        }

        this.registerHandlers();
    }

    private void registerHandlers() {
        var plugin = PluginManager.get().getPlugin(new PluginIdentifier("Nitrado", "WebServer"));

        if (!(plugin instanceof WebServerPlugin webServer)) {
            return;
        }

        this.webServerPlugin = webServer;

        var templateEngine = this.webServerPlugin.getTemplateEngineFactory().getEngineFor(this);

        try {
            webServerPlugin.addServlet(this, "", new QueryServlet(templateEngine, publicAddress));
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to register route.");
        }
    }

    @Override
    protected void shutdown() {
        webServerPlugin.removeServlets(this);
    }
}
