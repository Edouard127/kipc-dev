@file:Suppress("unused")

package dev.cbyrne.kdiscordipc.manager.impl

import dev.cbyrne.kdiscordipc.KDiscordIPC
import dev.cbyrne.kdiscordipc.core.event.impl.ReadyEvent
import dev.cbyrne.kdiscordipc.core.packet.outbound.impl.SetActivityPacket
import dev.cbyrne.kdiscordipc.core.util.currentPid
import dev.cbyrne.kdiscordipc.data.activity.Activity
import dev.cbyrne.kdiscordipc.manager.Manager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * This manager allows you to set the current user's activity (a.k.a. rich presence)
 */
class ActivityManager(override val ipc: KDiscordIPC) : Manager() {
    /**
     * Sets a user's presence in Discord to a new activity. This has a rate limit of 5 updates per 20 seconds.
     */
    var activity: Activity? = null
        set(value) {
            field = value

            runBlocking {
                withContext(Dispatchers.IO) {
                    if (ipc.connected)
                        sendActivity(value)
                }
            }
        }

    /**
     * Clear's a user's presence in Discord to make it show nothing.
     */
    fun clearActivity() {
        activity = null
    }

    override suspend fun init() {
        ipc.on<ReadyEvent> {
            activity?.let { sendActivity(activity) }
        }
    }

    private fun sendActivity(activity: Activity?) {
        ipc.firePacketSend(SetActivityPacket(currentPid, activity))
    }
}