import command from "../common/command.js";

function f(player) {
    const chars = player.characters
        .sort(cs => cs.entity.ROSTER_NUMBER)
        .map(cs => `${cs.entity.ROSTER_NUMBER} - ${cs.fullName}${!cs.entity.DEAD ? "" : " [DEAD]"}`)
        .reduce((a, v) => `${a}\n${v}`, "");

    player.print(chars.length > 0 ? chars : "You do not have any characters.");
}

export default {
    name: "list",
    aliases: ["ls"],
    apply: command.createPlayerCommand(f)
}