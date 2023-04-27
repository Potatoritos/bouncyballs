/// Vertex
#version 330

layout (location=0) in vec3 position;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
}

/// Fragment
#version 330

out vec4 fragColor;
