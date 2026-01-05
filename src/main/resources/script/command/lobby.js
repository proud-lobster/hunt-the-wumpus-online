import command from "../common/command.js";

export default {
    name: "lobby",
    aliases: ["lob"],
    apply: command.createCharacterCommand((c) => c.player.toLobby())
}