# Hytale Query Plugin

This plugin exposes server information via an HTTP endpoint. It depends on
[Nitrado:WebServer](https://github.com/nitrado/hytale-plugin-webserver).

## Purpose of this Plugin

When running a Hytale server, external systems often need access to basic server information for
monitoring, dashboards, or integration with other services. This plugin provides a simple HTTP
endpoint that returns structured JSON data about the server, universe, players, and installed plugins.

## Main Features

- **Server Information:** Exposes server name, version, protocol details, and maximum player count.
- **Universe Data:** Returns current player count and default world name.
- **Player List:** Provides a list of connected players with their names, UUIDs, and current world.
- **Plugin Status:** Shows installed plugins with their versions, load state, and enabled status.
- **Permission-Based Responses:** Returns only the data sections the authenticated user has permission to view.

## Installation

1. Install the [Nitrado:WebServer](https://github.com/nitrado/hytale-plugin-webserver) plugin.
2. Copy the JAR of this plugin into your Hytale server's `mods/` folder.

## Usage

### Endpoint

The plugin registers a single endpoint at `GET /Nitrado/Query`.

### API Versioning

The API uses content negotiation for versioning. Set the `Accept` header to request a specific version:

| Content Type                                        | Description                                    |
|-----------------------------------------------------|------------------------------------------------|
| `application/x.hytale.nitrado.query+json;version=1` | JSON response (current)                        |
| `application/json`                                  | Returns current Content Type (not recommended) |
| `text/html`                                         | HTML response when user visits with a browser  |

If no acceptable Content Type is provided, the server returns `406 Not Acceptable`.

**Example request:**
```http
GET /Nitrado/Query HTTP/1.1
Accept: application/x.hytale.nitrado.query+json;version=1
```

Consumers of this API should always explicitly set a list of accepted Content Types. When a breaking change is
introduced to this API (such as removal of a field), a new version of the Content Type will be introduced. The
old version will still be supported for an extended period of time. Consumers should check for the presence
of a `Deprecation` header in responses to notice that they should update to a new Content Type.

Consumers may also pass multiple supported versions in their `Accept` header for backwards compatibility with
outdated versions of this plugin. Given equal quality (parameter `q` in the `Accept` header), the plugin
will always respond with the newest supported and accepted type.

### Example Response

```json
{
  "Server": {
    "Name": "Hytale Server",
    "Version": "2026.01.10-ab2cd69ff",
    "Revision": "ab2cd69ff816cb831f8792a0782938dce22eeadc",
    "Patchline": "release",
    "ProtocolVersion": 1,
    "ProtocolHash": "34f442449d72fb78c4891edf9b218eb55ab2ad69ffd1cd152d66d71c814fcc7",
    "MaxPlayers": 100
  },
  "Universe": {
    "CurrentPlayers": 1,
    "DefaultWorld": "default"
  },
  "Players": {
    "e5c4ef9a-6281-406e-8a71-21028279f547": {
      "Name": "MyName",
      "UUID": "e5c4ef9a-6281-406e-8a71-21028279f547",
      "World": "default"
    }
  },
  "Plugins": {
    "Nitrado:Query": {
      "Version": "1.0.0",
      "Loaded": true,
      "Enabled": true,
      "State": "ENABLED"
    }
  }
}
```

### Permissions

The response is filtered based on the authenticated user's permissions:

| Permission                        | Data Section |
|-----------------------------------|--------------|
| `nitrado.query.web.read.server`   | Server       |
| `nitrado.query.web.read.universe` | Universe     |
| `nitrado.query.web.read.players`  | Players      |
| `nitrado.query.web.read.plugins`  | Plugins      |

A partial response is returned if the user only has a subset of these permissions.

### Making Data Publicly Accessible

To expose basic information without user authentication, configure the `ANONYMOUS` group in
`permissions.json`:

```json
{
  "Groups": {
    "ANONYMOUS": [
      "nitrado.query.web.read.server",
      "nitrado.query.web.read.universe"
    ]
  }
}
```

## Contributing

Community contributions are welcome and encouraged. Please keep in mind that this plugin
has a very narrow scope, so please open an Issue here on GitHub before working on a new feature.

### Security

If you believe to have found a security vulnerability, please report your findings via security@nitrado.net.