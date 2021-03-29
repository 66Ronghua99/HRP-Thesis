package com.ronghua.emu_plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import org.gradle.internal.impldep.org.apache.ivy.util.FileUtil

class EmulatorTransform extends Transform{
    private Project project

    private static final Set<QualifiedContent.Scope> SCOPES = new HashSet<>();

    static {
        SCOPES.add(QualifiedContent.Scope.PROJECT);
        SCOPES.add(QualifiedContent.Scope.SUB_PROJECTS);
        SCOPES.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES);
    }

    EmulatorTransform(Project project1){
        project = project1
    }

    @Override
    String getName() {
        return "EmulatorDetection_Plugin"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        println("Enter customized transform!")
        for (TransformInput input : transformInvocation.inputs) {

            if (null == input) continue
            //go through all directories
            for (DirectoryInput directoryInput : input.directoryInputs) {
                println("Directory name:" + directoryInput.name)
                // get output directory
                def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                // copy directory to destination
                FileUtils.copyDirectory(directoryInput.file, dest)
            }


            //go through jar file, instrument my code to Emulator Detection Code
            for (JarInput jarInput : input.jarInputs) {
                // Rename the jar file
                println("Jar name:" + jarInput.name)
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                println("Jar name + md5Name:" + jarInput.name + md5Name)
                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                if(jarInput.name.equals("com.github.framgia:android-emulator-detector:1.4.0")){
                    println("Dest: " + dest)
                    Injection.inject(jarInput, project, dest)
                }else
                    FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}