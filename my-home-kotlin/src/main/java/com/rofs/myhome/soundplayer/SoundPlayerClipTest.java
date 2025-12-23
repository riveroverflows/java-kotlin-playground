package com.rofs.myhome.soundplayer;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class SoundPlayerClipTest {

    public static void main(String[] args) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        SoundPlayerUsingClip clip = new SoundPlayerUsingClip();
        clip.play("mainMusic.wav", 0);
    }
}
