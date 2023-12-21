package com.runicrealms.runicbuildserver

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ExportsCommand: CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        Bukkit.getScheduler().runTaskAsynchronously(RunicBuildServer.instance, Runnable {
            if (args.isNotEmpty()) {
                try {
                    val artifactNames = RunicBuildServer.instance.getArtifacts().map { it.replace(".zip", "") }
                    when (args[0]) {
                        "list" -> {
                            sender.sendMessage("${ChatColor.AQUA}Tagged exports: " + artifactNames.joinToString(", "))
                        }

                        "create" -> {
                            if (args.size != 2) {
                                sender.sendMessage("${ChatColor.RED}Bad format! Use /export create <tag>")
                                return@Runnable
                            }
                            val artifactTag = args[1]
                            if (artifactNames.contains(artifactTag)) {
                                sender.sendMessage("${ChatColor.RED}Build export with tag $artifactTag already exists!")
                                return@Runnable
                            }
                            if (artifactNames.contains(".zip")) {
                                sender.sendMessage("${ChatColor.RED}Build export tag cannot contain \".zip\"")
                                return@Runnable
                            }
                            Bukkit.broadcastMessage("${ChatColor.DARK_AQUA}EXPORTING WORLD: do not move or place blocks!")
                            Bukkit.getScheduler().runTask(RunicBuildServer.instance, Runnable {
                                try {
                                    RunicBuildServer.instance.createExport(artifactTag)
                                } catch (exception: Exception) {
                                    sender.sendMessage("${ChatColor.RED}There was an error! Check the console for more information.")
                                }
                                Bukkit.broadcastMessage("${ChatColor.DARK_AQUA}EXPORTING WORLD: done!")
                            })
                        }

                        "delete" -> {
                            if (args.size != 2) {
                                sender.sendMessage("${ChatColor.RED}Bad format! Use /export delete <tag>")
                                return@Runnable
                            }
                            val artifactTag = args[1]
                            if (!artifactNames.contains(artifactTag)) {
                                sender.sendMessage("${ChatColor.RED}Build export with tag $artifactTag does not exists!")
                                return@Runnable
                            }
                            sender.sendMessage("${ChatColor.DARK_AQUA}Deleting export $artifactTag...")
                            RunicBuildServer.instance.deleteExport(artifactTag)
                            sender.sendMessage("${ChatColor.DARK_AQUA}Done!")
                        }
                    }
                } catch (exception: Exception) {
                    sender.sendMessage("${ChatColor.RED}There was an error! Check the console for more information.")
                }
            } else {
                sender.sendMessage("${ChatColor.DARK_AQUA}Usage:")
                sender.sendMessage("${ChatColor.AQUA}- /exports list: Lists all currently stored exports")
                sender.sendMessage("${ChatColor.AQUA}- /exports create <tag>: Immediately exports both worlds to destination with <tag>")
                sender.sendMessage("${ChatColor.AQUA}- /exports delete <tag>: Immediately deletes an existing export with <tag>")
            }
        })
        return true
    }

}