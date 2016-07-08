package com.nao20010128nao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import cn.nukkit.utils.Utils;

public class Util {
	private static final Class[] parameters = new Class[] { URL.class };

	/**
	 * http://stackoverflow.com/questions/1010919/adding-files-to-java-classpath
	 * -at-runtime
	 */
	public static void addURL(URLClassLoader sysloader, URL u) throws IOException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class sysclass = URLClassLoader.class;

		Method method = sysclass.getDeclaredMethod("addURL", parameters);
		method.setAccessible(true);
		method.invoke(sysloader, new Object[] { u });
	}

	public static void loadJar(SimpleAuth plugin, URLClassLoader pluginClassLoader, String name)
			throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		File f = new File(plugin.getDataFolder(), name);
		f.getParentFile().mkdirs();
		InputStream is = pluginClassLoader.getResourceAsStream(name);
		Utils.writeFile(f, is);
		is.close();
		addURL(pluginClassLoader, f.toURI().toURL());
	}

	public static void injectModules(SimpleAuth plugin) throws IOException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		URLClassLoader pluginClassLoader = (URLClassLoader) plugin.getClass().getClassLoader();
		loadJar(plugin, pluginClassLoader, "gnu-crypto/gnu-crypto.jar");
		loadJar(plugin, pluginClassLoader, "gnu-crypto/javax-crypto.jar");
		loadJar(plugin, pluginClassLoader, "gnu-crypto/javax-security.jar");
	}
}
