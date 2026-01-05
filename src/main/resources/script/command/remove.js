import character from "../common/character.js";
import command from "../common/command.js";
import movement from "../common/movement.js";

function f(player, args) {

    if (args.length < 1) {
        player.print("You must provide a character roster number to remove.  Use 'list' to display your roster.");
        return;
    }

    const chars = player.entity.CONTAINER
        .filter(c => c.CHARACTER)
        .filter(c => c.ROSTER_NUMBER == args[0]);

    if (chars.length < 1) {
        player.print("You do not have a character with that roster number.");
        return;
    }

    chars[0].ROSTER_NUMBER = 0;
    chars[0].EXPIRED = true;
    movement.transportToWaypoint(chars[0], "NOWHERE");
    player.entity.removeContents(chars[0]);
    player.entity.CONTAINER
        .filter(c => c.CHARACTER)
        .filter(c => c.ROSTER_NUMBER > args[0])
        .forEach(c => c.ROSTER_NUMBER = c.ROSTER_NUMBER - 1);

    player.print(`${character(chars[0]).fullName} has been removed.  Other character roster numbers have been adjusted accordingly.`);
}

export default {
    name: "remove",
    aliases: ["rem"],
    apply: command.createPlayerCommand(f)
}