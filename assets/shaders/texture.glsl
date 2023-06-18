/// Vertex
#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 inTexCoord;

out vec2 outTexCoord;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
    outTexCoord = inTexCoord;
}

/// Fragment
#version 330

in vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
    fragColor = texture(textureSampler, outTexCoord);
}
