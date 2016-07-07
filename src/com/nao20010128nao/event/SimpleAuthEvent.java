package com.nao20010128nao.event;

import com.nao20010128nao.SimpleAuth;

import cn.nukkit.event.plugin.PluginEvent;

public abstract class SimpleAuthEvent extends PluginEvent {
	public SimpleAuthEvent(SimpleAuth plugin) {
		super(plugin);
	}
}
