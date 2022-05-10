package com.lch.aop.plugin;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;

public class ApmPluginExtension {
    public boolean isOpenAop = true;
    public boolean isAopAllProject = true;
    public Function1<String, Boolean> isAopClass;
    public Function3<String, String, String, Boolean> isAopMethod;

}
