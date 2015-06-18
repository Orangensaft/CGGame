package simplePrimitives;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import mat.Vec3;
import simplePrimitives.GameUtils;
import simplePrimitives.GameUtils.SoundType;

import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
/**
 * Klasse für die Schläger
 * @author Nicolas
 * @author Jan
 */
public class Paddle {
	private Vec3 pos; //Position
	private float[] size = {.5f,.5f}; //Gr��e des Schl�gers; Breite/H�he
	public int vaoId;
	public int vboId;
	public int vbocId;
	public int vbonId;
	public int vbotId;
	public int vboiId;
	public int vaoNormalLinesId;
	public int vbonlId;
	public int vbonlcId;
	public int textureID = 0;

	// slope variables for ball curve
	private Vec3 slope;

	private int indicesCount;
	private String tex="";
	private int verticesCount;
	/**
	 * Schl�ger erstellen
	 * @param pos ZENTRUM des Schl�gers
	 */
	public Paddle(Vec3 pos,boolean front){
		this.pos = pos;
		this.slope = new Vec3(0d, 0d, 0d);
		if (front)
			tex = "assets/paddle.png";
			//textureID = GameUtils.loadPNGTexture("assets/paddle.png", GL13.GL_TEXTURE0);
		else
			tex = "assets/paddleb.png";
			//textureID = GameUtils.loadPNGTexture("assets/paddleb.png", GL13.GL_TEXTURE0);
		textureID = GameUtils.loadPNGTexture(tex, GL13.GL_TEXTURE0);
		
		
		updateGraphics();
	}
	
	/**
	 * Let the AI move the paddle towards the ball
	 * @param b
	 */
	public void AI_Act(Ball b){
		// Get ball position
		Vec3 target = b.getPos();
		// Get x,y,z distance
		Vec3 delta = target.sub(this.pos);
		// move paddle if ball is not inside range;
		if (Math.abs(delta.x) > this.size[0]/2f || Math.abs(delta.y) > this.size[1]/2f) {
			// calculate distance amount (x,y)
			double dst = Math.sqrt(delta.x * delta.x + delta.y * delta.y);
			// correct values if they are too wide away for slow KI
			if (dst > GameUtils.AIDeltaDist) {
				delta.x = delta.x/dst*GameUtils.AIDeltaDist;
				delta.y = delta.y/dst*GameUtils.AIDeltaDist;
			}
			// change paddle position
			this.setPos(
				(float) (this.pos.x + delta.x),
				(float) (this.pos.y + delta.y)
			);
		}
	}
	
	/**
	 * Schl�ger berechnen
	 */
	private void updateGraphics(){
		//Vertices
		float[] verts = new float[4*3];
		int i=0;
		//Unten Links
		verts[i++]=(float) (pos.x-size[0]/2f);
		verts[i++]=(float) (pos.y-size[1]/2f);
		verts[i++]=(float) (pos.z);
		//Unten Rechts
		verts[i++]=(float) (pos.x+size[0]/2f);
		verts[i++]=(float) (pos.y-size[1]/2f);
		verts[i++]=(float) (pos.z);
		//Oben Rechts
		verts[i++]=(float) (pos.x+size[0]/2f);
		verts[i++]=(float) (pos.y+size[1]/2f);
		verts[i++]=(float) (pos.z);
		//Oben Links
		verts[i++]=(float) (pos.x-size[0]/2f);
		verts[i++]=(float) (pos.y+size[1]/2f);
		verts[i++]=(float) (pos.z);
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(verts.length);
		verticesBuffer.put(verts);
		verticesBuffer.flip();
		verticesCount = verts.length/3;
		
		//Farben
		float[] colors = {
		        1f, 1f, 1f, 1f,
		        1f, 1f, 1f, 1f,
		        1f, 1f, 1f, 1f,
		        1f, 1f, 1f, 1f,
		};
		FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(colors.length);
		colorsBuffer.put(colors);
		colorsBuffer.flip();
		
		// Normalen -> Z zeigt in Richtung (0,0,0)
		float zn = (pos.z>0) ? -1f : 1f;
		float[] normals = {
				0f, 0f, zn,
				0f, 0f, zn,
				0f, 0f, zn,
				0f, 0f, zn
		};
		FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
		normalsBuffer.put(normals);
		normalsBuffer.flip();
		
		//Texturkoords
		float[] textureCoords = {
				0f,1f,
				1f,1f,
				1f,0f,
				0f,0f
				};
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(textureCoords.length);
		textureCoordsBuffer.put(textureCoords);
		textureCoordsBuffer.flip();
		
		//Indizes
		// OpenGL expects to draw the first vertices in counter clockwise order by default
		int[] indices = {1,2,0,3};	
		indicesCount = indices.length;
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();
		
		//normalen-geraden
		float[] normalLines = new float[verts.length*2];
		int pos=0;
		for (i=0;i<verts.length;i+=3){
			normalLines[pos++]=verts[i];
			normalLines[pos++]=verts[i+1];
			normalLines[pos++]=verts[i+2];
			normalLines[pos++]=verts[i]+GameUtils.normalScale*normals[i];
			normalLines[pos++]=verts[i+1]+GameUtils.normalScale*normals[i+1];
			normalLines[pos++]=verts[i+2]+GameUtils.normalScale*normals[i+2];
		}
		FloatBuffer normalLinesBuffer = BufferUtils.createFloatBuffer(normalLines.length);
		normalLinesBuffer.put(normalLines);
		normalLinesBuffer.flip();
		
		//Farben dazu
		float[] normalLinesColors = new float[colors.length*2];
		
		pos=0;
		for (i=0;i<colors.length;i+=4){
			normalLinesColors[pos++]=1f;
			normalLinesColors[pos++]=1f;
			normalLinesColors[pos++]=0f;
			normalLinesColors[pos++]=1f;
			normalLinesColors[pos++]=1f;
			normalLinesColors[pos++]=1f;
			normalLinesColors[pos++]=0f;
			normalLinesColors[pos++]=1f;
		}		
		FloatBuffer normalLinesColorsBuffer = BufferUtils.createFloatBuffer(normalLinesColors.length);
		normalLinesColorsBuffer.put(normalLinesColors);
		normalLinesColorsBuffer.flip();
		
		//Daten in Graka werfen
		// Create a new Vertex Array Object in memory and select it (bind)
		// A VAO can have up to 16 attributes (VBO's) assigned to it by default
		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);
		
