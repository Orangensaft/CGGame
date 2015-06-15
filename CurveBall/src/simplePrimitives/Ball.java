package simplePrimitives;

import simplePrimitives.GameUtils.Sides;
import mat.Vec3;

/**
 * Ball-Klasse
 * Steuert Logik des Balls (Flugbahn etc.)
 * @author Nicolas
 *
 */
public class Ball {
	
	private Vec3 direction; //Bewegungsrichtung
	private Vec3 pos; //Position
	private float r=1f/16f; //Radius des Balls
	
	/**
	 * Ball erstellen
	 * @param pos ZENTRUM des Balls
	 */
	public Ball(Vec3 pos){
		this.pos=pos;
		this.direction = new Vec3(0,0,0);
	}
	
	/**
	 * Nächste Position des Balls berechnen
	 */
	public void update(Paddle a, Paddle b){
		updateDirections(a,b);	
	}
	
	/**
	 * Flugbahn aktualisieren
	 */
	private void updateDirections(Paddle a, Paddle b){
		//Kollisionsabfrage
		//aktuell noch extrem simpel mit 45° Winkeln
		Sides col = checkCols();
		if (col == Sides.left || col==Sides.right){
			direction.x = -direction.x;
		}
		if (col == Sides.top || col == Sides.bottom){
			direction.y = -direction.y;
		}
		if (hitsPaddles(a,b)){
			direction.z = -direction.z;
		}
	}
	
	/**
	 * Testen ob Ball Wände berührt
	 */
	private Sides checkCols(){
		if (pos.x <= GameUtils.left)
				return Sides.left;
		if (pos.x >= GameUtils.right)
				return Sides.right;
		if (pos.y >= GameUtils.top)
				return Sides.top;
		if (pos.y <= GameUtils.bottom)
				return Sides.bottom;
		return Sides.none;
	}
	
	/**
	 * Testet, ob Ball einen der Schläger berührt
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
				if(posA.z >= pos.z-r && posA.z<=pos.z+r){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Ball Zeichen
	 */
	private void draw(){
		/* TODO:
		 * Ball erstellen, texturieren, in Grafikkarte werfen
		 */
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
