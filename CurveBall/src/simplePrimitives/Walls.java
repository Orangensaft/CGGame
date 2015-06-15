package simplePrimitives;

import org.lwjgl.opengl.GL13;

public class Walls {
	
	private float[] size = {1,5};
	private int textureId = 0;
	public Walls(float width, float depth){
		size[0]=width;
		size[1]=depth;
		textureId = GameUtils.loadPNGTexture("assets/wall.png", GL13.GL_TEXTURE0);
		
	}
	
	
}
