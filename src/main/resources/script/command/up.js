import command from "../common/command.js";
import movement from "../common/movement.js";

export default {
    name: "up",
    aliases: "u",
    apply: command.createCharacterCommand((c) => movement.move(c.entity, movement.directions.up.value))
}