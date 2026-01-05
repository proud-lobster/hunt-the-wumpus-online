import character from "./character.js";
import creature from "./creature.js";

const DIRECTIONS = {
    north: {
        value: "north",
        departureDescription: "to the north.",
        arrivalDescription: "the south."
    },
    east: {
        value: "east",
        departureDescription: "to the east.",
        arrivalDescription: "the west."
    },
    south: {
        value: "south",
        departureDescription: "to the south.",
        arrivalDescription: "the north."
    },
    west: {
        value: "west",
        departureDescription: "to the west.",
        arrivalDescription: "the east."
    },
    up: {
        value: "up",
        departureDescription: "above.",
        arrivalDescription: "below."
    },
    down: {
        value: "down",
        departureDescription: "below.",
        arrivalDescription: "above."
    },
    elsewhere: {
        value: "elsewhere",
        departureDescription: "elsewhere.",
        arrivalDescription: "elsewhere"
    }
}

export default {
    directions: DIRECTIONS,

    findDirection(arg) {
        const lowerArg = arg?.toLowerCase();

        for (let key in DIRECTIONS) {
            if (DIRECTIONS.hasOwnProperty(key)) {
                if (key.startsWith(lowerArg)) {
                    return DIRECTIONS[key];
                }
            }
        }

        return DIRECTIONS.elsewhere;
    },

    transport(e, target) {
        if (e.LOCATION) {
            e.LOCATION.removeContents(e);
        }
        target.addContents(e);
        e.LOCATION = target;
    },

    getWaypoint(wp) {
        const target = app.entitiesByComponentValue("WAYPOINT", wp.toUpperCase())[0];
        if (!target) {
            throw Error("USAGE:No such waypoint.");
        }
        return target;
    },

    transportToWaypoint(e, wp) {
        this.transport(e, this.getWaypoint(wp));
    },

    move(char, direction) {
        const eligibleLinks = char.LOCATION
            .CONTAINER
            .filter(c => c.LINK)
            .filter(c => c.DIRECTION == this.findDirection(direction).value);

        const obj = char.CHARACTER ? character(char) : creature(char);

        if (eligibleLinks.length < 1 && char.CHARACTER) {
            obj.print("That direction is not available here.");
            return;
        }

        obj.move(eligibleLinks[0]);

    }
}