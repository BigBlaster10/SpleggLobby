package main.java.trompgames.spleggLobby;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import main.java.trompgames.utils.Updateable;



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
	
}
