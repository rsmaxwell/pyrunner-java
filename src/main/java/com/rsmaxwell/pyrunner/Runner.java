package com.rsmaxwell.pyrunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.rsmaxwell.pyrunner.StreamReader.Operation;

public class Runner implements AutoCloseable, RunnerObserver {

    public RunnerAsync asyncClient;
    private Set<RunnerLogger> loggers;

    // *****************************************************************************
    // * Basic
    // *****************************************************************************
    public Runner() throws Exception {
        asyncClient = new RunnerAsync();
        asyncClient.attachObserver(this);
        loggers = new HashSet<RunnerLogger>();
    }

    public boolean attachLogger(RunnerLogger logger) throws RunnerException {
        return loggers.add(logger);
    }

    public boolean detachLogger(RunnerLogger logger) {
        return loggers.remove(logger);
    }

    private void log(List<String> lines) {
        for (RunnerLogger logger : loggers) {
            logger.log(lines);
        }
    }

    private void log(String line) {
        List<String> lines = new ArrayList<String>();
        lines.add(line);
        log(lines);
    }

    // *****************************************************************************
    // * Observer
    // *****************************************************************************
    @Override
    public void notify(Operation operation) {

        if (operation == Operation.stderr) {
            for (String line : asyncClient.errors()) {
                asyncClient.postResponseItem(line);
            }
        } else if (operation == Operation.stdout) {
            for (String line : asyncClient.read()) {
                log("python: " + line);
            }
        } else {
            // logger
            for (String line : asyncClient.readLog()) {
                log(line);
            }
        }
    }

    // *****************************************************************************
    // * Helpers
    // *****************************************************************************
    public void createArray(String field) throws RunnerException, IOException, InterruptedException {
        String token = asyncClient.createArray(field);
        log("Runner.CreateArray: token: " + token);

        asyncClient.waitForResponse(token);
        log("Runner.CreateArray: exit");
    }

    public void extendArray(String field, List<Double> list) throws RunnerException, IOException, InterruptedException {
        String token = asyncClient.extendArray(field, list);
        log("Runner.ExtendArray: token: " + token);

        asyncClient.waitForResponse(token);
        log("Runner.ExtendArray: exit");
    }

    public void runPythonFunction(String pythonFunction) throws RunnerException, IOException, InterruptedException {
        String token = asyncClient.runPythonFunction(pythonFunction);
        log("Runner.RunPythonFunction: token: " + token);

        asyncClient.waitForResponse(token);
        log("Runner.RunPythonFunction: exit");
    }

    public Result getResult() throws RunnerException, IOException, InterruptedException {
        String token = asyncClient.getResult();
        log("Runner.GetResult: token: " + token);

        JSONObject jObject = asyncClient.waitForResponse(token);
        Result result = asyncClient.handleResponseGetResult(jObject);
        log("Runner.GetResult: exit");
        return result;
    }

    @Override
    public void close() throws Exception {
        String token = asyncClient.close();
        log("Runner.close: token: " + token);

        asyncClient.waitForResponse(token);
        log("Runner.close: exit");
    }
}
