# Golem MCP Tools - Technical Specification

## Overview

This document provides detailed specifications for all MCP tools provided by the Golem plugin. Each tool follows the MCP protocol specification and includes JSON schema definitions for parameters and responses.

## Tool Categories

1. Player Management Tools
2. World Information Tools
3. Server Statistics Tools
4. Admin Command Tools

---

## 1. Player Management Tools

### 1.1 list_players

**Description:** Lists all currently online players on the server.

**Permission:** `golem.tool.list_players`

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "players": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "username": { "type": "string" },
          "uuid": { "type": "string" },
          "displayName": { "type": "string" },
          "world": { "type": "string" },
          "location": {
            "type": "object",
            "properties": {
              "x": { "type": "number" },
              "y": { "type": "number" },
              "z": { "type": "number" },
              "yaw": { "type": "number" },
              "pitch": { "type": "number" }
            }
          },
          "gamemode": { "type": "string", "enum": ["SURVIVAL", "CREATIVE", "ADVENTURE", "SPECTATOR"] },
          "health": { "type": "number" },
          "foodLevel": { "type": "integer" },
          "level": { "type": "integer" },
          "isOp": { "type": "boolean" }
        }
      }
    },
    "count": { "type": "integer" }
  }
}
```

**Example Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/call",
  "params": {
    "name": "list_players",
    "arguments": {}
  }
}
```

**Example Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Online Players (2):\n\n1. Steve\n   - UUID: 069a79f4-44e9-4726-a5be-fca90e38aaf5\n   - World: world\n   - Location: 100.5, 64.0, -200.3\n   - Gamemode: SURVIVAL\n   - Health: 20.0/20.0\n\n2. Alex\n   - UUID: 853c80ef-3c37-49fd-aa49-938b674adae6\n   - World: world_nether\n   - Location: 50.2, 70.0, -100.8\n   - Gamemode: CREATIVE\n   - Health: 20.0/20.0"
      }
    ],
    "isError": false
  }
}
```

---

### 1.2 get_player_info

**Description:** Retrieves detailed information about a specific player.

**Permission:** `golem.tool.get_player_info`

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "player": {
      "type": "string",
      "description": "Player username or UUID"
    }
  },
  "required": ["player"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "username": { "type": "string" },
    "uuid": { "type": "string" },
    "displayName": { "type": "string" },
    "online": { "type": "boolean" },
    "world": { "type": "string" },
    "location": { "type": "object" },
    "gamemode": { "type": "string" },
    "health": { "type": "number" },
    "maxHealth": { "type": "number" },
    "foodLevel": { "type": "integer" },
    "level": { "type": "integer" },
    "experience": { "type": "number" },
    "isOp": { "type": "boolean" },
    "isBanned": { "type": "boolean" },
    "firstPlayed": { "type": "string", "format": "date-time" },
    "lastPlayed": { "type": "string", "format": "date-time" },
    "playTime": { "type": "integer", "description": "Total play time in seconds" },
    "inventory": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "slot": { "type": "integer" },
          "material": { "type": "string" },
          "amount": { "type": "integer" },
          "displayName": { "type": "string" }
        }
      }
    }
  }
}
```

**Example Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/call",
  "params": {
    "name": "get_player_info",
    "arguments": {
      "player": "Steve"
    }
  }
}
```

---

### 1.3 kick_player

**Description:** Kicks a player from the server.

**Permission:** `golem.tool.kick_player` (requires operator)

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "player": {
      "type": "string",
      "description": "Player username or UUID"
    },
    "reason": {
      "type": "string",
      "description": "Reason for kicking the player",
      "default": "Kicked by an operator"
    }
  },
  "required": ["player"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "success": { "type": "boolean" },
    "player": { "type": "string" },
    "reason": { "type": "string" },
    "message": { "type": "string" }
  }
}
```

