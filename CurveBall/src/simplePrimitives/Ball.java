package simplePrimitives;

import simplePrimitives.GameUtils.Sides;
import simplePrimitives.GameUtils.SoundType;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import mat.Axis;
import mat.Vec3;
import mat.VectorHelper;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Ball-Klasse
 * Steuert Logik des Balls (Flugbahn etc.)
 * @author Nicolas
 * @author Jan
 *
 */
public class Ball {
	VectorHelper VecCalc = new VectorHelper();
	private Vec3 direction; //Bewegungsrichtung
	private Vec3 pos; //Position
	private Vec3 spin; // Drehung
	private Vec3 spinStep; // Drehung inkrement
	private float rotX;
	private float rotY; // Müssen X und Y rotiert werden?
	public static float r=1f/16f; //Radius des Balls
	public int vaoId;
	public int vboId;
	public int vbocId;
	public int vbonId;
	public int vbotId;
	public int vboiId;
	public int vaoNormalLinesId;
	public int vbonlId;
	public int vbonlcId;
	public int textureID;
	private String tex;
	private int indicesCount;
	/**
	 * Ball erstellen
	 * @param pos ZENTRUM des Balls
	 */
	public Ball(Vec3 pos){
		this.pos=pos;
		this.spin = new Vec3(0d,0d,0d);
		this.spinStep = new Vec3(0d, 0d, 0d);
		this.direction = new Vec3(0,0,0);
		rotX = 0;
		rotY = 0;
		tex = "assets/ball.png";
        // Bind to the VAO that has all the information about the vertices
		textureID = GameUtils.loadPNGTexture(tex, GL13.GL_TEXTURE0);
	}
	
	/**
	 * Nächste Position des Balls berechnen
	 */
	public void update(Paddle a, Paddle b){
		pos.x += direction.x;
		if (Math.abs(spin.x) > spinStep.x){
			rotX += Math.signum(spin.x)*spinStep.x;
			rotX %= 2*Math.PI;
			pos.x += spin.x;
			spin.x -= Math.signum(spin.x)*spinStep.x;
		}
		pos.y += direction.y; 
		if (Math.abs(spin.y) > spinStep.y){
			rotY += Math.signum(spin.y)*spinStep.y;
			rotY %= 2*Math.PI;
			pos.y += spin.y;
			spin.y -= Math.signum(spin.y)*spinStep.y;
		}
		updateDirections(a,b);
		updateGraphics();
	}
	
	/**
	 * Flugbahn aktualisieren
	 */
	private void updateDirections(Paddle a, Paddle b){
		//Kollisionsabfrage
		Sides col = checkCols();
		if (col == Sides.left || col==Sides.right){
			System.out.println("collided with side");
			GameUtils.requestSound(SoundType.WallCol);
			//GameUtils.sndHit.play();
			direction.x = -direction.x;
			spin.x = -spin.x;
			pos.x = col == Sides.left ? GameUtils.left + r : GameUtils.right - r;
		}
		if (col == Sides.top || col == Sides.bottom){

			System.out.println("collided with floor / ceiling");
			System.out.printf("pos:(%f, %f, %f); dir:(%f, %f, %f), spin:(%f, %f)\n", pos.x, pos.y, pos.z, 
					direction.x, direction.y, direction.z,
					spin.x,
					spin.y);
			GameUtils.requestSound(SoundType.WallCol);
			//GameUtils.sndHit.play();
			direction.y = -direction.y;
			spin.y = - spin.y;
			pos.y = col == Sides.top ? GameUtils.top + r : GameUtils.bottom - r; 
		}
		if (hitsPaddles(a,b)){
			System.out.println("collided with paddle");
			GameUtils.requestSound(SoundType.PaddleCol);
			//GameUtils.sndHit.play();
			direction.z = -direction.z;

		}
		
		//Testen ob Ball Spielfeld verl�sst
		if(Math.abs(pos.z) > Math.abs(GameUtils.far)){
			//GameUtils.sndPoint.play();
			GameUtils.requestSound(SoundType.Point);
			//TODO:
			//Spiel zur�ck setzen
			//Eventuell Bot st�rker machen
		}
		
	}
	
