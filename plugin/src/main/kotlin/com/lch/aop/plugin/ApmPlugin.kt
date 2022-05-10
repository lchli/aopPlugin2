package com.lch.aop.plugin

import com.android.build.api.instrumentation.*
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor

class ApmPlugin : Plugin<Project> {

    companion object{
        lateinit var gApmPluginExtension: ApmPluginExtension

    }

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            ApmPluginConfig.PLUGIN_EXTEND_NAME,
            ApmPluginExtension::class.java
        )
        gApmPluginExtension=extension

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            doOnVariant(variant, extension)
        }
    }

    private fun doOnVariant(variant: Variant, extension: ApmPluginExtension) {
        if (!extension.isOpenAop) {
            LogUtil.log("插件开关未打开")
            return
        }
        val scope =
            if (extension.isAopAllProject) InstrumentationScope.ALL else InstrumentationScope.PROJECT

        LogUtil.log("aop scope:${scope.name}")

        variant.transformClassesWith(
            ApmClassVisitorFactory::class.java,
            scope
        ) {
        }

        variant.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
    }


    abstract class ApmClassVisitorFactory : AsmClassVisitorFactory<ApmParams> {

        override fun createClassVisitor(
            classContext: ClassContext,
            nextClassVisitor: ClassVisitor
        ): ClassVisitor {
            return ApmClassVisitor(
                nextClassVisitor,
                gApmPluginExtension
            )
        }

        override fun isInstrumentable(classData: ClassData): Boolean {
            if (ApmPluginConfig.isIgnoredByPlugin(classData.className)) {
                //LogUtil.log("被插件忽略的类:${classData.className}")
                return false
            }

            if (!isAcceptByUser(classData)) {
                //LogUtil.log("被用户忽略的类:${classData.className}")
                return false
            }

            return true
        }

        private fun isAcceptByUser(classData: ClassData): Boolean {
            if (gApmPluginExtension.isAopClass == null) {
                return true
            }
            return gApmPluginExtension.isAopClass.invoke(classData.className)
        }
    }
}
