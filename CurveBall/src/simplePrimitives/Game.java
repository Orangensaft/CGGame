/**
  * A simple application for displaying a rectangle with drawElements and triangle strip
  * 
  * @author Thorsten Gattinger
  * 
  * sources:
  * http://wiki.lwjgl.org/wiki/The_Quad_with_DrawElements and the other Quad-parts
  * getting started: http://www.lwjgl.org/guide
  * http://hg.l33tlabs.org/twl/file/tip/src/de/matthiasmann/twl/utils/PNGDecoder.java
  * 
  * TinySound:
  * https://github.com/finnkuusisto/TinySound
  * Sounds:
  * https://www.freesound.org/
  */

package simplePrimitives;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import simplePrimitives.GameUtils.Sides;
import simplePrimitives.GameUtils.SoundType;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
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
    
    // Moving variables
    private int projectionMatrixLocation = 0;
    private int viewMatrixLocation = 0;
    public int modelMatrixLocation = 0;
    private int projectionMatrixLocationNormals = 0; // separate one for normals
    private int viewMatrixLocationNormals = 0;// separate one for normals
    private int modelMatrixLocationNormals = 0;// separate one for normals
    private Matrix4 projectionMatrix = null;
    private Matrix4 viewMatrix = null;
    private Matrix4 modelMatrix = null;
    private Vec3 modelAngle = new Vec3(0,0,0);
    private Vec3 cameraPos = new Vec3(0,0,-3.3f); //Kamera-Position
    private float deltaRot = 2.5f;
    
    // toggles & interactions
    private boolean showMesh = true;
    private boolean showNormals = false;
    private boolean useBackfaceCulling = true;
    private int useNormalColoring = 0;
    private int useNormalColoringLocation = 0;
    private int useTexture = 1;
    private int useTextureLocation = 0;
  	
    //Game
    Paddle paddleFront;
    Paddle paddleBack;
    Wall wallRight;
    Wall wallLeft;
    Wall wallTop;
    Wall wallBot;
    Ball ball;    
    boolean aiOnly = false;
	private Planar GameOver;
	private Planar Point;
	private Planar Player1;
	private Planar Player2;
	private int point;
    
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
            	
            	
            	//Reset
            	if (key == GLFW_KEY_R && action == GLFW_PRESS){
            		modelAngle.y = 0;
            		initGame();
            		GameUtils.state = GameUtils.GameState.BeforeStart;
            	}
            	
            	
            	//Leben hinzufügen
            	if (key == GLFW_KEY_Q && action == GLFW_PRESS){
            		GameUtils.setLives((GameUtils.getLives()+1)%4);
            	}
            	
            	if (key == GLFW_KEY_A && action == GLFW_PRESS){
            		aiOnly ^= true;
            	}
            	
            	//Spiel pausieren
            	if (key == GLFW_KEY_P && action == GLFW_PRESS){
            		if (GameUtils.state == GameUtils.GameState.Running){
            			GameUtils.state = GameUtils.GameState.Paused;
            		}
            		else if (GameUtils.state == GameUtils.GameState.Paused){
            			GameUtils.state = GameUtils.GameState.Running;
            		}
            	}
            	
            	//Spiel starten, falls möglich
            	if (key == GLFW_KEY_ENTER && action == GLFW_PRESS){
            		if(GameUtils.state == GameUtils.GameState.BeforeStart){
            			initGame();
            			GameUtils.state = GameUtils.GameState.Running;
            			startGame();
            		}
            		if (GameUtils.state == GameUtils.GameState.AfterPoint){
            			GameUtils.state = GameUtils.GameState.Running;
            			startGame();
            		}
            	}
            	
            	//level hinzufügen
            	if (key == GLFW_KEY_E && action == GLFW_PRESS){
            		GameUtils.setLevel((GameUtils.getLevel()+1)%11);
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
        GL11.glClearColor(0.5f, 0.7f, 0.7f, 1.0f);
        
        // Switch to wireframe
        glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
        // -> back to solid faces: glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
 
        // Backface culling: Shows, if the triangles are correctly defined
        // glDisable(GL_CULL_FACE);
        
		// Draw thicker lines

        GL11.glLineWidth(2);
        // Back Culling standard an
        glEnable(GL_CULL_FACE);
        // Transparency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        //GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        
	
	//Load TinySound
    TinySound.init();
    //Load Sounds
    GameUtils.bg = new BGMusicThread(TinySound.loadMusic(new File("assets/bg.wav")));
	GameUtils.bg.run();
	
	Vec3 hPos = new Vec3(1,1.2,1);
	System.out.println("INIT");
	for (int i=0;i<3;i++){
		GameUtils.hearts[i] = new Heart(hPos);
		hPos.x -= 0.125;
	}
	
	hPos.x -=.5;
	
	for (int i=0;i<10;i++){
		GameUtils.lvlGui[i] = new Skull(hPos);
		hPos.x -=0.125;
	}
	
	 
	
	
	// Initialize Sound Board
	String[] sounds = {"paddle1.wav", "paddle2.wav", "paddle3.wav", "paddle4.wav", "paddle5.wav", 
			"sides1.wav", "sides2.wav", "sides3.wav", "sides4.wav", 
			"KIPoint.wav", "KIPoint2.wav"};
	SoundType[] context = {SoundType.PaddleCol, SoundType.PaddleCol, SoundType.PaddleCol, SoundType.PaddleCol, SoundType.PaddleCol, 
			SoundType.WallCol, SoundType.WallCol, SoundType.WallCol, SoundType.WallCol, 
			SoundType.Point, SoundType.Point};
	GameUtils.initSoundBoard(sounds, context);	
    }
    
    
    private void setupMatrices() {
     	
    	// Setup projection and view matrix
    	projectionMatrix = new PerspectiveMatrix(GameUtils.left,GameUtils.right,GameUtils.bottom,GameUtils.top,GameUtils.near,GameUtils.far);
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
            System.out.println("ERROR - Could not create the shaders: "+errorCheckValue);
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
    
    
    private void initGame(){
    	paddleFront = new Paddle(new Vec3(.5,.5,1),false); //nach oben rechts verschoben
    	paddleBack = new Paddle(new Vec3(0,0,-1),true);
    	//Rechte Wand, beginnt vorne unten rechts, 2 Breit, 2 Tief
    	wallRight = new Wall(new Vec3(1,-1,-1),Sides.right,2f,2f);
    	wallLeft = new Wall(new Vec3(-1,-1,-1),Sides.left,2f,2f);
    	wallTop = new Wall(new Vec3(-1,1,-1),Sides.top,2f,2f);
    	wallBot = new Wall(new Vec3(-1,-1,-1),Sides.bottom,2f,2f);
    	// Ball
    	ball = new Ball(new Vec3(0, 0, 1-Ball.r-0.005d));
    	GameOver = new Planar( new Vec3(0,0,1d), new Vec3(1.5,1.5,0), "assets/GameOver.png");
    	Point = new Planar( new Vec3(0.3, 0, 1), new Vec3(1,1,0), "assets/point.png");
    	Player1 = new Planar( new Vec3(-0.3, 0, 1), new Vec3(1,1,0), "assets/player1.png");
    	Player2 = new Planar( new Vec3(-0.3, 0, 1), new Vec3(1,1,0), "assets/player2.png");
    	GameUtils.setLevel(1);
    	GameUtils.setLives(3);
    }
    
    /**
     * Überprüft aktuelle Spielsituation und reagiert dementsprechent
     */
    private void checkState(){
    	if (GameUtils.state == GameUtils.GameState.PointPC){
    		GameUtils.setLives(GameUtils.getLives()-1); //Loose 1 live
    		point = 1;
    		GameUtils.state = GameUtils.state.AfterPoint;
    	}
    	if (GameUtils.state == GameUtils.GameState.PointPlayer){
    		GameUtils.setLevel(GameUtils.getLevel()+1); // Level up!
    		//Reset Ball position.
    		ball.setPos(new Vec3(0, 0, 1-Ball.r-0.005d));
    		ball.setSpin(new Vec3(0,0,0));
    		point = 0;
    		GameUtils.state = GameUtils.state.AfterPoint;
    	}
		if (GameUtils.getLives()<1){ //Gameover
			GameUtils.isLost = true;
			GameUtils.state = GameUtils.state.BeforeStart;
		}
    }
    
    private void startGame(){
    	System.out.println("StartGame!");
    	ball.setDirection(new Vec3(0,0,-1d/GameUtils.fps) );
    	//Ball "Abschießen"
    	//--> Startdirection auf Kugel tun
    }
    
    
    private void loop() throws Exception {
    	
    	initGame();
        
    	double lastLoopTime = getTime();
    	float delta;
    	float timeCount=0;
    	int fpsCount = 0;
        while ( glfwWindowShouldClose(window) == GL_FALSE ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        	GameUtils.time = getTime();
        	delta = (float) (GameUtils.time-lastLoopTime);
        	lastLoopTime=GameUtils.time;
        	timeCount += delta;
        	fpsCount++;
        	if(timeCount > 1f){
        		GameUtils.fps = fpsCount;
        		fpsCount=0;
        		timeCount -=1f;
        		System.out.println(GameUtils.fps+" State? "+GameUtils.state);
        	}
        	
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
            float worldX = -1*GameUtils.mousetoWorld(xpos, WIDTH);
            //*-1 weil fenster koordinaten oben links beginnen
            float worldY = -1*GameUtils.mousetoWorld(ypos, HEIGHT);
            //System.out.println("X:"+xpos+", Y:"+ypos+" => XW="+worldX+", YW:"+worldY);
            
            float px = paddleFront.size[0]*.5f;
            float py = paddleFront.size[1]*.5f;
            worldX = worldX>=-1+px ? worldX : -1+px;
            worldX = worldX<=1-px ? worldX : 1-px;
            worldY = worldY>=-1+py ? worldY : -1+py;
            worldY = worldY<=1-py ? worldY : 1-py;
            //==================================Objekte updaten=================================

            if (GameUtils.state != GameUtils.GameState.Paused){
            	
            	if (!aiOnly)
	        	paddleFront.setPos(worldX, worldY);		//Eigener Schläger
	            
	        	if ((GameUtils.state == GameUtils.state.Running)){
	        		if (aiOnly)
	        			paddleFront.AI_Act(ball);
	            	paddleBack.AI_Act(ball);
	            	ball.update(paddleFront, paddleBack);	//kugel
	            } else {
	            	// spawn ball at player paddle without spin
	            	double tz = paddleFront.getPos().z;
	            	double z = 1 - (Ball.r+0.005d);
	            	ball.setPos(new Vec3(worldX, worldY, z));
	            	ball.setSpin(new Vec3(0,0,0));
	            }
	            checkState();
	            
	            GameUtils.adjustAI();
	            GameUtils.adjustSpeed(ball);
	            
	            wallTop.update(ball, GameUtils.state);
	            wallLeft.update(ball, GameUtils.state);
	            wallRight.update(ball, GameUtils.state);
	            wallBot.update(ball, GameUtils.state);
        	
            }
            
            if (GameUtils.isLost) {
            //	GameOver.draw(pID);
            }
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
            
            Vec3 rot = ball.getRot();
            Vec3 pos = ball.getPos();
            modelMatrix = (Matrix4) new RotationMatrix(-rot.x, mat.Axis.Y);
            modelMatrix = (Matrix4) new RotationMatrix(-rot.y, mat.Axis.X).mul(modelMatrix);
            modelMatrix = (Matrix4) new TranslationMatrix(new Vec3(pos.x,pos.y,1+pos.z)).mul(modelMatrix);  // translate...
            modelMatrix = (Matrix4) new RotationMatrix(modelAngle.y, mat.Axis.Y).mul(modelMatrix); // ... and rotate, multiply matrices 
            GL20.glUseProgram(pId);
            GL20.glUniformMatrix4fv(modelMatrixLocation, false, toFFB(modelMatrix));
            GL20.glUseProgram(0);
            
            ball.draw(pId);
            
            // ball �ndert modelMatrix -> reset;
            modelMatrix = new TranslationMatrix(new Vec3(0,0,1));  // translate...
            modelMatrix = (Matrix4) new RotationMatrix(modelAngle.y, mat.Axis.Y).mul(modelMatrix); // ... and rotate, multiply matrices 

            GL20.glUseProgram(pId);
            GL20.glUniformMatrix4fv(modelMatrixLocation, false, toFFB(modelMatrix));
            GL20.glUseProgram(0);
            
            paddleFront.draw(pId);
            GameUtils.drawHearts(pId);
            GameUtils.drawLevel(pId);
            if (GameUtils.state == GameUtils.state.AfterPoint) {
            	Point.draw(pId);
            	if (point == 0) Player1.draw(pId);
            	if (point == 1) Player2.draw(pId);
            }
            if (GameUtils.getLives()<1 && GameUtils.state == GameUtils.state.BeforeStart) {
            	GameOver.draw(pId);
            }
            //===============ENDE=========================
    	    glfwSwapBuffers(window);
            glfwPollEvents();
        }
        GameUtils.bg.interrupt();
        TinySound.shutdown();
        //AL.destroy();
    }
    
    /**
     * Converts a Matrix4 to a flipped float buffer
     * @param m
     * @return
     */
	public FloatBuffer toFFB(Matrix4 m){
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
