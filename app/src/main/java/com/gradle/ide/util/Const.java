package com.gradle.ide.util;

import android.os.Environment;
import java.io.File;

public class Const {

    public Const() {
        if (!PROJECT_DIR.exists()) {
            PROJECT_DIR.mkdirs();
        }
    }

    public static final File PROJECT_DIR = new File(Environment.getExternalStorageDirectory(), "GradleIDE");

}
