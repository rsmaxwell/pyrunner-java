
package com.rsmaxwell.pyrunner;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Thread that reads the process output. Extends class Thread, so InputThread instances need to be
 * explicitly started using start()
 */
public class StreamWriter extends Thread {

    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();

    /**
     * @param outputStream
     * @param inputStream
     */
    public StreamWriter(final OutputStream outputStream, final InputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        setName(this.getClass().getName());
    }

    /**
    * 
    */
    @Override
    public void run() {
        try {
            int inputByte;
            while ((inputByte = inputStream.read()) != -1) {
                outputStream.write(inputByte);
                outputStream2.write(inputByte);
            }
            outputStream.flush();
            outputStream.close();

            outputStream2.flush();
            outputStream2.close();
        }
        catch (final Exception exception) {
            final PrintStream printStream = new PrintStream(outputStream);
            printStream.println(this.getClass().getName() + " caught exception: " + exception);
            exception.printStackTrace(printStream);
        }
    }

    /**
     * @return outputStream
     */
    public OutputStream getOutputStream() {
        return outputStream2;
    }
}
