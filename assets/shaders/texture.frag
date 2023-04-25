#version 330

in vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
    fragColor = texture(textureSampler, outTexCoord);
//    fragColor = vec4(outTexCoord, 0, 1);
//    fragColor = texture(textureSampler, vec2(0.5,0.5));
//    fragColor = vec4(1.0, 1.0, 1.0, 1.0);
}