		// Create a new Vertex Buffer Object (VBO) in memory and select it (bind)
		// A VBO is a collection of Vectors which in this case resemble the location of each vertex.
		vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
		// Put the VBO in the attributes list at index 0
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		// Deselect (bind to 0) the VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		// Create a new VBO for the indices and select it (bind) - COLORS
        vbocId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsBuffer, GL15.GL_STATIC_DRAW);
        //index 1, in 0 are the vertices stored; 4 values (RGAB) instead of 3 (XYZ)
        GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0); 
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        // Create a new VBO for the indices and select it (bind) - NORMALS
        vbonId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbonId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalsBuffer, GL15.GL_STATIC_DRAW);
        //index 2, 3 values (XYZ)
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, true, 0, 0); 
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        // Create a new VBO and select it (bind) - TEXTURE COORDS
        vbotId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbotId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureCoordsBuffer, GL15.GL_STATIC_DRAW);
        //index 3, 2 values (ST)
        GL20.glVertexAttribPointer(3, 2, GL11.GL_FLOAT, true, 0, 0); 
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		// Create a new VBO for the indices and select it (bind) - INDICES
		vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		// Deselect (bind to 0) the VBO
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		// _Second_ VAO for normal visualization (optional)
		vaoNormalLinesId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoNormalLinesId);
		
		// Create a new VBO for normal lines and select it (bind)
        vbonlId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbonlId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalLinesBuffer, GL15.GL_STATIC_DRAW);
        //index 0, new VAO; 3 values (XYZ)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0); 
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        // Create a new VBO for normal lines and select it (bind) - COLOR
        vbonlcId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbonlcId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalLinesColorsBuffer, GL15.GL_STATIC_DRAW);
        //index 0, new VAO; 4 values (RGBA)
        GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0); 
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        // Deselect (bind to 0) the VAO
     	GL30.glBindVertexArray(0);
     	
	}
	
	public void draw(int pId){
		//Paddle zeichen
		GL20.glUseProgram(pId);
        // Bind to the VAO that has all the information about the vertices
		// Bind the texture
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL30.glBindVertexArray(vaoId);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glEnableVertexAttribArray(3); // texture coordinates
        // Bind to the index VBO that has all the information about the order of the vertices
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
        // Draw the vertices
        GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, indicesCount, GL11.GL_UNSIGNED_INT, 0);
        // Put everything back to default (deselect)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
	}
	
	public Vec3 getPos(){
		return this.pos;
	}

	public Vec3 getSlope(){
		return this.slope;	
	}
	
	public float[] getSize(){
		return this.size;
	}
	
	public void setPos(float x, float y){
		slope.x = (x - pos.x)/(1d/GameUtils.fps);
		slope.y = (y - pos.y)/(1d/GameUtils.fps);
		pos.x = x;
		pos.y = y;
		updateGraphics();
	}
}
