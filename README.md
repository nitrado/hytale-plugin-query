# Hytale Plugin Query
This plugin serves some basic information about a running Hytale server. It depends on
[Nitrado:WebServer](https://github.com/nitrado/hytale-plugin-webserver).

Example output of `GET /nitrado/query/`:

```json
{
  "Server": {
    "Name": "Hytale Server",
    "ProtocolVersion": "34f042349d72fb78c4891ed89b218eb55ab2cd69ffd1cdf52d66d71c814fcc7",
    "MaxPlayers": 100
  },
  "Players": {
    "Name": "MyName",
    "UUID": "e5c4ef9a-6281-406e-8a71-21028279f547",
    "World": "default"
  },
  "Universe": {
    "CurrentPlayers": 1
  }
}
```

## Installation
- Install the [Nitrado:WebServer](https://github.com/nitrado/hytale-plugin-webserver) plugin
- Copy the JAR of this plugin in your Hytale server's `plugins/` folder.

## Usage
The plugin uses the following permissions:
- `nitrado.query.view.server`
- `nitrado.query.view.players`
- `nitrado.query.view.universe`

A partial response will returned if the user only has a subset of those permissions.

You may decide to make some basic information available without user authentication. In
`permissions.json`, define:

```json
{
  "Groups": {
    "ANONYMOUS": [
      "nitrado.query.view.server",
      "nitrado.query.view.universe"
    ]
  }
}
```

## Contributing
Community contributions are welcome and encouraged. Please keep in mind though that this plugin
has a very narrow scope, so please open an Issue here on GitHub before working on a new feature.

### Security
If you believe to have found a security vulnerability, please report your findings via security@nitrado.net.