package com.rsmaxwell.pyrunner;

import com.rsmaxwell.pyrunner.StreamReader.Operation;

public interface RunnerObserver {

    void notify(Operation operation);

}
