package com.googlecode.gdxquake2.desktop;

import com.googlecode.gdxquake2.PlatformTools;


public class DesktopTools implements PlatformTools {

  @Override
  public void log(String s) {
    System.out.println(s);
  }

  @Override
  public void exit(int i) {
    System.exit(i);
  }
}
