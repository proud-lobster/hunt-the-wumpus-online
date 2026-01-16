import command from "../common/command.js";
import movement from "../common/movement.js";

export default {
    name: "west",
    aliases: "w",
    apply: command.createCharacterCommand((c) => movement.move(c.entity, movement.directions.west.value))
}