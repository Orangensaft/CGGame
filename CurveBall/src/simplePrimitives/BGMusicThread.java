package simplePrimitives;

import kuusisto.tinysound.Music;

public class BGMusicThread extends Thread{
	Music playback;
	BGMusicThread(Music playback) {
		this.playback = playback;
	}

	public void run() {
		playback.setLoop(true);
		playback.play(true);
	}
	public void end() {
		playback.play(false);
	}
}
