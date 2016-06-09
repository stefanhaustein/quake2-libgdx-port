package com.googlecode.gdxquake2.core.gl11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Simple class to emulate glBegin... glEnd...
 */
public class MeshBuilder {
  public enum Mode {TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN, QUADS, LINES, LINE_STRIP}
  
  public static final int OPTION_TEXTURE = 4;
  public static final int OPTION_NORMALS = 2;
  public static final int OPTION_COLOR = 1;

  private static final int FLOAT_SIZE = 4;
  private static final int FLOATS_PER_EDGE = 9;
  private static final int BYTE_STRIDE = FLOATS_PER_EDGE * FLOAT_SIZE;
  
  private static final int COLOR_OFFSET = 6;
  private static final int TEX_COORD_OFFSET = 7;
  private static final int NORMAL_OFFSET = 3;
  
  private ByteBuffer byteBuffer;
  private FloatBuffer floatBuffer;
  private IntBuffer intBuffer;
  private ShortBuffer indexBuffer;
  
  private Mode mode;
  private int floatBufferPos;
  private short edgeCount;
  private short modeStartEdge;
  private int indexCount;

  private int color;
  private float normalX;
  private float normalY;
  private float normalZ;
  private float texCoordS;
  private float texCoordT;
  
  // TODO(haustein) Support transformation matrix
  private float tx;
  private float ty;
  private float tz;
  
  private boolean hasColor;
  private boolean hasNormal;
  private boolean hasTexCoords;
  private int options;

  public MeshBuilder(int maxEdges) {
    byteBuffer = ByteBuffer.allocateDirect(maxEdges * 2 * 3);
    byteBuffer.order(ByteOrder.nativeOrder());
    indexBuffer = byteBuffer.asShortBuffer();
    
    byteBuffer = ByteBuffer.allocateDirect(maxEdges * BYTE_STRIDE);
    byteBuffer.order(ByteOrder.nativeOrder());
    floatBuffer = byteBuffer.asFloatBuffer();
    intBuffer = byteBuffer.asIntBuffer();
  }

  public void color3f(float r, float g, float b) {
    color = (Math.round(r * 255.f) ) | 
            (Math.round(g * 255.f) << 8) |
            (Math.round(b * 255.f) << 16) | (255 << 24);
  }

  public void texCoord2f(float s, float t) {
    texCoordS = s;
    texCoordT = t;
  }

  public void normal3f(float x, float y, float z) {
    normalX = x;
    normalY = y;
    normalZ = z;
  }

  public void begin(Mode mode, int options) {
    this.mode = mode;
    this.options = options;
    hasColor = (options & MeshBuilder.OPTION_COLOR) != 0;
    hasNormal = (options & MeshBuilder.OPTION_NORMALS) != 0;
    hasTexCoords = (options & MeshBuilder.OPTION_TEXTURE) != 0;

    floatBufferPos = 0;
    edgeCount = 0;
    modeStartEdge = 0;
    indexCount = 0;
    tx = 0;
    ty = 0; 
    tz = 0;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
    modeStartEdge = edgeCount;
  }

