package io.github.JoltMuz.BattleBox;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class Spectate implements CommandExecutor
{
	private MatchManager matchManager;
	private ArenaManager arenaManager;

	public Spectate(ArenaManager arenaManager, TeamManager teamManager, MatchManager matchManager) {
		this.matchManager = matchManager;
		this.arenaManager = arenaManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
		{
			sender.sendMessage(Commands.getPrefix() + "Only players can spectate");
			return true;
		}
		if (matchManager.getActiveMatches() == null || matchManager.getActiveMatches().size()  == 0)
		{
			sender.sendMessage(Commands.getPrefix() + "No On-going matches");
			return true;
		}
		Player player = (Player) sender;
		List<Match> matchList = new ArrayList<>(matchManager.getActiveMatches().values());
		int numRows = (int) Math.ceil(matchList.size() / 9.0);
		Inventory gui = Bukkit.createInventory(null, numRows*9, "On-going Matches");
        for (int i = 0; i < matchList.size(); i++) {
        	Match match = matchList.get(i);
        	ItemStack matchItem = new ItemStack(Material.PAPER); // You can change the material as desired
            	ItemMeta meta = matchItem.getItemMeta();
                meta.setDisplayName(ChatColor.DARK_GREEN + "Match ID: " + ChatColor.WHITE + match.getID());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.BLUE + match.getTeam1().getName() + ChatColor.YELLOW + " vs " + ChatColor.RED + match.getTeam2().getName());
                try {
                    lore.add(ChatColor.DARK_PURPLE + "Arena: " + ChatColor.LIGHT_PURPLE + arenaManager.getNameOfArena(match.getArena()));
                }
                catch (IllegalArgumentException e)
                {
                	lore.add(ChatColor.DARK_PURPLE + "Arena: " + ChatColor.DARK_GRAY + "[Deleted]");
                }
                if (match.isInProgress()) {
                    lore.add(ChatColor.GREEN + "On-going: Yes");
                    lore.add(ChatColor.DARK_GREEN + "Match Status:");

                    ChatColor[] colors = new ChatColor[9];
                    for (int j = 0; j < 9; j++) {
                        Location cp = match.getArena().getCapturePoints().get(j);
                        if (match.isPointCaptured(cp) && match.getCapturingTeam(cp).equals(match.getTeam1())) {
                            colors[j] = ChatColor.BLUE;
                        } else if (match.isPointCaptured(cp) && match.getCapturingTeam(cp).equals(match.getTeam2())) {
                            colors[j] = ChatColor.RED;
                        } else {
                            colors[j] = ChatColor.WHITE;
                        }
                    }

                    lore.add("      " + colors[2] + "▇" + colors[1] + "▇" + colors[0] + "▇");
                    lore.add("      " + colors[3] + "▇" + colors[4] + "▇" + colors[5] + "▇");
                    lore.add("      " + colors[6] + "▇" + colors[7] + "▇" + colors[8] + "▇");
                } else {
                    lore.add(ChatColor.YELLOW + "On-going: No");
                    lore.add(ChatColor.DARK_GREEN + "Won by: " + ChatColor.YELLOW + match.getWinner().getName());
                }
                meta.setLore(lore);
                matchItem.setItemMeta(meta);

            gui.addItem(matchItem);
        }

        player.openInventory(gui);
		return true;
	}
	

}
