package com.text.edit;

public interface OnFileListener {
    void onRead();

    void onWrite();

    void onProgress();

    void onFinish();
}
