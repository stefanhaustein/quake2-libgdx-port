package com.googlecode.gdxquake2.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.googlecode.gdxquake2.GdxQuake2;
import com.googlecode.playnquake.java.JavaTools;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new GdxQuake2(new JavaTools()), config);
	}
}
