# 🦴 Bare Bones Voice Chat
## About
Bare Bones VC is a Fabric client mod that aims to provide players a way to use Simple Voice Chat on Minecraft servers that don't support it.

This is achieved through the Bare Bones VC server jar that does (almost) everything the Simple Voice Chat server does, but without the Minecraft server.

## Server Setup
### Prerequisites
One person in your group will need to host the voice server, the following is required:
* A way to open a UDP port
* A reliable internet connection
* [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) installed on your system (do `java -version` in Command Prompt/Terminal to check)

If port forwarding is an issue or you aren't comfortable with sharing your IP address then you may want to look into a VPS or proxying solutions.

### Steps
1) Everyone must download the Bare Bones VC mod on Fabric, the server host will also download the Bare Bones VC server.
Both can be found [here](https://github.com/boooooooooooooooooooooooooooooooooooone/bare-bones-voice-chat/releases)

2) To start hosting the server, open Command Prompt and run `java -Xmx1G -jar <server file path>`.
To get `<server file path>` on Windows, you can right-click the jar in File Explorer and click "Copy as path"

3) After answering all the questions, the server will start

4) To make your server accessible to your friends, you will need to access your internet router's config page and forward the server port with UDP.
The steps to do this are different for each router so you will need to google instructions for your router's brand

5) Find your public IPv4 address by typing `curl -4 ifconfig.me` in a new Command Prompt window.

6) Now you and your friends may open the Simple Voice Chat menu in game and click the Bare Bones VC button, 
the host will be your public IPv4 address and the port will be whichever port you opened 
![](https://i.imgur.com/Min1erw.png) ![](https://i.imgur.com/9atCEph.png)

7) The voice chat connection will continue even after disconnecting from a Minecraft server,
to disconnect from the voice server use the button in the Simple Voice Chat menu.

## Dependencies
The Bare Bones VC Fabric mod requires both [Fabric API](https://modrinth.com/mod/fabric-api) and [Simple Voice Chat](https://modrinth.com/mod/simple-voice-chat) to function.

## Disclaimer
This mod is an independent addon for [Simple Voice Chat](https://github.com/henkelmax/simple-voice-chat) and is not affiliated with or endorsed by its original developers.

All credit for the underlying voice chat system belongs to the Simple Voice Chat project.

