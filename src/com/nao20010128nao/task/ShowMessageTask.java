package com.nao20010128nao.task;

import java.util.ArrayList;
import java.util.List;

import com.nao20010128nao.SimpleAuth;

import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.TextFormat;

public class ShowMessageTask extends PluginTask<SimpleAuth> {
	private List<Player> playerList = new ArrayList<>();

	public ShowMessageTask(SimpleAuth owner) {
		super(owner);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public SimpleAuth getPlugin() {
		return owner;
	}

	public void addPlayer(Player player) {
		playerList.add(player);
	}

	public void removePlayer(Player player) {
		playerList.remove(player);
	}

	@Override
	public void onRun(int currentTick) {
		// TODO 自動生成されたメソッド・スタブ
		SimpleAuth plugin = getPlugin();
		if (plugin.isDisabled())
			return;
		playerList.stream().filter(p -> p != null)
				.forEach(p -> p.sendPopup(TextFormat.ITALIC + TextFormat.GRAY + getPlugin().getMessage("join.popup")));
	}
}
