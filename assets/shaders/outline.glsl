/// Vertex
#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform float expand;

//out vec3 color;

void main() {
    vec4 pos = projectionMatrix * viewMatrix * vec4(position + expand*normalize(normal), 1.0);
    gl_Position = projectionMatrix * viewMatrix * vec4(position + expand*normalize(normal)*pos.w, 1.0);
}

/// Fragment
#version 330

out vec4 fragColor;
//in vec3 color;

void main() {
    fragColor = vec4(0, 0, 0, 1);
}
