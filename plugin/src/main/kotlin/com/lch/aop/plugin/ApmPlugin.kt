package com.lch.aop.plugin

import com.android.build.api.instrumentation.*
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor

class ApmPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create(
            ApmPluginConfig.PLUGIN_EXTEND_NAME,
            ApmPluginExtension::class.java
        )
        if (!extension.isOpenAop) {
            LogUtil.log("插件开关未打开")
            return
        }
        val scope =
            if (extension.isAopAllProject) InstrumentationScope.ALL else InstrumentationScope.PROJECT

        LogUtil.log("aop scope:${scope.name}")

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            variant.transformClassesWith(
                ApmClassVisitorFactory::class.java,
                scope
            ) {
                it.isAopMethod = extension.isAopMethod
                it.isAopClass = extension.isAopClass
            }
            variant.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
        }
    }


    abstract class ApmClassVisitorFactory : AsmClassVisitorFactory<ApmParams> {

        override fun createClassVisitor(
            classContext: ClassContext,
            nextClassVisitor: ClassVisitor
        ): ClassVisitor {
            return ApmClassVisitor(
                nextClassVisitor,
                parameters.orNull
            )
        }

        override fun isInstrumentable(classData: ClassData): Boolean {
            if (ApmPluginConfig.isIgnoredByPlugin(classData.className)) {
                LogUtil.log("被插件忽略的类:${classData.className}")
                return false
            }

            if (!isAcceptByUser(classData)) {
                LogUtil.log("被用户忽略的类:${classData.className}")
                return false
            }

            return true
        }

        private fun isAcceptByUser(classData: ClassData): Boolean {
            val apmParams = parameters.orNull
            if (apmParams?.isAopClass == null) {
                return true
            }
            return apmParams.isAopClass.invoke(classData.className)
        }
    }
}
