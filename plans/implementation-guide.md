# Golem MCP Server - Implementation Guide

## Phase 1: Project Setup & Migration to Kotlin

### 1.1 Update Build Configuration

**Root [`build.gradle.kts`](../build.gradle.kts)**
- Add Kotlin plugin
- Configure Kotlin version (1.9.x or later)
- Set up shared dependencies
- Configure subproject structure

**[`gradle/libs.versions.toml`](../gradle/libs.versions.toml)**
- Define version catalog for all dependencies
- Kotlin stdlib, coroutines, serialization
- **Official Kotlin MCP SDK** (`io.modelcontextprotocol:kotlin-sdk`)
- Ktor for HTTP/SSE server (used by MCP SDK)
- Platform-specific APIs (Paper, Velocity, Fabric)
- Testing libraries (JUnit, MockK)

**[`settings.gradle.kts`](../settings.gradle.kts)**
- Add new modules: common, velocity, fabric
- Configure module dependencies

### 1.2 Migrate Existing Code

**Convert [`GolemPlugin.java`](../paper/src/main/java/com/keplersj/golem/GolemPlugin.java) to Kotlin**
- Create `GolemPlugin.kt` in `paper/src/main/kotlin/`
- Migrate Java code to idiomatic Kotlin
- Keep basic plugin structure for now
- Will be enhanced with MCP server integration later

## Phase 2: Common Module - MCP Core

### 2.1 Create Module Structure

```
common/
├── build.gradle.kts
└── src/main/kotlin/com/keplersj/golem/
    ├── mcp/
    │   ├── GolemMcpServer.kt (extends MCP SDK Server)
    │   ├── GolemServerOptions.kt
    │   └── tools/
    │       ├── GolemTool.kt (wrapper for MCP SDK Tool)
    │       ├── PlayerManagementTools.kt
    │       ├── WorldInfoTools.kt
    │       ├── ServerStatsTools.kt
    │       └── AdminCommandTools.kt
    ├── config/
    │   ├── GolemConfig.kt
    │   ├── ConfigLoader.kt
    │   ├── ConfigValidator.kt
    │   └── ConfigWatcher.kt
    ├── platform/
    │   ├── PlatformAdapter.kt (interface)
    │   ├── PlayerInfo.kt
    │   ├── WorldInfo.kt
    │   └── ServerInfo.kt
    └── util/
        ├── Logger.kt
        └── Permissions.kt
```

### 2.2 MCP Server Implementation (Using Official SDK)

**[`GolemMcpServer.kt`]()**
```kotlin
import io.modelcontextprotocol.kotlin.sdk.Server
import io.modelcontextprotocol.kotlin.sdk.ServerOptions

class GolemMcpServer(
    private val platformAdapter: PlatformAdapter,
    private val config: GolemConfig
) {
    private val server: Server
    
    init {
        val options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ToolsCapability()
            )
        )
        
        server = Server(
            serverInfo = Implementation(
                name = "golem",
                version = "1.0.0"
            ),
            options = options
        )
        
        // Register all tools
        registerTools()
    }
    
    private fun registerTools() {
        // Register player management tools
        // Register world info tools
        // Register server stats tools
        // Register admin command tools
    }
    
    suspend fun start(transport: Transport) {
        server.connect(transport)
    }
    
    suspend fun stop() {
        server.close()
    }
}
```

**Note:** The official Kotlin MCP SDK handles:
- JSON-RPC 2.0 protocol implementation
- Request/response parsing and validation
- Transport layer abstractions (stdio, SSE)
- Tool registration and execution
- Error handling and formatting

### 2.3 Transport Layer (Using MCP SDK)

The official SDK provides transport implementations:

**Stdio Transport:**
```kotlin
import io.modelcontextprotocol.kotlin.sdk.StdioServerTransport

val transport = StdioServerTransport()
server.connect(transport)
```

**SSE Transport:**
```kotlin
import io.modelcontextprotocol.kotlin.sdk.server.sse.SseServerTransport

val transport = SseServerTransport(
    endpoint = "/mcp/sse",
    port = config.mcp.sse.port
)
server.connect(transport)
```

### 2.4 Tool System (Using MCP SDK)

