# Packet Plugin
The purpose of this plugin is to add a custom packet interface that does not interfere with the vanilla client, but is
detected by (our client)[https://github.com/mindustry-antigrief/mindustry-client-v6] and used for networking.  This
enables it to transmit faster and more reliably, as well as removing the requirement for rapidly-configured special
message blocks.

### Installing

Simply place the output jar from the step above in your server's `config/mods` directory and restart the server.
List your currently installed plugins/mods by running the `mods` command.

### Building a Jar

`gradlew jar` / `./gradlew jar`

Output jar should be in `build/libs`.

### How it works
This plugin uses java's Unsafe utilities to add a custom packet type (see PacketPlugin.kt for details).  **Please let me
(@blahblahbloopster) know if the plugin causes any issues or does not work.**
