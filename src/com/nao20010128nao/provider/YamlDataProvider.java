package com.nao20010128nao.provider;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import com.nao20010128nao.SimpleAuth;

import cn.nukkit.IPlayer;
import cn.nukkit.utils.Config;

public class YamlDataProvider implements DataProvider {
	SimpleAuth plugin;

	public YamlDataProvider(SimpleAuth plugin) {
		if (!new File(plugin.getDataFolder(), "players").exists())
			new File(plugin.getDataFolder(), "players").mkdirs();
	}

	@Override
	public Map<String, Object> getPlayer(IPlayer player) {
		String name = player.getName().toLowerCase().trim();
		if (name.equals(""))
			return null;
		File path = new File(new File(new File(plugin.getDataFolder(), "players"), String.valueOf(name.charAt(0))),
				name + ".yml");
		if (path.exists()) {
			Config config = new Config(path, Config.YAML);
			return config.getAll();
		}
		return null;
	}

	@Override
	public boolean isPlayerRegistered(IPlayer player) {
		String name = player.getName().toLowerCase().trim();
		File path = new File(new File(new File(plugin.getDataFolder(), "players"), String.valueOf(name.charAt(0))),
				name + ".yml");
		return path.exists();
	}

	@Override
	public Map<String, Object> registerPlayer(IPlayer player, String hash) {
		String name = player.getName().toLowerCase().trim();
		File path = new File(new File(plugin.getDataFolder(), "players"), String.valueOf(name.charAt(0)));
		path.mkdirs();
		path = new File(path, name + ".yml");
		Config data = new Config(path, Config.YAML);
		data.set("registerdate", System.currentTimeMillis());
		data.set("logindate", System.currentTimeMillis());
		data.set("lastip", null);
		data.set("hash", hash);
		data.save();
		return data.getAll();
	}

	@Override
	public void unregisterPlayer(IPlayer player) {

	}

	@Override
	public void savePlayer(IPlayer player, Map<String, Object> config) {
		String name = player.getName().toLowerCase().trim();
		Config data = new Config(
				new File(new File(new File(plugin.getDataFolder(), "players"), String.valueOf(name.charAt(0))),
						name + ".yml"),
				Config.YAML);
		data.setAll(new LinkedHashMap<>(config));
		data.save();
	}

	@Override
	public void updatePlayer(IPlayer player, String lastIp, Long loginDate) {
		Map<String, Object> data = getPlayer(player);
		if (data != null) {
			if (lastIp != null)
				data.put("lastip", lastIp);
			if (loginDate != null)
				data.put("logindate", loginDate);
			savePlayer(player, data);
		}
	}

	@Override
	public void close() {

	}
}
