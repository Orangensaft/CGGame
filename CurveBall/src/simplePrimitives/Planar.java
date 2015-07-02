package simplePrimitives;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import mat.Vec3;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;


/*
 * Einzelnes Wandsegment
 * @author Nicolas
 * @author Jan
 *
 */

public class Planar {	
	private int textureID;
	
	private Vec3 pos;
	private Vec3 size;
	private int indicesCount;
	private int vaoId;
	private int vboId;
	private int vbocId;
	private int vbonId;
	private int vbotId;
	private int vboiId;
	private int vaoNormalLinesId;
	private int vbonlId;
	private int vbonlcId;
	private int[] indices;
	/**
	 * Ursprung unten links
	 * @param pos Ausgangspunkt
	 * @param width Breite (Wenn 0 -> Hochkant)
	 * @param height H�he (Wenn 0 -> Horizontal)
	 * @param depth Tiefe  
	 */
	public Planar(Vec3 pos, Vec3 size, String tex){
		
		//indices = new int[] {0,1,3,2};
		indices = new int[] {0,3,2,1};	
		this.pos=pos;
		this.size=size;
		this.textureID = GameUtils.loadPNGTexture(tex, GL13.GL_TEXTURE0);
		updateGraphics();
	}
	private void updateGraphics() {
		float[] verts = new float[4*3];
		int i = 0;
		
		//links unten
		verts[i++]=(float) (pos.x-size.x/2d);
		verts[i++]=(float) (pos.y-size.y/2d);
		verts[i++]=(float) (pos.z);
		
		//rechts oben
		verts[i++]=(float) (pos.x+size.x/2d);
		verts[i++]=(float) (pos.y+size.y/2d);
		verts[i++]=(float) (pos.z);

		//rechts unten
		verts[i++]=(float) (pos.x+size.x/2d);
		verts[i++]=(float) (pos.y-size.y/2d);
		verts[i++]=(float) (pos.z);

		//links oben
		verts[i++]=(float) (pos.x-size.x/2d);
		verts[i++]=(float) (pos.y+size.y/2d);
		verts[i++]=(float) (pos.z);

		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(verts.length);
		verticesBuffer.put(verts);
		verticesBuffer.flip();
		
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
		
		float[] normals = new float[4*3];
		for (i=0;i<verts.length/3;i++){
				normals[i]=1f;
				normals[i+1]=0f;
				normals[i+2]=0f;
		}
	
		FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
		normalsBuffer.put(normals);
		normalsBuffer.flip();	
		
		//Texturkoords
		float[] textureCoords = {
				1f,1f,
				0f,0f,
				0f,1f,
				1f,0f
				};
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(textureCoords.length);
		textureCoordsBuffer.put(textureCoords);
		textureCoordsBuffer.flip();
		
		//Indizes
		// OpenGL expects to draw the first vertices in counter clockwise order by default
		indicesCount = indices.length;
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();
		
		//normalen-geraden
		float[] normalLines = new float[verts.length*2];
		int posp=0;
		for (i=0;i<verts.length;i+=3){
			normalLines[posp++]=verts[i];
			normalLines[posp++]=verts[i+1];
			normalLines[posp++]=verts[i+2];
			normalLines[posp++]=verts[i]+GameUtils.normalScale*normals[i];
			normalLines[posp++]=verts[i+1]+GameUtils.normalScale*normals[i+1];
			normalLines[posp++]=verts[i+2]+GameUtils.normalScale*normals[i+2];
		}
		FloatBuffer normalLinesBuffer = BufferUtils.createFloatBuffer(normalLines.length);
		normalLinesBuffer.put(normalLines);
		normalLinesBuffer.flip();
		
		
		//Farben dazu
		float[] normalLinesColors = new float[colors.length*2];
		
		posp=0;
		for (i=0;i<colors.length;i+=4){
			normalLinesColors[posp++]=1f;
			normalLinesColors[posp++]=1f;
			normalLinesColors[posp++]=0f;
			normalLinesColors[posp++]=1f;
			normalLinesColors[posp++]=1f;
			normalLinesColors[posp++]=1f;
			normalLinesColors[posp++]=0f;
			normalLinesColors[posp++]=1f;
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
        GL30.glBindVertexArray(vaoId);
        // Bind the texture
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
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
}