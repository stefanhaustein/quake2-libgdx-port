package com.googlecode.gdxquake2.gl11;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.googlecode.gdxquake2.GdxQuake2;

/**
 * This is not a complete emulation of GL11 -- it only covers the aspects
 * needed for porting Quake and the JBullet demos.
 * 
 * @author Stefan Haustein
 */
public final class GL11Emulation implements GL11 {

  static final String TAG = "GL11";

  public static final int GL_QUADS = 7;
  
  private static final int ARRAY_POSITION = 0; // Must be 0. 1
  private static final int ARRAY_TEXCOORD_0 = 1; // 2
  private static final int ARRAY_TEXCOORD_1 = 2; // 4
  private static final int ARRAY_COLOR = 3;
  private static final int ARRAY_NORMAL = 4;
  
  private static final int MATRIX_MODELVIEW = 1;
  private static final int MATRIX_PROJECTION = 2;
  private static final int MATRIX_TEXTURE_0 = 4;
  private static final int MATRIX_TEXTURE_1 = 8;

  private int arraysEnabled;
  
  private int uMvpMatrix;
  private int uModelViewMatrix;
  private int uSampler0;
  private int uSampler1;
  private int uAlphaMin;
  private int uTextureEnabled;
  private int uTexMatrix;
  
  private int uFogEnabled;
  private int uFogColor;
  private int uFogStart;
  private int uFogEnd;
  private int uFogMode;
  private int uFogDensity;
  
  private int programObject;

  private FloatBuffer texMatrixBuffer = BufferUtils.newFloatBuffer(2 * 16);
  private IntBuffer enableTexBuffer = BufferUtils.newIntBuffer(2);
  private FloatBuffer matrixBuffer = BufferUtils.newFloatBuffer(16);
  private IntBuffer tmpIntBuffer = BufferUtils.newIntBuffer(16);
  
  private boolean alphaTestEnabled = false;
  private float minAlpha = 0; 
  
  private int clientActiveTexture = 0;
  private int activeTexture = 0;
  private HashMap<String,String> unsupportedState = new HashMap<String,String>();
  private HashMap<Integer,Integer> internalFormats = new HashMap<Integer,Integer>();
  
  private int[] boundTextureId = new int[2];
  
  private int matrixFlag = MATRIX_MODELVIEW;
  private int matrixMode = GL11.GL_MODELVIEW;
  private int matrixDirty = 255;

  private int viewportX;
  private int viewportY;
  private int viewportW;
  private int viewportH;

  private final float[] projectionMatrix = new float[16 * 16];
  private final float[] modelViewMatrix = new float[16 * 32];
  private final float[] texture0Matrix = new float[16 * 16];
  private final float[] texture1Matrix = new float[16 * 16];

  // Space for two tmp matrices
  private final float[] tmpMatrix = new float[32];
  private final float[] mvpMatrix = new float[16];

  // Stack pointers
  private int projectionMatrixSp = 0;
  private int modelViewMatrixSp = 0;
  private int texture0MatrixSp = 0;
  private int texture1MatrixSp = 0;

  private float[] currentMatrix = modelViewMatrix;
  private int currentMatrixSp;
  
  private final GL20 gl;
  private boolean texture0Enabled;
  private boolean texture1Enabled;

  private int quadsToTrianglesVbo;
  
