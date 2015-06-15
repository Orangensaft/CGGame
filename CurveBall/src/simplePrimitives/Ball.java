package simplePrimitives;

import simplePrimitives.GameUtils.Sides;
import mat.Axis;
import mat.Vec3;

/**
 * Ball-Klasse
 * Steuert Logik des Balls (Flugbahn etc.)
 * @author Nicolas
 * @author Jan
 *
 */
public class Ball {
	
	private Vec3 direction; //Bewegungsrichtung
	private Vec3 pos; //Position
	private Vec3 spin; // Drehung
	private Vec3 spinStep; // Drehung inkrement
	private float rotX, rotY; // Müssen X und Y rotiert werden?
	private float r=1f/16f; //Radius des Balls
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
	}
	
	/**
	 * Nächste Position des Balls berechnen
	 */
	public void update(Paddle a, Paddle b){
		pos.x += direction.x;
		if (Math.abs(spin.x) > spinStep.x){
			rotX += Math.signum(spin.x)*spinStep.x;
			rotX %= 2*Math.PI;
			pos.x += Math.signum(spin.x)*spinStep.x;
			spin.x -= Math.signum(spin.x)*spinStep.x;
		}
		pos.y += direction.y;  		
		if (Math.abs(spin.y) > spinStep.y){
			rotY += Math.signum(spin.y)*spinStep.y;
			rotY %= 2*Math.PI;
			pos.y += Math.signum(spin.y)*spinStep.y;
			spin.y -= Math.signum(spin.y)*spinStep.y;
		}
		updateDirections(a,b);
	}
	
	/**
	 * Flugbahn aktualisieren
	 */
	private void updateDirections(Paddle a, Paddle b){
		//Kollisionsabfrage
		Sides col = checkCols();
		if (col == Sides.left || col==Sides.right){
			direction.x = -direction.x;
			spin.x = -spin.x;
			pos.x = col == Sides.left ? GameUtils.left + r : GameUtils.right - r;
		}
		if (col == Sides.top || col == Sides.bottom){
			direction.y = -direction.y;
			spin.y = - spin.y;
			pos.y = col == Sides.top ? GameUtils.top - r : GameUtils.bottom + r; 
		}
		if (hitsPaddles(a,b)){
			direction.z = -direction.z;

		}
	}
	
	/**
	 * Testen ob Ball Wände berährt
	 */
	private Sides checkCols(){
		if (pos.x - r <= GameUtils.left)
				return Sides.left;
		if (pos.x + r >= GameUtils.right)
				return Sides.right;
		if (pos.y + r >= GameUtils.top)
				return Sides.top;
		if (pos.y - r <= GameUtils.bottom)
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
				if(posA.z >= pos.z-r && posA.z<=pos.z+r){
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
	public void draw(int pID){
		
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
