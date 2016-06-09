package com.googlecode.playnquake.core.tools;

public interface Callback<T> {
    void onSuccess(T result);

    void onFailure(Throwable cause);
}
