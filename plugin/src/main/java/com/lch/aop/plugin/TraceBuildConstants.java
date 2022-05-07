package com.lch.aop.plugin;

public class TraceBuildConstants {

    public final static String MATRIX_TRACE_CLASS = "com/lch/AppMethodBeat";
    public final static String MATRIX_TRACE_ON_WINDOW_FOCUS_METHOD = "onWindowFocusChanged";
    public final static String MATRIX_TRACE_ATTACH_BASE_CONTEXT = "attachBaseContext";
    public final static String MATRIX_TRACE_ATTACH_BASE_CONTEXT_ARGS = "(Landroid/content/Context;)V";
    public final static String MATRIX_TRACE_APPLICATION_ON_CREATE = "onCreate";
    public final static String MATRIX_TRACE_APPLICATION_ON_CREATE_ARGS = "()V";
    public final static String MATRIX_TRACE_ACTIVITY_CLASS = "android/app/Activity";
    public final static String MATRIX_TRACE_V7_ACTIVITY_CLASS = "android/support/v7/app/AppCompatActivity";
    public final static String MATRIX_TRACE_V4_ACTIVITY_CLASS = "android/support/v4/app/FragmentActivity";
    public final static String MATRIX_TRACE_ANDROIDX_ACTIVITY_CLASS = "androidx/appcompat/app/AppCompatActivity";
    public final static String MATRIX_TRACE_APPLICATION_CLASS = "android/app/Application";
    public final static String MATRIX_TRACE_METHOD_BEAT_CLASS = "com/tencent/matrix/trace/core/AppMethodBeat";
    public final static String MATRIX_TRACE_ON_WINDOW_FOCUS_METHOD_ARGS = "(Z)V";
    public static final String[] UN_TRACE_CLASS = {"R.class", "R$", "Manifest", "BuildConfig"};
    public final static String DEFAULT_BLOCK_TRACE =
                    "[package]\n"
                    + "-keeppackage android/\n"
                    + "-keeppackage com/tencent/matrix/\n";

    private static final int METHOD_ID_MAX = 0xFFFFF;
    public static final int METHOD_ID_DISPATCH = METHOD_ID_MAX - 1;
}