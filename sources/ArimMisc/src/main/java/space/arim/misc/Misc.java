package space.arim.misc;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import space.arim.universal.registry.RegistryPriority;
import space.arim.universal.registry.UniversalRegistry;
import space.arim.universal.util.web.HttpStatusException;

import space.arim.api.platform.spigot.DefaultUUIDResolution;
import space.arim.api.platform.spigot.SpigotMessages;
import space.arim.api.util.StringsUtil;
import space.arim.api.util.log.LoggerConverter;
import space.arim.api.util.web.FetcherException;
import space.arim.api.util.web.FetcherUtil;
import space.arim.api.uuid.UUIDResolution;

import space.arim.lib.ArimLibPlugin;
import space.arim.lib.api.ArimLib;
import space.arim.misc.altcheck.AltCheck;
import space.arim.misc.listener.AutoLapis;
import space.arim.misc.listener.General;
import space.arim.misc.listener.SilkTouchSpawners;
import space.arim.misc.listener.Respawner;

import net.luckperms.api.LuckPerms;

public class Misc extends JavaPlugin {
	
	private AltCheck altcheck;
	private ArimLib api;
	
	@Override
	public void onEnable() {
		api = ArimLibPlugin.inst();

		getServer().getPluginManager().registerEvents(new Respawner(api), this);
		getServer().getPluginManager().registerEvents(new General(this, api), this);
		getServer().getPluginManager().registerEvents(new AutoLapis("Factions"), this);
		getServer().getPluginManager().registerEvents(new SilkTouchSpawners("Factions"), this);

		altcheck = new AltCheck(LoggerConverter.get().convert(getLogger()), new File(getDataFolder(), "altcheck"),
				UniversalRegistry.get(), (name) -> DefaultUUIDResolution.resolveFromCache(this, name),
				(uuid) -> DefaultUUIDResolution.resolveFromCache(this, uuid));
		altcheck.startLoad();

		getServer().getPluginCommand("altcheck").setExecutor((sender, command, label, args) -> {
			if (sender.hasPermission("arim.helper")) {
				if (args.length > 0) {
					UUID uuid = altcheck.resolveImmediately(args[0]);
					if (uuid != null) {
						altcheck.getIps(uuid).forEach((ip) -> {
							sendMessage(sender, "&7IP: &e" + ip + "&7. Players: " + StringsUtil.concat(altcheck.getPlayerNamesForIp(ip), ',') + ".");
						});
					} else {
						sendMessage(sender, "&cThat player cannot be found.");
					}
				} else {
					sendMessage(sender, "&6Arim>> &cUsage: /altcheck &e<player>&c.");
				}
			} else {
				sendMessage(sender, "&cSorry, you cannot use this.");
			}
			return true;
		});
		getServer().getPluginCommand("namehistory").setExecutor((sender, command, label, args) -> {
			if (sender.hasPermission("arim.helper")) {
				if (args.length > 0) {
					UUID uuid = altcheck.resolveImmediately(args[0]);
					if (uuid != null) {
						try {
							for (Map.Entry<String, String> entry : FetcherUtil.ashconNameHistory(uuid)) {
								String changedAt = entry.getValue();
								sendMessage(sender, "&3" + entry.getKey() + "&7, "+ ((changedAt.isEmpty()) ? "current" : changedAt));
							}
						} catch (FetcherException | HttpStatusException ex) {
							sendMessage(sender, "&6Arim>> &cError: \"" + ex.getMessage() + "\"&7. Please try again");
						}
					} else {
						sendMessage(sender, "&cThat player cannot be found.");
					}
				} else {
					sendMessage(sender, "&6Arim>> &cUsage: /altcheck &e<player>&c.");
				}
			} else {
				sendMessage(sender, "&cSorry, you cannot use this.");
			}
			return true;
		});
		(new ArimPapiExpansion()).register();
		if (getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
			RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
			if (provider != null) {
				LuckPerms lp = provider.getProvider();
				if (lp != null) {
					UniversalRegistry.get().register(UUIDResolution.class, RegistryPriority.HIGHER, new LuckPermsUUIDResolution(lp), "LuckPerms");
				}
			}
		}
		getServer().getPluginCommand("bossbars").setExecutor((sender, command, label, args) -> {
			if (sender.hasPermission("arim.admin")) {
				if (args.length >= 2) {
					int index = 0;
					try {
						index = Integer.parseInt(args[0]);
					} catch (NumberFormatException ex) {
						sendMessage(sender, "&cInvalid number: " + args[0]);
						return true;
					}
					List<String> bars = api.broadcast().getMutableBars();
					if (index > 0 && index <= bars.size()) {
						StringBuilder builder = new StringBuilder();
						for (int n = 1; n < args.length; n++) {
							builder.append(' ').append(args[n]);
						}
						bars.set(index - 1, builder.substring(1));
						sendMessage(sender, "&7Bossbar has been set.");
					} else {
						sendMessage(sender, "&cInvalid index: " + index);
					}
				} else {
					sendMessage(sender, "&6Arim>> &a&lBossbars");
					int n = 1;
					for (String line : api.broadcast().getMutableBars()) {
						sendMessage(sender, "&3" + n + ". &7" + line);
						n++;
					}
					sendMessage(sender, "&7To change a bossbar, use &e/bossbars <number> <content>&7.");
				}
			} else {
				sendMessage(sender, "&cSorry, you cannot use this.");
			}
			return true;
		});
		getServer().getScheduler().runTaskLater(this, altcheck::finishLoad, 1L);
	}
	
	private void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(SpigotMessages.get().transformFormattingCodes(message));
	}
	
	public AltCheck getAltCheck() {
		return altcheck;
	}
	
	@Override
	public void onDisable() {
		altcheck.close();
	}
	
}
