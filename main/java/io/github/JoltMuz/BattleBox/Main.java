package io.github.JoltMuz.BattleBox;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin
{
	private static Main instance;
	
	@Override
    public void onEnable() 
	{
		instance = this;
		TeamManager teamManager = TeamManager.getInstance();
		MatchManager matchManager = MatchManager.getInstance();
		ArenaManager arenaManager = ArenaManager.getInstance();
		this.getCommand("battlebox").setExecutor(new Commands(arenaManager, teamManager, matchManager));
		this.getCommand("team").setExecutor(new TeamCommands(teamManager));
		this.getCommand("spectate").setExecutor(new Spectate(arenaManager, teamManager, matchManager));
		getServer().getPluginManager().registerEvents(new Listeners(teamManager, matchManager, arenaManager), this);
	}
	
	public static Main getInstance()
	{
		return instance;
	}
    
}
