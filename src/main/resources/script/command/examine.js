import narrative from "../common/narrative.js";
import command from "../common/command.js";

function f(char, args) {
    if (args.length < 1) {
        narrative.examine(char.entity);
    } else {
        const match = char.entity.LOCATION.CONTAINER
            .filter(c => c.NAME.startsWith(args[0]))[0];

        if (match) {
            narrative.examine(char.entity, match);
        } else {
            char.print(`Could not find anything called '${args[0]}'.`);
        }
    }
}

export default {
    name: "examine",
    aliases: "ex",
    apply: command.createCharacterCommand(f)
}