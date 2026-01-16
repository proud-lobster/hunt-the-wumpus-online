import command from "../common/command.js";
import movement from "../common/movement.js";

export default {
    name: "east",
    aliases: "e",
    apply: command.createCharacterCommand((c) => movement.move(c.entity, movement.directions.east.value))
}