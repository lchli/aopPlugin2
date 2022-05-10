package com.lch.aop.plugin

object ApmPluginConfig {
    const val PLUGIN_EXTEND_NAME = "mexcApm"

    fun isIgnoredByPlugin(className: String): Boolean {
        return false
    }

    fun log(msg: String) {
        print("$PLUGIN_EXTEND_NAME>>>>>>$msg\n")
    }
}