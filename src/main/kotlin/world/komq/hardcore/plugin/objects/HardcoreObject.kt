/*
 * Copyright (c) 2023 Komtent Dev Team
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package world.komq.hardcore.plugin.objects

import world.komq.hardcore.plugin.HardcorePlugin
import world.komq.hardcore.plugin.config.HardcoreCorpseData
import world.komq.hardcore.plugin.events.HardcoreEvent
import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import io.github.monun.tap.mojangapi.MojangAPI
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.world.entity.Pose
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * @author Komtent Dev Team
 */

@Suppress("UNCHECKED_CAST")
object HardcoreObject {
    val plugin = HardcorePlugin.instance
    val server = plugin.server
    val fakeServer = FakeEntityServer.create(plugin)
    val fakePlayers get() = fakeServer.entities.filter { it.bukkitEntity is Player } as List<FakeEntity<Player>>
    var isRunning = false
    val linkedInventory = HashMap<UUID, Inventory>()
    val corpses = arrayListOf<HardcoreCorpseData>()
    val coroutines = arrayListOf<Job>()
    var unbanable = false
    var usableUnbans = 0

    fun start() {
        isRunning = true

        server.worlds.forEach {
            it.setGameRule(GameRule.KEEP_INVENTORY, true)
            it.setGameRule(GameRule.REDUCED_DEBUG_INFO, true)

            it.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        }

        server.onlinePlayers.forEach { fakeServer.addPlayer(it) }
        server.pluginManager.registerEvents(HardcoreEvent, plugin)

        HeartbeatScope().launch {
            while (true) {
                fakeServer.update()
                delay(1L)
            }
        }.also { coroutines.add(it) }

        usableUnbans = plugin.config.getInt("usableUnbans")

        corpses.forEach { createCorpseNpcFromData(it, true) }
    }

    fun stop() {
        isRunning = false
        fakeServer.entities.forEach { it.remove() }
        server.onlinePlayers.forEach { fakeServer.removePlayer(it) }
        HandlerList.unregisterAll(HardcoreEvent)
        coroutines.forEach { it.cancel() }
        corpses.clear()
        plugin.config.set("corpses", null)
        plugin.config.set("usableUnbans", 0)
        plugin.saveConfig()
    }

    private fun createCorpseNpcFromData(corpseData: HardcoreCorpseData, isLoaded: Boolean = false, skinProfile: MojangAPI.SkinProfile? = null) {
        server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val profile = skinProfile ?: corpseData.uniqueId?.let { MojangAPI.fetchSkinProfile(it) }!!
            server.scheduler.runTask(plugin, Runnable {
                val npc = fakeServer.spawnPlayer(corpseData.location, "${profile.name}의 시체", profile.profileProperties().toSet())

                npc.updateMetadata {
                    (this as CraftPlayer).handle.apply { pose = Pose.SLEEPING }
                    linkedInventory[uniqueId] = corpseData.inventory
                }

                if (!isLoaded) corpses += HardcoreCorpseData.from(npc, corpseData.uniqueId)
            })
        })
    }

    private fun createCorpseInventory(player: Player) = server.createInventory(null, 45, text("${player.name}의 시체", NamedTextColor.DARK_GRAY)).apply {
        contents = Array(45) { if (it < 41) player.inventory.contents[it] else ItemStack(Material.AIR) }
    }

    fun createCorpseNPC(player: Player, deathLocation: Location) = createCorpseNpcFromData(HardcoreCorpseData(deathLocation, player.uniqueId, createCorpseInventory(player), player.name))

    fun openInventory(player: Player, body: FakeEntity<Player>) = player.openInventory(linkedInventory[body.bukkitEntity.uniqueId]!!)
}