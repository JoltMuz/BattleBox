package io.github.JoltMuz.BattleBox;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Color;

public class Match {
    private final int matchID;
    private static int nextID = 1;
    private final Team team1;
    private final Team team2;
    private Team winner;
    private Team loser;
    private final Arena arena;
    private boolean inProgress;
    private final CapturePointManager capturePointManager;
    private static String prefix;

    public Match(Team team1, Team team2, Arena arena) {
        this.matchID = nextID++;
        this.team1 = team1;
        this.team2 = team2;
        this.arena = arena;
        this.inProgress = false;
        this.capturePointManager = new CapturePointManager();
        prefix = Commands.getPrefix();
    }

    public int getID() {
        return matchID;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public Team getWinner() {
        return winner;
    }

    public Team getLoser() {
        return loser;
    }

    public Arena getArena() {
        return arena;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void start() {
        inProgress = true;
        setupCapturePoints();
        giveItemsAndTeleportTeams();
        startCountdown();
        broadcastMessage(prefix + ChatColor.BLUE + team1.getName() + ChatColor.YELLOW + " vs " + ChatColor.RED + team2.getName());
    }

    private void setupCapturePoints() {
        for (Location capturePoint : arena.getCapturePoints()) {
            capturePoint.getBlock().setType(Material.WOOL);
        }
    }

    private void giveItemsAndTeleportTeams() {
        giveItemsAndTeleportTeam(team1, Color.BLUE, Material.WOOL, (short) 11, "blue");
        giveItemsAndTeleportTeam(team2, Color.RED, Material.WOOL, (short) 14, "red");
    }

    private void giveItemsAndTeleportTeam(Team team, Color armorColor, Material woolMaterial, short woolColor, String nameColor) {
    	ItemStack helmet = createUnbreakableColoredArmor(Material.LEATHER_HELMET, armorColor);
    	ItemStack chestplate = createUnbreakableColoredArmor(Material.LEATHER_CHESTPLATE, armorColor);
    	ItemStack leggings = createUnbreakableArmor(Material.IRON_LEGGINGS);
    	ItemStack boots = createUnbreakableArmor(Material.IRON_BOOTS);

        for (Player member : team.getAllPlayers()) {
            member.getInventory().setItem(0, new ItemStack(Material.STONE_SWORD));
            member.getInventory().setItem(1, new ItemStack(Material.BOW));
            member.getInventory().setItem(2, new ItemStack(Material.SHEARS));
            member.getInventory().setItem(4, new ItemStack(woolMaterial, 64, woolColor));
            member.getInventory().setItem(8, new ItemStack(Material.ARROW, 12));
            member.getInventory().setHelmet(helmet);
            member.getInventory().setChestplate(chestplate);
            member.getInventory().setLeggings(leggings);
            member.getInventory().setBoots(boots);
            teleportPlayer(member, getSpawnPoint(team));
            member.setWalkSpeed(0);
            NameTagColorManager.addColor(member, nameColor);
        }
    }

    private ItemStack createUnbreakableColoredArmor(Material material, Color color) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        meta.spigot().setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }
    private ItemStack createUnbreakableArmor(Material material)
    {
    	ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.spigot().setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    private void teleportPlayer(Player player, Location location) {
        player.teleport(location);
    }

    private void startCountdown() {
        BukkitRunnable countdownTask = new BukkitRunnable() {
            private int seconds = 5;

            @Override
            public void run() {
                if (seconds == 0) {
                    setWalkSpeed(team1, (float) 0.2);
                    setWalkSpeed(team2, (float) 0.2);
                    cancel();
                    teleportTeamToSpawn(team1);
                    teleportTeamToSpawn(team2);
                    playCountdownSoundToTeams(Sound.ENDERDRAGON_GROWL);
                    return;
                }
                sendTitleToTeams(seconds);
                playCountdownSoundToTeams(Sound.NOTE_STICKS);
                teleportTeamToSpawn(team1);
                teleportTeamToSpawn(team2);
                seconds--;
            }
        };
        countdownTask.runTaskTimer(Main.getInstance(), 0, 20); // Run every second (20 ticks)
    }

    private void sendTitleToTeams(int countdown) {
        String titleColor = ChatColor.WHITE.toString();
        if (countdown == 5) {
            titleColor = ChatColor.DARK_GREEN.toString();
        } else if (countdown == 4) {
            titleColor = ChatColor.GREEN.toString();
        } else if (countdown == 3) {
            titleColor = ChatColor.YELLOW.toString();
        } else if (countdown == 2) {
            titleColor = ChatColor.RED.toString();
        } else if (countdown == 1) {
            titleColor = ChatColor.DARK_RED.toString();
        }
        for (Player player : getAllPlayers()) {
            TitleSender.sendTitle(player, titleColor + countdown, "", 10, 20, 10);
        }
    }

    private void setWalkSpeed(Team team, float speed) {
        team.getLeader().setWalkSpeed(speed);
        for (Player player : team.getMembers()) {
            player.setWalkSpeed(speed);
        }
    }

    private void playCountdownSoundToTeams(Sound sound) {
        float volume = 1.0f;
        float pitch = 1.0f;
        for (Player player : getAllPlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    private void teleportTeamToSpawn(Team team) {
    	Location spawn = getSpawnPoint(team);
        for (Player player : team.getAllPlayers()) {
            teleportPlayer(player, spawn);
        }
    }

    public Map<Location, Team> getStatus() {
        return capturePointManager.getCapturedPoints();
    }
    public int getCapturedPointCount(Team team) {
        return capturePointManager.getCapturedPointCount(team);
    }

    public boolean isPointCaptured(Location capturePoint) {
        return capturePointManager.isCaptured(capturePoint);
    }

    public Team getCapturingTeam(Location capturePoint) {
        return capturePointManager.getCapturingTeam(capturePoint);
    }

    public void capturePoint(Location capturePoint, Team team) {
        capturePointManager.capturePoint(capturePoint, team);
    }

    public void releasePoint(Location capturePoint) {
        capturePointManager.releasePoint(capturePoint);
    }

    public void end(Team winner) {
        inProgress = false;
        ChatColor color = ChatColor.WHITE;
        if (team1.equals(winner)) {
        	color = ChatColor.BLUE;
            this.winner = team1;
            this.loser = team2;
            team1.setScore(team1.getScore() + 1);
        } else if (team2.equals(winner)) {
        	color = ChatColor.RED;
            
            this.winner = team2;
            this.loser = team1;
            team2.setScore(team2.getScore() + 1);
        }
        broadcastMessage(prefix + color + winner.getName() + ChatColor.YELLOW + " has won!");
        for (Player player : getAllPlayers()) {
            clearInventoryAndHealPlayer(player);
            teleportPlayer(player, arena.getCapturePointsMid().getWorld().getSpawnLocation());
            TitleSender.sendTitle(player, color + winner.getName() +" Won", "", 10, 20, 10);
            NameTagColorManager.removeColor(player);
            player.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST2, 1.0f, 1.0f);
        }

    }

    private void clearInventoryAndHealPlayer(Player player) {
        player.getInventory().clear();
        player.setHealth(20);
        player.getInventory().setArmorContents(null);
    }

    public ArrayList<Player> getAllPlayers()
    {
    	ArrayList<Player> allPlayers = new ArrayList<Player>();
    	allPlayers.addAll(team1.getAllPlayers());
    	allPlayers.addAll(team2.getAllPlayers());
        return allPlayers;
    }

    private void broadcastMessage(String message) {
        Main.getInstance().getServer().broadcastMessage(message);
    }

    public Location getSpawnPoint(Team team) {
        return team == team1 ? arena.getTeam1SpawnPoint() : arena.getTeam2SpawnPoint();
    }
}
