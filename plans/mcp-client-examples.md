# Golem MCP Client Configuration Examples

## Overview

This document provides example configurations for connecting various MCP clients to the Golem MCP server.

---

## Claude Desktop Configuration

### stdio Transport (Recommended for Local Servers)

**Location:** `~/Library/Application Support/Claude/claude_desktop_config.json` (macOS)
or `%APPDATA%\Claude\claude_desktop_config.json` (Windows)

**Note:** The Golem plugin runs within the Minecraft server process. For stdio transport, you need to connect to the server's stdio interface, typically via SSH or a local socket.

#### Option 1: SSH to Server with Port Forwarding

```json
{
  "mcpServers": {
    "minecraft-golem": {
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

#### Option 2: Local Server (Same Machine)

```json
{
  "mcpServers": {
    "minecraft-local": {
      "command": "nc",
      "args": [
        "localhost",
        "3001"
      ]
    }
  }
}
```

**Note:** The Golem plugin creates a stdio socket server on a configurable port (default: 3001) that MCP clients can connect to.

### SSE Transport (For Remote Servers)

```json
{
  "mcpServers": {
    "minecraft-remote": {
      "url": "http://minecraft-server.example.com:3000/mcp/sse"
    }
  }
}
```

---

## Custom MCP Client (Python Example)

### Using stdio Transport

```python
import asyncio
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client

async def main():
    # Connect to Golem's stdio socket via SSH tunnel
    server_params = StdioServerParameters(
        command="ssh",
        args=[
            "-L", "3001:localhost:3001",
            "user@minecraft-server.example.com",
            "nc localhost 3001"
        ]
    )
    
    async with stdio_client(server_params) as (read, write):
        async with ClientSession(read, write) as session:
            # Initialize the connection
            await session.initialize()
            
            # List available tools
            tools = await session.list_tools()
            print(f"Available tools: {[tool.name for tool in tools.tools]}")
            
            # Call a tool
            result = await session.call_tool("list_players", {})
            print(f"Players: {result.content}")

if __name__ == "__main__":
    asyncio.run(main())
```

### Using SSE Transport

```python
import asyncio
from mcp import ClientSession
from mcp.client.sse import sse_client

async def main():
    async with sse_client("http://minecraft-server.example.com:3000/mcp/sse") as (read, write):
        async with ClientSession(read, write) as session:
            await session.initialize()
            
            # Get server info
            result = await session.call_tool("get_server_info", {})
            print(f"Server info: {result.content}")

if __name__ == "__main__":
    asyncio.run(main())
```

---

## Custom MCP Client (TypeScript/Node.js Example)

### Using stdio Transport

```typescript
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";

async function main() {
  // Connect to Golem's stdio socket via SSH tunnel
  const transport = new StdioClientTransport({
    command: "ssh",
    args: [
      "-L", "3001:localhost:3001",
      "user@minecraft-server.example.com",
      "nc localhost 3001"
    ]
  });

  const client = new Client({
    name: "minecraft-client",
    version: "1.0.0"
  }, {
    capabilities: {}
  });

  await client.connect(transport);

  // List tools
  const tools = await client.listTools();
  console.log("Available tools:", tools.tools.map(t => t.name));

  // Call a tool
  const result = await client.callTool({
    name: "list_players",
    arguments: {}
  });
  console.log("Players:", result.content);

  await client.close();
}

main().catch(console.error);
```

### Using SSE Transport

```typescript
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { SSEClientTransport } from "@modelcontextprotocol/sdk/client/sse.js";

async function main() {
  const transport = new SSEClientTransport(
    new URL("http://minecraft-server.example.com:3000/mcp/sse")
  );

  const client = new Client({
    name: "minecraft-client",
    version: "1.0.0"
  }, {
    capabilities: {}
  });

  await client.connect(transport);

  // Get server stats
  const result = await client.callTool({
    name: "get_server_stats",
    arguments: {}
  });
  console.log("Server stats:", result.content);

  await client.close();
}

