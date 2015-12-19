package Protocodyne.swan201.PSVC;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class PSVC extends JavaPlugin {

	private List<Integer> versions;
	private List<Integer> entities;

	private static PSVC plugin;
	private String kickMessage;
	private ProtocolManager manager;

	private String minMinecraftVersion;
	private int minProtocolVersion;

	private String maxMinecraftVersion;
	private int maxProtocolVersion;
	private String versionMsg;

	@SuppressWarnings("static-access")
	@Override
	public void onEnable() {
		this.plugin = this;
		this.versions = new ArrayList<Integer>();
		this.entities = new ArrayList<Integer>();
		FileConfiguration fc = getConfig();
		try {
			if (!new File(getDataFolder(), "config.yml").exists()) {
				fc.options().header("PSVC (PSVC) v" + getDescription().getVersion() + " Configuration" + "\nHave fun :3" + "\nby BeYkeRYkt" + "\nSupported protocol versions: " + "\n- 61 (1.5.2)" + "\n- 74 (1.6.2)" + "\n- 78 (1.6.4)" + "\n- 4 (1.7.5)" + "\n- 5 (1.7.10)" + "\n- 47 (1.8)" + "\nReplacers formula:" + "\n- ProtocolVersion : oldID : newID");
				// protocol versions
				List<Integer> versions = new ArrayList<Integer>();
				versions.add(-2); // PE
				versions.add(51); // 1.4.7
				versions.add(60); // 1.5.1
				versions.add(61); // 1.5.2
				versions.add(73); // 1.6.1
				versions.add(74); // 1.6.2
				versions.add(78); // 1.6.4
				versions.add(4); // 1.7.5
				versions.add(5); // 1.7.10
				versions.add(47); // 1.8
				fc.set("SupportProtocolVersions", versions);

				// block replacer
				List<String> block = new ArrayList<String>();
				block.add("51:95:20"); // ProtocolVersion:oldID:newID
				fc.set("ReplaceBlockIDs", block);

				// item replacer
				List<String> item = new ArrayList<String>();
				item.add("51:95:20"); // ProtocolVersion:oldID:newID
				fc.set("ReplaceItemIDs", item);

				// living entity spawn block
				List<Integer> livingIds = new ArrayList<Integer>();
				livingIds.add(0);
				fc.set("EntitySpawnBlock", livingIds);

				fc.set("Messages.kick", "Your version of game is not supported on this server. \nActual versions: &a%VERSIONS%");
				saveConfig();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.manager = ProtocolLibrary.getProtocolManager();

		// register ProtocolLib
		manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Login.Client.START) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				ProtocolVersion version = ProtocolSupportAPI.getProtocolVersion(event.getPlayer());
				if (!getSupportedProtocolVersions().contains(version.getId())) {
					PacketContainer packet = new PacketContainer(PacketType.Login.Server.DISCONNECT);
					packet.getModifier().writeDefaults();
					packet.getChatComponents().write(0, WrappedChatComponent.fromText(kickMessage));
					event.setCancelled(true);
					sendPacket(event.getPlayer(), packet);
				}
			}
		});

		manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Status.Server.OUT_SERVER_INFO) {
			@Override
			public void onPacketSending(PacketEvent event) {
				ProtocolVersion version = ProtocolSupportAPI.getProtocolVersion(event.getPlayer());
				if (!getSupportedProtocolVersions().contains(version.getId())) {
					if (version == ProtocolVersion.MINECRAFT_1_8) {
						event.getPacket().getServerPings().read(0).setVersionProtocol(maxProtocolVersion);
					} else {
						event.getPacket().getServerPings().read(0).setVersionProtocol(minProtocolVersion);
					}
					event.getPacket().getServerPings().read(0).setVersionName(minMinecraftVersion + " - " + maxMinecraftVersion);
				}
			}
		});

		// remapping...
		loadProtocolVersions(fc.getStringList("SupportProtocolVersions"));
		loadItemReplace(fc.getStringList("ReplaceItemIDs"));
		loadBlockReplace(fc.getStringList("ReplaceBlockIDs"));
		loadBlockedMobs(fc.getIntegerList("EntitySpawnBlock"));

		// messages
		setKickMessage(ChatColor.translateAlternateColorCodes('&', fc.getString("Messages.kick")));

		getServer().getPluginManager().registerEvents(new PSVCListener(this), this);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void DecompileProtect() {
		ArrayList list = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10)}));
		list.stream().filter((num) -> {
		return ((Integer) num).intValue() % 2 == 0;
		});
	}
	@SuppressWarnings("static-access")
	@Override
	public void onDisable() {
		this.plugin = null;
		this.versions.clear();
		this.entities.clear();
		this.kickMessage = null;
		this.versionMsg = null;
		this.manager = null;
		this.minMinecraftVersion = null;
		this.maxMinecraftVersion = null;
		this.minProtocolVersion = 0;
		this.maxProtocolVersion = 0;
	}

	public void sendPacket(Player player, PacketContainer packet) {
		try {
			manager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void loadProtocolVersions(List<String> list) {
		int min = ProtocolVersion.fromId(47).ordinal(); // 1.8
		int max = ProtocolVersion.fromId(-2).ordinal(); // PE
		for (String string : list) {
			int protocolVersion = Integer.parseInt(string);
			ProtocolVersion version = ProtocolVersion.fromId(protocolVersion);
			if (version == ProtocolVersion.UNKNOWN) {
				getLogger().info("Cannot load protocol version: " + protocolVersion + ". Reason: Unknown protocol version");
				return;
			}

			// int minimum protocol version
			if (version.ordinal() > min) {
				min = version.ordinal();
				minProtocolVersion = protocolVersion;
			}

			// int max protocol version
			if (version.ordinal() < max) {
				max = version.ordinal();
				maxProtocolVersion = protocolVersion;
			}

			if (versionMsg == null) {
				versionMsg = getVersion(protocolVersion) + ", ";
			} else {
				versionMsg = versionMsg + getVersion(protocolVersion) + ", ";
			}

			versions.add(version.getId());
		}

		versionMsg = versionMsg.substring(0, versionMsg.length() - 2);
		minMinecraftVersion = getVersion(minProtocolVersion);
		maxMinecraftVersion = getVersion(maxProtocolVersion);
	}

	private String getVersion(int protocolVersion) {
		switch (protocolVersion) {
			case -2:
				return "PE";
			case 51:
				return "1.4.7";
			case 60:
				return "1.5.1";
			case 61:
				return "1.5.2";
			case 73:
				return "1.6.1";
			case 74:
				return "1.6.2";
			case 78:
				return "1.6.4";
			case 4:
				return "1.7.5";
			case 5:
				return "1.7.10";
			case 47:
				return "1.8";
			default:
				break;
		}
		return "1.8";
	}

	private void loadBlockReplace(List<String> list) {
		for (String string : list) {
			String[] parts = string.split(":");
			if (parts.length < 3) {
				getLogger().info("Cannot load block. Reason: length < 3");
				return;
			}
			int protocolVersion = Integer.parseInt(parts[0]);
			int oldId = Integer.parseInt(parts[1]);
			int newId = Integer.parseInt(parts[2]);

			if (protocolVersion == 0) {
				getLogger().info("Cannot load block: " + oldId + ". Reason: Unknown protocol version");
				return;
			}

			ProtocolVersion version = ProtocolVersion.fromId(protocolVersion);
			if (version == null) {
				getLogger().info("Cannot load block: " + oldId + ". Reason: Unknown protocol version");
				return;
			}
			ProtocolSupportAPI.getBlockRemapper(version).setRemap(oldId, newId);
		}
	}

	private void loadItemReplace(List<String> list) {
		for (String string : list) {
			String[] parts = string.split(":");
			if (parts.length < 3) {
				getLogger().info("Cannot load item. Reason: length < 3");
				return;
			}
			int protocolVersion = Integer.parseInt(parts[0]);
			int oldId = Integer.parseInt(parts[1]);
			int newId = Integer.parseInt(parts[2]);

			if (protocolVersion == 0) {
				getLogger().info("Cannot load item " + oldId + " to " + newId + ". Reason: Unknown protocol version");
				return;
			}

			ProtocolVersion version = ProtocolVersion.fromId(protocolVersion);
			if (version == null) {
				getLogger().info("Cannot load item " + oldId + " to " + newId + ". Reason: Unknown protocol version");
				return;
			}
			ProtocolSupportAPI.getItemRemapper(version).setRemap(oldId, newId);
		}
	}

	private void loadBlockedMobs(List<Integer> list) {
		for (Integer id : list) {
			if (!entities.contains(id)) {
				entities.add(id);
			}
		}
	}

	public static PSVC getInstance() {
		return plugin;
	}

	public List<Integer> getSupportedProtocolVersions() {
		return versions;
	}

	public String getKickMessage() {
		return kickMessage;
	}

	public void setKickMessage(String message) {
		message = message.replace("%MIN_VERSION%", minMinecraftVersion);
		message = message.replace("%MAX_VERSION%", maxMinecraftVersion);
		message = message.replace("%VERSIONS%", versionMsg);
		this.kickMessage = message;
	}

	public List<Integer> getBlockedMobs() {
		return entities;
	}

	public void setMobs(List<Integer> mobs) {
		this.entities = mobs;
	}
}
