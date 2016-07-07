package com.nao20010128nao;

import java.util.List;
import java.util.Map;

import com.nao20010128nao.event.PlayerAuthenticateEvent;
import com.nao20010128nao.event.PlayerDeauthenticateEvent;
import com.nao20010128nao.event.PlayerRegisterEvent;
import com.nao20010128nao.event.PlayerUnregisterEvent;
import com.nao20010128nao.provider.DataProvider;
import com.nao20010128nao.task.ShowMessageTask;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.event.Listener;
import cn.nukkit.permission.PermissionAttachment;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

public class SimpleAuth extends PluginBase implements Listener {
	protected Map<String, PermissionAttachment> needAuth;
	protected EventListener listener;
	protected DataProvider provider;
	protected int blockPlayers = 6;
	protected Map<String, Integer> blockSessions;
	protected List<String> messages;
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
	}
}
