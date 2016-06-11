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

public class GdxQuake2 extends ApplicationAdapter {

	public static final String DOWNLOAD_COMPLETE = "downloadComplete";

	public static Preferences imageSizes;
	public static Preferences state;
	public static PlatformTools tools;
	private boolean initialized;
	private double startTime;

	public GdxQuake2(PlatformTools tools) {
		GdxQuake2.tools = tools;
	}

	@Override
	public void create () {
		imageSizes = Gdx.app.getPreferences("imageSizes");
		state = Gdx.app.getPreferences("state");

		if (state.getBoolean(DOWNLOAD_COMPLETE, false)) {
			initGame();
		} else {
			Installer installer = new Installer(new Callback<Void>() {
				@Override
				public void onSuccess(Void result) {
					tools.println("All files successfully installed and converted");
					state.putBoolean(DOWNLOAD_COMPLETE, true);
					state.flush();
					initGame();
				}

				@Override
				public void onFailure(Throwable cause) {
					error("Error installing files", cause);
				}

			});
			installer.run();
		}
	}

	void error(String msg, Throwable cause) {
		tools.println(msg + ": " + cause.toString());
	}


	public static PlatformTools tools() {
		return tools;
	}




	public static Dimension getImageSize(String name) {
		if (name.startsWith("/")) {
			name = name.substring(1);
		}
		int size = imageSizes.getInteger(name, -1);
		return size == -1 ? null : new Dimension(size / 10000, size % 10000);
	}


	/**
	 * Game initialization, after resources are loaded / converted.
	 */
	void initGame() {
		System.out.println("Installation succeeded!");

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
