// Copyright (c) 2009 Aaftab Munshi, Dan Ginsburg, Dave Shreiner
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this 
// software and associated documentation files (the "Software"), to deal in the Software
// without restriction, including without limitation the rights to use, copy, modify, 
// merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
// permit persons to whom the Software is furnished to do so, subject to the following 
// conditions:
//
// The above copyright notice and this permission notice shall be included in all copies
// or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
// PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
// CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
// OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// Book:      OpenGL(R) ES 2.0 Programming Guide
// Authors:   Aaftab Munshi, Dan Ginsburg, Dave Shreiner
// ISBN-10:   0321502795
// ISBN-13:   9780321502797
// Publisher: Addison-Wesley Professional
// URLs:      http://safari.informit.com/9780321563835
//            http://www.opengles-book.com
//            http://www.opengles-book.com/downloads.html

package com.googlecode.gdxquake2.core.gl11;

public final class GL11VertexShader {

  static final String SOURCE = 
      "#define NUM_TEXTURES 2\n" +
  
      "struct light {" +
      "  vec4 position;" +
      "  vec4 ambient_color;" +
      "  vec4 diffuse_color;" +
      "  vec4 specular_color;" +
      "  vec3 spot_direction;" +
      "  vec3 attenuation_factors;" +
      "  float spot_exponent;" +
      "  float spot_cutoff_angle;" +
      "  bool compute_distance_attenuation;" +
      "};" +
      
      "struct material {" +
      "  vec4 ambient_color;" +
      "  vec4 diffuse_color;" +
      "  vec4 specular_color;" +
      "  vec4 emissive_color;" +
      "  float specular_exponent;" +
      "};" +
      
      "const float c_zero = 0.0;" + 
      "const float c_one = 1.0;" + 
      "const int index_zero = 0;" + 
      "const int index_one = 1;" +
      
      "uniform mat4 mvp_matrix;" +
      "uniform mat4 modelview_matrix;" + 
      "uniform mat3 inv_modelview_matrix;" +
      "uniform mat4 tex_matrix[NUM_TEXTURES];" +
      "uniform bool enable_tex[NUM_TEXTURES];" + 
      "uniform bool enable_tex_matrix[NUM_TEXTURES];" +
      
      "uniform material material_state;" +
      "uniform vec4 ambient_scene_color;" +
      "uniform light light_state[8];" +
      "uniform bool light_enable_state[8];" +
      "uniform int num_lights;" +
      "uniform bool enable_lighting;" +
      "uniform bool light_model_two_sided;" +
      "uniform bool enable_color_material;" +
      
      "uniform bool enable_fog;" + 
      "uniform int fog_mode;" +
      "uniform float fog_density;" +
      "uniform float fog_start;" +
      "uniform float fog_end;" +

      "uniform bool xform_eye_p;" +
      "uniform bool normalize_normal;" +
      "uniform float rescale_normal_factor;" +
      
      "uniform vec4 ucp_eqn;" +
      "uniform bool enable_ucp;" +
      
      // Vertex attributes

      "attribute vec4 a_position;" +
      "attribute vec4 a_texcoord0;" +
      "attribute vec4 a_texcoord1;" +
      "attribute vec4 a_color;" + 
      "attribute vec3 a_normal;" +

      // Varying variables
      
      "varying vec4 v_texcoord[NUM_TEXTURES];" +
      "varying vec4 v_front_color;" + 
      "varying vec4 v_back_color;" + 
      "varying float v_fog_factor;" +
      "varying float v_ucp_factor;" +
      
      // Temporary variables used by the vertex shader
      
      "vec4 p_eye;" +
      "vec3 n;" +
      "vec4 mat_ambient_color;" +
      "vec4 mat_diffuse_color;" +
      
      // Lighting equation
      
      "vec4 lighting_equation(int i) {" +
      "  vec4 computed_color = vec4(c_zero, c_zero, c_zero, c_zero);" +
      "  vec3 h_vec;" +
      "  float ndotl, ndoth;" +
      "  float att_factor;" + 
      "    float spot_factor;" +
      "    vec3 att_dist;" +
      "    vec3 VPpli;" +

