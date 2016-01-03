package main.java.trompgames.spleggLobby;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;

public class SignListener implements Listener{

	private SpleggLobbyMain plugin;
	
	public SignListener(SpleggLobbyMain plugin){
		this.plugin = plugin;
	}
	
	
	@EventHandler
	public void onBlockPlace(SignChangeEvent event){
		if(!event.getLine(0).equalsIgnoreCase("[splegg]")) return;
		String server = event.getLine(1);
		SpleggSign.createSign(event.getBlock().getLocation(), server, plugin);
		
		
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		
		Player player = event.getPlayer();	
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(SpleggSign.getSpleggSign(event.getClickedBlock().getLocation()) == null) return;
			SpleggSign sign = SpleggSign.getSpleggSign(event.getClickedBlock().getLocation());
			
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(sign.getServerName()); 
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());		
            player.sendMessage(ChatColor.GREEN + "Connecting to " + sign.getServerName() + "...");
			
		}
	}
	
}
