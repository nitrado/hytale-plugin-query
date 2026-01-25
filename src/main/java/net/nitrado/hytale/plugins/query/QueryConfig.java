package net.nitrado.hytale.plugins.query;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.HytaleServer;

import java.net.InetSocketAddress;

public class QueryConfig {
    public static final BuilderCodec<QueryConfig> CODEC = BuilderCodec.builder(QueryConfig.class, QueryConfig::new)
            .append(
                    new KeyedCodec<>("PublicAddress", Codec.STRING),
                    (config, value) -> config.publicAddressString = value,
                    config -> config.publicAddressString
            ).add()
            .build();

    private String publicAddressString = null;

    public String getPublicAddressString() {
        return publicAddressString;
    }

    public boolean hasPublicAddress() {
        return publicAddressString != null && !publicAddressString.isEmpty();
    }

    public InetSocketAddress getPublicAddress() {
        return parseAddress(publicAddressString);
    }

    public static InetSocketAddress parseAddress(String address) {
        if  (address == null || address.isEmpty()) {
            return null;
        }

        var addr = address.trim();

        // Handle IPv6 addresses (e.g., [::1]:25565 or [::1])
        if (addr.startsWith("[")) {
            int closingBracket = addr.indexOf(']');
            if (closingBracket == -1) {
                return null; // Invalid IPv6 format
            }
            String host = addr.substring(1, closingBracket);
            if (closingBracket == addr.length() - 1) {
                // No port specified: [::1]
                return InetSocketAddress.createUnresolved(host, HytaleServer.DEFAULT_PORT);
            }
            if (addr.charAt(closingBracket + 1) != ':') {
                return null; // Invalid format after closing bracket
            }
            try {
                int port = Integer.parseInt(addr.substring(closingBracket + 2));
                return InetSocketAddress.createUnresolved(host, port);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Handle IPv4 or hostname
        int colonIndex = addr.lastIndexOf(':');
        if (colonIndex == -1) {
            return InetSocketAddress.createUnresolved(addr, HytaleServer.DEFAULT_PORT);
        }

        // Check if there are multiple colons (IPv6 without brackets)
        if (addr.indexOf(':') != colonIndex) {
            // Bare IPv6 address without port
            return InetSocketAddress.createUnresolved(addr, HytaleServer.DEFAULT_PORT);
        }

        String host = addr.substring(0, colonIndex);
        try {
            int port = Integer.parseInt(addr.substring(colonIndex + 1));
            return InetSocketAddress.createUnresolved(host, port);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
