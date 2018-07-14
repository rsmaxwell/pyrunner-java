package com.rsmaxwell.pyrunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONObject;

public class Testcase implements RunnerLogger {

    // *****************************************************************************
    // * Simple Test !!! No error handling !!!
    // *****************************************************************************
    public void simpleTest() throws Exception {
        Runner client = new Runner();
        // client.attachLogger(this);

        client.createArray("array");

        List<Double> list = new ArrayList<Double>();
        list.add(123.456);
        list.add(456.789);
        client.extendArray("array", list);

        client.runPythonFunction("foobar");

        Result result = client.getResult();
        System.out.println("Results:");
        System.out.println("     count = " + result.getCount());
        System.out.println("     total = " + result.getTotal());

        client.close();
    }

    // *****************************************************************************
    // * Standard Test
    // *****************************************************************************
    public void standardTest() throws Exception {

        try (Runner client = new Runner()) {

            // client.attachLogger(this);

            System.out.println("Create array");
            client.createArray("array");

            int iterations = 2;
            for (int a = 0; a < iterations; a++) {
                List<Double> list = new ArrayList<Double>();

                int size = 1000;
                for (int b = 0; b < size; b++) {
                    double value = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
                    list.add(value);
                }

                System.out.println("(" + a + "):  Add " + size + " items to array");
                client.extendArray("array", list);
            }

            String functionName = "foobar";
            System.out.println("Run python function: " + functionName);
            client.runPythonFunction(functionName);

            System.out.println("Get result");
            Result result = client.getResult();

            System.out.println("Result:");
            System.out.println("     count = " + result.getCount());
            System.out.println("     total = " + result.getTotal());

        } catch (RunnerException e) {
            System.out.println(e.getMessage());
        }
    }

    // *****************************************************************************
    // * Asynchronous API Test
    // *****************************************************************************
    public void asyncTest() throws Exception {

        try (Runner client = new Runner()) {

            client.attachLogger(this);

            System.out.println("Create array");
            client.createArray("array");

            int iterations = 2;
            for (int a = 0; a < iterations; a++) {
                List<Double> list = new ArrayList<Double>();

                int size = 1000;
                for (int b = 0; b < size; b++) {
                    double value = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
                    list.add(value);
                }

                System.out.println("(" + a + "):  Add " + size + " items to array");
                client.extendArray("array", list);
            }

            String functionName = "foobar";
            System.out.println("Run python function: " + functionName);
            String token = client.asyncClient.runPythonFunction(functionName);
            System.out.println("after MyAsyncRunner.runPythonFunction: token: " + token);

            // Do other stuff here ...

            // But make sure that eventually "waitForResponse" is called, to clear the entry
            // in the "ResponseMap" ... (otherwise there will be a leak!)
            // "waitForResponse" can be called on a different thread

            System.out.println("before RunnerAsync.waitForResponse: token: " + token);
            client.asyncClient.waitForResponse(token);
            System.out.println("after RunnerAsync.waitForResponse: exit");

            System.out.println("Get result");
            token = client.asyncClient.getResult();

            // Do other stuff here ...

            JSONObject jObject = client.asyncClient.waitForResponse(token);
            Result result = client.asyncClient.handleResponseGetResult(jObject);

            System.out.println("Result:");
            System.out.println("     count = " + result.getCount());
            System.out.println("     total = " + result.getTotal());

        } catch (RunnerException e) {
            System.out.println(e.getMessage());
        }
    }

    // *****************************************************************************
    // * Performance Test With performance monitoring
    // *****************************************************************************
    public void performanceTest() throws Exception {

        System.out.println("Startup");
        long starttime = System.nanoTime();
        try (Runner client = new Runner()) {

            System.out.println("nanoseconds: " + (System.nanoTime() - starttime));
            client.attachLogger(this);

            System.out.println("Create array");
            starttime = System.nanoTime();
            client.createArray("array");
            System.out.println("nanoseconds: " + (System.nanoTime() - starttime));

            int iterations = 2;
            for (int a = 0; a < iterations; a++) {
                List<Double> list = new ArrayList<Double>();

                int size = 1000;
                for (int b = 0; b < size; b++) {
                    double value = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
                    list.add(value);
                }

                System.out.println("(" + a + "):  Add " + size + " items to array");
                starttime = System.nanoTime();
                client.extendArray("array", list);
                System.out.println("nanoseconds: " + (System.nanoTime() - starttime));
            }

            String functionName = "foobar";
            System.out.println("Run python function: " + functionName);
            starttime = System.nanoTime();
            client.runPythonFunction(functionName);
            System.out.println("nanoseconds: " + (System.nanoTime() - starttime));

            System.out.println("Get result");
            starttime = System.nanoTime();
            Result result = client.getResult();
            System.out.println("nanoseconds: " + (System.nanoTime() - starttime));

            System.out.println("Result:");
            System.out.println("     count = " + result.getCount());
            System.out.println("     total = " + result.getTotal());

        } catch (RunnerException e) {
            System.out.println(e.getMessage());
        }
    }

    // *****************************************************************************
    // * Logger
    // *****************************************************************************
    @Override
    public void log(List<String> lines) {
        for (String line : lines)
            System.out.println(line);
    }
}
