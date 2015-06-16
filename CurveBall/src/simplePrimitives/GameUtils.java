package simplePrimitives;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.util.ResourceLoader;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;


/**
 * Diverse globale Methoden/Variablen
 * @author Nicolas
 * @author Jan
 *
 */
public abstract class GameUtils {
	
	public static enum Sides {left,right,top,bottom,none};

	public static float normalScale=0.2f;
	//View-Volume
	public static float near=-1f;
	public static float far=1f;
	public static float left=-1f;
	public static float right=1f;
	public static float bottom=1f;
	public static float top=-1f;
	public static float fps;
	public static Audio waveEffect;

	public static enum SoundType {PaddleCol, WallCol, dummy};
	private static int types = SoundType.values().length;
	public static Audio[] SoundBoard;
	public static SoundType[] SoundContext;
	public static int[] SoundTypeCounts;
	public static Audio Request;
	public static Audio Playing;
	public static boolean played;
	public static Random rand = new Random();
	public static float mousetoWorld(double mousePos,int max){
		return (float) ((mousePos*1f/(max/2f))-1);
	}
	
	
	 /**
     * Uses an external class to load a PNG image and bind it as texture
     * @param filename
     * @param textureUnit
     * @return textureID
     */
    public static int loadPNGTexture(String filename, int textureUnit) {
        ByteBuffer buf = null;
        int tWidth = 0;
        int tHeight = 0;
         
        try {
            // Open the PNG file as an InputStream
            InputStream in = new FileInputStream(filename);
            // Link the PNG decoder to this stream
            PNGDecoder decoder = new PNGDecoder(in);
             
            // Get the width and height of the texture
            tWidth = decoder.getWidth();
            tHeight = decoder.getHeight();
             
             
            // Decode the PNG file in a ByteBuffer
            buf = ByteBuffer.allocateDirect(
                    4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
            buf.flip();
             
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
         
        // Create a new texture object in memory and bind it
        int texId = GL11.glGenTextures();
        GL13.glActiveTexture(textureUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
         
        // All RGB bytes are aligned to each other and each component is 1 byte
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
         
        // Upload the texture data and generate mip maps (for scaling)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, tWidth, tHeight, 0, 
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
         
        // Setup the ST coordinate system
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
         
        // Setup what to do when the texture has to be scaled
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 
                GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, 
                GL11.GL_LINEAR_MIPMAP_LINEAR);

        return texId;
    }
	/**
	* Create a SoundBoard from a list of filepaths and a list of contexts.
	* context array has one entry for each filepath, setting on wich context the file should be played
	* dummy sound context is a fallback in case a Sound could not be loaded
	* @return number of dummy Sounds 0 means everything worked fine 
	* ps: Kommentiere sonst immer englisch ist Gewohnheit =)
	*/
	 public static int initSoundBoard(String[] src, SoundType[] context ) {
		assert(src.length == context.length) : "Need one context per sound";
		SoundTypeCounts = new int[types]; // Count how many Sounds for each Context exist
		SoundBoard = new Audio[context.length];
		SoundContext = new SoundType[context.length];
		// init Contexts to dummy
		for (int i=0; i < context.length; i++) {
			SoundContext[i] = SoundType.dummy;
		}
		SoundTypeCounts[context.length - 1] = context.length;
		int n = 0;
		for (int i=0; i < context.length; i++) {
			try {
				SoundBoard[n] = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("assets/"+src[i]));
				SoundContext[n] = context[i];
				SoundTypeCounts[context[i].ordinal()]++;
				SoundTypeCounts[context.length - 1]--;
				n++;
			}catch(Exception ex){
				System.out.printf("Failed to load Audio Resource assets/%s", src[i]);
			}
		}
		return SoundTypeCounts[context.length - 1];
	}

	/**
	 * Returns a random Sound for a given context
	 */
	public static void requestSound(SoundType stype) {
		int rint = rand.nextInt(SoundTypeCounts[stype.ordinal()]);
		int i = 0;
		int n = 0;
		while (n < SoundContext.length) {
			if (SoundContext[n] == stype) {
				if (i == rint) {
					Request = SoundBoard[n];
					break;
				}
				i++;
			}
		}
	}
}
