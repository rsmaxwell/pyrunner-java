package com.rsmaxwell.pyrunner;

public class RunnerException extends Exception {

    private static final long serialVersionUID = -7258518788905730964L;

    public RunnerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RunnerException(String message) {
        super(message);
    }

    public RunnerException(Throwable cause) {
        super(cause);
    }
}
