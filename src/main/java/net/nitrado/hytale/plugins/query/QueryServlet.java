package net.nitrado.hytale.plugins.query;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.nitrado.hytale.plugins.webserver.WebServerPlugin;
import net.nitrado.hytale.plugins.webserver.authentication.HytaleUserPrincipal;
import net.nitrado.hytale.plugins.webserver.authorization.RequirePermissions;
import net.nitrado.hytale.plugins.webserver.servlets.TemplateServlet;
import net.nitrado.hytale.plugins.webserver.util.RequestUtils;
import org.bson.json.JsonWriterSettings;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class QueryServlet extends TemplateServlet {

    private static final String JSON_V1 = "application/x.hytale.nitrado.query+json;version=1";
    private static final String TEXT_HTML = "text/html";

    private JakartaServletWebApplication webApplication;
    private TemplateEngine templateEngine;
    private InetSocketAddress publicAddress;

    public QueryServlet(WebServerPlugin parentPlugin, JavaPlugin thisPlugin, TemplateEngine templateEngine, InetSocketAddress publicAddress) {
        super(parentPlugin, thisPlugin);

        this.templateEngine = templateEngine;
        this.publicAddress = publicAddress;
    }

    private JakartaServletWebApplication getWebApplication() {
        if (webApplication == null) {
            webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        }
        return webApplication;
    }

    @Override
    @RequirePermissions(
            mode = RequirePermissions.Mode.ANY,
            value = {
                Permissions.WEB_READ_BASIC,
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
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType(TEXT_HTML);

        var response = buildQueryResponse(req);

        var vars = new HashMap<String, Object>();
        vars.put("response", response);

        this.renderTemplate(req, resp, "nitrado.query", vars);
    }

    protected void handleJsonV1(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(JSON_V1);

        var response = buildQueryResponse(req);
        resp.getWriter().println(response.toDocument().toJson(JsonWriterSettings.builder().indent(true).build()));
    }

    protected QueryResponseV1 buildQueryResponse(HttpServletRequest req) {
        var response = new QueryResponseV1(publicAddress);

        var principal = req.getUserPrincipal();
        if (principal instanceof HytaleUserPrincipal user) {
            if (user.hasPermission(Permissions.WEB_READ_BASIC)) {
                response.addBasicData();
            }

            if (user.hasPermission(Permissions.WEB_READ_SERVER)) {
                response.addServerData();
            }

            if (user.hasPermission(Permissions.WEB_READ_PLAYERS)) {
                response.addPlayerData();
            }

            if (user.hasPermission(Permissions.WEB_READ_UNIVERSE)) {
                response.addUniverseData();
            }

            if (user.hasPermission(Permissions.WEB_READ_PLUGINS)) {
                response.addPluginData();
            }
        }

        return response;
    }

    /**
     * Sets a Deprecation header used to signal that the negotiated content type has been deprecated
     * @param resp The HttpServletResponse to set the header on
     */
    private void setDeprecationHeader(HttpServletResponse resp) {
        resp.setHeader("Deprecation", "true");
    }
}
