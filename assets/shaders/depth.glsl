/// Vertex
#version 330
layout (location = 0) in vec3 position;
uniform mat4 lightSpaceMatrix;
uniform mat4 worldMatrices[200];

void main() {
    gl_Position = lightSpaceMatrix * worldMatrices[gl_InstanceID] * vec4(position, 1.0);
}

/// Fragment
#version 330

void main() {
    gl_FragDepth = gl_FragCoord.z;
}
