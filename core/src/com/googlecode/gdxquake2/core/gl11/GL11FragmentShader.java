package com.googlecode.gdxquake2.core.gl11;

public class GL11FragmentShader {
  static final String SOURCE = 
      "#ifdef GL_ES\n" + 
      "  precision mediump float;\n" +
      "#endif\n" +
      
      "#define NUM_TEXTURES 2\n" +
      
      "uniform bool enable_tex[NUM_TEXTURES];" + 
      
      
          "" // Keep this ---------------------
          + "uniform sampler2D sampler0;   \n"
          + "uniform sampler2D sampler1;   \n"
          + "uniform float alphaMin;       \n"
          + "uniform vec4 fog_color;"
          
          + "varying vec4 v_front_color;           \n"
          + "varying vec4 v_texcoord[NUM_TEXTURES];"
          + "varying float v_fog_factor;           \n"
          
          + "vec4 finalColor;      \n"        
          
          + "void main() {                 \n"
          + "  finalColor = v_front_color;"
          
          // Textures
          + "  if (enable_tex[0]) { \n"
          + "    vec4 texel0 = texture2D(sampler0, vec2(v_texcoord[0].x, v_texcoord[0].y)); \n"
          + "    finalColor = finalColor * texel0;"
          + "  }"
         
          + "  if (enable_tex[1]) { \n"
          + "      vec4 texel1 = texture2D(sampler1, vec2(v_texcoord[1].x, v_texcoord[1].y)); \n"
          + "      finalColor = finalColor * texel1;"
          + "  } \n"
          
          // FOG
          + "  finalColor = vec4(v_fog_factor * finalColor.r + fog_color.r * (1.0 - v_fog_factor),"
          + "                    v_fog_factor * finalColor.g + fog_color.g * (1.0 - v_fog_factor),"
          + "                    v_fog_factor * finalColor.b + fog_color.b * (1.0 - v_fog_factor),"
              + "                    finalColor.a);"
          
          + "  if (finalColor.a <= alphaMin) {\n"
          + "    discard;\n"
          + "  }\n"
          
          + "  gl_FragColor = finalColor;"
          + "}\n";

}
