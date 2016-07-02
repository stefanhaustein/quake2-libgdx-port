package com.googlecode.gdxquake2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.googlecode.gdxquake2.game.sound.ALSoundImpl;
import com.googlecode.gdxquake2.gl11.GL11Emulation;
import com.googlecode.gdxquake2.game.client.Dimension;
import com.googlecode.gdxquake2.game.client.Screen;
import com.googlecode.gdxquake2.game.common.ConsoleVariables;
import com.googlecode.gdxquake2.game.common.Globals;
import com.googlecode.gdxquake2.game.common.QuakeCommon;
import com.googlecode.gdxquake2.game.common.ResourceLoader;
import com.googlecode.gdxquake2.game.render.GlRenderer;
import com.googlecode.gdxquake2.game.sound.Sound;
import com.googlecode.gdxquake2.installer.Installer;
import com.googlecode.gdxquake2.game.gdxadapter.ResourceLoaderImpl;
import com.googlecode.gdxquake2.gdxext.AsyncLocalStorage;
import com.googlecode.gdxquake2.gdxext.Callback;

public class GdxQuake2 extends ApplicationAdapter {
	static final String DOWNLOAD_COMPLETE = "downloadComplete";

	public static PlatformTools tools;
	public static Preferences imageSizes;
	public static AsyncLocalStorage asyncLocalStorage;
	public static boolean properGL20;

	private static Preferences state;
	private static Label statusLabel;

	private boolean initialized;
	private double startTime;
	private Stage installationStage;


	public GdxQuake2(PlatformTools tools) {
		GdxQuake2.tools = tools;
	}

	@Override
	public void create () {
		asyncLocalStorage = new AsyncLocalStorage();

		imageSizes = Gdx.app.getPreferences("q2gdx-imageSizes");
		state = Gdx.app.getPreferences("q2gdx-state");

		if (state.getBoolean(DOWNLOAD_COMPLETE, false)) {
			initGame();
		} else {
			showInstaller();
		}
	}

	void initError(String msg, Throwable cause) {
		showInitStatus(msg + ": " + cause.toString());
	}


	/**
	 * Use this to show updates before the quake engine is ready.
     */
	public static void showInitStatus(String s) {
		if (statusLabel != null) {
			statusLabel.setText(s);
		}
	}

	public void showInstaller() {
		Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
		installationStage = new Stage();
		Gdx.input.setInputProcessor(installationStage);

		final Table table = new Table(skin);
		table.setFillParent(true);
		table.add("Quake II libGDX port installer");
		table.row();
		table.add(" ");
		table.row();
		table.add("Download assets from:");
		table.row();
		final TextField urlField = new TextField(
   				"http://commondatastorage.googleapis.com/quake2demo/q2-314-demo-x86.exe",
				skin);
		table.add(urlField).width(600);
		table.row();
		table.add(" ");
		table.row();
		final TextButton button = new TextButton("Engage!", skin);
		table.add(button);
		table.row();
		table.add(" ");
		table.row();
		statusLabel = new Label("(Waiting for user confirmation)", skin);
		table.add(statusLabel).expandX();
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event instanceof ChangeListener.ChangeEvent) {
					urlField.setDisabled(true);
					button.removeListener(this);
					button.setVisible(false);
					String url = urlField.getText();
					showInitStatus("(Initiating download...)");
					Installer installer = new Installer(url, new Callback<Void>() {
						@Override
						public void onSuccess(Void result) {
							showInitStatus("All files successfully installed and converted");
							state.putBoolean(DOWNLOAD_COMPLETE, true);
							state.flush();
							initGame();
						}

						@Override
						public void onFailure(Throwable cause) {
							initError("Error installing files", cause);
						}

					});
					Gdx.app.postRunnable(installer);
				}
				return true;
			}
		});
		installationStage.addActor(table);
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
		Sound.impl = new ALSoundImpl();

		QuakeCommon.Init(new String[] { "GQuake" });
		Globals.nostdout = ConsoleVariables.Get("nostdout", "0", 0);

		startTime = TimeUtils.millis();
		initialized = true;
		properGL20 = true;
	}


	@Override
	public void render() {
		if (initialized) {
			Globals.re.checkPendingImages();
			if (ResourceLoader.Pump()) {
				Screen.UpdateScreen2();
			} else {
				double curTime = TimeUtils.millis();
				// GwtKBD.Frame((int) alpha);
				QuakeCommon.Frame((int) (curTime - startTime));
				startTime = curTime;
			}
		} else if (installationStage != null) {
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			installationStage.act(Gdx.graphics.getDeltaTime());
			installationStage.draw();
		}
	}
}
