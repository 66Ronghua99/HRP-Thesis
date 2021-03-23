package com.ronghua.emu_plugin

import com.android.build.api.transform.JarInput
import com.android.utils.FileUtils
import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.NotFoundException
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

public class Injection {
    private static ClassPool classPool = ClassPool.getDefault()

    public static void inject(JarInput jarInput, Project project, def dest){
        println("---------Injecting-----------")
        println("Absolute path: " + jarInput.file.getAbsolutePath())
        String path = jarInput.file.getAbsolutePath()
        classPool.appendClassPath(jarInput.file.getAbsolutePath())
        classPool.appendClassPath(project.android.bootClasspath[0].toString())

        CtClass clazz = classPool.getCtClass("com.framgia.android.emulator.EmulatorDetector")
        if(null != clazz){
            CtMethod[] methods = clazz.getDeclaredMethods("detect")
            for (CtMethod method: methods) {
                println("Method type: " + method.returnType.getName())
                if (method.returnType.getName().equals("boolean")){
                    String insertedCode = "{\n" +
                            "boolean result = false;\n" +
                            "\n" +
                            "        log(getDeviceInfo());\n" +
                            "\n" +
                            "        // Check Basic\n" +
                            "        result |= checkBasic();\n" +
                            "        log(\"Check basic \" + result);\n" +
                            "\n" +
                            "        // Check Advanced\n" +
                            "        boolean temp= checkAdvanced();\n" +
                            "        result |= temp;\n" +
                            "        log(\"Check Advanced \" + temp);\n" +
                            "\n" +
                            "        // Check Package Name\n" +
                            "        temp = checkPackageName();\n" +
                            "        result |= temp;\n" +
                            "        log(\"Check Package Name \" + result);\n" +
                            "\n" +
                            "        return result;\n" +
                            "}"
                    method.setBody(insertedCode)
                }
            }
            CtMethod method = clazz.getDeclaredMethod("checkDeviceId")
            method.setBody("{return false;}")

            method = clazz.getDeclaredMethod("checkImsi")
            method.setBody("{return false;}")

            method = clazz.getDeclaredMethod("log")
            method.setBody("{\n" +
                    "        if (this.isDebug) {\n" +
                    "            android.util.Log.i(getClass().getName(), \$1);\n" + //argument $0 stands for the class
                    "        }\n" +
                    "    }")

            replaceJarFile(path, clazz.toBytecode(),"com/framgia/android/emulator/EmulatorDetector.class", dest)
            clazz.detach()
//            CtMethod method = clazz.getDeclaredMethod("detect", null)
        }
    }


    private static void replaceJarFile(String jarPathAndName,byte[] fileByteCode,String fileName, def dest) throws IOException {
        File jarFile = new File(jarPathAndName);
        File tempJarFile = new File(jarPathAndName + ".tmp");
        JarFile jar = new JarFile(jarFile);
        boolean jarWasUpdated = false;

        try {
            JarOutputStream tempJar =
                    new JarOutputStream(new FileOutputStream(tempJarFile));
            // Allocate a buffer for reading entry data.
            byte[] buffer = new byte[1024];
            int bytesRead;
            try {
                // Open the given file.
                try {
                    // Create a jar entry and add it to the temp jar.
                    JarEntry entry = new JarEntry(fileName);
                    tempJar.putNextEntry(entry);
                    tempJar.write(fileByteCode);

                } catch (Exception ex) {
                    println(ex);
                    // Add a stub entry here, so that the jar will close without an
                    // exception.
                    tempJar.putNextEntry(new JarEntry("stub"));
                }
                // Loop through the jar entries and add them to the temp jar,
                // skipping the entry that was added to the temp jar already.
                InputStream entryStream = null;
                for (Enumeration entries = jar.entries(); entries.hasMoreElements();) {
                    // Get the next entry.
                    JarEntry entry = (JarEntry) entries.nextElement();
                    // If the entry has not been added already, so add it.
                    println("Entry name : " + entry.getName())
                    if (!entry.getName().equals(fileName)) {
                        // Get an input stream for the entry.
                        entryStream = jar.getInputStream(entry);
                        JarEntry tempEntry = new JarEntry(entry.getName())
                        println("get input stream successfully")
                        tempJar.putNextEntry(tempEntry);
                        while ((bytesRead = entryStream.read(buffer)) != -1) {
                            println("bytesRead: " + bytesRead + " buffer size: " + buffer.size())
                            tempJar.write(buffer, 0, bytesRead);
                        }
                    } else
                        System.out.println("Does Equal");
                }
                if (entryStream != null)
                    entryStream.close();
                jarWasUpdated = true;
            }
            catch (Exception ex) {
                System.out.println(ex);
                // IMportant so the jar will close without an
                // exception.
                tempJar.putNextEntry(new JarEntry("stub"));
            }
            finally {
                tempJar.close();
            }
        }
        finally {
            jar.close();
            if (!jarWasUpdated) {
                tempJarFile.delete();
            }
        }
        FileUtils.copyFile(tempJarFile, dest)
        println("CopyFile successfully!")
        tempJarFile.delete()
    }
}