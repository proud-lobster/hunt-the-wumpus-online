import command from "../common/command.js";

function f(char) {
    const o = char.entity.CONTAINER
        .filter(c => c.ITEM)
        .map(c => `${c.COUNT}x ${c.DESCRIPTIVE}`)
        .join("\n");

    char.print("Your inventory contains...\n" + o);
}

export default {
    name: "inventory",
    aliases: ["in"],
    apply: command.createCharacterCommand(f)
}