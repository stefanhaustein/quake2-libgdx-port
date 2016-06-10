package com.googlecode.gdxquake2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.TimeUtils;
import com.googlecode.gdxquake2.core.gdx.PlayNSoundImpl;
import com.googlecode.gdxquake2.core.gl11.GL11Emulation;
import com.googlecode.gdxquake2.core.id.client.Dimension;
import com.googlecode.gdxquake2.core.id.client.Screen;
import com.googlecode.gdxquake2.core.id.common.ConsoleVariables;
import com.googlecode.gdxquake2.core.id.common.Globals;
import com.googlecode.gdxquake2.core.id.common.QuakeCommon;
import com.googlecode.gdxquake2.core.id.common.ResourceLoader;
import com.googlecode.gdxquake2.core.id.render.GlRenderer;
import com.googlecode.gdxquake2.core.id.sound.Sound;
import com.googlecode.gdxquake2.core.tools.Callback;
import com.googlecode.gdxquake2.core.tools.PlatformTools;

import java.util.HashMap;
import java.util.Map;

public class GdxQuake2 extends ApplicationAdapter {

	public static Preferences prefs;

	public static PlatformTools tools;
	private static Map<String,Dimension> imageSizes = new HashMap<String,Dimension>();
	private boolean initialized;
	private double startTime;

	public GdxQuake2(PlatformTools tools) {
		GdxQuake2.tools = tools;
	}

	@Override
	public void create () {
		prefs = Gdx.app.getPreferences("default");
		Installer installer = new Installer(tools, prefs, new Callback<Void>() {
			@Override
			public void onSuccess(Void result) {
				tools.println("All files successfully installed and converted");
				initGame();
			}

			@Override
			public void onFailure(Throwable cause) {
				error("Error installing files", cause);
			}

		});
		installer.run();
	}

	void error(String msg, Throwable cause) {
		tools.println(msg + ": " + cause.toString());
	}


	public static PlatformTools tools() {
		return tools;
	}




	public static Dimension getImageSize(String name) {
		if (!name.startsWith("/")) {
			name = "/" + name;
		}
		return imageSizes.get(name);
	}


	/**
	 * Game initialization, after resources are loaded / converted.
	 */
	void initGame() {
		System.out.println("Installation succeeded!");

		loadImageSizes();
		Globals.autojoin.value = 0;
		Globals.re = new GlRenderer(
				new GL11Emulation(Gdx.gl20),
				Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());


		//   Globals.re.GLimp_SetMode(new Dimension(PlayN.graphics().screenWidth(),  PlayN.graphics().screenHeight()), 0, false);

//    System.out.println("Screen dimension: " + new Dimension(PlayN.graphics().screenWidth(),  PlayN.graphics().screenHeight()));

		ResourceLoader.impl = new ResourceLoaderImpl();
		Sound.impl = new PlayNSoundImpl();


		QuakeCommon.Init(new String[] { "GQuake" });
		Globals.nostdout = ConsoleVariables.Get("nostdout", "0", 0);

		startTime = TimeUtils.millis();

		initialized = true;
	}

	void loadImageSizes() {
		String all = prefs.getString("imageSizes");
		for(String line: all.split("\n")) {
			String[] parts = line.split(",");
			if (parts.length > 2) {
				imageSizes.put(parts[0], new Dimension(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
			} else {
				System.out.println("Strange imageSizes line: '" + line + "'");
			}
		}

		System.out.println("" + imageSizes);
	}


	@Override
	public void render() {
		Globals.re.checkPendingImages();
		if (!initialized) {
			return;
		}
		if (ResourceLoader.Pump()) {
			Screen.UpdateScreen2();
		} else {
			double curTime = TimeUtils.millis();
			// GwtKBD.Frame((int) alpha);
			QuakeCommon.Frame((int) (curTime - startTime));
			startTime = curTime;
		}
	}
}
