package com.rofs.myhome.area;

import com.rofs.myhome.audio.SoundPlayerUsingClip;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public abstract class Area {
    private final String name;
    private final SoundPlayerUsingClip soundPlayer;

    protected Area(String name) {
        this.name = name;
        soundPlayer = new SoundPlayerUsingClip();
    }

    public String getName() {
        return name;
    }

    public void playSound() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        soundPlayer.play("harvest.wav", 1);
    }

    protected static void printNotPlantable() {
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("    플레이어의 레벨이 충족되지 않아 아직 획득 할 수 없습니다.");
    }
}