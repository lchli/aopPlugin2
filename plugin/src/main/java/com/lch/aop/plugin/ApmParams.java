package com.lch.aop.plugin;

import com.android.build.api.instrumentation.InstrumentationParameters;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;

/**
 * 作者：simpleli on 2022/5/9 01:00
 * 邮箱：lchli@mexc.com
 */
public class ApmParams implements InstrumentationParameters {
    public Function1<String,Boolean> isAopClass;
    public Function3<String,String,String,Boolean> isAopMethod;
}
