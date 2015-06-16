package simplePrimitives;

import kuusisto.tinysound.Sound;

public class AudioThread extends Thread{
	Sound playback;
	AudioThread(Sound playback) {
		this.playback = playback;
	}

	public void run() {
		playback.play();
	}
	public void end() {
		playback.stop();
	}
}
