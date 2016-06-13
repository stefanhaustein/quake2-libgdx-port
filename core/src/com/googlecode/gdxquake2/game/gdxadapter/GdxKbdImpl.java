/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.gdxquake2.game.gdxadapter;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.googlecode.gdxquake2.game.client.IdKeys;
import com.googlecode.gdxquake2.game.sys.KBD;
import com.googlecode.gdxquake2.game.sys.Timer;


public class GdxKbdImpl extends KBD {

  
  @Override
  public void Init() {

    Gdx.input.setInputProcessor(new InputProcessor() {
      @Override
      public boolean keyDown(int keycode) {
        Do_Key_Event(translateKeyCode(keycode), true);
        return true;
      }

      @Override
      public boolean keyUp(int keycode) {
        Do_Key_Event(translateKeyCode(keycode), false);
        return true;
      }

      @Override
      public boolean keyTyped(char character) {
        return false;
      }

      @Override
      public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
      }

      @Override
      public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
      }

      @Override
      public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
      }

      @Override
      public boolean mouseMoved(int screenX, int screenY) {
        return false;
      }

      @Override
      public boolean scrolled(int amount) {
        return false;
      }
    });

    /*
    PlayN.keyboard().setListener(new Keyboard.Listener() {
      
      @Override
      public void onKeyUp(Event event) {
        Do_Key_Event(translateKeyCode(event), false);
      }
      
      @Override
      public void onKeyTyped(TypedEvent event) {
       // System.out.println("onTypes " + event.typedChar());
      }
      
      @Override
      public void onKeyDown(Event event) {
        Do_Key_Event(translateKeyCode(event), true);
      }
    });
    
    
    PlayN.mouse().setListener(new Mouse.Listener() {
      
      @Override
      public void onMouseWheelScroll(WheelEvent event) {
        int v = (int) event.velocity();
        while (v < 0) {
          Do_Key_Event(IdKeys.K_MWHEELUP, true);
          Do_Key_Event(IdKeys.K_MWHEELUP, false);
          v++;
        } 
        while (v > 0) {
          Do_Key_Event(IdKeys.K_MWHEELDOWN, true);
          Do_Key_Event(IdKeys.K_MWHEELDOWN, false);
          v--;
        }
      }
      
      @Override
      public void onMouseUp(ButtonEvent event) {
        mouseButton(event, false);
      }
      
      @Override
      public void onMouseMove(MotionEvent event) {
        mx = (int) event.dx();
        my = (int) event.dy();
      }
      
      @Override
      public void onMouseDown(ButtonEvent event) {
        mouseButton(event, true);
      }
    });
*/
  }

  /*
  private void mouseButton(ButtonEvent event, boolean down) {
    switch(event.button()) {
    case Mouse.BUTTON_LEFT:
      Do_Key_Event(IdKeys.K_MOUSE1, down);
      break;
    case Mouse.BUTTON_RIGHT:
      Do_Key_Event(IdKeys.K_MOUSE2, down);
      break;
    case Mouse.BUTTON_MIDDLE:
      Do_Key_Event(IdKeys.K_MOUSE3, down);
      break;
    }
  }*/
  
  private int translateKeyCode(int gdxKeyCode) {
    switch (gdxKeyCode) {
      case Keys.ALT_LEFT: return IdKeys.K_ALT;
      case Keys.ALT_RIGHT: return IdKeys.K_ALT;
      case Keys.STAR: return '*';
      case Keys.AT: return '@';
      case Keys.BACKSPACE: return IdKeys.K_BACKSPACE;
      case Keys.CONTROL_LEFT: return IdKeys.K_CTRL;
      case Keys.CONTROL_RIGHT: return IdKeys.K_CTRL;
      case Keys.COMMA: return ',';
      case Keys.COLON: return ':';
      case Keys.FORWARD_DEL: return IdKeys.K_DEL;
      case Keys.DOWN: return IdKeys.K_DOWNARROW;
      case Keys.END: return IdKeys.K_END;
      case Keys.ENTER: return IdKeys.K_ENTER;
      case Keys.ESCAPE: return IdKeys.K_ESCAPE;
      case Keys.EQUALS: return '=';
      case Keys.INSERT: return IdKeys.K_INS;
      case Keys.HOME: return IdKeys.K_HOME;
      case Keys.LEFT: return IdKeys.K_LEFTARROW;
      case Keys.LEFT_BRACKET: return '[';
      case Keys.MINUS: return '-';
      case Keys.PAGE_DOWN: return IdKeys.K_PGDN;
      case Keys.PAGE_UP: return IdKeys.K_PGUP;
      case Keys.PERIOD: return '.';
      case Keys.PLUS: return'+';
      case Keys.RIGHT: return IdKeys.K_RIGHTARROW;
      case Keys.RIGHT_BRACKET: return ']';
      case Keys.SEMICOLON: return';';
      case Keys.SHIFT_LEFT: return IdKeys.K_SHIFT;
      case Keys.SHIFT_RIGHT: return IdKeys.K_SHIFT;
      case Keys.SLASH: return '/';
      case Keys.SPACE: return IdKeys.K_SPACE;
      case Keys.TAB: return IdKeys.K_TAB;
      case Keys.UP: return IdKeys.K_UPARROW;
    }
    if (gdxKeyCode >= Keys.F1 && gdxKeyCode <= Keys.F12) {
      return gdxKeyCode - Keys.F1 + IdKeys.K_F1;
    }
    if (gdxKeyCode >= Keys.A && gdxKeyCode <= Keys.Z) {
      return gdxKeyCode - Keys.A + 'a';
    }
    if (gdxKeyCode >= Keys.NUM_0 && gdxKeyCode <= Keys.NUM_9) {
      return gdxKeyCode - Keys.NUM_0 + '0';
    }
    if (gdxKeyCode >= Keys.NUMPAD_0 && gdxKeyCode <= Keys.NUMPAD_9) {
      return gdxKeyCode - Keys.NUMPAD_0 + '0';
    }
    return gdxKeyCode;
  }

  @Override
  public void Update() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void Close() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void Do_Key_Event(int key, boolean down) {
    com.googlecode.gdxquake2.game.client.Key.Event(
        key, down, Timer.Milliseconds());
  }

  @Override
  public void installGrabs() {
      System.out.println("PlayN.mouse().lock();");
  }

  @Override
  public void uninstallGrabs() {
      System.out.println("PlayN.mouse().unlock();");
  }

}
