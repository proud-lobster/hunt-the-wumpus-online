import command from "../common/command.js";
import movement from "../common/movement.js";

export default {
    name: "down",
    aliases: ["d"],
    apply: command.createCharacterCommand((c) => movement.move(c.entity, movement.directions.down.value))
}