/**
  * A simple application for displaying a rectangle with drawElements and triangle strip
  * 
  * @author Thorsten Gattinger
  * 
  * sources:
  * http://wiki.lwjgl.org/wiki/The_Quad_with_DrawElements and the other Quad-parts
  * getting started: http://www.lwjgl.org/guide
  * http://hg.l33tlabs.org/twl/file/tip/src/de/matthiasmann/twl/utils/PNGDecoder.java
  */

package simplePrimitives;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.openal.*;

import simplePrimitives.GameUtils.Sides;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;
// matrix Utilities was removed with lwjgl3, we use our own
import mat.*;

public class Game {
 
    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback   keyCallback;
    private GLFWWindowSizeCallback window_size_callback;
 
    // The window handle
    private long window;
    
    // Window size
    private int WIDTH = 800;
    private int HEIGHT = 640;
  	
  	// Shader variables
  	private int vsId = 0;
    private int fsId = 0;
    private int pId = 0;
    private int vsNormalsId = 0;
    private int fsNormalsId = 0;
    private int pNormalsId = 0;
    private int textureID = 0; //Wo benutzen? -> im original unbenutzt
    
    // Moving variables
    private int projectionMatrixLocation = 0;
    private int viewMatrixLocation = 0;
    private int modelMatrixLocation = 0;
    private int projectionMatrixLocationNormals = 0; // separate one for normals
    private int viewMatrixLocationNormals = 0;// separate one for normals
    private int modelMatrixLocationNormals = 0;// separate one for normals
    private Matrix4 projectionMatrix = null;
    private Matrix4 viewMatrix = null;
    private Matrix4 modelMatrix = null;
    private Vec3 modelAngle = new Vec3(0,0,0);
    private Vec3 cameraPos = new Vec3(0,0,-3); //Kamera-Position
    private float deltaRot = 2.5f;
    
    // toggles & interactions
    private boolean showMesh = true;
    private boolean showNormals = false;
    private boolean useBackfaceCulling = false;
    private int useNormalColoring = 0;
    private int useNormalColoringLocation = 0;
    private int useTexture = 1;
    private int useTextureLocation = 0;
  	
    public void run() {
        System.out.println("Hello LWJGL " + Sys.getVersion() + "!");
 
        try {
            init();
            setupShaders();
            setupMatrices();
            loop();
 
            // Release window and window callbacks
            glfwDestroyWindow(window);
            keyCallback.release();
        } catch (Exception e){
        	e.printStackTrace();
        } finally {
            // Terminate GLFW and release the GLFWerrorfun
            glfwTerminate();
            errorCallback.release();
        }
    }
 
