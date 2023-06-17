/// Vertex
#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 inNormal;
layout (location=2) in vec3 inColor;

uniform mat4 viewMatrices[100];
uniform mat4 projectionMatrix;
uniform vec4 color0[100];
uniform vec4 color1[100];

uniform mat4 lightSpaceMatrix;
uniform mat4 worldMatrices[100];

out vec4 color;
out float glow;
out vec3 normal;
out vec3 orthoPos;

out vec4 fragPosLightSpace;

void main() {
    gl_Position = projectionMatrix * viewMatrices[gl_InstanceID] * vec4(position, 1.0);
    color = vec4(mix(color0[gl_InstanceID], color1[gl_InstanceID], inColor.r));
    glow = 1 - inColor.g;

    normal = inNormal;

    orthoPos = vec3(worldMatrices[gl_InstanceID] * vec4(position, 1.0));
    fragPosLightSpace = lightSpaceMatrix * vec4(orthoPos, 1.0);
}

/// Fragment
#version 330

in vec4 color;
in float glow;
in vec3 normal;
in vec3 orthoPos;
in vec4 fragPosLightSpace;
out vec4 fragColor;

uniform sampler2D normalTexture;
uniform sampler2D depthTexture;
uniform sampler2D shadowMap;

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
float shadow(vec4 fragPosLightSpace) {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    float closestDepth = texture(shadowMap, projCoords.xy).r;
    float currentDepth = projCoords.z;

    vec3 lightDir = normalize(vec3(0, 0, 1) - orthoPos);
//    vec3 lightDir = vec3(0, 0, -1);
    float bias = max(0.005 * (1.0 - dot(normal, lightDir)), 0.0005);

    float shadow = 0;
    vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            float depth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - bias > depth ? 1 : 0.0;
        }
    }
    shadow = max(0.0, shadow/9 - 0.8);
    shadow = 1 - (1-shadow)*(1-shadow);
    return shadow;
}
void main() {
    mat3 r, g, b, depth;
    int size = 2;
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

    if (gradientDepth > 0.015 || gradientNormal > 1.25) {
        fragColor = vec4(0, 0, 0, color.a);
    } else {
        float shadowFactor = glow*shadow(fragPosLightSpace);
//        vec3 mixed = mix(color.rgb, vec3(100, 100, 100), shadowFactor);
        fragColor = vec4(color.rgb * (1-shadowFactor), color.a);
//        fragColor = vec4(mixed, color.a);
    }
}