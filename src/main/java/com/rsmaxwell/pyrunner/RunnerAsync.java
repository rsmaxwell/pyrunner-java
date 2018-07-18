package com.rsmaxwell.pyrunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rsmaxwell.pyrunner.StreamReader.Operation;

public class RunnerAsync {

    static final int CarriageReturn = 13;
    static final int LineFeed = 10;
    private static final String pythonProgramName = "pythonw.exe";
    private static final String launcherProgramName = "pyw.exe";

    private List<RunnerObserver> observers;
    private Map<String, ResponseItem> responseMap;
    private List<String> logBuffer;
    private Process process;
    private StreamReader outputReader;
    private StreamReader errorReader;
    private PrintWriter printWriter;

    public static String findExecutableOnPath(String name) {
        for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
            File file = new File(dirname, name);
            if (file.isFile() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static String findPythonExecutable() throws RunnerException {
        List<String> programs = new ArrayList<String>();

        String os = System.getProperty("os.name");
        if (os.startsWith("Windows")) {
            programs.add("pythonw.exe");
            programs.add("pyw.exe");
        } else if (os.equals("Linux")) {
            programs.add("python3");
            programs.add("python");
        } else {
            throw new RunnerException("OS not supported: " + os);
        }

        String programPath = null;
        for (String program : programs) {
            programPath = findExecutableOnPath(program);
            if (programPath != null) {
                break;
            }
        }

        if (programPath == null) {
            throw new RunnerException("Could not find Python executable on the PATH");
        }

        return programPath;
    }

    public RunnerAsync() throws Exception {

        observers = new ArrayList<RunnerObserver>();
        responseMap = new HashMap<String, ResponseItem>();
        logBuffer = new ArrayList<String>();

        // *************************************************************************
        // * Find the python program path
        // *************************************************************************
        String programPath = findPythonExecutable();
        log("programPath: " + programPath);

        // *************************************************************************
        // * Launch the python server
        // *************************************************************************
        final ProcessBuilder pb = new ProcessBuilder();

        final List<String> command = new ArrayList<String>();
        command.add(programPath);
        command.add("server.py");
        pb.command(command);
        pb.redirectInput();

        process = pb.start();

        // *************************************************************************
        // * Capture the standard streams
        // *************************************************************************
        outputReader = new StreamReader(process.getInputStream(), Operation.stdout, observers);
        outputReader.start();

        errorReader = new StreamReader(process.getErrorStream(), Operation.stderr, observers);
        errorReader.start();

        OutputStream stdin = process.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
        printWriter = new PrintWriter(writer);
    }

    public List<String> read() {
        return outputReader.read();
    }

    public List<String> errors() {
        return errorReader.read();
    }

    private String makeToken() {
        return UUID.randomUUID().toString();
    }

    public JSONObject waitForResponse(String token) throws RunnerException, InterruptedException {

        ResponseItem responseItem = responseMap.get(token);

        log("RunnerAsync.waitForResponse: waiting for: " + token);
        responseItem.semaphore.acquire();

        log("RunnerAsync.waitForResponse: continuing: " + token);
        responseMap.remove(token);
        String line = responseItem.line;
        // responseItem.Destroy();

        Object jData = new JSONObject(line);

        if (!(jData instanceof JSONObject))
            throw new RunnerException("Error: unexpected response. type = " + jData.getClass().getSimpleName());

        JSONObject jObject = (JSONObject) jData;

        if (!jObject.has("status"))
            throw new RunnerException("The \"status\" field is missing");

        jData = jObject.get("status");
        if (!(jData instanceof String))
            throw new RunnerException("Error: unexpected status type. type = " + jData.getClass().getSimpleName());

        String status = (String) jData;

        String message = null;
        if (jObject.has("message")) {

            jData = jObject.get("message");
            if (!(jData instanceof String))
                throw new RunnerException("Error: unexpected message type. type = " + jData.getClass().getSimpleName());

            message = (String) jData;
        }

        if (!status.equals("ok")) {
            if (message == null)
                throw new RunnerException(status + ": " + message);
            else
                throw new RunnerException(status + ": unexpected error");
        }

        return jObject;
    }

    public void postResponseItem(String line) {

        try {
            log("RunnerAsync.postResponseItem: " + line);

            Object jData = new JSONObject(line);

            if (jData instanceof JSONObject) {
                JSONObject jObject = (JSONObject) jData;

                String token;
                if (!jObject.has("token")) {
                    log("RunnerAsync.postResponseItem: The \"token\" field is missing");
                    return;
                } else {
                    jData = jObject.get("token");
                    if (jData instanceof String) {
                        token = (String) jData;
                    } else {
                        log("RunnerAsync.postResponseItem: Error: unexpected token type. jType = " + jData.getClass().getSimpleName());
                        return;
                    }
                }

                ResponseItem responseItem = responseMap.get(token);
                responseItem.line = line;
                responseItem.semaphore.release();
            } else {
                log("RunnerAsync.postResponseItem: Error: unexpected response. jType = " + jData.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log("RunnerAsync.postResponseItem: Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    // *****************************************************************************
    // * Observers
    // *****************************************************************************

    public void attachObserver(RunnerObserver observer) {
        observers.add(observer);
    }

    // *****************************************************************************
    // * Logging
    // *****************************************************************************

    public void log(String line) {

        synchronized (this) {
            logBuffer.add(line);
        }

        notifyObservers();
    }

    public List<String> readLog() {

        List<String> temp;

        synchronized (this) {
            temp = logBuffer;
            logBuffer = new ArrayList<String>();
        }

        return temp;
    }

    private void notifyObservers() {
        for (RunnerObserver observer : observers) {
            observer.notify(Operation.logger);
        }
    }

    // *****************************************************************************
    // * Helpers
    // *****************************************************************************

    public String createArray(String field) throws IOException, InterruptedException {

        // data["array"] = []

        String python = "data[\"" + field + "\"] = []";

        JSONObject jObject = new JSONObject();
        jObject.put("command", "run");

        JSONArray jArray = new JSONArray();
        jArray.put(python);
        jObject.put("arguments", jArray);

        String token = makeToken();
        jObject.put("token", token);

        String command = jObject.toString();
        printWriter.println(command);
        printWriter.flush();
        responseMap.put(token, new ResponseItem());
        return token;
    }

    public String extendArray(String field, List<Double> list) throws IOException, InterruptedException {

        // data["array"].extend( (11,12,13) )

        String python = "data[\"" + field + "\"].extend( (";
        String sep = "";
        for (Double value : list) {
            python = python + sep + value;
            sep = ", ";
        }
        python = python + ") )";

        JSONObject jObject = new JSONObject();
        jObject.put("command", "run");

        JSONArray jArray = new JSONArray();
        jArray.put(python);
        jObject.put("arguments", jArray);

        String token = makeToken();
        jObject.put("token", token);

        String command = jObject.toString();
        printWriter.println(command);
        printWriter.flush();
        responseMap.put(token, new ResponseItem());
        return token;
    }

    public String runPythonFunction(String pythonFunction) throws IOException, InterruptedException {

        // foobar()

        String python = pythonFunction + "()";

        JSONObject jObject = new JSONObject();
        jObject.put("command", "run");

        JSONArray jArray = new JSONArray();
        jArray.put(python);
        jObject.put("arguments", jArray);

        String token = makeToken();
        jObject.put("token", token);

        String command = jObject.toString();
        printWriter.println(command);
        printWriter.flush();
        responseMap.put(token, new ResponseItem());
        return token;
    }

    public String getResult() throws IOException, InterruptedException {

        JSONObject jObject = new JSONObject();
        jObject.put("command", "get");

        JSONArray jArray = new JSONArray();
        jArray.put("result");
        jObject.put("arguments", jArray);

        String token = makeToken();
        jObject.put("token", token);

        String command = jObject.toString();
        printWriter.println(command);
        printWriter.flush();
        responseMap.put(token, new ResponseItem());
        return token;
    }

    public String close() throws IOException, InterruptedException {

        JSONObject jObject = new JSONObject();
        jObject.put("command", "quit");

        String token = makeToken();
        jObject.put("token", token);

        String command = jObject.toString();
        printWriter.println(command);
        printWriter.flush();
        responseMap.put(token, new ResponseItem());
        return token;
    }

    // *****************************************************************************
    // * HandleResponse helpers
    // *****************************************************************************

    public Result handleResponseGetResult(JSONObject jObject) throws RunnerException {

        log("RunnerAsync.handleResponseGetResult: entry");

        if (!jObject.has("result")) {
            throw new RunnerException("The \"result\" field is missing");
        }

        Object jData = jObject.get("result");
        if (!(jData instanceof JSONObject))
            throw new RunnerException("Error: Unexpected result type. type = " + jData.getClass().getSimpleName());

        JSONObject jResult = (JSONObject) jData;

        if (!jResult.has("count")) {
            throw new RunnerException("Error:The \"result.count\" field is missing");
        }

        jData = jResult.get("count");
        if (!(jData instanceof Integer))
            throw new RunnerException("Error: Unexpected result.count type. type =" + jData.getClass().getSimpleName());

        Integer count = (Integer) jData;

        if (!jResult.has("total"))
            throw new RunnerException("Error: The \"result.total\" field is missing");

        jData = jResult.get("total");
        if (!(jData instanceof Double))
            throw new RunnerException("Error: Unexpected result.total type. type = " + jData.getClass().getSimpleName());

        Double total = (Double) jData;

        log("RunnerAsync.handleResponseGetResult: exit");

        return new Result(count, total);
    }

    public void HandleResponseClose() {
        process.destroy();
    }
}