**[`GolemTool.kt`]()**
```kotlin
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.*

abstract class GolemTool(
    override val name: String,
    override val description: String,
    private val requiredPermission: String? = null
) : Tool {
    
    abstract suspend fun executeInternal(
        arguments: JsonObject,
        platformAdapter: PlatformAdapter
    ): String
    
    override suspend fun execute(arguments: JsonObject): String {
        // Check permissions if required
        if (requiredPermission != null) {
            // Permission check logic
        }
        
        // Execute the tool
        return executeInternal(arguments, platformAdapter)
    }
}
```

**Example Tool Implementation:**
```kotlin
class ListPlayersTool(
    private val platformAdapter: PlatformAdapter
) : GolemTool(
    name = "list_players",
    description = "List all online players",
    requiredPermission = "golem.tool.list_players"
) {
    override val inputSchema = buildJsonObject {
        put("type", "object")
        put("properties", buildJsonObject {})
    }
    
    override suspend fun executeInternal(
        arguments: JsonObject,
        platformAdapter: PlatformAdapter
    ): String {
        val players = platformAdapter.listPlayers()
        return formatPlayerList(players)
    }
}
```

**Tool Registration:**
```kotlin
// In GolemMcpServer.registerTools()
server.addTool(ListPlayersTool(platformAdapter))
server.addTool(GetPlayerInfoTool(platformAdapter))
server.addTool(KickPlayerTool(platformAdapter))
// ... etc
```

### 2.5 Configuration System (Unchanged)

**[`GolemConfig.kt`]()**
```kotlin
@Serializable
data class GolemConfig(
    val mcp: McpConfig,
    val tools: ToolsConfig,
    val permissions: PermissionsConfig,
    val logging: LoggingConfig
)
```

**[`ConfigLoader.kt`]()**
- Load from YAML file
- Parse and deserialize
- Apply defaults for missing values
- Validate configuration

**[`ConfigWatcher.kt`]()**
- Watch config file for changes
- Debounce rapid changes
- Trigger reload events
- Handle reload errors

## Phase 3: Platform Adapters

### 3.1 Platform Adapter Interface

**[`PlatformAdapter.kt`]()**
```kotlin
interface PlatformAdapter {
    // Player operations
    suspend fun listPlayers(): List<PlayerInfo>
    suspend fun getPlayer(identifier: String): PlayerInfo?
    suspend fun kickPlayer(identifier: String, reason: String): Boolean
    suspend fun banPlayer(identifier: String, reason: String, duration: Long?): Boolean
    suspend fun teleportPlayer(player: String, target: String): Boolean
    
    // World operations
    suspend fun listWorlds(): List<WorldInfo>
    suspend fun getWorld(name: String): WorldInfo?
    suspend fun setWorldTime(world: String, time: Long): Boolean
    suspend fun setWorldWeather(world: String, weather: String): Boolean
    
    // Server operations
    suspend fun getServerStats(): ServerStats
    suspend fun getServerInfo(): ServerInfo
    suspend fun broadcastMessage(message: String): Boolean
    suspend fun executeConsoleCommand(command: String): CommandResult
    
    // Permission operations
    fun hasPermission(identifier: String, permission: String): Boolean
}
```

### 3.2 Paper Adapter

**[`PaperPlatformAdapter.kt`]()**
- Implement all PlatformAdapter methods
- Use Bukkit/Paper API
- Handle async operations properly
- Convert between Paper and common types

### 3.3 Velocity Adapter

