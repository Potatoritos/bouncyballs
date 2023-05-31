/// Vertex
#version 330
layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;

out vec3 color;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrices[100];

void main() {
    gl_Position = projectionMatrix * viewMatrices[gl_InstanceID] * vec4(position, 1.0);
    color = normal;
}
/// Fragment
#version 330
in vec3 color;
out vec4 fragColor;

//uniform vec3 color;
//uniform sampler2D sampler;

void main() {
    fragColor = vec4((color + vec3(1.0, 1.0, 1.0))/2, 1.0);
//    fragColor = vec4(color, 1.0);

}