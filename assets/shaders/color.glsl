/// Vertex
#version 330

layout (location=0) in vec3 position;
layout (location=2) in vec3 inColor;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec4 color0;
uniform vec4 color1;

out vec4 color;

void main() {
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);

    color = vec4(mix(color0, color1, inColor.r));
}

/// Fragment
#version 330

in vec4 color;

out vec4 fragColor;

void main() {
    fragColor = color;
}