main().catch(console.error);
```

---

## Golem Configuration for MCP Server

### Paper Plugin Configuration

**File:** `plugins/Golem/config.yml`

```yaml
golem:
  # MCP Server Settings
  mcp:
    enabled: true
    
    # stdio transport (for local/SSH connections)
    stdio:
      enabled: true
      # Optional: Create standalone stdio JAR
      standalone_jar: true
    
    # SSE/HTTP transport (for remote connections)
    sse:
      enabled: true
      port: 3000
      host: "0.0.0.0"  # Listen on all interfaces
      # For production, use specific IP or localhost
      # host: "127.0.0.1"  # Local only
      # host: "10.0.0.5"   # Specific IP
  
  # Tool Configuration
  tools:
    player_management:
      enabled: true
      allowed_tools:
        - list_players
        - get_player_info
        - kick_player
        - ban_player
        - teleport_player
    
    world_info:
      enabled: true
      allowed_tools:
        - list_worlds
        - get_world_info
        - set_world_time
        - set_world_weather
    
    server_stats:
      enabled: true
      allowed_tools:
        - get_server_stats
        - get_server_info
        - get_performance_report
    
    admin_commands:
      enabled: true
      allowed_tools:
        - broadcast_message
        - execute_console_command
        - reload_config
      
      # Command execution security
      command_whitelist: []  # Empty = allow all (except blacklist)
      command_blacklist:
        - "stop"
        - "restart"
        - "op"
        - "deop"
        - "reload"  # Use golem's reload_config tool instead
  
  # Permission Settings
  permissions:
    require_permissions: true
  
  # Logging
  logging:
    level: INFO
    log_tool_usage: true
    log_failed_attempts: true
```

---

## Example Tool Usage Scenarios

### Scenario 1: Monitor Server Health

```python
async def monitor_server():
    """Monitor server health and alert if TPS drops"""
    while True:
        result = await session.call_tool("get_server_stats", {})
        stats = parse_stats(result.content)
        
        if stats['tps']['current'] < 18.0:
            await session.call_tool("broadcast_message", {
                "message": "Warning: Server TPS is low!",
                "format": "plain"
            })
        
        await asyncio.sleep(60)  # Check every minute
```

### Scenario 2: Automated Player Management

```python
async def welcome_new_players():
    """Welcome new players and give them starter items"""
    current_players = set()
    
    while True:
        result = await session.call_tool("list_players", {})
        players = parse_player_list(result.content)
        new_players = set(players) - current_players
        
        for player in new_players:
            await session.call_tool("broadcast_message", {
                "message": f"Welcome {player} to the server!",
                "format": "plain"
            })
            
            # Give starter kit via console command
            await session.call_tool("execute_console_command", {
                "command": f"give {player} minecraft:diamond_sword 1"
            })
        
        current_players = set(players)
        await asyncio.sleep(10)
```

### Scenario 3: World Management

```python
async def set_day_cycle():
    """Set all worlds to day at midnight real-time"""
    result = await session.call_tool("list_worlds", {})
    worlds = parse_world_list(result.content)
    
    for world in worlds:
        await session.call_tool("set_world_time", {
            "world": world['name'],
            "time": "day"
        })
        
        await session.call_tool("set_world_weather", {
            "world": world['name'],
            "weather": "clear"
        })
```

### Scenario 4: Performance Monitoring Dashboard

```python
async def create_dashboard():
    """Create a real-time performance dashboard"""
    import curses
    
    stdscr = curses.initscr()
    
    try:
        while True:
            # Get stats
            stats = await session.call_tool("get_server_stats", {})
            info = await session.call_tool("get_server_info", {})
            players = await session.call_tool("list_players", {})
            
            # Clear screen and display
            stdscr.clear()
            stdscr.addstr(0, 0, "=== Minecraft Server Dashboard ===")
            stdscr.addstr(2, 0, f"TPS: {stats['tps']['current']:.2f}")
            stdscr.addstr(3, 0, f"Memory: {stats['memory']['used']/1024/1024:.0f}MB / {stats['memory']['max']/1024/1024:.0f}MB")
            stdscr.addstr(4, 0, f"Players: {stats['players']['online']}/{stats['players']['max']}")
            stdscr.addstr(6, 0, "Online Players:")
            
            for i, player in enumerate(parse_player_list(players)):
                stdscr.addstr(7 + i, 2, f"- {player}")
            
            stdscr.refresh()
            await asyncio.sleep(1)
    finally:
        curses.endwin()
