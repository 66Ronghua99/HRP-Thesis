package com.ronghua.emu_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class EmulatorPlugin implements Plugin<Project> {
    @Override
    void apply (Project project){
        System.out.println("Hello Gradle!")
    }
}
