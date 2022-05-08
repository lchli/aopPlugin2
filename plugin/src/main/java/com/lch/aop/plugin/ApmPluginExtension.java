package com.lch.aop.plugin;

import org.gradle.api.provider.Property;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;

/**
 * 作者：simpleli on 2022/5/9 00:29
 * 邮箱：lchli@mexc.com
 */
public class ApmPluginExtension {
    public boolean isOpenAop=true;
    public boolean isAopAllProject=true;
    public Function1<String,Boolean> isAopClass;
    public Function3<String,String,String,Boolean> isAopMethod;

}
