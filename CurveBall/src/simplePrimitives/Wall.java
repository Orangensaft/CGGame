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

import simplePrimitives.GameUtils.GameState;
import simplePrimitives.GameUtils.Sides;

/**
 * Einzelnes Wandsegment
 * @author Nicolas
 *
 */
public class Wall {
	
	private float[] size = {1,5,0};
	
	private final static int[] textures = new int[] {
		GameUtils.loadPNGTexture("assets/wall.png", GL13.GL_TEXTURE0),
		GameUtils.loadPNGTexture("assets/wall1.png", GL13.GL_TEXTURE0),
		GameUtils.loadPNGTexture("assets/wall2.png", GL13.GL_TEXTURE0),
		GameUtils.loadPNGTexture("assets/wall3.png", GL13.GL_TEXTURE0),
		GameUtils.loadPNGTexture("assets/wall4.png", GL13.GL_TEXTURE0),
		GameUtils.loadPNGTexture("assets/wall5.png", GL13.GL_TEXTURE0),
		GameUtils.loadPNGTexture("assets/wall6.png", GL13.GL_TEXTURE0),
		GameUtils.loadPNGTexture("assets/wall7.png", GL13.GL_TEXTURE0),
		GameUtils.loadPNGTexture("assets/wall8.png", GL13.GL_TEXTURE0),
		GameUtils.loadPNGTexture("assets/wall9.png", GL13.GL_TEXTURE0),
		GameUtils.loadPNGTexture("assets/wall10.png", GL13.GL_TEXTURE0),
	};

	private int activeTexture;
	private final double[] textureSections = new double[] {-0.9, -0.7, -0.5, -0.3, -0.1, 0.1, 0.3, 0.5, 0.7, 0.9};

	private int verticesCount;
	private Vec3 pos;
	private Sides side;
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
	/**
	 * Ursprung unten links
	 * @param pos Ausgangspunkt
	 * @param width Breite (Wenn 0 -> Hochkant)
	 * @param height H�he (Wenn 0 -> Horizontal)
	 * @param depth Tiefe  
	 */
	public Wall(Vec3 pos, Sides side, float width, float depth){
		this.side = side;
		if (side == Sides.left || side == Sides.right){
			size[0]=0;	//Hochkant
			size[1]=width;
			size[2]=depth;
		}else{
			size[0]=width;
			size[1]=0;	//Horizontal
			size[2]=depth;
		}
		this.pos=pos;
		
		updateGraphics();
	}
	private void updateGraphics() {
		float[] verts = new float[4*3];
		float dxy = size[0];
		float dz = size[1];
		int i = 0;
		
		verts[i++]=(float) pos.x;
		verts[i++]=(float) pos.y;
		verts[i++]=(float) pos.z;
		
		verts[i++]=(float) pos.x+size[0];
		verts[i++]=(float) pos.y+size[1];
		verts[i++]=(float) pos.z;

		verts[i++]=(float) pos.x+size[0];
		verts[i++]=(float) pos.y+size[1];
		verts[i++]=(float) pos.z+size[2];

		verts[i++]=(float) pos.x;
		verts[i++]=(float) pos.y;
		verts[i++]=(float) pos.z+size[2];
		
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
		
		float[] normals = new float[4*3];
		for (i=0;i<verts.length/3;i++){
			if(side == Sides.left){
				normals[i]=1f;
				normals[i+1]=0f;
				normals[i+2]=0f;
			}
			if(side == Sides.right){
				normals[i]=-1f;
				normals[i+1]=0f;
				normals[i+2]=0f;
			}
			if(side == Sides.top){
				normals[i]=0f;
				normals[i+1]=-1f;
				normals[i+2]=0f;
			}
			if(side == Sides.bottom){
				normals[i]=0f;
				normals[i+1]=1f;
				normals[i+2]=0f;
			}
		}
		FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
		normalsBuffer.put(normals);
		normalsBuffer.flip();
		
		
		//Texturkoords
		float[] textureCoords = {
				0f,1f,
				0f,0f,
				1f,0f,
				1f,1f
				};
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(textureCoords.length);
		textureCoordsBuffer.put(textureCoords);
		textureCoordsBuffer.flip();
		
		//Indizes
		// OpenGL expects to draw the first vertices in counter clockwise order by default
		int[] indices = {3,2,0,1};	
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
	
	public void update(Ball ball, GameState state) {
		if (state == GameState.Running) {
			// select texture to highlight balls position
			double progrss = ball.getPos().z;
			activeTexture = this.textures[closest(progrss, textureSections) + 1];
		} else {
			activeTexture = this.textures[0];
		}
	}
	public void draw(int pId){
		//Paddle zeichen
		GL20.glUseProgram(pId);
        GL30.glBindVertexArray(vaoId);
        // Bind the texture
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, activeTexture);
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
	
	public int closest(double value, double[] sorted) {
		  if(value < sorted[0])
		    return 0;

		  int i = 1;
		  for( ; i < sorted.length && value > sorted[i] ; i++);

		  if(i >= sorted.length)
		    return sorted.length - 1;

		  return Math.abs(value - sorted[i]) < Math.abs(value - sorted[i-1]) ?
		         i : i-1;
	}
}
