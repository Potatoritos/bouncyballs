/// Vertex
#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 inTexCoord;

out vec2 outTexCoord;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * vec4(position, 1.0);
    outTexCoord = inTexCoord;
}
/// Fragment
#version 330

in vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
//    float depthValue = texture(textureSampler, outTexCoord).r;
//    if (depthValue > 0.9999 && depthValue <= 0.99999) {
//        depthValue = 0.5;
//    } else if (depthValue > 0.99999) {
//        depthValue = 0;
//    }
//    fragColor = vec4(vec3(depthValue), 1.0);
    fragColor = texture(textureSampler, outTexCoord);
    //    fragColor = vec4(outTexCoord, 0, 1);
    //    fragColor = texture(textureSampler, vec2(0.5,0.5));
    //    fragColor = vec4(1.0, 1.0, 1.0, 1.0);
}
