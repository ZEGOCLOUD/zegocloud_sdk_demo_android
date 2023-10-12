package com.zegocloud.demo.bestpractice.internal.business.audioroom;

public class LiveAudioRoomLayoutRowConfig {

    public int count;
    public int seatSpacing;
    public LiveAudioRoomLayoutAlignment alignment;

    public LiveAudioRoomLayoutRowConfig(int count, LiveAudioRoomLayoutAlignment alignment) {
        this.count = count;
        this.alignment = alignment;
    }

    public LiveAudioRoomLayoutRowConfig(int count, int seatSpacing, LiveAudioRoomLayoutAlignment alignment) {
        this.count = count;
        this.seatSpacing = seatSpacing;
        this.alignment = alignment;
    }

    public LiveAudioRoomLayoutRowConfig() {
        this.alignment = LiveAudioRoomLayoutAlignment.SPACE_AROUND;
    }
}
