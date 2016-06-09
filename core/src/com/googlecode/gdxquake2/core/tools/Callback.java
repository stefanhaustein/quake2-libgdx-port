package com.googlecode.gdxquake2.core.tools;

public interface Callback<T> {
    void onSuccess(T result);

    void onFailure(Throwable cause);
}