**Example Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "tools/call",
  "params": {
    "name": "kick_player",
    "arguments": {
      "player": "Steve",
      "reason": "Violating server rules"
    }
  }
}
```

---

### 1.4 ban_player

**Description:** Bans a player from the server.

**Permission:** `golem.tool.ban_player` (requires operator)

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "player": {
      "type": "string",
      "description": "Player username or UUID"
    },
    "reason": {
      "type": "string",
      "description": "Reason for banning the player",
      "default": "Banned by an operator"
    },
    "duration": {
      "type": "integer",
      "description": "Ban duration in seconds (null for permanent)",
      "default": null
    }
  },
  "required": ["player"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "success": { "type": "boolean" },
    "player": { "type": "string" },
    "reason": { "type": "string" },
    "duration": { "type": "integer" },
    "expiresAt": { "type": "string", "format": "date-time" },
    "message": { "type": "string" }
  }
}
```

---

### 1.5 teleport_player

**Description:** Teleports a player to a location or another player.

**Permission:** `golem.tool.teleport_player` (requires operator)

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "player": {
      "type": "string",
      "description": "Player to teleport"
    },
    "target": {
      "type": "string",
      "description": "Target player name or coordinates (x,y,z) or (x,y,z,world)"
    }
  },
  "required": ["player", "target"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "success": { "type": "boolean" },
    "player": { "type": "string" },
    "target": { "type": "string" },
    "location": {
      "type": "object",
      "properties": {
        "x": { "type": "number" },
        "y": { "type": "number" },
        "z": { "type": "number" },
        "world": { "type": "string" }
      }
    },
    "message": { "type": "string" }
  }
}
```

**Example Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "4",
  "method": "tools/call",
  "params": {
    "name": "teleport_player",
    "arguments": {
      "player": "Steve",
      "target": "100,64,-200"
    }
  }
}
```

---

## 2. World Information Tools

### 2.1 list_worlds

**Description:** Lists all loaded worlds on the server.

**Permission:** `golem.tool.list_worlds`

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "worlds": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "name": { "type": "string" },
          "type": { "type": "string", "enum": ["NORMAL", "NETHER", "THE_END", "CUSTOM"] },
          "environment": { "type": "string" },
          "difficulty": { "type": "string" },
          "playerCount": { "type": "integer" },
          "time": { "type": "integer" },
          "weather": { "type": "string" },
          "spawnLocation": {
            "type": "object",
            "properties": {
              "x": { "type": "integer" },
              "y": { "type": "integer" },
              "z": { "type": "integer" }
            }
          }
        }
      }
    },
    "count": { "type": "integer" }
  }
}
```

---

### 2.2 get_world_info

**Description:** Retrieves detailed information about a specific world.

**Permission:** `golem.tool.get_world_info`

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "world": {
      "type": "string",
      "description": "World name"
    }
  },
  "required": ["world"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "name": { "type": "string" },
    "type": { "type": "string" },
    "environment": { "type": "string" },
    "difficulty": { "type": "string" },
    "hardcore": { "type": "boolean" },
    "pvp": { "type": "boolean" },
    "time": { "type": "integer" },
    "fullTime": { "type": "integer" },
    "weather": { "type": "string" },
    "thundering": { "type": "boolean" },
    "seed": { "type": "integer" },
    "spawnLocation": { "type": "object" },
    "worldBorder": {
      "type": "object",
      "properties": {
        "center": { "type": "object" },
        "size": { "type": "number" },
        "damageAmount": { "type": "number" }
      }
    },
    "playerCount": { "type": "integer" },
    "entityCount": { "type": "integer" },
    "chunkCount": { "type": "integer" }
  }
}
```

---

### 2.3 set_world_time

**Description:** Sets the time in a specific world.

