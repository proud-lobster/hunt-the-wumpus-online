import command from "../common/command.js";
import movement from "../common/movement.js";

export default {
    name: "south",
    aliases: "s",
    apply: command.createCharacterCommand((c) => movement.move(c.entity, movement.directions.south.value))
}