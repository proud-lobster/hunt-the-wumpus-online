import command from "../common/command.js";
import lair from "../common/lair.js";
import movement from "../common/movement.js";
import output from "../common/output.js";

function f(char, args) {

    if (!char.rangedWeapon) {
        char.print("You do not have a ranged weapon equipped.");
        return;
    }

    if (!char.ammo) {
        char.print("You do not have any ammunition equipped.");
        return;
    }

    if (char.creature.basicCooldown) {
        char.print("This action is not ready yet.");
        return;
    }

    const direction = movement.findDirection(args[0]);

    if (direction.value != movement.directions.elsewhere.value) {
        const link = char.entity.LOCATION.CONTAINER
            .filter(c => c.LINK)
            .filter(c => c.DIRECTION == movement.findDirection(args[0]).value)[0];

        if (!link) {
            char.print("You cannot shoot in that direction.");
            return;
        }

        char.announce((p) => `${p.subject} ${p.verb} an arrow ${p.adverb}`, {
            verb: "fire",
            adverb: direction.departureDescription
        });

        const target = link.LINK.CONTAINER
            .filter(c => c.ENEMY)
            .filter(c => !c.DEAD)[0];

        if (!target) {
            output.print(link.LINK, "An arrow hits the ground nearby.");
            output.print(char.entity.LOCATION, "You hear the arrow hit the ground with a soft plunk, finding no target.");
        }

        char.shoot(target);

        const targetZone = link.LINK.ZONE_REF;
        if (targetZone.NAME == "Lair" && !target?.WUMPUS) {
            lair.startleWumpus(targetZone);
        }

    } else if (args[0]) {
        const target = char.entity.LOCATION.CONTAINER
            .filter(c => c.NAME)
            .filter(c => !c.DEAD)
            .filter(c => c.NAME.toLowerCase().startsWith(args[0].toLowerCase()))[0];

        if (!target) {
            char.print('There is no target here by that name.');
        } else {
            char.announce((p) => `${p.subject} ${p.verb} an arrow at ${p.object}`, {
                verb: "fire",
                object: target.NAME
            });

            char.shoot(char.entity);
        }

    } else {
        const target = char.entity.LOCATION.CONTAINER
            .filter(c => c.ENEMY)
            .filter(c => !c.DEAD)[0];

        if (!target) {
            char.print('There is no target here.');
        } else {
            char.announce((p) => `${p.subject} ${p.verb} an arrow at ${p.object}`, {
                verb: "fire",
                object: target.NAME
            });

            char.shoot(target);
        }
    }

}

export default {
    name: "shoot",
    aliases: "sh",
    apply: command.createCharacterCommand(f)
}