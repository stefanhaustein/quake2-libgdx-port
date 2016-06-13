package com.googlecode.gdxquake2.gl11;



public class GLDebug {

  public static String getConstantName(int c) {
      switch (c) {
      case GL11.GL_ALPHA_TEST:
        return "GL_ALPHA_TEST";
      case GL11.GL_BYTE:
          return "GL_BYTE";
      case GL11.GL_BLEND:
          return "GL_BLEND";
      case GL11.GL_COLOR_ARRAY:
          return "GL_COLOR_ARRAY";
      case GL11.GL_DITHER:
          return "GL_DITHER";
     
      case GL11.GL_EXP:
        return "GL_EXP";

      case GL11.GL_EXP2:
        return "GL_EXP2";

      case GL11.GL_GREATER:
          return "GL_GREATER";
          
      case GL11.GL_FLAT:
        return "GL_FLAT";
      case GL11.GL_FLOAT:
          return "GL_FLOAT";
      case GL11.GL_FOG:
          return "GL_FOG";
      case GL11.GL_FOG_START:
        return "GL_FOG_START";
      case GL11.GL_FOG_END:
        return "GL_FOG_END";
      case GL11.GL_FOG_COLOR:
        return "GL_FOG_COLOR";
      case GL11.GL_FOG_MODE:
        return "GL_FOG_MODE";
      case GL11.GL_FOG_DENSITY:
        return "GL_FOG_DENSITY";
          
      case GL11.GL_INVALID_OPERATION:
        return "GL_INVALID_OPERATION";
        
      case GL11.GL_LIGHT0:
      case GL11.GL_LIGHT1:
      case GL11.GL_LIGHT2:
      case GL11.GL_LIGHT3:
      case GL11.GL_LIGHT4:
      case GL11.GL_LIGHT5:
      case GL11.GL_LIGHT6:
      case GL11.GL_LIGHT7:
        return "GL_LIGHT" + (c - GL11.GL_LIGHT0);
      case GL11.GL_LIGHTING:
          return "GL_LIGHTING";
      case GL11.GL_LINEAR:
          return "GL_LINEAR";
      case GL11.GL_LINES:
          return "GL_LINES";
      case GL11.GL_LINE_LOOP:
          return "GL_LINE_LOOP";
      case GL11.GL_LINE_SMOOTH:
          return "GL_LINE_SMOOTH";
      case GL11.GL_LINE_SMOOTH_HINT:
          return "GL_LINE_SMOOTH_HINT";
      case GL11.GL_LINE_STRIP:
          return "GL_LINE_STRIP";

      case GL11.GL_MODELVIEW:
          return "GL_MODELVIWEW";
          
      case GL11.GL_NO_ERROR:
          return "GL_NO_ERROR";

      case GL11.GL_PROJECTION:
          return "GL_PROJECTION";

      case GL11.GL_SHORT:
          return "GL_SHORT";
      case GL11.GL_SMOOTH:
        return "GL_SMOOTH";
      case GL11.GL_SPECULAR:
        return "GL_SPECULAR";

      case GL11.GL_TRIANGLE_FAN:
          return "GL_TRIANGLE_FAN";
      case GL11.GL_TRIANGLE_STRIP:
          return "GL_TRIANGLE_STRIP";
      case GL11.GL_TRIANGLES:
          return "GL_TRIANGLES";
      case GL11.GL_TEXTURE:
          return "GL_TEXTURE";
      case GL11.GL_TEXTURE_COORD_ARRAY:
          return "GL_TEXTURE_COORD_ARRAY";
      case GL11.GL_TEXTURE_2D:
          return "GL_TEXTURE_2D";

      case GL11.GL_UNSIGNED_BYTE:
          return "GL_UNISGNED_BYTE";
      case GL11.GL_UNSIGNED_SHORT:
          return "GL_UNSIGNED_SHORT";
  
      default:
          return "0x" + Integer.toHexString(c);
      }
  }
  

  public static void checkError(GL11 gl, String string) {
    int error = gl.glGetError();
    if (error != 0) {
//      PlayN.log().warn("GL Error @ " + string + ": " + error, new Throwable());
      throw new RuntimeException("GL Error " + GLDebug.getConstantName(error) +" @ " + string);
    }
  }

}
