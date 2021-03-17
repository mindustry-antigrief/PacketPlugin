package example

import arc.Events
import arc.graphics.Color
import arc.struct.ObjectIntMap
import mindustry.Vars
import mindustry.content.Fx
import mindustry.game.EventType
import mindustry.gen.Call
import mindustry.gen.Player
import mindustry.mod.Plugin
import mindustry.net.Net
import mindustry.net.NetConnection
import mindustry.net.Registrator
import sun.misc.Unsafe
import java.util.*
import kotlin.concurrent.schedule

class PacketPlugin : Plugin() {
    /** A set of players known to be using the client. */
    private val clientUsers = mutableSetOf<Player>()

    override fun init() {
        /** This area adds a custom packet type to [Registrator]. */

        ClientNetworkPacket()  // Make sure it's loaded

        // Get unsafe
        val f = Unsafe::class.java.getDeclaredField("theUnsafe")
        f.isAccessible = true
        val unsafe = f.get(null) as Unsafe

        // Get the field to modify (Registrator's classes field)
        val registratorClass = Registrator::class.java
        val registeredPackets = registratorClass.getDeclaredField("classes")

        // Make it public
        registeredPackets.isAccessible = true

        // Get the contents and convert it to a list
        val entries = (registeredPackets.get(null) as Array<Registrator.ClassEntry>).toMutableList()

        // Add custom class entry for the packet
        entries.add(Registrator.ClassEntry(ClientNetworkPacket::class.java, ::ClientNetworkPacket))

        // Put the modified entry list
        val base = unsafe.staticFieldBase(registeredPackets)  // Registrator's position in memory
        val offset = unsafe.staticFieldOffset(registeredPackets)  // The offset from Registrator's position and the field we want to modify
        unsafe.putObject(base, offset, entries.toTypedArray())  // Overwrite the existing array with a pointer to our new, modified one

        // Add it to the id map
        val idField = Registrator::class.java.getDeclaredField("ids")
        idField.isAccessible = true
        val ids = idField.get(null) as ObjectIntMap<Class<*>>
        ids.put(ClientNetworkPacket::class.java, 5)

        // Add handlers
        Vars.net.handleServer(ClientNetworkPacket::class.java) { con: NetConnection?, clientNetworkPacket: ClientNetworkPacket ->
            con ?: return@handleServer
            if (clientNetworkPacket.content.contentEquals(byteArrayOf(1))) {  // Ack packet, confirms this is a client user
                clientUsers.add(con.player)
                return@handleServer
            }

            clientNetworkPacket.sender = con.player.id  // Overwrite sender id from the packet to prevent spoofing
            for (player in clientUsers) {  // Forward it to all client users
                if (player.con == null) {
                    clientUsers.remove(player)
                    continue
                }
                player.con.send(clientNetworkPacket, Net.SendMode.udp)
            }
        }
        Events.on(EventType.PlayerJoin::class.java) { event ->
            clientUsers.remove(event.player)  // Just in case they were accidentally added and then they crashed
            Timer().schedule(50L) {
                // Transmit event that normal clients will ignore, but the modified one will acknowledge
                Call.effect(event.player.con, Fx.none, -1f, 0f, 1f, Color.clear)
            }
        }
    }
}