    private void init() {
    	
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));
 
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( glfwInit() != GL11.GL_TRUE )
            throw new IllegalStateException("Unable to initialize GLFW");
 
        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE); // the window will be resizable
		// Set OpenGL version to 3.2.0
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
 
        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "CurveBall by Jan F. & Nicolas M.", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");
 
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
            	if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, GL_TRUE); // We will detect this in our rendering loop
            	if ( key == GLFW_KEY_RIGHT )
                    modelAngle.y += deltaRot;
            	if ( key == GLFW_KEY_LEFT )
                    modelAngle.y -= deltaRot;
            	if ( key == GLFW_KEY_M && action == GLFW_PRESS ) {
            		showMesh = !showMesh;
            		if (showMesh) glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
            		else glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
            	}
            	
            	if (key == GLFW_KEY_R && action == GLFW_PRESS){
            		modelAngle.y = 0;
            	}
            	
            	if ( key == GLFW_KEY_N && action == GLFW_PRESS )
            		showNormals = !showNormals;
            	if ( key == GLFW_KEY_T && action == GLFW_PRESS ){
            		if (useTexture==1)
            			useTexture=0;
            		else
            			useTexture=1;
            	}
            	if ( key == GLFW_KEY_C && action == GLFW_PRESS ) {
            		useBackfaceCulling = !useBackfaceCulling;
            		if (useBackfaceCulling) glEnable(GL_CULL_FACE);
            		else glDisable(GL_CULL_FACE);
            	}
            	if ( key == GLFW_KEY_V && action == GLFW_PRESS ){
            		if (useNormalColoring==1)
            			useNormalColoring=0;
            		else
            			useNormalColoring=1;
            	}
            	
            }
        });
   
        // Get the resolution of the primary monitor
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
            window,
            (GLFWvidmode.width(vidmode) - WIDTH) / 2,
            (GLFWvidmode.height(vidmode) - HEIGHT) / 2
        );
 
        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);
 
        // Make the window visible
        glfwShowWindow(window);
        
        // Setup a window size callback for viewport adjusting while resizing
        glfwSetWindowSizeCallback(window, window_size_callback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				// Viewport: Use full display size
		        GL11.glViewport(0, 0, width, height);
			}
        });
        
        
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GLContext.createFromCurrent();
 
        // Debug: We need version 3.2 or newer
        System.out.println("We need OpenGL version 3.2.0. You use " + GL11.glGetString(GL11.GL_VERSION));
        
        // Viewport: Use full display size
        GL11.glViewport(0, 0, WIDTH, HEIGHT);
        
        // Set the clear color - gray
        glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
        
        // Switch to wireframe
        glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
        // -> back to solid faces: glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
 
        // Backface culling: Shows, if the triangles are correctly defined
        glDisable(GL_CULL_FACE);
        
        // Draw thicker lines
        GL11.glLineWidth(2);
        
    }
    
    
    private void setupMatrices() {
     	
    	// Setup projection and view matrix
    	projectionMatrix = new PerspectiveMatrix(GameUtils.near,GameUtils.far,GameUtils.left,GameUtils.right,GameUtils.bottom,GameUtils.top);
    	viewMatrix = new TranslationMatrix(cameraPos);
    }
    
    public int loadShader(String filename, int type) {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID = 0;
         
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Could not read file.");
            e.printStackTrace();
            System.exit(-1);
        }
         
        shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);
         
        return shaderID;
    }
    
    private void setupShaders() {
        int errorCheckValue = GL11.glGetError();
         
        // ============================= 1. Shader: For vertices ==================================
        // Load the vertex shader
        vsId = this.loadShader("src/simplePrimitives/vertex.glsl", GL20.GL_VERTEX_SHADER);
        // Load the fragment shader
        fsId = this.loadShader("src/simplePrimitives/fragment.glsl", GL20.GL_FRAGMENT_SHADER);
         
        // Create a new shader program that links both shaders
        pId = GL20.glCreateProgram();
        GL20.glAttachShader(pId, vsId);
        GL20.glAttachShader(pId, fsId);
 
        // Position information will be attribute 0
        GL20.glBindAttribLocation(pId, 0, "in_Position");
        // Color information will be attribute 1
        GL20.glBindAttribLocation(pId, 1, "in_Color");
        // Normal information will be attribute 2
        GL20.glBindAttribLocation(pId, 2, "in_Normal");
        // Texture coordinates information will be attribute 3
        GL20.glBindAttribLocation(pId, 3, "in_TextureCoord");
        
        GL20.glLinkProgram(pId);
        GL20.glValidateProgram(pId);
         
        errorCheckValue = GL11.glGetError();
        if (errorCheckValue != GL11.GL_NO_ERROR) {
        	//todo: error msg
            System.out.println("ERROR - Could not create the shaders:");
            System.exit(-1);
        }
        
        // Get matrices uniform locations
        projectionMatrixLocation = GL20.glGetUniformLocation(pId,"projectionMatrix");
        viewMatrixLocation = GL20.glGetUniformLocation(pId, "viewMatrix");
        modelMatrixLocation = GL20.glGetUniformLocation(pId, "modelMatrix");
        
        // the switch for toggling normals as vertex colors and texture
        useNormalColoringLocation = GL20.glGetUniformLocation(pId, "useNormalColoring");
        useTextureLocation = GL20.glGetUniformLocation(pId, "useTexture");
        
        
        // ============================= 2. Shader: For normal lines ==============================
        // Each vertex has a position and color, but no normal. So we use a shader without them
        // NVIDIA and ATI graphics cards may ignore missing values, Intel not
        // No valid shader and all values -> no shader used -> white lines
        
        
        // Load the vertex shader
        vsNormalsId = this.loadShader("src/simplePrimitives/vertexNormals.glsl", GL20.GL_VERTEX_SHADER);
        // Load the fragment shader
        fsNormalsId = this.loadShader("src/simplePrimitives/fragmentNormals.glsl", GL20.GL_FRAGMENT_SHADER);
         
        // Create a new shader program that links both shaders
        pNormalsId = GL20.glCreateProgram();
        GL20.glAttachShader(pNormalsId, vsNormalsId);
        GL20.glAttachShader(pNormalsId, fsNormalsId);
 
        // Position information will be attribute 0
        GL20.glBindAttribLocation(pNormalsId, 0, "in_Position");
        // Color information will be attribute 1
        GL20.glBindAttribLocation(pNormalsId, 1, "in_Color");
        
        GL20.glLinkProgram(pNormalsId);
        GL20.glValidateProgram(pNormalsId);
         
        errorCheckValue = GL11.glGetError();
        if (errorCheckValue != GL11.GL_NO_ERROR) {
        	//todo: error msg
            System.out.println("ERROR - Could not create the shaders:"+errorCheckValue);
            System.exit(-1);
        }
        
        // Get matrices uniform locations
        projectionMatrixLocationNormals = GL20.glGetUniformLocation(pNormalsId,"projectionMatrix");
        viewMatrixLocationNormals = GL20.glGetUniformLocation(pNormalsId, "viewMatrix");
        modelMatrixLocationNormals = GL20.glGetUniformLocation(pNormalsId, "modelMatrix");        
    }

    public double getTime() {
        return glfwGetTime();
    }
    
    
    
    
    private void loop() throws Exception {
    	
    	Paddle paddleFront = new Paddle(new Vec3(.5,.5,1),false); //nach oben rechts verschoben
    	Paddle paddleBack = new Paddle(new Vec3(0,0,-1),true);
    	//Rechte Wand, beginnt vorne unten rechts, 2 Breit, 2 Tief
    	Wall wallRight = new Wall(new Vec3(1,-1,-1),Sides.right,2f,2f);
    	Wall wallLeft = new Wall(new Vec3(-1,-1,-1),Sides.left,2f,2f);
    	Wall wallTop = new Wall(new Vec3(-1,1,-1),Sides.top,2f,2f);
    	Wall wallBot = new Wall(new Vec3(-1,-1,-1),Sides.bottom,2f,2f);
    	
    	double lastLoopTime = getTime();
    	double time;
    	float delta;
    	float timeCount=0;
    	int fpsCount = 0;
    	
        while ( glfwWindowShouldClose(window) == GL_FALSE ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        	time = getTime();
        	delta = (float) (time-lastLoopTime);
        	lastLoopTime=time;
        	timeCount += delta;
        	fpsCount++;
        	if(timeCount > 1f){
        		GameUtils.fps = fpsCount;
        		fpsCount=0;
        		timeCount -=1f;
        	}
        	System.out.println(GameUtils.fps);
            //matrix inits etc.
            modelMatrix = new TranslationMatrix(new Vec3(0,0,1));  // translate...
            modelMatrix = (Matrix4) new RotationMatrix(modelAngle.y, mat.Axis.Y).mul(modelMatrix); // ... and rotate, multiply matrices 
            GL20.glUseProgram(pId);
            GL20.glUniformMatrix4fv(projectionMatrixLocation, false , toFFB(projectionMatrix));
            GL20.glUniformMatrix4fv(viewMatrixLocation, false, toFFB(viewMatrix));
            GL20.glUniformMatrix4fv(modelMatrixLocation, false, toFFB(modelMatrix));
            GL20.glUniform1i(useNormalColoringLocation, useNormalColoring);
            GL20.glUniform1i(useTextureLocation, useTexture);
            GL20.glUseProgram(0);
            
            //Mausposition abgreifen
            DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer y = BufferUtils.createDoubleBuffer(1);
            glfwGetCursorPos(window,x,y);
            double xpos=x.get();
            xpos = xpos>=0 ? xpos : 0;
            xpos = xpos<=WIDTH ? xpos : WIDTH; 
            double ypos=y.get();
            ypos = ypos>=0 ? ypos : 0;
            ypos = ypos<=HEIGHT ? ypos : HEIGHT;
            float worldX = GameUtils.mousetoWorld(xpos, WIDTH);
            //*-1 weil fenster koordinaten oben links beginnen
            float worldY = -1*GameUtils.mousetoWorld(ypos, HEIGHT);
            //System.out.println("X:"+xpos+", Y:"+ypos+" => XW="+worldX+", YW:"+worldY);
            
            //==================================Objekte updaten=================================
            paddleFront.setPos(worldX, worldY);
            // ================================== Draw objects =================================
            /*Hier die Objekte von hinten nach vorne zeichen
            *-> Painters-Algo
            *Wände
            *Hinteres Paddle
            *Kugel
            *Vorderes Paddle
            */
            wallTop.draw(pId);
            wallLeft.draw(pId);
            wallRight.draw(pId);
            wallBot.draw(pId);
            paddleBack.draw(pId);
            paddleFront.draw(pId);
            //===============ENDE=========================
    		glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
    
    /**
     * Converts a Matrix4 to a flipped float buffer
     * @param m
     * @return
     */
	private FloatBuffer toFFB(Matrix4 m){
		FloatBuffer res = BufferUtils.createFloatBuffer(16);
		for (int i=0;i<4;i++){
			for (int j=0;j<4;j++){
				res.put((float) m.get(i).get(j));
			}
		}
		return (FloatBuffer) res.flip();
	}
	
    public static void main(String[] args) {
        new Game().run();
    }
 
}