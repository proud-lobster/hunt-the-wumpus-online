import command from "../common/command.js";
import movement from "../common/movement.js";

export default {
    name: "north",
    aliases: ["n"],
    apply: command.createCharacterCommand((c) => movement.move(c.entity, movement.directions.north.value))
}