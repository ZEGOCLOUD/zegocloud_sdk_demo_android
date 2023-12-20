package com.zegocloud.demo.bestpractice.internal.business.call;

import java.util.List;

public class CallInviteInfo {
    public String requestID;
    public String firstInviter;
    public String inviter;
    public List<CallInviteUser> userList;

    @Override
    public String toString() {
        return "PKBattleInfo{" + "requestID='" + requestID + '\'' + ", userList=" + userList
            + ", pkExtendedData=" + '}';
    }
}
