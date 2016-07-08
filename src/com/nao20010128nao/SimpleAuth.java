package com.nao20010128nao;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.nao20010128nao.event.PlayerAuthenticateEvent;
import com.nao20010128nao.event.PlayerDeauthenticateEvent;
import com.nao20010128nao.event.PlayerRegisterEvent;
import com.nao20010128nao.event.PlayerUnregisterEvent;
import com.nao20010128nao.provider.DataProvider;
import com.nao20010128nao.provider.DummyDataProvider;
import com.nao20010128nao.provider.YamlDataProvider;
import com.nao20010128nao.task.ShowMessageTask;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Listener;
import cn.nukkit.permission.Permission;
import cn.nukkit.permission.PermissionAttachment;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import gnu.crypto.hash.Whirlpool;

public class SimpleAuth extends PluginBase implements Listener {
	protected Map<String, PermissionAttachment> needAuth;
	protected EventListener listener;
	protected DataProvider provider;
	protected int blockPlayers = 6;
	protected Map<String, Integer> blockSessions;
	protected Map<String, Object> messages;
	protected ShowMessageTask messageTask = null;

	public boolean isPlayerAuthenticated(Player player) {
		return !needAuth.containsKey(player.getName());
	}

	public boolean isPlayerRegistered(IPlayer player) {
		return provider.isPlayerRegistered(player);
	}

	public boolean authencatePlayer(Player player) {
		if (this.isPlayerAuthenticated(player))
			return true;
		PlayerAuthenticateEvent ev;
		getServer().getPluginManager().callEvent(ev = new PlayerAuthenticateEvent(this, player));
		if (ev.isCancelled())
			return false;
		if (needAuth.containsKey(player.getName())) {
			PermissionAttachment attachment = needAuth.get(player.getName());
			player.removeAttachment(attachment);
			needAuth.remove(player.getName());
		}
		provider.updatePlayer(player, player.getUniqueId().toString(), System.currentTimeMillis());
		player.sendMessage(TextFormat.GREEN + getMessage("login.success"));
		getMessageTask().removePlayer(player);
		blockSessions.remove(player.getAddress() + ":" + player.getName().toLowerCase());
		return true;
	}

	public boolean deauthenticatePlayer(Player player) {
		if (!this.isPlayerAuthenticated(player))
			return true;
		PlayerDeauthenticateEvent ev;
		getServer().getPluginManager().callEvent(ev = new PlayerDeauthenticateEvent(this, player));
		if (ev.isCancelled())
			return false;
		PermissionAttachment attachment = player.addAttachment(this);
		removePermissions(attachment);
		needAuth.put(player.getName(), attachment);
		sendAuthenticateMessage(player);
		getMessageTask().addPlayer(player);
		return true;
	}

	public void tryAuthenticatePlayer(Player player) {
		if (blockPlayers <= 0 & isPlayerAuthenticated(player))
			return;
		if (blockSessions.size() > 2048)
			blockSessions.clear();
		if (!blockSessions.containsKey(player.getAddress()))
			blockSessions.put(player.getAddress() + ":" + player.getName().toLowerCase(), 1);
		else
			blockSessions.put(player.getAddress() + ":" + player.getName().toLowerCase(),
					blockSessions.get(player.getAddress() + ":" + player.getName().toLowerCase()) + 1);
		if (blockSessions.get(player.getAddress() + ":" + player.getName().toLowerCase()) > blockPlayers) {
			player.kick(getMessage("login.error.block"), true);
			getServer().getNetwork().blockAddress(player.getAddress(), 600);
		}
	}

	public boolean registerPlayer(IPlayer player, String password) {
		if (!isPlayerRegistered(player)) {
			PlayerRegisterEvent ev;
			getServer().getPluginManager().callEvent(ev = new PlayerRegisterEvent(this, (Player) player));
			if (ev.isCancelled())
				return false;
			provider.registerPlayer(player, hash(player.getName().toLowerCase(), password));
			return true;
		}
		return false;
	}

	public boolean unregisterPlayer(IPlayer player, String password) {
		if (isPlayerRegistered(player)) {
			PlayerUnregisterEvent ev;
			getServer().getPluginManager().callEvent(ev = new PlayerUnregisterEvent(this, (Player) player));
			if (ev.isCancelled())
				return false;
			provider.unregisterPlayer(player);
		}
		return true;
	}

	public DataProvider getDataProvider() {
		return provider;
	}

	public void setDataProvider(DataProvider provider) {
		this.provider = provider;
	}

	public void closePlayer(Player player) {
		needAuth.remove(player.toString());
		messageTask.removePlayer(player);
	}

