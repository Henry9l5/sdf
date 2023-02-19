/*
 * Copyright (c) 2023 Komtent Dev Team
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package world.komq.hardcore.plugin

import world.komq.hardcore.plugin.commands.HardcoreKommand.gameKommand
import world.komq.hardcore.plugin.commands.HardcoreKommand.unbanKommand
import world.komq.hardcore.plugin.config.HardcoreCorpseData
import world.komq.hardcore.plugin.objects.HardcoreObject.coroutines
import world.komq.hardcore.plugin.objects.HardcoreObject.corpses
import world.komq.hardcore.plugin.objects.HardcoreObject.fakeServer
import world.komq.hardcore.plugin.objects.HardcoreObject.isRunning
import world.komq.hardcore.plugin.objects.HardcoreObject.start
import world.komq.hardcore.plugin.objects.HardcoreObject.usableUnbans
import io.github.monun.kommand.kommand
import org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Komtent Dev Team
 */

@Suppress("UNCHECKED_CAST")
class HardcorePlugin : JavaPlugin() {

    companion object {
        lateinit var instance: HardcorePlugin
            private set
    }

    override fun onEnable() {
        instance = this

        registerClass(HardcoreCorpseData::class.java)
        corpses.addAll(config.getList("corpses", listOf<HardcoreCorpseData>()) as List<HardcoreCorpseData>)

        if (config.getBoolean("isRunning")) {
            logger.info("기존에 게임 실행 상태로 서버를 종료하여 게임이 계속 실행됩니다. 게임 종료를 원하시는 경우가 아니라면 \"/hardcore\" 명령어를 다시 입력하실 필요가 없습니다.")
            start()
        }

        kommand {
            register("hardcore") {
                requires { isConsole }
                gameKommand(this)
            }
            register("unban") {
                unbanKommand(this)
            }
        }
    }

    override fun onDisable() {
        coroutines.forEach { it.cancel() }
        server.onlinePlayers.forEach { fakeServer.removePlayer(it) }

        if (isRunning) config.set("isRunning", true) else config.set("isRunning", false)
        config.set("usableUnbans", usableUnbans)
        config.set("corpses", corpses.toList())
        saveConfig()
    }
}