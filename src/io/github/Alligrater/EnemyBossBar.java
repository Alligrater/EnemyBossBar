package io.github.Alligrater;

import org.bukkit.plugin.java.JavaPlugin;

public class EnemyBossBar extends JavaPlugin{
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new AttackListener(), this);
	}
	
	@Override
	public void onDisable() {
		for(String k:AttackListener.Enemies.keySet()) {
			AttackListener.Enemies.get(k).removeAll();
		}
	}
}
