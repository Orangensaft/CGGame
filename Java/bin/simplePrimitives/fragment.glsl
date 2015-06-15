#version 150 core

in vec4 pass_Color;
in vec3 out_Normal;
in vec2 pass_TextureCoord;

uniform int useNormalColoring;
uniform int useTexture;
uniform sampler2D texture_diffuse;

out vec4 out_Color;

void main(void) {

	// display the passed color
	out_Color = pass_Color;
	
	// maybe override it with the texture
	if (useTexture==1){
		out_Color = texture(texture_diffuse, pass_TextureCoord);
	}
	
	// maybe override it with our transformed normals
	if (useNormalColoring==1){
		out_Color = vec4(out_Normal,1);
	}
}