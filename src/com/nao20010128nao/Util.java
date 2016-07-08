package com.nao20010128nao;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

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

	public static void injectModules(SimpleAuth plugin) throws IOException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		URLClassLoader pluginClassLoader = (URLClassLoader) plugin.getClass().getClassLoader();
		addURL(pluginClassLoader, pluginClassLoader.getResource("gnu-crypto/gnu-crypto.jar"));
		addURL(pluginClassLoader, pluginClassLoader.getResource("gnu-crypto/javax-crypto.jar"));
		addURL(pluginClassLoader, pluginClassLoader.getResource("gnu-crypto/javax-security.jar"));
	}
}