**Permission:** `golem.tool.set_world_time` (requires operator)

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "world": {
      "type": "string",
      "description": "World name"
    },
    "time": {
      "type": "integer",
      "description": "Time value (0-24000) or preset (day, noon, night, midnight)",
      "oneOf": [
        { "type": "integer", "minimum": 0, "maximum": 24000 },
        { "type": "string", "enum": ["day", "noon", "night", "midnight"] }
      ]
    }
  },
  "required": ["world", "time"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "success": { "type": "boolean" },
    "world": { "type": "string" },
    "time": { "type": "integer" },
    "message": { "type": "string" }
  }
}
```

---

### 2.4 set_world_weather

**Description:** Sets the weather in a specific world.

**Permission:** `golem.tool.set_world_weather` (requires operator)

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "world": {
      "type": "string",
      "description": "World name"
    },
    "weather": {
      "type": "string",
      "enum": ["clear", "rain", "thunder"],
      "description": "Weather type"
    },
    "duration": {
      "type": "integer",
      "description": "Duration in seconds (optional)",
      "default": null
    }
  },
  "required": ["world", "weather"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "success": { "type": "boolean" },
    "world": { "type": "string" },
    "weather": { "type": "string" },
    "duration": { "type": "integer" },
    "message": { "type": "string" }
  }
}
```

---

## 3. Server Statistics Tools

### 3.1 get_server_stats

**Description:** Retrieves current server performance statistics.

**Permission:** `golem.tool.get_server_stats`

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "tps": {
      "type": "object",
      "properties": {
        "current": { "type": "number" },
        "average1m": { "type": "number" },
        "average5m": { "type": "number" },
        "average15m": { "type": "number" }
      }
    },
    "memory": {
      "type": "object",
      "properties": {
        "used": { "type": "integer", "description": "Used memory in bytes" },
        "free": { "type": "integer" },
        "total": { "type": "integer" },
        "max": { "type": "integer" }
      }
    },
    "players": {
      "type": "object",
      "properties": {
        "online": { "type": "integer" },
        "max": { "type": "integer" }
      }
    },
    "uptime": {
      "type": "integer",
      "description": "Server uptime in seconds"
    },
    "timestamp": {
      "type": "string",
      "format": "date-time"
    }
  }
}
```

---

### 3.2 get_server_info

**Description:** Retrieves server configuration and version information.

**Permission:** `golem.tool.get_server_info`

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "name": { "type": "string" },
    "version": { "type": "string" },
    "minecraftVersion": { "type": "string" },
    "platform": { "type": "string", "enum": ["Paper", "Velocity", "Fabric"] },
    "motd": { "type": "string" },
    "maxPlayers": { "type": "integer" },
    "viewDistance": { "type": "integer" },
    "simulationDistance": { "type": "integer" },
    "onlineMode": { "type": "boolean" },
    "hardcore": { "type": "boolean" },
    "difficulty": { "type": "string" },
    "defaultGamemode": { "type": "string" },
    "plugins": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "name": { "type": "string" },
          "version": { "type": "string" },
          "enabled": { "type": "boolean" }
        }
      }
    }
  }
}
```

---

### 3.3 get_performance_report

**Description:** Generates a detailed performance analysis report.

**Permission:** `golem.tool.get_performance_report` (requires operator)

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "tps": { "type": "object" },
    "memory": { "type": "object" },
    "worlds": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "name": { "type": "string" },
          "chunks": { "type": "integer" },
          "entities": { "type": "integer" },
          "tileEntities": { "type": "integer" }
        }
      }
    },
    "plugins": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "name": { "type": "string" },
          "timings": { "type": "number", "description": "Average execution time in ms" }
        }
      }
    },
    "recommendations": {
      "type": "array",
      "items": { "type": "string" }
    }
  }
}
```

---

## 4. Admin Command Tools

### 4.1 broadcast_message

**Description:** Broadcasts a message to all online players.

**Permission:** `golem.tool.broadcast_message` (requires operator)

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "message": {
      "type": "string",
      "description": "Message to broadcast"
    },
    "format": {
      "type": "string",
      "enum": ["plain", "legacy", "minimessage"],
      "default": "plain",
      "description": "Message format type"
    }
  },
  "required": ["message"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "success": { "type": "boolean" },
    "message": { "type": "string" },
    "recipientCount": { "type": "integer" }
  }
}
```