      "  att_factor = c_one;" +
      "  if(light_state[i].position.w != c_zero) {" +

      "    VPpli = light_state[i].position.xyz - p_eye.xyz;" +
      "    if(light_state[i].compute_distance_attenuation) {" +
      "      att_dist.x = c_one;" +
      "      att_dist.z = dot(VPpli, VPpli);" +
      "      att_dist.y = sqrt(att_dist.z);" +
      "      att_factor = c_one / dot(att_dist, light_state[i].attenuation_factors);" +
      "    }" +
      "    VPpli = normalize(VPpli);" +

      "    if (light_state[i].spot_cutoff_angle < 180.0) {" +
      "      spot_factor = dot(-VPpli, light_state[i].spot_direction);" +
      "      if (spot_factor >= cos(radians(light_state[i].spot_cutoff_angle))) {" +
      "        spot_factor = pow(spot_factor, light_state[i].spot_exponent);" +
      "      } else {" +
      "        spot_factor = c_zero;" +
      "      }" +
      "      att_factor *= spot_factor;" +
      "    }" +
      "  } else {" +
      "    VPpli = light_state[i].position.xyz;" +
      "  }" +

      "  if (att_factor > c_zero) {" +
      "    computed_color += (light_state[i].ambient_color * mat_ambient_color);" +
      "    ndotl = max(c_zero, dot(n, VPpli));" +
      "    computed_color += (ndotl * light_state[i].diffuse_color * mat_diffuse_color);" +
      "    h_vec = normalize(VPpli + vec3(c_zero, c_zero, c_one));" +
      "    ndoth = dot(n, h_vec);" +
      "    if (ndoth > c_zero) {" +
      "      computed_color *= (pow(ndoth, material_state.specular_exponent) * " +
      "                                   material_state.specular_color *" +
      "                                   light_state[i].specular_color);" +
      "    }" +
      "    computed_color *= att_factor;" +
      "  }" +
      "  return computed_color;" +
      "}" +

      "float compute_fog() {" + 
      "  float f;" +
      "  float distance;" + 
      "  distance = length(p_eye);" + //?
      "  if (fog_mode == 2048) {" + // GL_EXP
      "    f = exp(-(distance * fog_density));" +
      "  } else if (fog_mode == 9729) {" + // GL_LINEAR
      "    f = (fog_end - distance) / (fog_end - fog_start);" +
      "  } else {" +
      "    f = distance * fog_density;" +
      "  }" +
      "  f = clamp(f, 0.0, 1.0);" +
      "  return f;" +
      "}" +
      
      "vec4 do_lighting() {" +
      "  vec4 vtx_color;" +
      "  int i, j;" +
      "  vtx_color = material_state.emissive_color +" +
      "      (mat_ambient_color * ambient_scene_color);" +
      "  j = 0;" +
      "  for (int i = 0; i < 8; i++) {" +
      "    if (j >= num_lights) {" +
      "      break;" +
      "    }" +
      "    if (light_enable_state[i]) {" +
      "      j++;" +
      "      vtx_color += lighting_equation(i);" +
      "    }" +
      "  }" +
      "  vtx_color.a = mat_diffuse_color.a;" +
      "  return vtx_color;" +
      "}" + 
      
      "void main() {" +
      "  if (enable_fog) {" + // TODO(haustein) xform_eye_p
      "    p_eye = modelview_matrix * a_position;" +
      "  }" +
      
      "  gl_Position = mvp_matrix * a_position;" +
      "  v_front_color = a_color;" + 
      "  v_texcoord[0] = tex_matrix[0] * a_texcoord0;" +
      "  v_texcoord[1] = tex_matrix[1] * a_texcoord1;" +
      "  v_fog_factor = enable_fog ? compute_fog() : 1.0;" +
      "}\n";
}
