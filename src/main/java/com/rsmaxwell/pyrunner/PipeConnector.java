
package com.rsmaxwell.pyrunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Thread that reads the process output. Extends class Thread, so InputThread instances need to be explicitly started using start()
 */
public class PipeConnector extends Thread {

    private final OutputStream outputStream;
    private final InputStream inputStream;

    /**
     * @param outputStream
     * @param inputStream
     */
    public PipeConnector(final OutputStream outputStream, final InputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        setName(this.getClass().getName());
    }

    /**
    * 
    */
    @Override
    public void run() {

        int value;
        while (true) {
            try {
                value = inputStream.read();
                if (value == -1) {
                    break;
                }
                outputStream.write(value);
                outputStream.flush();

            } catch (final IOException ex) {
            }
        }

        try {
            outputStream.close();
        } catch (IOException error) {
        }
        try {
            inputStream.close();
        } catch (IOException error) {
        }
    }
}
