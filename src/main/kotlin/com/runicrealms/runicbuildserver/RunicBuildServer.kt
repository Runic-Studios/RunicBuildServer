package com.runicrealms.runicbuildserver

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class RunicBuildServer : JavaPlugin() {

    lateinit var buildArtifactLocation: File

    override fun onEnable() {
        instance = this

        saveDefaultConfig()
        config.options().copyDefaults(true)
        buildArtifactLocation = File(config.getString("artifact-destination") ?: throw IllegalStateException("Configuration file missing artifact-destination"))

        Bukkit.getPluginCommand("exports")!!.setExecutor(ExportsCommand())
    }

    fun createExport(tag: String) {
        val alterra = Bukkit.getWorld("Alterra")!!.apply { save() }
        val dungeons = Bukkit.getWorld("dungeons")!!.apply { save() }
        zipWorlds(File(buildArtifactLocation, "$tag.zip"), alterra.worldFolder, dungeons.worldFolder)
    }

    fun deleteExport(tag: String) {
        File(buildArtifactLocation, "$tag.zip").delete()
    }

    private fun zipWorlds(zipFile: File, vararg worlds: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { outputStream ->
            for (world in worlds) zipFile(world, world.name, outputStream)
        }
    }

    private fun zipFile(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {
        if (fileToZip.isHidden) {
            return
        }
        if (fileToZip.isDirectory) {
            val children = fileToZip.listFiles()!!
            for (childFile in children) {
                zipFile(childFile, fileName + "/" + childFile.name, zipOut)
            }
        } else {
            val inputStream = FileInputStream(fileToZip)
            val zipEntry = ZipEntry(fileName)
            zipOut.putNextEntry(zipEntry)
            val bytes = ByteArray(1024)
            var length: Int
            while (inputStream.read(bytes).also { length = it } >= 0) {
                zipOut.write(bytes, 0, length)
            }
            inputStream.close()
        }
    }

    companion object {
        lateinit var instance: RunicBuildServer
            private set
    }
}
