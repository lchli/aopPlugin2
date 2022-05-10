package com.lch.aop.plugin

object LogUtil {
    fun log(msg: String) {
        print("${ApmPluginConfig.PLUGIN_EXTEND_NAME}>>>>>>$msg\n")
    }
}