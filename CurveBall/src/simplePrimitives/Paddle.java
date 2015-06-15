package simplePrimitives;

import mat.Vec3;


/**
 * Klasse f�r die Schl�ger
 * @author Nicolas
 *
 */
public class Paddle {
	private Vec3 pos; //Position
	private float[] size = {.5f,.5f}; //Gr��e des Schl�gers; Breite/H�he
	
	/**
	 * Schl�ger erstellen
	 * @param pos ZENTRUM des Schl�gers
	 */
	public Paddle(Vec3 pos){
		this.pos = pos;
		draw();
	}
	
	/**
	 * Schl�ger zeichen
	 */
	private void draw(){
		/*
		 * TODO:
		 * Vertices in Grafikkarte werfen
		 */
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
		
	}
	
	public Vec3 getPos(){
		return this.pos;
	}
	
	public float[] getSize(){
		return this.size;
	}
}
