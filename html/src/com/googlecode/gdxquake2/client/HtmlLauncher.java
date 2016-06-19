package com.googlecode.gdxquake2.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.googlecode.gdxquake2.GdxQuake2;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(640, 400);
        }

        @Override
        public ApplicationListener createApplicationListener () {
/*
                HtmlPlatform.Config config = new HtmlPlatform.Config();
                config.experimentalFullscreen = true;
                config.transparentCanvas = false;
                config.mode = HtmlPlatform.Mode.WEBGL;
                HtmlPlatform platform = HtmlPlatform.register(config);
                final Tools tools = new GwtTools();
*/
                return new GdxQuake2(new GwtTools());
        }
}