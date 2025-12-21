# Golem MCP Server - Planning Documentation

## Overview

This directory contains comprehensive planning documentation for the Golem MCP Server project - a BlueMap-style Minecraft plugin/mod that provides integrated MCP (Model Context Protocol) server functionality with an extensible add-on API and data-driven tool system.

## Documentation Files

### 1. [`golem-mcp-architecture.md`](golem-mcp-architecture.md)
**High-level architecture and design decisions**

- Technology stack overview
- System architecture diagrams
- Module structure
- MCP server implementation approach using official Kotlin SDK
- Core MCP tools catalog
- Configuration system design
- Permission system
- Security considerations
- Testing strategy
- Deployment approach

### 2. [`implementation-guide.md`](implementation-guide.md)
**Detailed step-by-step implementation guide**

- Phase-by-phase implementation plan
- Module structure and file organization
- Code examples and patterns
- Platform adapter interface design
- Tool implementation approach using MCP SDK
- Configuration system implementation
- Testing approach
- Documentation requirements
- Key technical decisions

### 3. [`mcp-tools-specification.md`](mcp-tools-specification.md)
**Complete technical specification for all MCP tools**

- Detailed tool definitions
- JSON schemas for inputs and outputs
- Example requests and responses
- Permission requirements
- Error handling specifications
- Tool categories:
  - Player Management (list, info, kick, ban, teleport)
  - World Information (list, info, time, weather)
  - Server Statistics (stats, info, performance)
  - Admin Commands (broadcast, execute, reload)

### 4. [`dependencies-and-build.md`](dependencies-and-build.md)
**Build configuration and dependency management**

- Kotlin MCP SDK integration details
- Complete Gradle configuration
- Version catalog setup
- Module-specific build files
- Dependency graph
- Build commands
- CI/CD integration examples
- Troubleshooting guide

### 5. [`mcp-client-examples.md`](mcp-client-examples.md)
**MCP client configuration and usage examples**

- Claude Desktop configuration (stdio and SSE)
- Custom Python MCP client examples
- Custom TypeScript/Node.js client examples
- Golem server configuration examples
- Real-world usage scenarios
- Security best practices
- Integration examples (Discord bot, web dashboard)
- Troubleshooting guide

### 6. [`addon-api-specification.md`](addon-api-specification.md)
**Add-on API for third-party plugins/mods**

- GolemAPI interface and access patterns
- ToolRegistry for registering custom tools
- McpTool interface for implementing tools
- ToolExecutionContext and ToolResult
- Complete usage examples (Economy, Regions, Custom mods)
- Best practices and conventions
- Events API for tool lifecycle
- Future enhancements

### 7. [`data-driven-tools.md`](data-driven-tools.md)
**Data-driven tool system for data packs**

- JSON-based tool definitions
- Data pack integration
- Action types (command, query, conditional, loop)
- Template system with variables and filters
- Complete examples (teleport, maintenance, batch operations)
- Security considerations
- Configuration options
- Best practices

## Quick Start

### For Implementers

1. Start with [`golem-mcp-architecture.md`](golem-mcp-architecture.md) to understand the overall design
2. Review [`dependencies-and-build.md`](dependencies-and-build.md) to set up the build system
3. Follow [`implementation-guide.md`](implementation-guide.md) phase by phase
4. Reference [`mcp-tools-specification.md`](mcp-tools-specification.md) when implementing tools
5. Use [`mcp-client-examples.md`](mcp-client-examples.md) for testing

### For Users

1. Read [`golem-mcp-architecture.md`](golem-mcp-architecture.md) for feature overview
2. Check [`mcp-tools-specification.md`](mcp-tools-specification.md) for available tools
3. Use [`mcp-client-examples.md`](mcp-client-examples.md) to configure your MCP client

## Key Highlights

### Using Official Kotlin MCP SDK

The project leverages the official Kotlin MCP SDK from Anthropic:
- **Repository:** https://github.com/modelcontextprotocol/kotlin-sdk
- **Benefits:** 
  - Complete MCP protocol implementation
  - Built-in transport support (stdio, SSE)
  - Tool registration framework
  - Standard error handling
  - Maintained by Anthropic

### Multi-Platform Support

