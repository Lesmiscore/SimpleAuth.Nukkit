package com.nao20010128nao.provider;

import java.util.Map;

import cn.nukkit.IPlayer;

public interface DataProvider {
	public Map<String, Object> getPlayer(IPlayer player);

	public boolean isPlayerRegistered(IPlayer player);

	public Map<String, Object> registerPlayer(IPlayer player, String hash);

	public void unregisterPlayer(IPlayer player);

	public void savePlayer(IPlayer player, Map<String, Object> config);

	public void updatePlayer(IPlayer player, String lastId, Long loginDate);

	public void close();
}