```

---

## Security Considerations

### For stdio Transport

1. **SSH Key Authentication**
   ```bash
   # Generate SSH key
   ssh-keygen -t ed25519 -f ~/.ssh/minecraft_mcp
   
   # Add to server
   ssh-copy-id -i ~/.ssh/minecraft_mcp.pub user@minecraft-server.example.com
   ```

2. **Claude Desktop Config with SSH Key**
   ```json
   {
     "mcpServers": {
       "minecraft": {
         "command": "ssh",
         "args": [
           "-i", "/home/user/.ssh/minecraft_mcp",
           "user@minecraft-server.example.com",
           "cd /path/to/minecraft && java -jar golem-mcp-stdio.jar"
         ]
       }
     }
   }
   ```

### For SSE Transport

1. **Use Reverse Proxy with Authentication**
   
   **Nginx Example:**
   ```nginx
   server {
       listen 443 ssl;
       server_name minecraft-mcp.example.com;
       
       ssl_certificate /path/to/cert.pem;
       ssl_certificate_key /path/to/key.pem;
       
       location /mcp/sse {
           auth_basic "MCP Access";
           auth_basic_user_file /etc/nginx/.htpasswd;
           
           proxy_pass http://localhost:3000/mcp/sse;
           proxy_http_version 1.1;
           proxy_set_header Connection "";
           proxy_buffering off;
           proxy_cache off;
       }
   }
   ```

2. **Firewall Rules**
   ```bash
   # Only allow specific IPs
   sudo ufw allow from 203.0.113.0/24 to any port 3000
   sudo ufw deny 3000
   ```

3. **VPN Access**
   - Use WireGuard or OpenVPN
   - Only expose MCP server on VPN interface

---

## Troubleshooting

### Connection Issues

**Problem:** Cannot connect to MCP server

**Solutions:**
1. Check if Golem plugin is enabled:
   ```
   /plugins
   ```

2. Verify MCP server is running:
   ```
   /golem status
   ```

3. Check configuration:
   ```
   /golem info
   ```

4. Review logs:
   ```
   tail -f logs/latest.log | grep Golem
   ```

### Permission Errors

**Problem:** Tool execution fails with permission denied

**Solutions:**
1. Check player permissions:
   ```
   /lp user <player> permission check golem.tool.list_players
   ```

2. Grant permissions:
   ```
   /lp user <player> permission set golem.tool.* true
   ```

3. For console/MCP access, ensure `require_permissions: false` in config or grant appropriate permissions

### Tool Not Found

**Problem:** Tool is not available in tool list

**Solutions:**
1. Check if tool is enabled in config.yml
2. Verify tool is in `allowed_tools` list
3. Reload configuration:
   ```
   /golem reload
   ```

---

## Advanced Usage

### Custom Tool Wrapper

Create a wrapper script for easier tool access:

```python
# minecraft_tools.py
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client
import asyncio

class MinecraftServer:
    def __init__(self, ssh_host, minecraft_path):
        self.server_params = StdioServerParameters(
            command="ssh",
            args=[ssh_host, f"cd {minecraft_path} && java -jar golem-mcp-stdio.jar"]
        )
        self.session = None
    
    async def __aenter__(self):
        self.client = stdio_client(self.server_params)
        read, write = await self.client.__aenter__()
        self.session = ClientSession(read, write)
        await self.session.__aenter__()
        await self.session.initialize()
        return self
    
    async def __aexit__(self, *args):
        await self.session.__aexit__(*args)
        await self.client.__aexit__(*args)
    
    async def list_players(self):
        result = await self.session.call_tool("list_players", {})
        return result.content
    
    async def kick_player(self, player, reason="Kicked by admin"):
        result = await self.session.call_tool("kick_player", {
            "player": player,
            "reason": reason
        })
        return result.content
    
    async def broadcast(self, message):
        result = await self.session.call_tool("broadcast_message", {
            "message": message
        })
        return result.content

# Usage
async def main():
    async with MinecraftServer("minecraft.example.com", "/opt/minecraft") as mc:
        players = await mc.list_players()
        print(f"Online players: {players}")
        
        await mc.broadcast("Server maintenance in 5 minutes!")

asyncio.run(main())
```

---

## Integration Examples

### Discord Bot Integration

```python
import discord
from discord.ext import commands
from minecraft_tools import MinecraftServer

bot = commands.Bot(command_prefix='!')

@bot.command()
async def players(ctx):
    """List online players"""
    async with MinecraftServer("minecraft.example.com", "/opt/minecraft") as mc:
        players = await mc.list_players()
        await ctx.send(f"Online players: {players}")

@bot.command()
@commands.has_role("Admin")
async def kick(ctx, player: str, *, reason: str = "Kicked by admin"):
    """Kick a player"""
    async with MinecraftServer("minecraft.example.com", "/opt/minecraft") as mc:
        result = await mc.kick_player(player, reason)
        await ctx.send(f"Kicked {player}: {reason}")

bot.run("YOUR_BOT_TOKEN")
```

### Web Dashboard

```javascript
// Express.js server with MCP client
const express = require('express');
const { Client } = require('@modelcontextprotocol/sdk/client');
const { SSEClientTransport } = require('@modelcontextprotocol/sdk/client/sse');

const app = express();

app.get('/api/players', async (req, res) => {
  const transport = new SSEClientTransport(
    new URL('http://localhost:3000/mcp/sse')
  );
  
  const client = new Client({ name: 'web-dashboard', version: '1.0.0' }, {});
  await client.connect(transport);
  
  const result = await client.callTool({ name: 'list_players', arguments: {} });
  
  await client.close();
  res.json(result.content);
});

app.listen(8080, () => console.log('Dashboard running on port 8080'));
```