  public GL11Emulation(GL20 gl) {
    this.gl = gl;

    initShader();

    Matrix.setIdentityM(modelViewMatrix, 0);
    Matrix.setIdentityM(projectionMatrix, 0);
    Matrix.setIdentityM(texture0Matrix, 0);
    Matrix.setIdentityM(texture1Matrix, 0);
    
    ShortBuffer indices = BufferUtils.newShortBuffer(65536 * 3 / 2);
    for (int i = 0, offset = 0; i < indices.capacity(); i += 6) {
        indices.put(i + 0, (short) (offset + 0));
        indices.put(i + 1, (short) (offset + 1));
        indices.put(i + 2, (short) (offset + 2));
        indices.put(i + 3, (short) (offset + 0));
        indices.put(i + 4, (short) (offset + 2));
        indices.put(i + 5, (short) (offset + 3));
        offset += 4;
    }
    
    tmpIntBuffer.position(0).limit(1);
    gl.glGenBuffers(1, tmpIntBuffer);
    quadsToTrianglesVbo = tmpIntBuffer.get(0);
    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, quadsToTrianglesVbo);
    gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * 2, indices, GL_STATIC_DRAW);
  }

  private int loadShader(int shaderType, String shaderSource) {
    // Create the shader object
    int shader = gl.glCreateShader(shaderType);
    if (shader == 0) {
      throw new RuntimeException();
    }
    // Load the shader source
    gl.glShaderSource(shader, shaderSource);

    // Compile the shader
    gl.glCompileShader(shader);

    // Check the compile status
    //boolean compiled = gl.getShaderParameterb(shader, COMPILE_STATUS);
    //if (!compiled) {
      // Something went wrong during compilation; get the error
    //  throw new RuntimeException("Shader compile error: "
     //     + gl.getShaderInfoLog(shader));
    //}
    return shader;
  }

  private void initShader() {
    // create our shaders
    int vertexShader = loadShader(GL20.GL_VERTEX_SHADER, GL11VertexShader.SOURCE);
    checkError("loadVertexShader");
    
    int fragmentShader = loadShader(GL20.GL_FRAGMENT_SHADER, GL11FragmentShader.SOURCE);
    checkError("loadFragmentShader");

    if (vertexShader == 0 || fragmentShader == 0) {
      throw new RuntimeException("shader error");
    }

    // Create the program object
    programObject = gl.glCreateProgram();
    if (programObject == 0 || gl.glGetError() != GL_NO_ERROR) {
      throw new RuntimeException("program error");
    }

    // Attach our two shaders to the program
    gl.glAttachShader(programObject, vertexShader);
    checkError("vs0");
    System.err.println("vertex shader: " + gl.glGetShaderInfoLog(vertexShader));
    checkError("vs1");
    
    gl.glAttachShader(programObject, fragmentShader);
    checkError("fs0");
//    System.err.println("fragment Shader: " + gl.glGetProgramInfoLog(fragmentShader));
    checkError("fs1");
    
    // Bind "vPosition" to attribute 0
    gl.glBindAttribLocation(programObject, ARRAY_POSITION, "a_position");
    gl.glBindAttribLocation(programObject, ARRAY_COLOR, "a_color");
    gl.glBindAttribLocation(programObject, ARRAY_TEXCOORD_0, "a_texcoord0");
    gl.glBindAttribLocation(programObject, ARRAY_TEXCOORD_1, "a_texcoord1");
    checkError("bind vertex arrays");

    // Link the program
    gl.glLinkProgram(programObject);
    // // Check the link status
    System.err.println("Linker: " + gl.glGetProgramInfoLog(programObject));
    
//  boolean linked = gl.getProgramParameterb(programObject, LINK_STATUS);
//   if (!linked) {
//    throw new RuntimeException("linker Error: "
 //       + gl.getProgramInfoLog(programObject));
//  }

    uMvpMatrix = gl.glGetUniformLocation(programObject, "mvp_matrix");
    checkError("mvpMatrix");
    uModelViewMatrix = gl.glGetUniformLocation(programObject, "modelview_matrix");
    checkError("modelViewMatrix");
    uTexMatrix = gl.glGetUniformLocation(programObject, "tex_matrix");
    checkError("texMatrix");
    
    uSampler0 = gl.glGetUniformLocation(programObject, "sampler0");
    uSampler1 = gl.glGetUniformLocation(programObject, "sampler1");
    uAlphaMin = gl.glGetUniformLocation(programObject, "alphaMin");
    uTextureEnabled = gl.glGetUniformLocation(programObject, "enable_tex");
    checkError("uniforms");
    
    uFogEnabled = gl.glGetUniformLocation(programObject, "enable_fog");
    uFogMode = gl.glGetUniformLocation(programObject, "fog_mode");
    uFogColor = gl.glGetUniformLocation(programObject, "fog_color");
    uFogDensity = gl.glGetUniformLocation(programObject, "fog_density");
    uFogStart = gl.glGetUniformLocation(programObject, "fog_start");
    uFogEnd = gl.glGetUniformLocation(programObject, "fog_end");
    checkError("fog uniforms");

    gl.glUseProgram(programObject);

    gl.glUniform1i(uSampler0, 0);
    gl.glUniform1i(uSampler1, 1);
    gl.glActiveTexture(GL11.GL_TEXTURE0);
  }

  public final void glLoadIdentity() {
    Matrix.setIdentityM(currentMatrix, currentMatrixSp);
    matrixDirty |= matrixFlag;
  }

  private void updateMatrixSp() {
    switch (matrixMode) {
    case GL_MODELVIEW:
      modelViewMatrixSp = currentMatrixSp;
      break;
    case GL_PROJECTION:
      projectionMatrixSp = currentMatrixSp;
      break;
    case GL11.GL_TEXTURE:
      if (activeTexture == 0) {
        texture0MatrixSp = currentMatrixSp;
      } else {
        texture1MatrixSp = currentMatrixSp;
      }
      break;
    default:
      throw new IllegalArgumentException("Unrecoginzed matrix mode");
    }
  }
  
  public final void glMatrixMode(int mm) {
    // Can't short-circuit texture because this depends on activeTexture
    updateMatrixSp();
    switch (mm) {
    case GL_MODELVIEW:
      currentMatrix = modelViewMatrix;
      currentMatrixSp = modelViewMatrixSp;
      matrixFlag = MATRIX_MODELVIEW;
      break;
    case GL_PROJECTION:
      currentMatrix = projectionMatrix;
      currentMatrixSp = projectionMatrixSp;
      matrixFlag = MATRIX_PROJECTION;
      break;
    case GL11.GL_TEXTURE:
      if (activeTexture == 0) {
        currentMatrix = texture0Matrix;
        currentMatrixSp = texture0MatrixSp;
        matrixFlag = MATRIX_TEXTURE_0;
      } else {
        currentMatrix = texture1Matrix;
        currentMatrixSp = texture1MatrixSp;
        matrixFlag = MATRIX_TEXTURE_1;
      }
      break;
    default:
      throw new IllegalArgumentException("Unrecoginzed matrix mode: " + mm);
    }
    this.matrixMode = mm;
  }

  public void glGetInteger(int what, IntBuffer params) {
    switch (what) {
    case GL11.GL_MATRIX_MODE:
      params.put(matrixMode);
      break;
    default:
      throw new IllegalArgumentException();
    }
  }

  public final void glMultMatrixf(float[] matrix, int ofs) {
    Matrix.multiplyMM(tmpMatrix, 0, currentMatrix, currentMatrixSp, matrix, ofs);
    System.arraycopy(tmpMatrix, 0, currentMatrix, currentMatrixSp, 16);
    matrixDirty |= matrixFlag;
  }

  public final void glPushMatrix() {
    System.arraycopy(currentMatrix, currentMatrixSp, currentMatrix, currentMatrixSp + 16, 16);
    currentMatrixSp += 16;
  }

  public final void glPopMatrix() {
    currentMatrixSp -= 16;
    matrixDirty |= matrixFlag;
  }

  public final void glRotatef(float angle, float x, float y, float z) {
    if (x != 0 || y != 0 || z != 0) {
      // right thing to do? or rotate around a default axis?
      Matrix.rotateM(currentMatrix, currentMatrixSp, angle, x, y, z);
    }
    matrixDirty |= matrixFlag;
  }

  public final void glScalef(float x, float y, float z) {
    Matrix.scaleM(currentMatrix, currentMatrixSp, x, y, z);
    matrixDirty |= matrixFlag;
  }

  public final void glTranslatef(float tx, float ty, float tz) {
    Matrix.translateM(currentMatrix, currentMatrixSp, tx, ty, tz);
    matrixDirty |= matrixFlag;
  }

  @Override
  public void glViewport(int x, int y, int w, int h) {
    viewportX = x;
    viewportY = y;
    viewportW = w;
    viewportH = h;
    gl.glViewport(x, y, w, h);
  }

  @Override
  public void glFrustumf(float left, float right, float bottom, float top,
      float znear, float zfar) {
    float[] matrix = new float[16];
    float n2, rml, tmb, fmn;
    n2 = 2 * znear;
    rml = right - left;
    tmb = top - bottom;
    fmn = zfar - znear;
    matrix[0] = n2 / rml;
//    matrix[1] = 0;
//    matrix[2] = 0;
//    matrix[3] = 0;
//    matrix[4] = 0;
    matrix[5] = n2 / tmb;
//    matrix[6] = 0;
//    matrix[7] = 0;
    matrix[8] = (right + left) / rml;
    matrix[9] = (top + bottom) / tmb;
    matrix[10] = -(zfar + znear) / fmn;
    matrix[11] = -1f;
//    matrix[12] = 0;
//    matrix[13] = 0;
    matrix[14] = (-n2 * zfar) / fmn;
//    matrix[15] = 0;

    glMultMatrixf(matrix, 0);
  }

  @Override
  public void glOrthof(float l, float r, float b, float t, float n, float f) {

    float[] matrix = { 
        2f / (r - l), 0, 0, 0,
        0, 2f / (t - b), 0, 0,
        0, 0, -2f / (f - n), 0,
        -(r + l) / (r - l), -(t + b) / (t - b), -(f + n) / (f - n), 1f 
    };
    glMultMatrixf(matrix, 0);
  }