**[`VelocityPlatformAdapter.kt`]()**
- Implement proxy-level operations
- Cross-server player management
- Proxy statistics
- Limited world operations (proxy doesn't have worlds)

### 3.4 Fabric Adapter

**[`FabricPlatformAdapter.kt`]()**
- Implement using Fabric API
- Server-side and client-side support
- Mod compatibility considerations

## Phase 4: MCP Tools Implementation

### 4.1 Player Management Tools

**[`ListPlayersTool.kt`]()**
```kotlin
class ListPlayersTool(
    private val adapter: PlatformAdapter
) : Tool {
    override val name = "list_players"
    override val description = "List all online players"
    override val inputSchema = buildJsonObject { }
    
    override suspend fun execute(
        parameters: JsonObject,
        context: ToolContext
    ): ToolResult {
        val players = adapter.listPlayers()
        return ToolResult.success(players)
    }
}
```

**Additional Player Tools:**
- [`GetPlayerInfoTool.kt`]()
- [`KickPlayerTool.kt`]()
- [`BanPlayerTool.kt`]()
- [`TeleportPlayerTool.kt`]()

### 4.2 World Information Tools

**[`ListWorldsTool.kt`]()**
**[`GetWorldInfoTool.kt`]()**
**[`SetWorldTimeTool.kt`]()**
**[`SetWorldWeatherTool.kt`]()**

### 4.3 Server Statistics Tools

**[`GetServerStatsTool.kt`]()**
**[`GetServerInfoTool.kt`]()**
**[`GetPerformanceReportTool.kt`]()**

### 4.4 Admin Command Tools

**[`BroadcastMessageTool.kt`]()**
**[`ExecuteConsoleCommandTool.kt`]()**
- Implement command whitelist/blacklist
- Security validation
- Audit logging

**[`ReloadConfigTool.kt`]()**

## Phase 5: Paper Plugin Integration

### 5.1 Plugin Main Class

**[`GolemPlugin.kt`](../paper/src/main/kotlin/com/keplersj/golem/GolemPlugin.kt)**
```kotlin
class GolemPlugin : JavaPlugin() {
    private lateinit var mcpServer: McpServer
    private lateinit var config: GolemConfig
    
    override fun onEnable() {
        // Load configuration
        // Initialize platform adapter
        // Create and register tools
        // Start MCP server
        // Register event listeners
        // Register commands
    }
    
    override fun onDisable() {
        // Stop MCP server
        // Cleanup resources
    }
}
```

### 5.2 Configuration Files

**[`plugin.yml`](../paper/src/main/resources/plugin.yml)**
- Update with proper metadata
- Define commands
- Define permissions

**Default [`config.yml`]()**
- Create in plugin data folder on first run
- Include all configuration options
- Add helpful comments

### 5.3 Commands

**[`/golem`]() command**
- `/golem reload` - Reload configuration
- `/golem status` - Show MCP server status
- `/golem tools` - List available tools
- `/golem info` - Show plugin information

### 5.4 Event Listeners

**[`PlayerEventListener.kt`]()**
- Track player joins/quits
- Update player cache
- Notify MCP clients (future: notifications)

## Phase 6: Velocity Plugin

### 6.1 Plugin Structure

```
velocity/
├── build.gradle.kts
└── src/main/kotlin/com/keplersj/golem/
    ├── GolemVelocityPlugin.kt
    ├── VelocityPlatformAdapter.kt
    └── commands/
        └── GolemCommand.kt
```

### 6.2 Implementation

**[`GolemVelocityPlugin.kt`]()**
- Velocity plugin lifecycle
- Similar structure to Paper plugin
- Proxy-specific features

**Configuration**
- Use TOML format (Velocity standard)
- Similar options to Paper version

## Phase 7: Fabric Mod

### 7.1 Mod Structure

```
fabric/
├── build.gradle.kts
└── src/main/kotlin/com/keplersj/golem/
    ├── GolemFabricMod.kt
    ├── FabricPlatformAdapter.kt
    └── config/
        └── ModConfig.kt
```

### 7.2 Implementation

**[`GolemFabricMod.kt`]()**
- Fabric mod initialization
- Server and client support
- Mod menu integration

**Configuration**
- Use JSON format (Fabric standard)
- Auto-config library integration

## Phase 8: Testing

### 8.1 Unit Tests

**Common Module Tests:**
- [`JsonRpcHandlerTest.kt`]() - Protocol parsing
- [`ToolRegistryTest.kt`]() - Tool registration
- [`ConfigLoaderTest.kt`]() - Configuration loading
- [`TransportTest.kt`]() - Transport layer

### 8.2 Integration Tests

**Platform Tests:**
- [`PaperPluginTest.kt`]() - Full plugin lifecycle
- [`McpServerTest.kt`]() - End-to-end MCP communication
- [`ToolExecutionTest.kt`]() - Tool execution with mocked platform

### 8.3 Test Utilities

**[`MockPlatformAdapter.kt`]()**
- Mock implementation for testing
- Configurable responses
- Verification helpers

## Phase 9: Documentation

### 9.1 User Documentation

**[`README.md`](../README.md)**
- Project overview
- Quick start guide
- Feature highlights
- Links to detailed docs

**[`docs/installation.md`]()**
- Platform-specific installation
- Configuration setup
- First-time setup guide

**[`docs/configuration.md`]()**
- Complete configuration reference
- All options explained
- Examples for common scenarios

**[`docs/tools.md`]()**
- Complete tool catalog
- Parameter descriptions
- Example requests/responses
- Permission requirements

**[`docs/mcp-clients.md`]()**
- Claude Desktop setup
- Custom client examples
- Integration guides

### 9.2 Developer Documentation

**[`docs/development.md`]()**
- Building from source
- Development environment setup
- Contributing guidelines

**[`docs/custom-tools.md`]()**
- Creating custom tools
- Tool API reference
- Examples

**[`docs/architecture.md`]()**
- System architecture
- Module structure
- Design decisions

## Phase 10: Packaging & Distribution

### 10.1 Build Configuration

**Shadow JAR for Paper:**
- Include common module
- Include dependencies
- Relocate packages to avoid conflicts

**Shadow JAR for Velocity:**
- Similar to Paper
- Velocity-specific packaging

**Fabric Mod JAR:**
- Include common module
- Fabric mod metadata
- Mod dependencies

### 10.2 Release Artifacts

- Paper plugin JAR
- Velocity plugin JAR
- Fabric mod JAR
- Source code archive
- Documentation bundle

### 10.3 Distribution Channels

- GitHub Releases
- SpigotMC (Paper)
- Modrinth (Fabric)
- CurseForge (Fabric)
- Hangar (Paper/Velocity)

## Implementation Order

1. **Foundation** (Phase 1-2)
   - Set up Kotlin build
   - Create common module
   - Implement MCP protocol core

2. **Core Features** (Phase 3-4)
   - Platform adapter interface
   - Paper adapter implementation
   - Core MCP tools

3. **Paper Plugin** (Phase 5)
   - Complete Paper integration
   - Configuration system
   - Commands and events

4. **Testing & Refinement** (Phase 8)
   - Unit tests
   - Integration tests
   - Bug fixes

5. **Additional Platforms** (Phase 6-7)
   - Velocity plugin
   - Fabric mod

6. **Documentation & Release** (Phase 9-10)
   - Complete documentation
   - Build packaging
   - Initial release

## Key Technical Decisions

### Kotlin Coroutines
- All async operations use coroutines
- Structured concurrency
- Proper cancellation handling

### Kotlinx Serialization
- JSON serialization/deserialization
- Type-safe configuration
- Custom serializers for Minecraft types

### Ktor for HTTP/SSE
- Lightweight HTTP server
- Built-in SSE support
- Kotlin-first design

### Shared Common Module
- Maximum code reuse
- Consistent behavior across platforms
- Easier maintenance

### Configuration Hot-Reload
- File watcher pattern
- Event-driven updates
- Graceful error handling

### Permission Integration
- Platform-native permission systems
- Fallback to basic checks
- Configurable enforcement

## Potential Challenges & Solutions

### Challenge: Async Operations in Minecraft
**Solution:** Use Kotlin coroutines with proper dispatchers for Minecraft's main thread

### Challenge: Cross-Platform Compatibility
**Solution:** Well-defined platform adapter interface, comprehensive testing

### Challenge: MCP Protocol Complexity
**Solution:** Incremental implementation, start with core features, add advanced features later

### Challenge: Performance Impact
**Solution:** Async processing, efficient caching, configurable rate limiting

### Challenge: Security Concerns
**Solution:** Permission system, command whitelist/blacklist, audit logging

## Success Criteria

- [ ] MCP server starts successfully on Paper
- [ ] Both stdio and SSE transports working
- [ ] All core tools functional
- [ ] Configuration hot-reload working
- [ ] Permission system integrated
- [ ] Comprehensive documentation
- [ ] Unit test coverage >80%
- [ ] Integration tests passing
- [ ] Velocity plugin functional
- [ ] Fabric mod functional
- [ ] Ready for initial release
