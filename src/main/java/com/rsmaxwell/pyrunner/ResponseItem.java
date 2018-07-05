package com.rsmaxwell.pyrunner;

import java.util.concurrent.Semaphore;

public class ResponseItem {

    public Semaphore semaphore;
    public String line;

    public ResponseItem() throws InterruptedException {
        semaphore = new Semaphore(1);
        semaphore.acquire();
    }
}
