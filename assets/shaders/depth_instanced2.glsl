/// Vertex
#version 330
layout (location = 0) in vec3 position;
uniform mat4 lightSpaceMatrix;
uniform mat4 worldMatrices[100];

void main() {
    gl_Position = lightSpaceMatrix * worldMatrices[gl_InstanceID] * vec4(position, 1.0);
}

/// Fragment
#version 330

out vec4 fragColor;

void main() {
    gl_FragDepth = gl_FragCoord.z;
    fragColor = vec4(1, 0, 0, 1);
}
