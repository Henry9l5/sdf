/*
 * Copyright (c) 2023 Komtent Dev Team
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package world.komq.hardcore.plugin.commands

import world.komq.hardcore.plugin.objects.HardcoreObject.isRunning
import world.komq.hardcore.plugin.objects.HardcoreObject.plugin
import world.komq.hardcore.plugin.objects.HardcoreObject.server
import world.komq.hardcore.plugin.objects.HardcoreObject.start
import world.komq.hardcore.plugin.objects.HardcoreObject.stop
import world.komq.hardcore.plugin.objects.HardcoreObject.unbanable
import world.komq.hardcore.plugin.objects.HardcoreObject.usableUnbans
import io.github.monun.kommand.getValue
import io.github.monun.kommand.node.LiteralNode
import io.github.monun.kommand.node.RootNode
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.BanList
import org.bukkit.OfflinePlayer

/**
 * @author Komtent Dev Team
 */

object HardcoreKommand {
    fun gameKommand(builder: RootNode) {
        builder.apply {
            description = "."
            usage = "/hardcore"

            executes {
                if (!isRunning) {
                    plugin.logger.info("게임 시작")
                    start()
                } else {
                    plugin.logger.info("게임 종료")
                    stop()
                }
            }
        }
    }

    fun unbanKommand(builder: RootNode) {
        builder.apply {
            description = "커스텀 차단 해제 명령어"
            usage = "/unban <플레이어 이름>"

            val bannedPlayers = dynamic { _, input ->
                server.getOfflinePlayer(input)
            }.apply {
                suggests {
                    val players = server.bannedPlayers.map { player -> player.name.toString() }
                    suggest(players) {
                        text(it)
                    }
                }
            }


            then("target" to bannedPlayers) {
                executes {
                    if (unbanable) {
                        val target: OfflinePlayer by it

                        target.name?.let { name -> server.getBanList(BanList.Type.NAME).pardon(name) }
                        sender.sendMessage(text("완료"))
                        usableUnbans--
                        if (usableUnbans == 0) unbanable = false
                    }
                    else sender.sendMessage(text("사용 가능한 차단 해제 횟수가 없습니다.", NamedTextColor.RED))

                }
            }
        }
    }
}