	/**
	 * Testen ob Ball Wände berührt
	 */
	private Sides checkCols(){
		if (pos.x - r <= GameUtils.left)
				return Sides.left;
		if (pos.x + r >= GameUtils.right)
				return Sides.right;
		if (pos.y - r <= GameUtils.top)
				return Sides.top;
		if (pos.y + r >= GameUtils.bottom)
				return Sides.bottom;
		return Sides.none;
	}
	
	/**
	 * Testet, ob Ball einen der Schläger berährt
	 * @return
	 */
	private boolean hitsPaddles(Paddle a, Paddle b){
		return hitsPaddle(a) || hitsPaddle(b);
	}
	
	/**
	 * Testen ob Ball mit Schläger kollidiert
	 * Ball wird wie eine Box behandelt.
	 * @param a Schläger
	 * @return True<=>Kollision
	 */
	private boolean hitsPaddle(Paddle a){
		Vec3 posA = a.getPos();
		float[] sizeA = a.getSize();
		//checks für a
		//Passt x?
		if (pos.x >= posA.x-sizeA[0]/2f && pos.x<=posA.x+sizeA[0]/2f){
			//passt y?
			if(pos.y >= posA.y-sizeA[1]/2f && pos.y<=posA.y+sizeA[1]/2f){
				//passt z -> Liegt Schläger im Radius der Kugel 
				if(posA.z >= pos.z - r && posA.z<=pos.z + r){
					// setze spin zu slope
					Vec3 tmp = a.getSlope();
					spin.x += tmp.x;
					spin.y += tmp.y;
					spinStep.x = Math.abs(spin.x/100d);
					spinStep.y = Math.abs(spin.y/100d);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Ball Zeichen
	 */
	public void updateGraphics(){
		int x = 5;
		int y = 5;
		float vertices[] = new float[x*y*3 + 2*3];
		float normals[] = new float[x*y*3 + 2*3];
		float texture[] = new float[x*y*2 + 4];
		float colors[] = new float[x*y*4 + 2*4];
		// pol points
		vertices[0] = 0f + (float) pos.x;
		vertices[1] = (float) Math.cos(rotX)*r + (float) pos.y;
		vertices[2] = 0f + (float) pos.z;
		normals[0] = 0;
		normals[1] = vertices[1] - (float) pos.y;
		normals[2] = 0;
		texture[0] = 1;
		texture[1] = 0.5f;

		vertices[3] = 0f + (float) pos.x;
		vertices[4] = (float) -Math.cos(rotX)*r + (float) pos.y;
		vertices[5] = 0f + (float) pos.z;
		normals[3] = 0f;
		normals[4] = vertices[4] - (float) pos.y;
		normals[5] = 0f;
		texture[2] = 0;
		texture[3] = 0.5f;

		// prepare loop
		int vp = 6; // vertex index
		double hangle = 2*Math.PI / y; // horizonzal angle
		double vangle = Math.PI / (x + 1); // vertical angle
		double hc,vc; // prepare vars for angle calculation
		for ( int slice=0; slice < y; slice++) {
			for ( int stack=0; stack < x; stack++) {
				// Calculate angles: shift them by -Math.PI 
				// so we can always connect the first vector of a slice with the upper pole
				vc = -Math.PI + vangle * (stack + 1) + rotX; // cos(-pi) = 1
				hc = -Math.PI + hangle * slice + rotY; // cos(-pi) = 1
				normals[vp] = (float) (Math.sin(vc) * Math.cos(hc));
				normals[vp + 1] = (float) (Math.cos(vc));
				normals[vp + 2] = (float) (Math.sin(vc) * Math.sin(hc));

				vertices[vp] = normals[vp] * -r;
				vertices[vp + 1] = normals[vp + 1] * -r;
				vertices[vp + 2] = normals[vp + 2] * -r;
				
				vertices[vp] += pos.x;
				vertices[vp + 1] += pos.y;
				vertices[vp + 2] += pos.z;

				vp += 3;
				// add color to every vertex
			}
		}
		
		int indices[] = new int[2*x*(y) + (y)*2];
		int ip = 0; // indices pointer
		int tp = 4; // texture pointer
		double slice_iteration = 1 / (y-1);
		double stack_iteration = 1 / (x+1);
		for (int slice=0; slice < y; slice ++) {
			if (slice % 2 == 0) {
				indices[ip++] = 0;
				for (int stack=0; stack < x; stack ++) {
					indices[ip++] = slice*x + stack + 2;
					indices[ip++] = ((slice + 1) % y)*x + stack + 2; 
					texture[tp++] = (float) stack_iteration * stack;
					texture[tp++] = (float) slice_iteration * slice;
					}
				indices[ip++] = 1;
			} else {
				indices[ip++] = 1;
				for (int stack=x - 1; stack >= 0; stack --) {
					indices[ip++] = ((slice + 1)%y)*x + stack + 2;
					indices[ip++] = slice*x + stack + 2;
					texture[tp++] = (float) stack_iteration * stack;
					texture[tp++] = (float) slice_iteration * slice;
					}
				indices[ip++] = 0;
			}
		}
		// make buffers
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(texture.length);
		textureCoordsBuffer.put(texture);
		textureCoordsBuffer.flip();

		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();
		
		FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
		normalsBuffer.put(normals);
		normalsBuffer.flip();
		
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
		indicesBuffer.put(indices);
		indicesBuffer.flip();
		indicesCount = indices.length;
		
		// normal lines. Each line is represented by its start "vertex" and and "vertex + normalScale*normal"
		float[] normalLines = new float[vertices.length*2];
		
		int pos=0;
		
		// in each loop we set two XYZ points
		for (int i=0;i<vertices.length;i+=3){
			normalLines[pos++]=vertices[i];
			normalLines[pos++]=vertices[i+1];
			normalLines[pos++]=vertices[i+2];
			normalLines[pos++]=vertices[i] + normals[i];
			normalLines[pos++]=vertices[i+1] + normals[i+1];
			normalLines[pos++]=vertices[i+2] + normals[i+2];
		}
		FloatBuffer normalLinesBuffer = BufferUtils.createFloatBuffer(normalLines.length);
		normalLinesBuffer.put(normalLines);
		normalLinesBuffer.flip();
		
		
		// color for normal lines. Each vertex has the same RGBA value (1,1,0,1) -> yellow
		float[] normalLinesColors = new float[colors.length*2];
		
		pos=0;
		int cpos=0;
		for (int i=0; i < colors.length; i+=4){
			colors[cpos++]=1f;
			normalLinesColors[pos++]=1f;
			colors[cpos++]=1f;
			normalLinesColors[pos++]=1f;
			colors[cpos++]=1f;
			normalLinesColors[pos++]=0f;
			colors[cpos++]=1f;
			normalLinesColors[pos++]=1f;
			normalLinesColors[pos++]=1f;
			normalLinesColors[pos++]=1f;
			normalLinesColors[pos++]=0f;
			normalLinesColors[pos++]=1f;
		}		
		FloatBuffer normalLinesColorsBuffer = BufferUtils.createFloatBuffer(normalLinesColors.length);
		normalLinesColorsBuffer.put(normalLinesColors);
		normalLinesColorsBuffer.flip();


		FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(colors.length);
		colorsBuffer.put(colors);
		colorsBuffer.flip();
		
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
	
	public Vec3 getDirs(){
		return this.direction;
	}
	
	public float getR(){
		return this.r;
	}
}
