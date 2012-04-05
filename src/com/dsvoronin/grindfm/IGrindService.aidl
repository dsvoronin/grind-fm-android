package com.dsvoronin.grindfm;

interface IGrindService {

    String getInfo();

    boolean playing();

    void startAudio();

    void stopAudio();

}