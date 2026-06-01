package org.example.project.data.update

import java.io.File
import java.nio.file.Files

actual suspend fun platformInstall(bytes: ByteArray, fileName: String): Boolean {
    return try {
        val isMsi = fileName.endsWith(".msi", ignoreCase = true)
        val isDmg = fileName.endsWith(".dmg", ignoreCase = true)
        val isDeb = fileName.endsWith(".deb", ignoreCase = true)

        val tempDir = File(System.getProperty("java.io.tmpdir"), "tutoriaiad_update")
        tempDir.mkdirs()
        val targetFile = File(tempDir, fileName)
        Files.write(targetFile.toPath(), bytes)

        when {
            isMsi -> {
                val process = ProcessBuilder(
                    "msiexec", "/i", targetFile.absolutePath, "/passive", "/norestart"
                ).start()
                process.waitFor()
                process.exitValue() == 0
            }
            isDmg -> {
                ProcessBuilder("open", targetFile.absolutePath).start()
                true
            }
            isDeb -> {
                val process = ProcessBuilder(
                    "sudo", "dpkg", "-i", targetFile.absolutePath
                ).start()
                process.waitFor()
                process.exitValue() == 0
            }
            else -> false
        }
    } catch (_: Exception) {
        false
    }
}
