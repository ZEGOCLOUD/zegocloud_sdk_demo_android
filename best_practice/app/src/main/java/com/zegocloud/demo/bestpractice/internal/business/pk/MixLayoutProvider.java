package com.zegocloud.demo.bestpractice.internal.business.pk;

import im.zego.zegoexpress.entity.ZegoMixerInput;
import im.zego.zegoexpress.entity.ZegoMixerVideoConfig;
import java.util.ArrayList;
import java.util.List;

public interface MixLayoutProvider {

    ArrayList<ZegoMixerInput> getMixVideoInputs(List<String> streamList, ZegoMixerVideoConfig videoConfig);
}
