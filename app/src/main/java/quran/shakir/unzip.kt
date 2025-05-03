import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun unzip(zipFilePath: String, destDirPath: String) {
    val destDir = File(destDirPath)
    if (!destDir.exists()) destDir.mkdirs()

    ZipInputStream(FileInputStream(zipFilePath)).use { zipIn ->
        var entry: ZipEntry? = zipIn.nextEntry
        while (entry != null) {
            val filePath = File(destDir, entry.name)
            if (entry.isDirectory) {
                filePath.mkdirs()
            } else {
                // Ensure parent directory exists
                filePath.parentFile?.mkdirs()
                FileOutputStream(filePath).use { fos ->
                    val buffer = ByteArray(4096)
                    var len: Int
                    while (zipIn.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                }
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
    }
}
