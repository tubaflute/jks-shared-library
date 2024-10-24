#!/usr/bin/env groovy

// def call(String directoryPath) {
//     def targetSuffix = "-spring-boot.jar" 
//     def minSizeInMB = 50 * 1024 * 1024   
//     def directory = new File(directoryPath)

//     if (!directory.exists() || !directory.isDirectory()) {
//         println "directory not exist or invalid: $directoryPath"
//         return
//     }

//     def findFilesRecursively(File dir) {
//         dir.eachFile { file ->
//             if (file.isDirectory()) {
//                 // 递归调用，进入子目录
//                 findFilesRecursively(file)
//             } else if (file.isFile() && file.name.endsWith(targetSuffix) && file.size() > minSizeInMB) {
//                 println "file --> : ${file.absolutePath} size --> : ${file.size() / (1024 * 1024)} MB"
//                 // throw new StopIterationException()
//             }
//         }
//     }

//     findFilesRecursively(directory)
    

// }

def call(String directoryPath, String targetSuffix) {

    // def targetSuffix = "-spring-boot.jar" 
    def minSizeInBytes = 50 * 1024 * 1024  
    def directory = new File(directoryPath)

    if (!directory.exists() || !directory.isDirectory()) {
        println "directory not exist or invalid: $directoryPath"
        return 
    }

    def queue = new LinkedList<File>()
    queue.add(directory) 

    while (!queue.isEmpty()) {
        def currentDir = queue.poll()
        def files = currentDir.listFiles() 

        if (files) {
            for (file in files) {
                if (file.isDirectory()) {
                    queue.add(file)
                } else if (file.isFile() && file.name.endsWith(targetSuffix) && file.length() > minSizeInBytes) {
                    println "File: ${file.absolutePath} Size: ${file.length() / (1024 * 1024)} MB"
                    return file.absolutePath
                }
            }
        }
    }

}