//  public boolean project(float objX, float objY, float objZ, int[] view,
//      float[] win) {
//    float[] v = { objX, objY, objZ, 1f };
//    float[] v2 = new float[4];
//
//    Matrix.multiplyMV(v2, 0, mvpMatrix, 0, v, 0);
//
//    float w = v2[3];
//    if (w == 0.0f) {
//      return false;
//    }
//
//    float rw = 1.0f / w;
//
//    win[0] = viewportX + viewportW * (v2[0] * rw + 1.0f) * 0.5f;
//    win[1] = viewportY + viewportH * (v2[1] * rw + 1.0f) * 0.5f;
//    win[2] = (v2[2] * rw + 1.0f) * 0.5f;
//
//    return true;
//  }

  @Override
  public void glLoadMatrixf(FloatBuffer m) {
    // if (AUTO_REWIND) {
    // m.rewind();
    // }
    int p = m.position();
    m.get(currentMatrix, currentMatrixSp, 16);
    m.position(p);
    matrixDirty |= matrixFlag;
  }

  @Override
  public void glPolygonMode(int i, int j) {
    throw new RuntimeException("NYI WebGL11.glPolygonMode()");
  }

  @Override
  public void glTexCoordPointer(int size, int type, int byteStride, Buffer buf) {
    gl.glVertexAttribPointer(ARRAY_TEXCOORD_0 + clientActiveTexture, size, type, 
        false, byteStride, buf);
  }

  @Override
  public void glTexEnvi(int target, int pid, int value) {
    unsupportedState.put("texEnvi-" + GLDebug.getConstantName(target) + "-" + 
  GLDebug.getConstantName(pid) + "-" + activeTexture, GLDebug.getConstantName(value));
    
  }

  @Override
  public final void glColor4f(float red, float green, float blue, float alpha) {
    gl.glVertexAttrib4f(ARRAY_COLOR, red, green, blue, alpha);
//    checkError("glColor4f");
  }

  private void checkError(String string) {
    int error = gl.glGetError();
    if (error != 0) {
      throw new RuntimeException("GL Error @ " + string + ": " + 
          GLDebug.getConstantName(error));
    }
  }

  @Override
  public final void glEnableClientState(int i) {
//    checkError("Before glEnableClientState()");
    switch (i) {
    case GL11.GL_COLOR_ARRAY:
      gl.glEnableVertexAttribArray(ARRAY_COLOR);
      arraysEnabled |= 1 << ARRAY_COLOR;
//      checkError("enableClientState colorArr");
      break;
    case GL11.GL_VERTEX_ARRAY:
      gl.glEnableVertexAttribArray(ARRAY_POSITION);
      arraysEnabled |= 1 << ARRAY_POSITION;
//      checkError("enableClientState vertexArrr");
      break;
    case GL11.GL_NORMAL_ARRAY:
      gl.glEnableVertexAttribArray(ARRAY_NORMAL);
      arraysEnabled |= 1 << ARRAY_NORMAL;
//      checkError("enableClientState normalArr");
      break;
    case GL11.GL_TEXTURE_COORD_ARRAY:
      gl.glEnableVertexAttribArray(ARRAY_TEXCOORD_0 + clientActiveTexture);
      arraysEnabled |= 1 << (ARRAY_TEXCOORD_0 + clientActiveTexture);
      break;
    default:
      Gdx.app.error(TAG, "unsupported / unrecognized client state " + i);
    }
  }

  @Override
  public final void glDisableClientState(int i) {
    switch (i) {
    case GL11.GL_COLOR_ARRAY:
      gl.glDisableVertexAttribArray(ARRAY_COLOR);
      arraysEnabled &= ~(1 << ARRAY_COLOR);
      break;
    case GL11.GL_VERTEX_ARRAY:
      gl.glDisableVertexAttribArray(ARRAY_POSITION);
      arraysEnabled &= ~(1 << ARRAY_POSITION);
      break;
    case GL11.GL_NORMAL_ARRAY:
      gl.glDisableVertexAttribArray(ARRAY_NORMAL);
      arraysEnabled &= ~(1 << ARRAY_NORMAL);
      break;
    case GL11.GL_TEXTURE_COORD_ARRAY:
      gl.glDisableVertexAttribArray(ARRAY_TEXCOORD_0 + clientActiveTexture);
      arraysEnabled &= ~(1 << (ARRAY_TEXCOORD_0 + clientActiveTexture));
      break;
    default:
      Gdx.app.error(TAG, "unsupported / unrecogized client state name: " +
          GLDebug.getConstantName(i));
    }
//    checkError("DisableClientState");
  }

  @Override
  public final void glShadeModel(int s) {
    // Set color shading to FLAT or SMOOTH (default is SMOOTH, MC probably uses FLAT, 
    // needs to be addressed in shader...
    unsupportedState.put("shadeModel", GLDebug.getConstantName(s));
  }

  @Override
  public final void glDisable(int i) {
    // In ES, you don't enable/disable TEXTURE_2D. We use it this call to
    // disable one of the two active textures supported by the shader.
    switch (i) {
    case GL11.GL_TEXTURE_2D:
      switch (activeTexture) {
      case 0:
        texture0Enabled = false;
        break;
      case 1:
        texture1Enabled = false;
        break;
      default:
        throw new RuntimeException("Texture out of range");
      }
      break;
    case GL11.GL_ALPHA_TEST:
      alphaTestEnabled = false;
      gl.glUniform1f(uAlphaMin, -1);
      break;
    case GL11.GL_FOG:
      gl.glUniform1i(uFogEnabled, 0);
      break;
    case GL11.GL_LIGHTING: 
      unsupportedState.put(GLDebug.getConstantName(i), "disabled");
      break;
    default:
      gl.glDisable(i);
//      checkError("glDisable " + GLDebug.getConstantName(i));
    }
  }

  @Override
  public void glClientActiveTexture(int texture) {
    if (texture < GL11.GL_TEXTURE0 || texture > GL11.GL_TEXTURE1) {
        throw new RuntimeException("glClientActiveTexture invalid value: " + texture);
    }
    clientActiveTexture = texture - GL11.GL_TEXTURE0;
  }

  @Override
  public void glColorPointer(int size, int type, int stride, Buffer buf) {
    gl.glVertexAttribPointer(ARRAY_COLOR, size, type, true, stride, buf);
//    checkError("glColorPointer");
  }


  @Override
  public void glVertexPointer(int size, int type, int byteStride, Buffer buf) {
    gl.glVertexAttribPointer(ARRAY_POSITION, size, type, false, byteStride, buf);
//    checkError("glVertexPointer");
  }

  @Override
  public final void glEnable(int i) {
    // In ES20, you don't enable/disable TEXTURE_2D. We use it this call to
    // enable one of the two active textures supported by the shader.
    switch(i) {
    case GL11.GL_TEXTURE_2D:
      switch (activeTexture) {
      case 0:
        texture0Enabled = true;
        break;
      case 1:
        texture1Enabled = true;
        break;
      default:
        throw new RuntimeException("texure out of range");
      }
      break;
    case GL11.GL_ALPHA_TEST:
      alphaTestEnabled = true;
      gl.glUniform1f(uAlphaMin, minAlpha);
      break;
    case GL11.GL_FOG:
      gl.glUniform1i(uFogEnabled, 1);
      break;
    case GL11.GL_LIGHTING:
      unsupportedState.put(GLDebug.getConstantName(i), "enabled");
      break;
    default:
      gl.glEnable(i);
//      checkError("glEnable(0x"+Integer.toHexString(i));
    }
  }

  @Override
  public void glActiveTexture(int texture) {
    if (texture == activeTexture + GL11.GL_TEXTURE0) {
      return;
    }
    // Save current stack pointer for the currently active texture
    updateMatrixSp();
    gl.glActiveTexture(texture);
    activeTexture = texture - GL11.GL_TEXTURE0;
    glMatrixMode(matrixMode); // adjust active matrix
//    checkError("glActiveTexture");
  }

  @Override
  public void glBindTexture(int target, int textureId) {
    gl.glBindTexture(target, textureId);

    boundTextureId[activeTexture] = textureId;

    // glColor3f((float)Math.random(), (float)Math.random(),
    // (float)Math.random());
//    checkError("glBindTexture");

  }

  // Only used in prepareDraw
  private int oldEnableTex0 = -1;
  private int oldEnableTex1 = -1;
  protected void prepareDraw() {
//    if (debugInfo.equals("ModelPart")) {
     /* GdxQuake2.tools.log("GL11 emul. prepDraw sys state:" +
            " arraysEnabled: " + arraysEnabled +
            " activeTexture: " + activeTexture + " activeClientTexture: " + clientActiveTexture +
      		" tex0enabled: " + texture0Enabled + " tex1enabled: " + texture1Enabled  +
      		" matrix dirty: " + matrixDirty);
   */
   // }
    //logFirst("Unsupported state for drawArrays: " + unsupportedState.toString());
    
 //   gl.glUseProgram(programObject);
    
    updateMatrixSp();
    if ((matrixDirty & (MATRIX_MODELVIEW | MATRIX_PROJECTION)) != 0) {
      Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, projectionMatrixSp, modelViewMatrix, modelViewMatrixSp);
      matrixBuffer.position(0);
      matrixBuffer.put(mvpMatrix);
      matrixBuffer.position(0);
      gl.glUniformMatrix4fv(uMvpMatrix, 1, false, matrixBuffer);
      if ((matrixDirty & MATRIX_MODELVIEW) != 0) {
        matrixBuffer.position(0);
        matrixBuffer.put(modelViewMatrix, modelViewMatrixSp, 16);
        matrixBuffer.position(0);
        gl.glUniformMatrix4fv(uModelViewMatrix, 1, false, matrixBuffer);
      }
    }
    
    if ((matrixDirty & (MATRIX_TEXTURE_0 | MATRIX_TEXTURE_1)) != 0) {
      if ((matrixDirty & MATRIX_TEXTURE_0) != 0) {
        texMatrixBuffer.position(0);
        texMatrixBuffer.put(texture0Matrix, texture0MatrixSp, 16);
      }
      if ((matrixDirty & MATRIX_TEXTURE_1) != 0) {
        texMatrixBuffer.position(16);
        texMatrixBuffer.put(texture1Matrix, texture0MatrixSp, 16);
      }
      texMatrixBuffer.position(0);
      gl.glUniformMatrix4fv(uTexMatrix, 2, false, texMatrixBuffer);
    }
    
    matrixDirty = 0;

    int enableTex0 = texture0Enabled /*&& (arraysEnabled & (1 << ARRAY_TEXCOORD_0)) != 0*/ ? 1 : 0;
    int enableTex1 = texture1Enabled /*&& (arraysEnabled & (1 << ARRAY_TEXCOORD_1)) != 0 */? 1 : 0;
    if (enableTex0 != oldEnableTex0 || enableTex1 != oldEnableTex1) {
      enableTexBuffer.put(0, enableTex0);
      enableTexBuffer.put(1, enableTex1);
      gl.glUniform1iv(uTextureEnabled, 2, enableTexBuffer);
      oldEnableTex0 = enableTex0;
      oldEnableTex1 = enableTex1;
    }
  }


  @Override
  public void glAlphaFunc(int func, float ref) {
    if (func == GL11.GL_GREATER) {
      minAlpha = ref;
      gl.glUniform1f(uAlphaMin, alphaTestEnabled ? minAlpha : -1);
    } else {
      throw new RuntimeException("Unsupported alpha func");
    }
  }

  @Override
  public void glDeleteTextures(int n, int[] textures, int offset) {
    glDeleteBuffers(n, textures, offset);
  }

  @Override
  public void glFogf(int pname, float param) {
    switch(pname) {
    case GL_FOG_MODE:
      gl.glUniform1i(uFogMode, (int) param);
      break;
    case GL_FOG_START:
      gl.glUniform1f(uFogStart, param);
      break;
    case GL_FOG_END:
      gl.glUniform1f(uFogEnd, param);
      break;
    case GL_FOG_DENSITY:
      gl.glUniform1f(uFogDensity, param);
      break;
    default:
      throw new RuntimeException("Unsuported fog parameter: " + 
          GLDebug.getConstantName(pname));
    }
  }

  @Override
  public void glFogfv(int pname, float[] params, int offset) {
    throw new RuntimeException ("NYI");
  }

  private HashSet<String> logged = new HashSet<String>();
  private void logFirst(String msg) {
    if (!logged.contains(msg)) {
      Gdx.app.error(TAG, msg);
      logged.add(msg);
    }
  }

  @Override
  public void glFogfv(int pname, FloatBuffer params) {
    if (pname == GL_FOG_COLOR) {
      gl.glUniform4fv(uFogColor, 1, params);
    } else {
      throw new RuntimeException("glFogfv unsupportet parameter name: " + 
          GLDebug.getConstantName(pname));
    }
  }

  @Override
  public void glGenTextures(int n, int[] textures, int offset) {
    throw new RuntimeException("gl.glGenTextures(n, textures, offset);");
  }

  @Override
  public int glGenTexture() {
    return gl.glGenTexture();
  }


  @Override
  public void glGetIntegerv(int pname, int[] params, int offset) {
    
    switch(pname) {
    case GL11.GL_CLIENT_ACTIVE_TEXTURE:
        params[offset] = clientActiveTexture + GL11.GL_TEXTURE0;
        break;
    default:
        throw new RuntimeException("WebGL11.glGetIntegerv unsupported parameter: "+pname);
    }
  }

  @Override
  public void glLightModelf(int pname, float param) {
    unsupportedState.put("lightModel-" + GLDebug.getConstantName(pname), "" + param);
  }

  @Override
  public void glLightModelfv(int pname, float[] params, int offset) {
    unsupportedState.put("lightModelv-" + GLDebug.getConstantName(pname), Arrays.toString(params) + " offset " + offset);
  }

  @Override
  public void glLightModelfv(int pname, FloatBuffer params) {
    unsupportedState.put("lightModelfv-" + GLDebug.getConstantName(pname), "BufferUtils.toString(params)");
  }

  @Override
  public void glLightf(int light, int pname, float param) {
    unsupportedState.put("lightf-" + GLDebug.getConstantName(light) + "-" + GLDebug.getConstantName(pname), "" + param);
  }

  @Override
  public void glLightfv(int light, int pname, float[] params, int offset) {
    unsupportedState.put("lightfv-" + GLDebug.getConstantName(light) + "-" + GLDebug.getConstantName(pname), Arrays.toString(params) + " offset " + offset);
  }

  @Override
  public void glLightfv(int light, int pname, FloatBuffer params) {
    unsupportedState.put("lightfv-" + GLDebug.getConstantName(light) + "-" + GLDebug.getConstantName(pname), "Buffers.toString(params)");
  }

  @Override
  public void glLoadMatrixf(float[] m, int offset) {
    throw new RuntimeException("WebGL11 NYI glLoadMatrixf");
  }

  @Override
  public void glLogicOp(int opcode) {
    throw new RuntimeException("WebGL11 NYI glLogicOp");
  }

  @Override
  public void glMaterialf(int face, int pname, float param) {
    throw new RuntimeException("WebGL11 NYI glMaterialf");
  }

  @Override
  public void glMaterialfv(int face, int pname, float[] params, int offset) {
    throw new RuntimeException("WebGL11 NYI glMaterialfv");
  }

  @Override
  public void glMaterialfv(int face, int pname, FloatBuffer params) {
    throw new RuntimeException("WebGL11 NYI glMaterialfv");
  }

  @Override
  public void glMultMatrixf(FloatBuffer m) {
    throw new RuntimeException("WebGL11 NYI glMultMatrixf");
  }

  @Override
  public void glMultiTexCoord2f(int target, float f, float g) {
    gl.glVertexAttrib4f(target - GL_TEXTURE0 + ARRAY_TEXCOORD_0, f, g, 0, 1);
  }

  @Override
  public void glMultiTexCoord4f(int target, float s, float t, float r, float q) {
    gl.glVertexAttrib4f(target - GL_TEXTURE0 + ARRAY_TEXCOORD_0, s, t, r, q);
  }

  @Override
  public void glNormal3f(float nx, float ny, float nz) {
    logFirst("WebGL11 NYI glNormal3f");
  }

  @Override
  public void glNormalPointer(int type, int stride, Buffer pointer) {
    logFirst("WebGL11 NYI glNormalPointer");
  }

  @Override
  public void glPointSize(float size) {
    //System.out.println("WebGL11 NYI glPointSize");
  }

  @Override
  public void glTexEnvf(int target, int pname, float param) {
    throw new RuntimeException("WebGL11 NYI glTexEnvf");
  }

  @Override
  public void glTexEnvfv(int target, int pname, float[] params, int offset) {
    throw new RuntimeException("WebGL11 glTexEnvfv NYI");
  }

  @Override
  public void glTexEnvfv(int target, int pname, FloatBuffer params) {
    throw new RuntimeException("WebGL11 glTexEnvfv NYI");
  }

  @Override
  public void glClipPlanef(int plane, float[] equation, int offset) {
    throw new RuntimeException("WebGL11 glClipPlanef NYI");
  }

  @Override
  public void glClipPlanef(int plane, FloatBuffer equation) {
    throw new RuntimeException("WebGL11 glClipPlanef NYI");
  }

  @Override
  public void glGetClipPlanef(int pname, float[] eqn, int offset) {
    throw new RuntimeException("WebGL11 glGetClipPlanef NYI");
  }

  @Override
  public void glGetClipPlanef(int pname, FloatBuffer eqn) {
    throw new RuntimeException("WebGL11 glGetClipPlanef NYI");
  }

  @Override
  public void glGetFloatv(int pname, float[] params, int offset) {
    throw new RuntimeException("gl.glGetFloatv(pname, params, offset);");
  }

  @Override
  public void glGetLightfv(int light, int pname, float[] params, int offset) {
    throw new RuntimeException("WebGL11 glGetLightfv NYI");
  }

  @Override
  public void glGetLightfv(int light, int pname, FloatBuffer params) {
    throw new RuntimeException("WebGL11 glGetLightfv NYI");
  }

  @Override
  public void glGetMaterialfv(int face, int pname, float[] params, int offset) {
    throw new RuntimeException("WebGL11 glGetMaterialfv NYI");
  }

  @Override
  public void glGetMaterialfv(int face, int pname, FloatBuffer params) {
    throw new RuntimeException("WebGL11 glGetMaterialfv NYI");
  }

  @Override
  public void glGetTexParameterfv(int target, int pname, float[] params,
      int offset) {
    throw new RuntimeException("gl.glGetTexParameterfv(target, pname, params, offset);");
  }

  @Override
  public void glPointParameterf(int pname, float param) {
    System.out.println("glPointParameterf NYI");
  }

  @Override
  public void glPointParameterfv(int pname, float[] params, int offset) {
    throw new RuntimeException("WebGL11 glPointParameterfv NYI");
  }

  @Override
  public void glPointParameterfv(int pname, FloatBuffer params) {
    throw new RuntimeException("WebGL11 glPointParameterfv NYI");
  }

  @Override
  public void glTexParameterfv(int target, int pname, float[] params, int offset) {
    throw new RuntimeException("WebGL11 glTexParameterfv NYI");
  }

  @Override
  public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
    glColor4f((red&255)/255f, (green&255)/255f, (blue&255)/255f, (alpha&255)/255f);
  }

  @Override
  public void glDeleteBuffers(int n, int[] buffers, int offset) {
    if (tmpIntBuffer.capacity() < n) {
      tmpIntBuffer = BufferUtils.newIntBuffer(n * 3 / 2);
    }
    tmpIntBuffer.position(0).limit(n);;
    tmpIntBuffer.put(buffers, offset, n);
    tmpIntBuffer.position(0);
    gl.glDeleteBuffers(n, tmpIntBuffer);
  }

  @Override
  public void glGetBooleanv(int pname, boolean[] params, int offset) {
    throw new RuntimeException("WebGL11 glGetBooleanv NYI");
  }

  @Override
  public void glGetBooleanv(int pname, IntBuffer params) {
    throw new RuntimeException("WebGL11 glGetBooleanv NYI");
  }

  @Override
  public void glGetBufferParameteriv(int target, int pname, int[] params,
      int offset) {
    throw new RuntimeException("WebGL11 glGetBufferParameteriv NYI");
  }

  @Override
  public void glGetPointerv(int pname, Buffer[] params) {
    throw new RuntimeException("WebGL11 glGetPointerv NYI");
  }

  @Override
  public void glGetTexEnviv(int env, int pname, int[] params, int offset) {
    throw new RuntimeException("WebGL11 glGetTexEnviv NYI");
  }

  @Override
  public void glGetTexEnviv(int env, int pname, IntBuffer params) {
    throw new RuntimeException("WebGL11 glGetTexEnviv NYI");
  }

  @Override
  public void glGetTexParameteriv(int target, int pname, int[] params,
      int offset) {
    throw new RuntimeException("WebGL11 glGetTexParameteriv NYI");
  }

  @Override
  public void glTexEnviv(int target, int pname, int[] params, int offset) {
    throw new RuntimeException("WebGL11 glTexEnviv NYI");
  }

  @Override
  public void glTexEnviv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("WebGL11 glTexEnviv NYI");
  }

  @Override
  public void glTexParameteriv(int target, int pname, int[] params, int offset) {
    throw new RuntimeException("WebGL11 glTexParameteriv NYI");
  }

  @Override
  public void glPointSizePointerOES(int type, int stride, Buffer pointer) {
    throw new RuntimeException("WebGL11 glPointSizePointerOES NYI");
  }

  @Override
  public void glVertexPointer(int size, int type, int stride, int pointer) {
      gl.glVertexAttribPointer(ARRAY_POSITION, size, type, false, stride, pointer);
//      checkError("glVertexPointer");
  }

  @Override
  public void glColorPointer(int size, int type, int stride, int pointer) {
    gl.glVertexAttribPointer(ARRAY_COLOR, size, type, true, stride, pointer);
  }

  @Override
  public void glNormalPointer(int type, int stride, int pointer) {
    gl.glVertexAttribPointer(ARRAY_NORMAL, 3, type, true, stride, pointer);
  }

  @Override
  public void glTexCoordPointer(int size, int type, int stride, int pointer) {
    gl.glVertexAttribPointer(ARRAY_TEXCOORD_0 + clientActiveTexture, size, type, false, stride, pointer);
  }

  @Override
  public void glSampleCoverage(float value, boolean invert) {
    throw new RuntimeException("GL11Emulation NYI: glSampleCoverage");
  }

  @Override
  public void glBlendFunc(int sfactor, int dfactor) {
    gl.glBlendFunc(sfactor, dfactor);
  }

  @Override
  public void glClear(int mask) {
    gl.glClear(mask);
//    checkError("glClear");
  }

  @Override
  public void glClearColor(float red, float green, float blue, float alpha) {
    gl.glClearColor(red, green, blue, alpha);
  }

  @Override
  public void glClearDepthf(float depth) {
    gl.glClearDepthf(depth);
  }

  @Override
  public void glClearStencil(int s) {
    throw new RuntimeException("GL11Emulation NYI: glClearStencil");
  }

  @Override
  public void glColorMask(boolean red, boolean green, boolean blue,
      boolean alpha) {
    gl.glColorMask(red, green, blue, alpha);
  }

  @Override
  public void glCompressedTexImage2D(int target, int level, int internalformat,
      int width, int height, int border, int imageSize, Buffer data) {
    throw new RuntimeException("GL11Emulation NYI: glCompressedTexImage2D");
  }

  @Override
  public void glCompressedTexSubImage2D(int target, int level, int xoffset,
      int yoffset, int width, int height, int format, int imageSize, Buffer data) {
    throw new RuntimeException("GL11Emulation NYI: glCompressedTexSubImage2D");
  }

  @Override
  public void glCopyTexImage2D(int target, int level, int internalformat,
      int x, int y, int width, int height, int border) {
    throw new RuntimeException("GL11Emulation NYI: glCopyTexImage2D");
  }

  @Override
  public void glCopyTexSubImage2D(int target, int level, int xoffset,
      int yoffset, int x, int y, int width, int height) {
    gl.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
  }

  @Override
  public void glCullFace(int mode) {
    gl.glCullFace(mode);
  }

  @Override
  public void glDeleteTextures(int n, IntBuffer textures) {
    gl.glDeleteTextures(n, textures);
  }

  @Override
  public void glDepthFunc(int func) {
    gl.glDepthFunc(func);
  }

  @Override
  public void glDepthMask(boolean flag) {
    gl.glDepthMask(flag);
  }

  @Override
  public void glDepthRangef(float zNear, float zFar) {
    gl.glDepthRangef(zNear, zFar);
  }

  @Override
  public void glDrawArrays(int mode, int first, int count) {
    prepareDraw();
    if (mode == GL_QUADS) {
      gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, quadsToTrianglesVbo);
      gl.glDrawElements(GL_TRIANGLES, count * 3 / 2, GL_UNSIGNED_SHORT, 2 * first * 3 / 2);
    } else {
      gl.glDrawArrays(mode, first, count);
    }
  }

  @Override
  public void glDrawElements(int mode, int count, int type, Buffer indices) {
    //GdxQuake2.tools.log("GL11Emulation: DrawElements");
    prepareDraw();
    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    gl.glDrawElements(mode, count, type, indices);
  }

  @Override
  public void glFinish() {
    gl.glFinish();
  }

  @Override
  public void glFlush() {
    gl.glFlush();
  }

  @Override
  public void glFrontFace(int mode) {
    throw new RuntimeException("GL11Emulation NYI: glFrontFace");
  }

  @Override
  public void glGenTextures(int n, IntBuffer textures) {
    gl.glGenTextures(n, textures);
  }

  @Override
  public int glGetError() {
    return gl.glGetError();
  }

  @Override
  public void glGetIntegerv(int pname, IntBuffer params) {
    if (pname == GL_VIEWPORT) {
      int pos = params.position();
      params.put(pos, viewportX);
      params.put(pos + 1, viewportY);
      params.put(pos + 2, viewportW);
      params.put(pos + 3, viewportH);
    } else {
      gl.glGetIntegerv(pname, params);
    }
  }

  @Override
  public String glGetString(int name) {
    return gl.glGetString(name);
  }

  @Override
  public void glHint(int target, int mode) {
    throw new RuntimeException("GL11Emulation NYI: glHint");
  }

  @Override
  public void glLineWidth(float width) {
    gl.glLineWidth(width);
  }

  @Override
  public void glPixelStorei(int pname, int param) {
    throw new RuntimeException("GL11Emulation NYI: glPixelStorei");
  }

  @Override
  public void glPolygonOffset(float factor, float units) {
    gl.glPolygonOffset(factor, units);
  }

  @Override
  public void glReadPixels(int x, int y, int width, int height, int format,
      int type, Buffer pixels) {
    throw new RuntimeException("GL11Emulation NYI: glReadPixels");
  }

  @Override
  public void glScissor(int x, int y, int width, int height) {
    throw new RuntimeException("GL11Emulation NYI: glScissor");
  }

  @Override
  public void glStencilFunc(int func, int ref, int mask) {
    throw new RuntimeException("GL11Emulation NYI: glStencilFunc");
  }

  @Override
  public void glStencilMask(int mask) {
    throw new RuntimeException("GL11Emulation NYI: glStencilMask");
  }

  @Override
  public void glStencilOp(int fail, int zfail, int zpass) {
    throw new RuntimeException("GL11Emulation NYI: glStencilOp");
  }

  @Override
  public void glTexImage2D(int target, int level, int internalformat,
      int width, int height, int border, int format, int type, Buffer pixels) {
    gl.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    
    internalFormats.put(boundTextureId[activeTexture], internalformat);
  }

