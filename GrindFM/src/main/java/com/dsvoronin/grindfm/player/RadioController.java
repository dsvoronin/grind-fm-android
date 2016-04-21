package com.dsvoronin.grindfm.player;

public interface RadioController {

    float DUCK_VOLUME = 0.5f;

    float FULL_VOLUME = 1.0f;

    void startRadio();

    void pauseRadio();

    void lowerVolume();
}
