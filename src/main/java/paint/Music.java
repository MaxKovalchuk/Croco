package paint;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Music extends Thread {
	private boolean playing;
	private File soundFile;
	protected String mainMenuTheme = "Herbie Hancock - The Eye Of The Hurricane.wav";
	protected String inGameTheme = "Kenny Burrell - Midnight Blue.wav";
	Clip clip;

	public Music() {
		soundFile = new File(mainMenuTheme);
		playing = true;
		start();
	}

	@Override
	public void run() {
		play();
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
		if (playing) {
			clip.start();
		} else {
			clip.stop();
			clip.setFramePosition(0);
		}
	}

	public void setSong(String songURL) {
		soundFile = new File(songURL);
		clip.close();
		play();
		if(!playing)
		clip.stop();
		clip.setFramePosition(0);
	}

	public void play() {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
			clip = AudioSystem.getClip();
			clip.open(ais);
			clip.setFramePosition(0);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
