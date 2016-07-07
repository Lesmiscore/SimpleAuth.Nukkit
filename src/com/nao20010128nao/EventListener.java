package com.nao20010128nao;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.inventory.InventoryOpenEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemConsumeEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.utils.Config;

public class EventListener implements Listener {
	private SimpleAuth plugin;

	public EventListener(SimpleAuth plugin) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (plugin.getConfig().getBoolean("authenticateByLastUniqueId")
				& event.getPlayer().hasPermission("simpleauth.lastid")) {
			Config config = plugin.getDataProvider().getPlayer(event.getPlayer());
			if (config != null & config.getString("lastip").equals(event.getPlayer().getUniqueId())) {
				plugin.authencatePlayer(event.getPlayer());
				return;
			}
		}
		plugin.deauthencatePlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerPreJoin(PlayerPreLoginEvent event) {
		if (!plugin.getConfig().getBoolean("forceSingleSession"))
			return;
		Player player = event.getPlayer();
		for (Player p : plugin.getServer().getOnlinePlayers().values())
			if (p != player & !p.getName().equalsIgnoreCase(player.getName()))
				if (plugin.isPlayerAuthenticated(player)) {
					event.setCancelled(true);
					player.kick("already logged in");
					return;
				}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (!plugin.isPlayerAuthenticated(event.getPlayer()))
			plugin.sendAuthenticateMessage(event.getPlayer());
	}

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		if (!plugin.isPlayerAuthenticated(event.getPlayer())) {
			String message = event.getMessage();
			if (message.startsWith("/")) {
				event.setCancelled(true);
				String command = message.substring(1);
				String[] args = command.split(" ");
				if (args[0].equalsIgnoreCase("register") | args[0].equalsIgnoreCase("login")
						| args[0].equalsIgnoreCase("help"))
					plugin.getServer().dispatchCommand(event.getPlayer(), command);
				else
					plugin.sendAuthenticateMessage(event.getPlayer());
			} else if (!event.getPlayer().hasPermission("simpleauth.chat"))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!plugin.isPlayerAuthenticated(event.getPlayer()))
			if (!event.getPlayer().hasPermission("simpleauth.chat")) {
				event.setCancelled(true);
				event.getPlayer().onGround = true;
			}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!plugin.isPlayerAuthenticated(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (!plugin.isPlayerAuthenticated(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.closePlayer(event.getPlayer());
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if (!plugin.isPlayerAuthenticated(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer() instanceof Player & !plugin.isPlayerAuthenticated(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getPlayer() instanceof Player & !plugin.isPlayerAuthenticated(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (!plugin.isPlayerAuthenticated(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPickupItem(InventoryPickupItemEvent event) {
		InventoryHolder player = event.getInventory().getHolder();
		if (player instanceof Player && !plugin.isPlayerAuthenticated((Player) player))
			event.setCancelled(true);
	}
}
