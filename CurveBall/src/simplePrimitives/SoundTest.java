package simplePrimitives;

import java.io.File;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

public class SoundTest {
	public static void main(String[] args){
				TinySound.init();
				Music song = TinySound.loadMusic(new File("assets/bg.wav"));
				Sound coin = TinySound.loadSound(new File("assets/can.wav"));
				song.play(true);
				for (int i = 0; i < 20; i++) {
					coin.play();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
				}
				TinySound.shutdown();
	}

}