package com.zegocloud.demo.bestpractice.internal.sdk.basic;

import androidx.annotation.NonNull;

public abstract class MergeCallBack<T, U> {

    private T t;
    private U u;

    public void setResult1(@NonNull T t) {
        this.t = t;
        if (u != null) {
            onResult(t, u);
            t = null;
            u = null;
        }
    }

    public void setResult2(@NonNull U u) {
        this.u = u;
        if (t != null) {
            onResult(t, u);
            t = null;
            u = null;
        }
    }

    public abstract void onResult(T t, U u);

}
