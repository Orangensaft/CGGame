package simplePrimitives;

import mat.Vec3;


/**
 * Klasse für die Schläger
 * @author Nicolas
 *
 */
public class Paddle {
	private Vec3 pos; //Position
	private float[] size = {.5f,.5f}; //Größe des Schlägers
	
	/**
	 * Schläger erstellen
	 * @param pos ZENTRUM des Schlägers
	 */
	public Paddle(Vec3 pos){
		this.pos = pos;
		draw();
	}
	
	/**
	 * Schläger zeichen
	 */
	private void draw(){
		/*
		 * TODO:
		 * Schläger berechnen und in Grafikkarte werfen
		 */
	}
	
	public Vec3 getPos(){
		return this.pos;
	}
	
	public float[] getSize(){
		return this.size;
	}
}
