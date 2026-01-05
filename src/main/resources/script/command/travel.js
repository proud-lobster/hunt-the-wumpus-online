import command from "../common/command.js";

function f(char, args) {
    const globalWaypoints = app.entitiesByComponent("WAYPOINT").filter(w => w.ROOM);
    const waypoints = char.entity.CONTAINER
        .filter(w => w.WAYPOINT)
        .filter(w => w.LINK)
        .concat(globalWaypoints);

    if (args.length > 0) {
        const match = waypoints.filter(w => w.WAYPOINT == args[0].toUpperCase());
        if (match.length > 0) {
            char.move(match[0]);
            char.creature.stamina.pay(4);
            char.creature.energy.pay(1);
        } else {
            char.print(`Waypoint "${args[0]}" is not valid.`);
        }
    } else {
        const wptext = waypoints
            .map(w => `${w.WAYPOINT} - ${w.DESCRIPTIVE}`)
            .join("\n");
        char.print(wptext);
    }
}

export default {
    name: "travel",
    aliases: ["tr"],
    apply: command.createCharacterCommand(f)
}