package com.dsvoronin.grindfm;

interface IGrindPlayer {

    String getInfo();

    boolean playing();

    void startAudio();

    void stopAudio();

}