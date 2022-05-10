package com.lch.aop.plugin

object ApmPluginConfig {
    const val PLUGIN_EXTEND_NAME = "mexcApm"
    const val APM_TRACE_CLASS = "com/lch/AppMethodBeat"
    const val APM_TRACE_START_METHOD = "i"
    const val APM_TRACE_END_METHOD = "o"

    fun isIgnoredByPlugin(className: String): Boolean {
        return false
    }

}