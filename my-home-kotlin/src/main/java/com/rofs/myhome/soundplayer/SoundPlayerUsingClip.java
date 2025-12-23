package com.rofs.myhome.soundplayer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SoundPlayerUsingClip implements LineListener {

    private boolean isPlaybackCompleted;
    private AudioInputStream audioStream;
    private Clip audioClip;
    private Long clipTime;

    public boolean isPlaybackCompleted() {
        return isPlaybackCompleted;
    }

    @Override
    public void update(LineEvent event) {
        if (LineEvent.Type.START == event.getType()) {
//            System.out.println("Playback started.");
            isPlaybackCompleted = false;
        }
        if (LineEvent.Type.STOP == event.getType()) {
            isPlaybackCompleted = true;
//            System.out.println("Playback completed.");
        }
    }

    public void play(String audioFilePath, int count) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        String path = "audio/" + audioFilePath;
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new IllegalStateException("audio resource not found: " + path);
        }
        audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
        AudioFormat format = audioStream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);

        audioClip = (Clip) AudioSystem.getLine(info);
        audioClip.addLineListener(this);
        audioClip.open(audioStream);
        audioClip.loop(count == 0 ? Clip.LOOP_CONTINUOUSLY : count);
        audioClip.start();
    }

    public void stop() throws IOException {
        audioClip.setFramePosition(0);
        audioClip.stop();
        audioClip.close();
        audioStream.close();
    }

    public void pause() {
        clipTime = audioClip.getMicrosecondPosition();
        audioClip.stop();
    }

    public void resume() {
        audioClip.setMicrosecondPosition(clipTime);
        audioClip.start();
    }
}
