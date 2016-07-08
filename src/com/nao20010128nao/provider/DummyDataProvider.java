package com.nao20010128nao.provider;

import java.util.Map;

import com.nao20010128nao.SimpleAuth;

import cn.nukkit.IPlayer;

public class DummyDataProvider implements DataProvider {

	public DummyDataProvider(SimpleAuth p) {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	public Map<String, Object> getPlayer(IPlayer player) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public boolean isPlayerRegistered(IPlayer player) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public Map<String, Object> registerPlayer(IPlayer player, String hash) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void unregisterPlayer(IPlayer player) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void savePlayer(IPlayer player, Map<String, Object> config) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void updatePlayer(IPlayer player, String lastId, Long loginDate) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void close() {
		// TODO 自動生成されたメソッド・スタブ

	}

}
