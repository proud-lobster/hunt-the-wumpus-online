import command from "../common/command.js";
import movement from "../common/movement.js";

function f(char, args) {
    if (args.length < 1) {
        out(e.PLAYER_REF, "You must provide a direction to move.");
        return;
    }

    movement.move(char, movement.findDirection(args[0]).value);
}

export default {
    name: "move",
    aliases: ["m"],
    apply: command.createCharacterCommand(f)
}