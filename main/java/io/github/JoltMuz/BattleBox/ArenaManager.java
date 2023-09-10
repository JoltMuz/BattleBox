	package io.github.JoltMuz.BattleBox;
	
	import java.util.Map;
	import java.util.Collection;
	import java.util.HashMap;
	import java.util.List;
	import java.util.ArrayList;
	import java.util.Random;
	
	public class ArenaManager 
	{
		private Map<String,Arena> arenas;
		
		private static ArenaManager instance;
	
	    public ArenaManager() 
	    {
	    	arenas = new HashMap<String, Arena>();
	    }
	    
	    public synchronized static ArenaManager getInstance() 
	    {
			if (instance == null) {
				instance = new ArenaManager();
			}
			return instance;
		}
	
	    public synchronized void addArena(String name, Arena arena) 
	    {
	    	
	        arenas.put(name, arena);
	    }
	
	    public synchronized void removeArena(String name) 
	    {
	    	if (!arenas.isEmpty() && arenas.containsKey(name))
	    	{
	    		arenas.remove(name);
	    	}
	    	else
	    	{
	    		throw new IllegalArgumentException("No Arena Found with that name");
	    	}
	        
	    }
	    
	    public synchronized Arena getArenaByName(String name)
	    {
	    	if (!arenas.isEmpty() && arenas.containsKey(name))
	    	{
	    		return arenas.get(name);
	    	}
	    	else
	    	{
	    		throw new IllegalArgumentException("No Arena Found with that name");
	    	}
	    	
	    }
	
	    public synchronized Map<String,Arena> getArenasWithNames() 
	    {
	        return arenas;
	    }
	    public synchronized Collection<Arena> getArenas()
	    {
	    	return arenas.values();
	    }
	    public String getNameOfArena(Arena arena) {
	        for (Map.Entry<String, Arena> entry : arenas.entrySet()) {
	            if (entry.getValue().equals(arena)) {
	                return entry.getKey();
	            }
	        }
	        throw new IllegalArgumentException("Arena not found.");
	    }
	    public synchronized Arena getRandomAvailableArena(List<Arena> usedArenas)
	    {
	    	List<Arena> availableArenas = new ArrayList<>(getArenas());
	        availableArenas.removeAll(usedArenas); // Remove the used arenas from the available arenas
	        if (availableArenas.isEmpty()) {
	            throw new IllegalStateException("No available arenas for the current round.");
	        }
	        Random random = new Random();
	        int randomIndex = random.nextInt(availableArenas.size());
	        return availableArenas.get(randomIndex);
	
	    }
	    
	}
