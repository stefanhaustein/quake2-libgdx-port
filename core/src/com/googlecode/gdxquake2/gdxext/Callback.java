package com.googlecode.gdxquake2.gdxext;

public interface Callback<T> {
    void onSuccess(T result);

    void onFailure(Throwable cause);
}
