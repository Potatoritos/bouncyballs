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
            vec3 normalSample = texelFetch(normalTexture, ivec2(gl_FragCoord) + 3*ivec2(i-1,j-1), 0).rgb;
            r[i][j] = normalSample.r;
            g[i][j] = normalSample.g;
            b[i][j] = normalSample.b;

            float depthSample = texelFetch(depthTexture, ivec2(gl_FragCoord) + 3*ivec2(i-1,j-1), 0).r;
            depth[i][j] = (linearizeDepth(depthSample) - near) / (far - near);
        }
    }

    float gradientNormal = max(max(gradient(r), gradient(g)), gradient(b));
    float gradientDepth = gradient(depth);
    if (gradientDepth > 0.2) {
        fragColor = vec4(0, 1, 0, 1);
    } else if (gradientNormal > 0.7) {
        fragColor = vec4(1, 0, 0, 1);
    } else {
        fragColor = vec4(0, 0, 0, 0);
    }
}