**Example Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "5",
  "method": "tools/call",
  "params": {
    "name": "broadcast_message",
    "arguments": {
      "message": "Server will restart in 5 minutes!",
      "format": "plain"
    }
  }
}
```

---

### 4.2 execute_console_command

**Description:** Executes a command as the server console.

**Permission:** `golem.tool.execute_console_command` (requires operator)

**Security:** Subject to whitelist/blacklist configuration

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "command": {
      "type": "string",
      "description": "Command to execute (without leading slash)"
    }
  },
  "required": ["command"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "success": { "type": "boolean" },
    "command": { "type": "string" },
    "output": { "type": "string" },
    "blocked": { "type": "boolean" },
    "reason": { "type": "string" }
  }
}
```

**Example Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "6",
  "method": "tools/call",
  "params": {
    "name": "execute_console_command",
    "arguments": {
      "command": "whitelist add Steve"
    }
  }
}
```

**Blocked Commands (Default):**
- `stop`
- `restart`
- `op`
- `deop`
- Any command in blacklist configuration

---

### 4.3 reload_config

**Description:** Reloads the Golem plugin configuration.

**Permission:** `golem.tool.reload_config` (requires operator)

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "success": { "type": "boolean" },
    "message": { "type": "string" },
    "errors": {
      "type": "array",
      "items": { "type": "string" }
    }
  }
}
```

---

## Error Handling

### Standard Error Codes

| Code | Name | Description |
|------|------|-------------|
| -32700 | Parse error | Invalid JSON |
| -32600 | Invalid Request | Invalid JSON-RPC request |
| -32601 | Method not found | Tool does not exist |
| -32602 | Invalid params | Invalid tool parameters |
| -32603 | Internal error | Server error |
| -32000 | Permission denied | Insufficient permissions |
| -32001 | Player not found | Specified player not found |
| -32002 | World not found | Specified world not found |
| -32003 | Command blocked | Command is blacklisted |
| -32004 | Tool disabled | Tool is disabled in config |

### Error Response Format

```json
{
  "jsonrpc": "2.0",
  "id": "request-id",
  "error": {
    "code": -32001,
    "message": "Player not found",
    "data": {
      "player": "NonExistentPlayer",
      "suggestion": "Use list_players to see online players"
    }
  }
}
```

---

## Tool Execution Context

Each tool receives a [`ToolContext`]() object containing:

```kotlin
data class ToolContext(
    val executor: String,           // Who is executing the tool
    val permissions: Set<String>,   // Available permissions
    val config: GolemConfig,        // Current configuration
    val timestamp: Instant          // Execution timestamp
)
```

---

## Rate Limiting

Future enhancement: Rate limiting per tool or per client

```yaml
golem:
  rate_limiting:
    enabled: true
    default_limit: 60  # requests per minute
    tool_limits:
      execute_console_command: 10
      kick_player: 20
      ban_player: 10
```

---

## Audit Logging

All tool executions are logged:

```
[INFO] [Golem] Tool executed: list_players by console (success)
[INFO] [Golem] Tool executed: kick_player by console (success) - player: Steve, reason: Testing
[WARN] [Golem] Tool execution failed: ban_player by console - Player not found: NonExistent
[WARN] [Golem] Tool execution blocked: execute_console_command by console - Command blacklisted: stop
```

---

## Future Tool Ideas

### Economy Tools (with Vault integration)
- `get_player_balance`
- `set_player_balance`
- `transfer_money`

### Plugin Management Tools
- `list_plugins`
- `enable_plugin`
- `disable_plugin`

### Backup Tools
- `create_backup`
- `list_backups`
- `restore_backup`

### Whitelist Tools
- `list_whitelist`
- `add_to_whitelist`
- `remove_from_whitelist`

### Advanced World Tools
- `create_world`
- `delete_world`
- `import_world`