  /**
   * Adds a vertex with the given coordinates and other attributes as defined
   * by options and set previously.
   */
  public void vertex3f(float x, float y, float z) {
    floatBuffer.put(floatBufferPos + 0, x + tx);
    floatBuffer.put(floatBufferPos + 1, y + ty);
    floatBuffer.put(floatBufferPos + 2, z + tz);
    if (hasColor) {
      intBuffer.put(floatBufferPos + COLOR_OFFSET, color);
    } 
    if (hasNormal) {
      floatBuffer.put(floatBufferPos + NORMAL_OFFSET + 0, normalX);
      floatBuffer.put(floatBufferPos + NORMAL_OFFSET + 1, normalY);
      floatBuffer.put(floatBufferPos + NORMAL_OFFSET + 2, normalZ);
    } 
    if (hasTexCoords) {
      floatBuffer.put(floatBufferPos + TEX_COORD_OFFSET + 0, texCoordS);
      floatBuffer.put(floatBufferPos + TEX_COORD_OFFSET + 1, texCoordT);
    }
    floatBufferPos += FLOATS_PER_EDGE;
    edgeCount++;
    
    switch(mode) {
    case TRIANGLES:
    case LINES:
      indexBuffer.put(indexCount++, (short) (edgeCount - 1));
      break;
    case LINE_STRIP:
      throw new RuntimeException("LINE_STRIP NYI");
    case QUADS:
      if (((edgeCount - modeStartEdge) & 3) == 0) {
        indexBuffer.put(indexCount + 0, (short) (edgeCount - 4));
        indexBuffer.put(indexCount + 1, (short) (edgeCount - 3));
        indexBuffer.put(indexCount + 2, (short) (edgeCount - 2));
        
        indexBuffer.put(indexCount + 3, (short) (edgeCount - 4));
        indexBuffer.put(indexCount + 4, (short) (edgeCount - 2));
        indexBuffer.put(indexCount + 5, (short) (edgeCount - 1));
        indexCount += 6;
      }
      break;
    case TRIANGLE_FAN:
      if ((edgeCount - modeStartEdge > 3)) {
        indexBuffer.put(indexCount + 0, (short) modeStartEdge);
        indexBuffer.put(indexCount + 1, (short) (edgeCount - 2));
        indexBuffer.put(indexCount + 2, (short) (edgeCount - 1));
        indexCount += 3;
      } else {
        indexBuffer.put(indexCount++, (short) (edgeCount - 1));
      }
      break;
    case TRIANGLE_STRIP:
      if (edgeCount - modeStartEdge > 3) {
        indexBuffer.put(indexCount + 0, (short) (edgeCount - 3));
        indexBuffer.put(indexCount + 2, (short) (edgeCount - 2));
        indexBuffer.put(indexCount + 1, (short) (edgeCount - 1));
        indexCount += 3;
      } else {
        indexBuffer.put(indexCount++, (short) (edgeCount - 1));
      }
      break;
    default:
      throw new RuntimeException("Unrecognized mode: " + mode);
    }
  }

  public Mode getMode() {
    return mode;
  }

  public ByteBuffer getBuffer() {
    return byteBuffer;
  }

  public int getOptions() {
    return options;
  }

  public int getColorByteOffset() {
    return COLOR_OFFSET * FLOAT_SIZE;
  }

  public int getTexCoordByteOffset() {
    return TEX_COORD_OFFSET * FLOAT_SIZE;
  }

  public int getByteStride() {
    return BYTE_STRIDE;
  }

  public int getByteLimit() {
    return edgeCount * BYTE_STRIDE;
  }
  
  public int getIndexCount() {
    return indexCount;
  }

  public ShortBuffer getIndices() {
    return indexBuffer;
  }

  public void translate(float tx, float ty, float tz) {
    this.tx = tx;
    this.ty = ty;
    this.tz = tz;
  }

  public void end(GL11 gl) {

    byteBuffer.position(0);
    gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
//    gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vboHandle);
    gl.glVertexPointer(3, GL11.GL_FLOAT, BYTE_STRIDE, floatBuffer);

    if (hasColor) {
      gl.glEnableClientState(GL11.GL_COLOR_ARRAY);
      byteBuffer.position(getColorByteOffset());
      gl.glVertexPointer(4, GL11.GL_UNSIGNED_BYTE, BYTE_STRIDE, byteBuffer);
    } else {
      gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
    }
    
//    if (hasNormal) {
//      gl.glEnableClientState(GL11.GL_NORMAL_ARRAY);
//      byteBuffer.position(NORMAL_OFFSET * FLOAT_SIZE);
//      gl.glNormalPointer(GL11.GL_FLOAT, BYTE_STRIDE, byteBuffer);
//    } else {
//      gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
//    }

    if (hasTexCoords) {
      gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
      byteBuffer.position(getTexCoordByteOffset());
      gl.glTexCoordPointer(2, GL11.GL_FLOAT, BYTE_STRIDE, byteBuffer);
    } else {
      gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
    }
    
    if (indexCount % 3 != 0) {
      System.out.println("Unexpected index count: " + indexCount);
    }
    // TODO(haustein) fix in PlayN
    indexBuffer.limit(indexCount);
    gl.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_SHORT, indexBuffer);
    indexBuffer.limit(indexBuffer.capacity());
    
   // GLDebug.checkError(gl, "drawElements");
//    gl.glDrawArrays(mode, 0, pos / FLOATS_PER_EDGE);
    
    if (hasColor) {
      gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
    }
    if (hasTexCoords) {
      gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
    }
  //  gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
  }

  public void color4ub(byte b, byte c, byte d, byte e) {
    throw new RuntimeException();
  }

  public void vertex2f(int x, int y) {
    vertex3f(x, y, 0);
  }
}
