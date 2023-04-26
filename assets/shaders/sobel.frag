#version 330

out vec4 fragColor;

uniform sampler2D normalTexture;
uniform sampler2D depthTexture;

mat3 sobelX = mat3(
    1.0, 2.0, 1.0,
    0.0, 0.0, 0.0,
    -1.0, -2.0, -1.0
);
mat3 sobelY = mat3(
    1.0, 0.0, -1.0,
    2.0, 0.0, -2.0,
    1.0, 0.0, -1.0
);

float gradient(in mat3 m) {
    float gx = dot(sobelX[0], m[0]) + dot(sobelX[1], m[1]) + dot(sobelX[2], m[2]);
    float gy = dot(sobelY[0], m[0]) + dot(sobelY[1], m[1]) + dot(sobelY[2], m[2]);
    return sqrt(gx*gx + gy*gy);
}
float near = 0.01;
float far = 100;
float linearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - z * (far - near));
}
void main() {
    mat3 r, g, b, depth;
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            vec3 s = texelFetch(normalTexture, ivec2(gl_FragCoord) + 3*ivec2(i-1,j-1), 0).rgb;
            r[i][j] = s.r;
            g[i][j] = s.g;
            b[i][j] = s.b;

            depth[i][j] = texelFetch(depthTexture, ivec2(gl_FragCoord) + 3*ivec2(i-1,j-1), 0).r;
        }
    }
//    float G = max(max(gradient(r), gradient(g)), gradient(b));
//    fragColor = vec4(vec3(G), 1.0);
//    fragColor = vec4(vec3(gradient(depth)), 1.0);
    float z = texelFetch(depthTexture, ivec2(gl_FragCoord), 0).r;
//    float linearDepth = (linearizeDepth(z) - near) / (far - near);
//    fragColor = vec4(vec3((linearDepth)), 1.0);
    fragColor = vec4(vec3(linearizeDepth(z)*0.01), 1.0);

//    fragColor = vec4(z, z, z, 1.0);
}