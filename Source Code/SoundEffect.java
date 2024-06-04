package assPart3;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundEffect 
{
	private Clip _clip;
	
	/**
	 * Used to instantiate playable SoundEffect objects
	 * @param path Path (relative to resources folder)
	 */
	public SoundEffect(String path)
	{
		try 
		{
			_clip = AudioSystem.getClip();
			_clip.open(AudioSystem.getAudioInputStream(getClass().getResource(path)));
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Play sound once 
	 */
	public void play()
	{
		_clip.setMicrosecondPosition(0);
		_clip.start();
	}
	/**
	 * Play sound on a continuous loop
	 */
	public void loop()
	{
		_clip.setMicrosecondPosition(0);
		_clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	/**
	 * Stop sound playing
	 */
	public void stop()
	{
		_clip.stop();
	}
}
