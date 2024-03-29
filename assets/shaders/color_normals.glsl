/// Vertex
#version 330
layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;

out vec4 color;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform float transparency;

void main() {
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
    color = vec4(normal, transparency);
}
/// Fragment
#version 330
in vec4 color;
out vec4 fragColor;

//uniform vec3 color;
//uniform sampler2D sampler;

void main() {
    fragColor = vec4((color.rgb + vec3(1.0, 1.0, 1.0))/2, color.a);
    //    fragColor = vec4(color, 1.0);

}