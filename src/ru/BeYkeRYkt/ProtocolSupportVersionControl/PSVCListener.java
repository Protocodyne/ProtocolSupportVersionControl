package ru.BeYkeRYkt.ProtocolSupportVersionControl;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class PSVCListener implements Listener {

	private ProtocolSupportVersionControl plugin;

	public PSVCListener(ProtocolSupportVersionControl plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if (event.getPlugin().getName().equals("ProtocolLib")) {
			plugin.getLogger().info("OMG! ProtocolLib disabled!");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		EntityType type = event.getEntityType();
		int id = type.getTypeId();
		if (plugin.getBlockedMobs().contains(id)) {
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		for (Entity entity : event.getChunk().getEntities()) {
			EntityType type = entity.getType();
			int id = type.getTypeId();
			if (plugin.getBlockedMobs().contains(id)) {
				entity.remove();
			}
		}
	}
}
