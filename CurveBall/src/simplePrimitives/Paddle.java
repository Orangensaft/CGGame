package simplePrimitives;

import mat.Vec3;


/**
 * Klasse f�r die Schl�ger
 * @author Nicolas
 *
 */
public class Paddle {
	private Vec3 pos; //Position
	private float[] size = {.5f,.5f}; //Gr��e des Schl�gers
	
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
		 * Schl�ger berechnen und in Grafikkarte werfen
		 */
	}
	
	public Vec3 getPos(){
		return this.pos;
	}
	
	public float[] getSize(){
		return this.size;
	}
}
