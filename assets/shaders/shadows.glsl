/// Vertex
#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 inNormal;
layout (location=2) in vec3 inColor;

out vec3 normal;
out vec3 color;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
    normal = inNormal;
    color = inColor;
}

/// Fragment
#version 330

in vec3 normal;
in vec3 color;

out vec4 fragColor;

vec3 lightDirection = normalize(vec3(0.5, 0.75, 1));
float ambient = 0.3;

void main() {
    float diffuse = max(dot(normal, lightDirection), 0.0);
    fragColor = vec4((ambient + diffuse) * color, 1.0);
}
