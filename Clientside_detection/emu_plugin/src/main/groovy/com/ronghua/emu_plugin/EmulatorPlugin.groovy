package com.ronghua.emu_plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class EmulatorPlugin implements Plugin<Project> {
    @Override
    void apply (Project project){
        System.out.println("Hello Gradle!")
        def android = project.extensions.getByType(AppExtension.class)
        def transform = new EmulatorTransform(project)
        android.registerTransform(transform)
    }
}
