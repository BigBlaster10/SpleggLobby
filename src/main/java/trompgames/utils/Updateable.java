package main.java.trompgames.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;

public abstract class Updateable {

	public static ArrayList<Updateable> updates = new ArrayList<Updateable>();
	
	private int ticks;
	
	private int currentTicks = 0;
	public Updateable(int ticks){
		this.ticks = ticks;
		updates.add(this);
	}
	
	protected abstract void update();
	
	
	static int totalTicks = 1;
	public static void updateUpdateables(){
		
		for(int i = updates.size()-1; i>= 0; i--){
			Updateable update = Updateable.updates.get(i);
			update.currentTicks++;
			if(update.currentTicks >= update.ticks){
				update.currentTicks = 0;
				update.update();				
			}			
		}	
		totalTicks++;
		if(totalTicks > 20) totalTicks = 1;
	}
	
	public void setTicks(int ticks){
		this.ticks = ticks;
	}
	
	public void remove(){
		updates.remove(this);
	}
	
}
