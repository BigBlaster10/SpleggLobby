package main.java.trompgames.spleggLobby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import main.java.trompgames.utils.Updateable;
import net.md_5.bungee.api.ChatColor;



public class SpleggLobbyMain extends JavaPlugin implements PluginMessageListener{

	
	private String pluginChannel;
	
	@Override
	public void onEnable(){
		
        this.saveDefaultConfig();
        
        this.pluginChannel = this.getConfig().getString("bungee.pluginChannel");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, pluginChannel);
  	    this.getServer().getMessenger().registerIncomingPluginChannel(this, pluginChannel, this);
		
  	    SpleggSign.getSpleggServers(this.getConfig(), pluginChannel, this);		
        Bukkit.getServer().getPluginManager().registerEvents(new SignListener(this), this);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, Updateable::updateUpdateables, 0L, 1L);

        String url = this.getConfig().getString("mysql.url");
        String user = this.getConfig().getString("mysql.username");
        String pass = this.getConfig().getString("mysql.password");
        String schema = this.getConfig().getString("mysql.schema");
        String table = this.getConfig().getString("mysql.table");
        
        PlayerStats.createConnection(url, user, pass, schema, table);        
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            if (cmd.getName().equalsIgnoreCase("stats")) {
            	if(args.length < 1){
            		PlayerStats stats = PlayerStats.getPlayer(player.getName());
            		
            		player.sendMessage(ChatColor.GREEN + "Total Points: " + ChatColor.GOLD + stats.getCurrentPoints());
            		player.sendMessage(ChatColor.GREEN + "Eggs Shot: " + ChatColor.GOLD + stats.getCurrentEggsShot());
            		player.sendMessage(ChatColor.GREEN + "Blocks Destroyed: " + ChatColor.GOLD + stats.getCurrentBlocksDestroyed());
            		player.sendMessage(ChatColor.GREEN + "Games Played: " + ChatColor.GOLD + stats.getCurrentGamesPlayed());
            		player.sendMessage(ChatColor.GREEN + "Deaths:" + ChatColor.GOLD + stats.getCurrentDeaths());
            		player.sendMessage(ChatColor.GREEN + "Wins: " + ChatColor.GOLD + stats.getCurrentWins());

            		
            	}else{           		
            		String p = args[0];
            		PlayerStats stats = PlayerStats.getPlayer(p);
            		player.sendMessage(ChatColor.GREEN + "Total Points: " + ChatColor.GOLD + stats.getCurrentPoints());
            		player.sendMessage(ChatColor.GREEN + "Eggs Shot: " + ChatColor.GOLD + stats.getCurrentEggsShot());
            		player.sendMessage(ChatColor.GREEN + "Blocks Destroyed: " + ChatColor.GOLD + stats.getCurrentBlocksDestroyed());
            		player.sendMessage(ChatColor.GREEN + "Games Played: " + ChatColor.GOLD + stats.getCurrentGamesPlayed());
            		player.sendMessage(ChatColor.GREEN + "Deaths:" + ChatColor.GOLD + stats.getCurrentDeaths());
            		player.sendMessage(ChatColor.GREEN + "Wins: " + ChatColor.GOLD + stats.getCurrentWins());
            		
            	}
            			
            			
            	
            	
            	
            }
            
            
        }
        return false;
	}
	

	public String getPluginChannel(){
		return pluginChannel;
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if(!channel.equals(pluginChannel)) return;
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		String toServer = in.readUTF();
		String fromServer = in.readUTF();
		//Bukkit.broadcastMessage("Sub: " + subchannel);
		//Bukkit.broadcastMessage("From: " + fromServer);
		if(SpleggSign.getSpleggSign(fromServer) != null){
			SpleggSign sign = SpleggSign.getSpleggSign(fromServer);
			int players = in.readInt();
			int maxPlayers = in.readInt();
			String gameState = in.readUTF();
			String map = in.readUTF();
			
			sign.setPlayerCount(players);
			sign.setMaxPlayers(maxPlayers);
			sign.setGameState(gameState);
			sign.setMapName(map);
			sign.recieveMessage();
			//Bukkit.broadcastMessage("Players: " + players);
			//Bukkit.broadcastMessage("Max Players: " + maxPlayers);
			//Bukkit.broadcastMessage("GameState: " + gameState);
			//Bukkit.broadcastMessage("Map: " + map);
			
			
			return;
		}	
	}
	
	public static class PlayerStats{
		
		private String player;
		
		private int deaths;
		private int gamesPlayed;
		private int eggsShot;
		private int blocksDestroyed;
		private int points;		
		private int wins;
		
		private int currentDeaths;
		private int currentGamesPlayed;
		private int currentEggsShot;
		private int currentBlocksDestroyed;
		private int currentPoints;		
		private int currentWins;
		
	

		public static Connection conn;
		public static String table;
		public static String schema;
		
		public static ArrayList<PlayerStats> stats = new ArrayList<PlayerStats>();
		
		private PlayerStats(String player){
			this.player = player;
			stats.add(this);
			loadStats();
		}
		
		private void loadStats(){
			if(conn == null) return;
			String query = "SELECT * FROM " + table + " WHERE PlayerName = '" + player +  "'";
			Statement stmt;			
			ResultSet rs;
			
			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query);
				if(rs.next()){
					this.currentDeaths = rs.getInt("Deaths");
					this.currentGamesPlayed = rs.getInt("GamesPlayed");
					this.currentEggsShot = rs.getInt("EggsShot");
					this.currentBlocksDestroyed = rs.getInt("BlocksDestroyed");
					this.currentPoints = rs.getInt("Points");
					this.currentWins = rs.getInt("Wins");					
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		
		
		
		public static PlayerStats getPlayer(String player){
			for(PlayerStats stat : stats){
				if(stat.getPlayer().equals(player)) return stat;
			}
			return new PlayerStats(player);
		}			
		
		public static void createConnection(String url, String user, String pass, String schema, String table){
			PlayerStats.schema = schema;
			PlayerStats.table = table;
			try {
				conn = DriverManager.getConnection(url, user, pass);				
				//createTable(conn);
			} catch (SQLException e1) {
				Bukkit.broadcastMessage(ChatColor.RED + "[Splegg] ERROR: Failed to connect with mysql");
				e1.printStackTrace();
			}
		}		
		
		
		
		
		
		public String getPlayer(){
			return player;
		}
		
		


		public static ArrayList<PlayerStats> getPlayerStats() {
			return stats;
		}		
		
		public int getCurrentDeaths() {
			return currentDeaths;
		}

		public int getCurrentGamesPlayed() {
			return currentGamesPlayed;
		}

		public int getCurrentEggsShot() {
			return currentEggsShot;
		}

		public int getCurrentBlocksDestroyed() {
			return currentBlocksDestroyed;
		}

		public int getCurrentPoints() {
			return currentPoints;
		}

		public int getCurrentWins() {
			return currentWins;
		}
	}
	
	
}
