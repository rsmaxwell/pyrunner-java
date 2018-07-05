
package com.rsmaxwell.pyrunner;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread that reads the process output. Extends class Thread, so InputThread instances need to be explicitly started using start()
 */
public class StreamReader extends Thread {

    static final int CarriageReturn = 13;
    static final int LineFeed = 10;

    public enum Operation {
        stdout, stderr, logger;
    }

    private InputStream stream;
    private List<String> lines;
    private List<RunnerObserver> observers;
    private Operation operation;

    /**
     * @param outputStream
     * @param inputStream
     */
    public StreamReader(final InputStream stream, final Operation operation, final List<RunnerObserver> observers) {
        this.stream = stream;
        this.observers = observers;
        this.operation = operation;
        this.lines = new ArrayList<String>();
    }

    public synchronized List<String> read() {
        List<String> temp = lines;
        lines = new ArrayList<String>();
        return temp;
    }

    public synchronized void update(byte[] bytes) throws UnsupportedEncodingException {
        String line = new String(bytes, "UTF-8");
        lines.add(line);
        notifyObservers();
    }

    private void notifyObservers() {
        for (RunnerObserver observer : observers) {
            observer.notify(operation);
        }
    }

    /**
    * 
    */
    @Override
    public void run() {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int inputByte;
            while ((inputByte = stream.read()) != -1) {

                switch (inputByte) {
                case CarriageReturn:
                    update(buffer.toByteArray());
                    buffer.reset();
                    break;

                case LineFeed:
                    break;

                default:
                    buffer.write((byte) inputByte);
                }
            }

        } catch (final Exception exception) {
            for (RunnerObserver observer : observers) {
                observer.notify(Operation.logger);
            }
        }
    }
}
