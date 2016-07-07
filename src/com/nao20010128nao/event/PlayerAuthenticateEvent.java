package com.nao20010128nao.event;

import com.nao20010128nao.SimpleAuth;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;

public class PlayerAuthenticateEvent extends SimpleAuthEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlers() {
		return handlers;
	}

	Player player;

	public PlayerAuthenticateEvent(SimpleAuth plugin, Player player) {
		super(plugin);
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}
}
