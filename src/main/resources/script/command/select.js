import character from "../common/character.js";
import command from "../common/command.js";

function f(player, args) {

    if (args.length < 1) {
        player.print("You must provide a character roster number to select.  Use 'list' to display your roster.");
        return;
    }

    const chars = player.entity.CONTAINER
        .filter(c => c.CHARACTER)
        .filter(c => c.ROSTER_NUMBER == args[0]);

    if (chars.length < 1) {
        player.print("You do not have a character with that roster number.");
        return;
    }

    if (chars[0].DEAD) {
        player.print("You cannot select a dead character.");
        return;
    }

    character(chars[0]).active = true;
}

export default {
    name: "select",
    aliases: "sel",
    apply: command.createPlayerCommand(f)
}