//  @Override
//  public void glTexImage2D(int target, int level, int internalformat,
//      int format, int type, Image image) {
//    gl.glTexImage2D(target, level, internalformat, format, type, image);
//  }

  @Override
  public void glTexParameterf(int target, int pname, float param) {
    gl.glTexParameterf(target, pname, param);
  }

  @Override
  public void glTexSubImage2D(int target, int level, int xoffset, int yoffset,
      int width, int height, int format, int type, Buffer pixels) {
    gl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
  }

  @Override
  public void glGetFloatv(int pname, FloatBuffer params) {
    updateMatrixSp();
    int p = params.position();
    switch(pname) {
    case GL_PROJECTION_MATRIX:
      params.put(projectionMatrix, projectionMatrixSp, 16);
      break;
    case GL_MODELVIEW_MATRIX:
      params.put(modelViewMatrix, modelViewMatrixSp, 16);
      break;
      
    default:
      throw new RuntimeException("glGetFloatv unsuported pname: 0x" + Integer.toHexString(pname));
    }
    params.position(p);
  }

  @Override
  public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
    throw new RuntimeException("GL11Emulation NYI: glGetTexParameterfv");
  }

  @Override
  public void glTexParameterfv(int target, int pname, FloatBuffer params) {
    throw new RuntimeException("GL11Emulation NYI: glTexParametrfv");
  }

  @Override
  public void glBindBuffer(int target, int buffer) {
    gl.glBindBuffer(target, buffer);
  }

  @Override
  public void glBufferData(int target, int size, Buffer data, int usage) {
    gl.glBufferData(target, size, data, usage);
  }

  @Override
  public void glBufferSubData(int target, int offset, int size, Buffer data) {
    throw new RuntimeException("GL11Emulation NYI: glBufferSubData");
  }

  @Override
  public void glDeleteBuffers(int n, IntBuffer buffers) {
    throw new RuntimeException("GL11Emulation NYI: glDeleteBuffers");
  }

  @Override
  public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("GL11Emulation NYI: glGetBufferParmeteriv");
  }

  @Override
  public void glGenBuffers(int n, int[] buffers, int offset) {
    IntBuffer intBuffer = BufferUtils.newIntBuffer(n);
    gl.glGenBuffers(n, intBuffer);
    intBuffer.get(buffers, offset, n);
  }

  @Override
  public void glGenBuffers(int n, IntBuffer buffer) {
    gl.glGenBuffers(n, buffer);
  }

  @Override
  public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("GL11Emulation NYI: glGetTexParameteriv");
  }

  @Override
  public boolean glIsBuffer(int buffer) {
    throw new RuntimeException("GL11Emulation NYI: glGIsBuffer");
  }

  @Override
  public boolean glIsEnabled(int cap) {
    switch(cap) {
    case GL_COLOR_ARRAY:
      return (arraysEnabled & (1 << ARRAY_COLOR)) != 0;
    case GL_VERTEX_ARRAY:
      return (arraysEnabled & (1 << ARRAY_POSITION)) != 0;
    case GL_NORMAL_ARRAY:
      return (arraysEnabled & (1 << ARRAY_NORMAL)) != 0;
    case GL_TEXTURE_COORD_ARRAY:
      return (arraysEnabled & (1 << (ARRAY_TEXCOORD_0 + clientActiveTexture))) != 0;
    default:
      boolean en = gl.glIsEnabled(cap);
      return en;
    }
  }

  @Override
  public boolean glIsTexture(int texture) {
    throw new RuntimeException("GL11Emulation NYI: glIsTexture");
  }

  @Override
  public void glTexParameteri(int target, int pname, int param) {
    gl.glTexParameteri(target, pname, param);
  }

  @Override
  public void glTexParameteriv(int target, int pname, IntBuffer params) {
    throw new RuntimeException("GL11Emulation NYI: glTexParameteriv");
  }

  @Override
  public void glDrawElements(int mode, int count, int type, int offset) {
    prepareDraw();
    gl.glDrawElements(mode, count, type, offset);
  }
}

