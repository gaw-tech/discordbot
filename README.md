# discordbot
My first discordbot.

For the bot to work there must be a file called "config.txt" in the same directory as the main funcion.
In this file there must be the following lines:
 slash_channels:{"<ids of allowed channels>", "<...>", ..., "<...>"}
 myId:"<id of bot owner>"
 modules:{}
 token:"<bot token>"

If you start it without a config file it should ask you for you discord id and the bot token and generate a config file. All command modues have to be loaded once with "?load [class name]"
