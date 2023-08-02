/// Vertex
#version 330
layout (location = 0) in vec3 position;
uniform mat4 lightSpaceMatrix;
uniform mat4 worldMatrix;

void main() {
    gl_Position = lightSpaceMatrix * worldMatrix * vec4(position, 1.0);
}

/// Fragment
#version 330

void main() {
    gl_FragDepth = gl_FragCoord.z;
}
