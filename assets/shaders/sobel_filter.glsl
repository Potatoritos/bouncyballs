/// Vertex
#version 330

layout (location=0) in vec3 position;
layout (location=2) in vec3 inColor;

out vec3 color;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
    color = inColor;
}

/// Fragment
#version 330

in vec3 color;
out vec4 fragColor;

uniform sampler2D normalTexture;
uniform sampler2D depthTexture;
//uniform vec3 color;

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

//mat3 sobelX = mat3(
//    1.0, 1.0, 1.0,
//    0.0, 0.0, 0.0,
//    -1.0, -1.0, -1.0
//);
//mat3 sobelY = mat3(
//    1.0, 0.0, -1.0,
//    1.0, 0.0, -1.0,
//    1.0, 0.0, -1.0
//);

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
    int size = 10;
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            vec3 normalSample = texelFetch(normalTexture, ivec2(gl_FragCoord) + size*ivec2(i-1,j-1), 0).rgb;
            r[i][j] = normalSample.r;
            g[i][j] = normalSample.g;
            b[i][j] = normalSample.b;

            float depthSample = texelFetch(depthTexture, ivec2(gl_FragCoord) + size*ivec2(i-1,j-1), 0).r;
            depth[i][j] = (linearizeDepth(depthSample) - near) / (far - near);
        }
    }

    float gradientNormal = max(max(gradient(r), gradient(g)), gradient(b));
    float gradientDepth = gradient(depth);

//    vec3 c1 = color - gradientDepth * 0.8;
//    vec3 c2 = color - gradientNormal * 0.2;
    if (gradientDepth > 1 || gradientNormal > 0.7) {
        fragColor = vec4(0, 0, 0, 1);
//        if (length(c1) < length(c2)) {
//            fragColor = vec4(c1, 1);
//        } else {
//            fragColor = vec4(c2, 1);
//        }
    } else {
        fragColor = vec4(color, 0);
    }
}