- **Primary:** Paper (Bukkit/Spigot) - Full feature set
- **Secondary:** Velocity (Proxy) - Proxy-level operations
- **Secondary:** Fabric (Mod) - Client and server support

### Extensible Add-on API

Third-party plugins and mods can register custom MCP tools with Golem:
- **Simple Integration:** Implement [`McpTool`](addon-api-specification.md#3-mcptool) interface
- **Automatic Discovery:** Tools appear in MCP tool listings
- **Permission Integration:** Leverage Golem's permission system
- **Event System:** React to tool registration and execution
- **Examples:** Economy, Regions, Custom mods

### Data-Driven Tools

Server admins and data pack creators can define custom tools using JSON:
- **No Coding Required:** Define tools with JSON files
- **Data Pack Integration:** Distribute tools via Minecraft data packs
- **Hot-Reload:** Changes apply without server restart
- **Template System:** Variables, filters, and expressions
- **Action Types:** Commands, queries, conditionals, loops
- **Examples:** Teleportation, economy, world management

### Core Features

1. **MCP Tools** - 13+ tools for server management
2. **Dual Transport** - stdio and SSE support
3. **Hot-Reload Config** - No restart required for config changes
4. **Permission System** - Integrated with platform permissions
5. **Security** - Command whitelist/blacklist, audit logging

### Architecture Highlights

```
MCP Clients (Claude, Custom)
    ↓
Transport Layer (stdio/SSE)
    ↓
Kotlin MCP SDK Server
    ↓
Golem MCP Server Wrapper
    ├─→ Core Tools (15 built-in)
    └─→ Add-on Tools (from third-party plugins/mods)
    ↓
Platform Adapter Interface
    ↓
Minecraft Platforms (Paper/Velocity/Fabric)
```

## Add-on API Example (Code-Based)

```kotlin
class EconomyPlugin : JavaPlugin() {
    override fun onEnable() {
        val golem = server.pluginManager.getPlugin("Golem") as? GolemAPI
        golem?.getToolRegistry()?.registerTool(GetBalanceTool())
    }
}

class GetBalanceTool : McpTool {
    override val name = "economy_get_balance"
    override val description = "Get a player's balance"
    override val requiredPermission = "golem.addon.economy.get_balance"
    
    override suspend fun execute(
        arguments: JsonObject,
        context: ToolExecutionContext
    ): ToolResult {
        val player = arguments["player"]?.jsonPrimitive?.content
        val balance = getBalance(player)
        return ToolResult.success("Balance: $$balance")
    }
}
```

## Data-Driven Tool Example (JSON)

**File:** `plugins/Golem/tools/teleport_home.json` or `world/datapacks/mypack/data/golem/tools/teleport_home.json`

```json
{
  "name": "teleport_home",
  "description": "Teleport a player to their home",
  "permission": "golem.custom.teleport_home",
  "input_schema": {
    "type": "object",
    "properties": {
      "player": {
        "type": "string",
        "description": "Player to teleport"
      }
    },
    "required": ["player"]
  },
  "actions": [
    {
      "type": "command",
      "command": "tp {player} ~ ~ ~"
    }
  ],
  "response": {
    "template": "Teleported {player} to their home"
  }
}
```

## Implementation Phases

### Phase 1: Foundation
- Kotlin build setup
- API module creation
- Common module creation
- MCP SDK integration

### Phase 2: Core Features
- GolemAPI and ToolRegistry implementation
- Platform adapter interface
- Paper adapter implementation
- Core MCP tools

### Phase 3: Paper Plugin
- Complete Paper integration
- Configuration system
- Commands and events
- Add-on API integration

### Phase 4: Testing
- Unit tests
- Integration tests
- Add-on API testing
- Bug fixes

### Phase 5: Additional Platforms
- Velocity plugin
- Fabric mod

### Phase 6: Documentation & Release
- Complete documentation
- Add-on developer guide
- Example add-ons
- Build packaging
- Initial release

## MCP Tools Summary

### Built-in Core Tools (15 tools)

**Player Management (5 tools):**
- `list_players` - List all online players
- `get_player_info` - Get detailed player information
- `kick_player` - Kick a player from the server
- `ban_player` - Ban a player from the server
- `teleport_player` - Teleport a player

**World Information (4 tools):**
- `list_worlds` - List all loaded worlds
- `get_world_info` - Get detailed world information
- `set_world_time` - Change world time
- `set_world_weather` - Change world weather

**Server Statistics (3 tools):**
- `get_server_stats` - Get current server performance metrics
- `get_server_info` - Get server configuration and version info
- `get_performance_report` - Generate detailed performance analysis

**Admin Commands (3 tools):**
- `broadcast_message` - Send message to all players
- `execute_console_command` - Execute command as console
- `reload_config` - Hot-reload plugin configuration

### Add-on Tools (Extensible)

Third-party plugins/mods can add unlimited custom tools:
- Economy tools (balance, transfer, etc.)
- Region management tools
- Custom gameplay mechanics
- Integration with other services
- And more...

## Technology Stack

- **Language:** Kotlin 1.9.22+
- **MCP SDK:** Official Kotlin MCP SDK
- **Build System:** Gradle with Kotlin DSL
- **Async:** Kotlin Coroutines
- **Serialization:** kotlinx.serialization
- **HTTP/SSE:** Ktor Server (via MCP SDK)
- **Logging:** SLF4J

## Configuration Example

```yaml
golem:
  mcp:
    enabled: true
    stdio:
      enabled: true
    sse:
      enabled: true
      port: 3000
      host: "0.0.0.0"
  
  tools:
    player_management:
      enabled: true
    world_info:
      enabled: true
    server_stats:
      enabled: true
    admin_commands:
      enabled: true
      command_blacklist:
        - "stop"
        - "op"
  
  permissions:
    require_permissions: true
  
  logging:
    level: INFO
    log_tool_usage: true
```

## Client Configuration Example

### Claude Desktop (stdio via SSH tunnel)
```json
{
  "mcpServers": {
    "minecraft": {
      "command": "ssh",
      "args": [
        "-L", "3001:localhost:3001",
        "user@minecraft-server.example.com",
        "nc localhost 3001"
      ]
    }
  }
}
```

**Note:** Golem runs as a plugin/mod within the Minecraft server and creates a stdio socket server on port 3001 (configurable).

### Claude Desktop (SSE)
```json
{
  "mcpServers": {
    "minecraft": {
      "url": "http://minecraft-server.example.com:3000/mcp/sse"
    }
  }
}
```

## Security Considerations

1. **Permission System** - All tools respect platform permissions
2. **Command Blacklist** - Dangerous commands blocked by default
3. **Audit Logging** - All tool executions logged
4. **Transport Security** - SSH for stdio, HTTPS recommended for SSE
5. **Rate Limiting** - Future enhancement for tool call limits

## Success Criteria

- [x] Comprehensive planning documentation created
- [ ] MCP server starts successfully on Paper
- [ ] Both stdio and SSE transports working
- [ ] All core tools functional
- [ ] Configuration hot-reload working
- [ ] Permission system integrated
- [ ] Unit test coverage >80%
- [ ] Integration tests passing
- [ ] Velocity plugin functional
- [ ] Fabric mod functional
- [ ] Documentation complete
- [ ] Ready for initial release

## Next Steps

1. **Review this plan** - Ensure all requirements are captured
2. **Switch to Code mode** - Begin implementation
3. **Start with Phase 1** - Set up Kotlin build and common module
4. **Iterate through phases** - Follow implementation guide
5. **Test continuously** - Write tests as you implement
6. **Document as you go** - Keep docs in sync with code

## Contributing

When implementing this plan:

1. Follow the phase-by-phase approach in [`implementation-guide.md`](implementation-guide.md)
2. Reference the official Kotlin MCP SDK documentation
3. Maintain code quality with tests
4. Keep documentation updated
5. Follow Kotlin coding conventions
6. Use meaningful commit messages

## Resources

- **Kotlin MCP SDK:** https://github.com/modelcontextprotocol/kotlin-sdk
- **MCP Specification:** https://spec.modelcontextprotocol.io/
- **Paper API:** https://docs.papermc.io/
- **Velocity API:** https://docs.velocitypowered.com/
- **Fabric Wiki:** https://fabricmc.net/wiki/

## Questions?

If you have questions about the plan:

1. Review the relevant documentation file
2. Check the MCP SDK examples
3. Consult the MCP specification
4. Ask for clarification before implementing

---

**Ready to implement?** Switch to Code mode and start with Phase 1!
