package com.lch.aop.plugin;

import com.android.build.api.artifact.MultipleArtifact

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFiles
import org.gradle.internal.impldep.com.google.common.io.Files.isFile
import java.io.File
import java.io.FileInputStream

abstract class ModifyClassesTask: DefaultTask() {

    @get:InputFiles
    abstract val allClasses: ListProperty<RegularFile>

    @get:OutputFiles
    abstract val output: RegularFileProperty

    @TaskAction
    fun taskAction() {

        //val pool = ClassPool(ClassPool.getDefault())

        println("allClasses : ${allClasses}")

        allClasses.get().forEach { directory ->
            println("Directory : ${directory.asFile.absolutePath}")

//            directory.asFile.walk().filter(File::isFile).forEach { file ->
//                println("File : ${file.absolutePath}")
//               if (file.name == "SomeSource.class") {
//
//                    println("File : ${file.absolutePath}")
//                    val interfaceClass = pool.makeInterface("com.android.api.tests.SomeInterface");
//                    println("Adding $interfaceClass")
//                    interfaceClass.writeFile(output.get().asFile.absolutePath)
//                    FileInputStream(file).use {
//                        val ctClass = pool.makeClass(it);
//                        ctClass.addInterface(interfaceClass)
//                        val m = ctClass.getDeclaredMethod("toString");
//                        if (m != null) {
//                            m.insertBefore("{ System.out.println(\"Some Extensive Tracing\"); }");
//                        }
//                        ctClass.writeFile(output.get().asFile.absolutePath)
//                    }
                //}
            //}
        }
    }
}