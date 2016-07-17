package com.googlecode.gdxquake2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.googlecode.gdxquake2.game.sound.ALSoundImpl;
import com.googlecode.gdxquake2.gdxext.ProgressTracker;
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
import javafx.beans.binding.ObjectExpression;

public class GdxQuake2 extends ApplicationAdapter {
	static final String DOWNLOAD_COMPLETE = "downloadComplete";

	public static PlatformTools tools;
	public static Preferences imageSizes;
	public static AsyncLocalStorage asyncLocalStorage;
	public static boolean properGL20;

	private static Preferences state;

	private Label actionLabel;
	private Label fileLabel;
	private Label progressLabel;

	private ProgressTracker progressTracker;

	private boolean initialized;
	private double startTime;
	private Stage installationStage;
	private Skin skin;
	private Table urlTable;
	private Table progressTable;

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
			skin = new Skin(Gdx.files.internal("uiskin.json"));
			installationStage = new Stage();
			Gdx.input.setInputProcessor(installationStage);
			showInstaller();
		}
	}

	void initError(String msg, Throwable cause) {
		Dialog errorDialog = new Dialog("Error", skin) {
			protected void result(Object object) {
				progressTable.remove();
				showInstaller();
			}
		};
		errorDialog.text(msg + ":\n" + cause.getMessage());
		errorDialog.button("Back");
		errorDialog.show(installationStage);
	}



	public void showInstaller() {
		if (urlTable != null) {
			installationStage.addActor(urlTable);
			return;
		}
		urlTable = new Table(skin);
		urlTable.setFillParent(true);
		urlTable.add("Quake II libGDX Port Installer");
		urlTable.row();
		urlTable.add(" ");
		urlTable.row();
		urlTable.add("Download assets from:");
		urlTable.row();
		final TextField urlField = new TextField(
   				"http://commondatastorage.googleapis.com/quake2demo/q2-314-demo-x86.exe",
				skin);
		urlTable.add(urlField).width(600);
		urlTable.row();
		urlTable.add(" ");
		urlTable.row();
		final TextButton button = new TextButton("Engage!", skin);
		urlTable.add(button);
		urlTable.row();
		urlTable.add(" ");
		urlTable.row();
		//fileLabel = new Label("(Waiting for user confirmation)", skin);
		//table.add(fileLabel).expandX();
		button.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event instanceof ChangeListener.ChangeEvent) {
					String url = urlField.getText();
					// showInitStatus("(Initiating download...)");

					urlTable.remove();

					showProgressScreen();
					progressTracker = new ProgressTracker(new Runnable() {
						public void run() {
							actionLabel.setText(progressTracker.action);
							fileLabel.setText(progressTracker.file);
							if (progressTracker.total != 0 || progressTracker.processed > 0) {
								progressLabel.setText(progressTracker.processed + " / " + progressTracker.total +
										" (" + (100 * progressTracker.processed / progressTracker.total) + "%)");
							}
						}
					});
					fileLabel.setText(url);

					Installer installer = new Installer(url, new Callback<Void>() {
						@Override
						public void onSuccess(Void result) {
							state.putBoolean(DOWNLOAD_COMPLETE, true);
							state.flush();
							initGame();
						}

						@Override
						public void onFailure(Throwable cause) {
							initError("Error installing files", cause);
						}

					}, progressTracker);
					Gdx.app.postRunnable(installer);
				}
				return true;
			}
		});
		installationStage.addActor(urlTable);
	}

	public void showProgressScreen() {
		if (progressTable != null) {
			installationStage.addActor(progressTable);
			return;
		}
		progressTable = new Table(skin);
		progressTable.setFillParent(true);
		progressTable.add("Quake II libGDX Port Installation Progress").center().colspan(2);
		progressTable.row();
		progressTable.add(" ");
		progressTable.row();

		progressTable.add("Action: ").right();
		actionLabel = new Label("Waiting for Connection", skin);
		progressTable.add(actionLabel).expandX().left();
		progressTable.row();

		progressTable.add("File: ").right();
		fileLabel = new Label("", skin);
		progressTable.add(fileLabel).expandX().left();
		progressTable.row();

		progressTable.add("Processed: ").right();
		progressLabel = new Label("N/A", skin);
		progressTable.add(progressLabel).expandX().left();
		progressTable.row();

		installationStage.addActor(progressTable);
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
