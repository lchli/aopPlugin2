package com.lch.aop.plugin

/**
 * 作者：simpleli on 2022/5/8 23:10
 * 邮箱：lchli@mexc.com
 */
object ApmPluginConfig {
    const val PLUGIN_EXTEND_NAME="mexcApm"

    fun isIgnoredByPlugin(className:String):Boolean{
        return false
    }

    fun log(msg:String){
        print("$PLUGIN_EXTEND_NAME>>>>>>$msg\n")
    }
}