	public void sendAuthenticateMessage(Player player) {
		Map<String, Object> config = provider.getPlayer(player);
		player.sendMessage(TextFormat.ITALIC + TextFormat.GRAY + getMessage("join.message1"));
		player.sendMessage(TextFormat.ITALIC + TextFormat.GRAY + getMessage("join.message2"));
		if (config == null)
			player.sendMessage(TextFormat.ITALIC + TextFormat.GRAY + getMessage("join.register"));
		else
			player.sendMessage(TextFormat.ITALIC + TextFormat.GRAY + getMessage("join.login"));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		switch (command.getName().toLowerCase()) {
			case "login":
				if (sender instanceof Player) {
					Map<String, Object> data;
					if (!isPlayerRegistered((IPlayer) sender) | (data = provider.getPlayer((IPlayer) sender)) == null) {
						sender.sendMessage(TextFormat.RED + getMessage("login.error.registered"));

						return true;
					}
					if (args.length != 1) {
						sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());

						return true;
					}

					String password = String.join(" ", args);

					if (data.get("hash").equals(hash(sender.getName().toLowerCase(), password)))
						if (authencatePlayer((Player) sender))
							return true;
						else {
							tryAuthenticatePlayer((Player) sender);
							sender.sendMessage(TextFormat.RED + getMessage("login.error.password"));
							return true;
						}
				} else
					sender.sendMessage(TextFormat.RED + "This command only works in-game.");
				break;
			case "register":
				if (sender instanceof Player) {
					if (isPlayerRegistered((IPlayer) sender)) {
						sender.sendMessage(TextFormat.RED + getMessage("register.error.registered"));

						return true;
					}
					String password = String.join(" ", args);
					if (registerPlayer((IPlayer) sender, password) & authencatePlayer((Player) sender))
						return true;
					else {
						sender.sendMessage(TextFormat.RED + getMessage("register.error.general"));
						return true;
					}
				} else
					sender.sendMessage(TextFormat.RED + "This command only works in-game.");
				break;
		}
		return false;
	}

	private Map<String, Object> parseMessages(Map<String, Object> messages) {
		Map<String, Object> result = new HashMap<>();
		messages.forEach((key, value) -> {
			if (value instanceof Map)
				parseMessages((Map<String, Object>) value).forEach((k, v) -> result.put(key + "." + k, v));
			else
				result.put(key, value);
		});
		return result;
	}

	public String getMessage(String key) {
		return (String) messages.getOrDefault(key, key);
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		reloadConfig();
		saveResource("messages.yml", false);
		Map<String, Object> messages = new Config(new File(getDataFolder(), "messages.yml")).getAll();
		this.messages = parseMessages(messages);
		Command registerCommand = (Command) getCommand("register");
		registerCommand.setUsage(getMessage("register.usage"));
		registerCommand.setDescription(getMessage("register.description"));
		registerCommand.setPermissionMessage(getMessage("register.permission"));

		Command loginCommand = (Command) getCommand("login");
		loginCommand.setUsage(getMessage("login.usage"));
		loginCommand.setDescription(getMessage("login.description"));
		loginCommand.setPermissionMessage(getMessage("login.permission"));

		blockPlayers = getConfig().getInt("blockAfterFail", 6);

		String provider = getConfig().getString("dataProvider");
		this.provider = null;
		switch (provider.toLowerCase()) {
			case "yaml":
				getLogger().debug("Using YAML data provider");
				this.provider = new YamlDataProvider(this);
				break;
			case "sqlite3":
				getLogger().error("Provider \"" + provider + "\" isn't supported right now!");
				getLogger().error("Please convert the provider format to YAML!");
			case "none":
			default:
				this.provider = new DummyDataProvider(this);
				break;
		}

		listener = new EventListener(this);
		getServer().getPluginManager().registerEvents(listener, this);
		getServer().getOnlinePlayers().values().stream().forEach(player -> deauthenticatePlayer(player));
		getLogger().info("Everything loaded!");
	}

	@Override
	public void onDisable() {
		provider.close();
		messageTask = null;
		blockSessions.clear();
	}

	public static int orderPermissionsCallback(String perm1, String perm2) {
		if (isChild(perm1, perm2))
			return -1;
		else if (isChild(perm2, perm1))
			return 1;
		else
			return 0;
	}

	public static boolean isChild(String perm, String name) {
		String[] perm_ = perm.split(Pattern.quote("_"));
		String[] name_ = name.split(Pattern.quote("_"));
		for (int k = 0; k < perm_.length; k++) {
			String component = perm_[k];
			if (name_.length < k)
				return false;
			else if (!name_[k].equals(component))
				return false;
		}
		return true;
	}

	protected void removePermissions(PermissionAttachment attachment) {
		Map<String, Boolean> permissions = new HashMap<>();
		for (Permission permission : getServer().getPluginManager().getPermissions().values())
			permissions.put(permission.getName(), false);
		permissions.put("pocketmine.command.help", true);
		permissions.put(Server.BROADCAST_CHANNEL_USERS, true);
		permissions.put(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, true);

		permissions.remove("simpleauth.chat");
		permissions.remove("simpleauth.move");
		permissions.remove("simpleauth.lastid");

		permissions.put("simpleauth.command.register", !getConfig().getBoolean("disableRegister"));
		permissions.put("simpleauth.command.login", !getConfig().getBoolean("disableLogin"));

		attachment.setPermissions(permissions);
	}

	private String hash(String salt, String password) {
		try {
			MessageDigest sha512 = MessageDigest.getInstance("sha512");
			sha512.update(password.getBytes());
			sha512.update(salt.getBytes());
			//////
			Whirlpool whirlpool = new Whirlpool();
			byte[] tmp = salt.getBytes();
			whirlpool.update(tmp, 0, tmp.length);
			tmp = password.getBytes();
			whirlpool.update(tmp, 0, tmp.length);
			//////
			byte[] sha512Result = sha512.digest();
			byte[] whirlpoolResult = whirlpool.digest();
			byte[] result = new byte[64];
			for (int i = 0; i < 64; i++)
				result[i] = (byte) (sha512Result[i] ^ whirlpoolResult[i]);
			sha512.reset();
			whirlpool.reset();
			return bin2hex(result);
		} catch (NoSuchAlgorithmException e) {
			char[] data = new char[64];
			Arrays.fill(data, '0');
			return String.valueOf(data);
		}
	}

	protected ShowMessageTask getMessageTask() {
		if (messageTask == null) {
			messageTask = new ShowMessageTask(this);
			getServer().getScheduler().scheduleRepeatingTask(messageTask, 10);
		}
		return messageTask;
	}

	private static String bin2hex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append(Character.forDigit(b >> 4 & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
		return sb.toString();
	}
}
