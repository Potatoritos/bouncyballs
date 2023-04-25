#version 330
in vec3 color;
out vec4 fragColor;

//uniform vec3 color;
//uniform sampler2D sampler;

void main() {
    fragColor = vec4((color + vec3(1.0, 1.0, 1.0))/2, 1.0);
}