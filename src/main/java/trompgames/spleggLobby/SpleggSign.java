package main.java.trompgames.spleggLobby;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import main.java.trompgames.utils.Updateable;
import net.md_5.bungee.api.ChatColor;

public class SpleggSign extends Updateable{

	public static String thisServerName;
	
	private Location loc;
	private String serverName;
	private String pluginChannel;
	private SpleggLobbyMain spleggLobbyMain;
	
	private int playerCount = 0;
	private int maxPlayers = 24;
	private String gameState = "PREGAME";
	private String mapName = "Voting...";
	
	public static ArrayList<SpleggSign> signs = new ArrayList<>();
	
	long lastUpdate;
	
	private SpleggSign(String serverName, String pluginChannel, SpleggLobbyMain spleggLobbyMain){
		super(20);
		this.serverName = serverName;
		this.pluginChannel = pluginChannel;
		this.spleggLobbyMain = spleggLobbyMain;
		this.lastUpdate = System.currentTimeMillis();
		getSignLoc();		
		signs.add(this);
	}
	
	
	
	private void getSignLoc(){
		if(spleggLobbyMain.getConfig().getString("sign." + serverName) == null) return;
		this.loc = getLocationFromConfig("sign." + serverName, spleggLobbyMain.getConfig());
	}
	
    public Location getLocationFromConfig(String path, FileConfiguration config){
    	double x = config.getDouble(path + ".x");
    	double y = config.getDouble(path + ".y");
    	double z = config.getDouble(path + ".z");
    	String world = config.getString(path + ".world");

        Location loc = new Location(Bukkit.getWorld(world), x, y, z);

        return loc;   	
    }
    
    public void removeSign(FileConfiguration config){
    	config.set("sign." + serverName, null);
    	spleggLobbyMain.saveConfig();
    	signs.remove(this);
    	this.remove();
    }
	
	public static void getSpleggServers(FileConfiguration config, String pluginChanel, SpleggLobbyMain spleggLobbyMain){
		List<String> servers = (List<String>) config.getList("servers");
		for(String serverName : servers){
			new SpleggSign(serverName, pluginChanel, spleggLobbyMain);
		}	
		thisServerName = config.getString("bungee.serverName");
	}

	public static SpleggSign getSpleggSign(String string){
		for(SpleggSign sign : signs){
			if(sign.getServerName().equals(string)) return sign;
		}
		return null;
	}
	
	public static SpleggSign getSpleggSign(Location loc){
		for(SpleggSign sign : signs){
			if(loc.equals(sign.loc)) return sign;
		}
		return null;
	}
	
	public static void createSign(Location loc, String server, SpleggLobbyMain plugin){
		addSign(loc, server, plugin);
		new SpleggSign(server, plugin.getPluginChannel(), plugin);
	}
	
	private static void addSign(Location loc, String server, SpleggLobbyMain plugin){
		plugin.getConfig().set("sign." + server + ".x", 1.0 * loc.getBlockX());
		plugin.getConfig().set("sign." + server + ".y", 1.0 * loc.getBlockY());
		plugin.getConfig().set("sign." + server + ".z", 1.0 * loc.getBlockZ());
		plugin.getConfig().set("sign." + server + ".world", loc.getWorld().getName());
        plugin.saveConfig();
    }
	
	

	@Override
	protected void update() {
		Sign sign = null;
		if(loc == null || !(loc.getBlock().getState() instanceof Sign) ||(Sign) loc.getBlock().getState() == null){			
			removeSign(spleggLobbyMain.getConfig());
			Bukkit.broadcastMessage("remove");
			return;
		}else{
			sign = (Sign) loc.getBlock().getState();
		}
		
		
		if(System.currentTimeMillis() - lastUpdate >= 5000){
			playerCount = 0;
		}
		
		if(gameState.equals("OFFLINE")){
			sign.setLine(0, getConfigString("sign.offlineLine1"));
			sign.setLine(1, getConfigString("sign.offlineLine2"));
			sign.setLine(2, getConfigString("sign.offlineLine3"));
			sign.setLine(3, getConfigString("sign.offlineLine4"));
		}else{
			sign.setLine(0, getConfigString("sign.line1"));
			sign.setLine(1, getConfigString("sign.line2"));
			sign.setLine(2, getConfigString("sign.line3"));
			sign.setLine(3, getConfigString("sign.line4"));
		}
		
		
		
		sign.update();
		
		
		if(Bukkit.getOnlinePlayers() == null || Bukkit.getOnlinePlayers().size() == 0) return;
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("getStats");
		out.writeUTF(serverName);	
		out.writeUTF(thisServerName);
		Bukkit.getOnlinePlayers().iterator().next().sendPluginMessage(spleggLobbyMain, pluginChannel, out.toByteArray());	
	}
	
	public String getConfigString(String path){
		String s = spleggLobbyMain.getConfig().getString(path);
		FileConfiguration config = spleggLobbyMain.getConfig();
		
		
		
		s = ChatColor.translateAlternateColorCodes('&', s);
		s = s.replaceAll("%maxPlayers%", "" + maxPlayers);
		s = s.replaceAll("%players%", "" + playerCount);
		s = s.replaceAll("%map%", "" + mapName);
		
		if(gameState.equalsIgnoreCase("PREGAME")){
			s = s.replaceAll("%gameState%", ChatColor.translateAlternateColorCodes('&', config.getString("sign.PREGAME")));
		}else if(gameState.equalsIgnoreCase("INGAME")){
			s = s.replaceAll("%gameState%", ChatColor.translateAlternateColorCodes('&', config.getString("sign.INGAME")));
		}else if(gameState.equalsIgnoreCase("OVER")){
			s = s.replaceAll("%gameState%", ChatColor.translateAlternateColorCodes('&', config.getString("sign.OVER")));
		}
		return s;
		
	}
	
	
	
	
	
	public void recieveMessage(){
		this.lastUpdate = System.currentTimeMillis();
	}
	

	public String getServerName(){
		return serverName;
	}
	
	public int getPlayerCount() {
		return playerCount;
	}

	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public String getGameState() {
		return gameState;
	}

	public void setGameState(String gameState) {
		this.gameState = gameState;
	}

	public String getMapName() {
		return mapName;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}


}
