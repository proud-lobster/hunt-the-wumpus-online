import command from "../common/command.js";

function f(char, args) {
    if (!char.meleeWeapon) {
        char.print("You do not have a melee weapon equipped.");
        return;
    }

    if (char.creature.basicCooldown) {
        char.print("This action is not ready yet.");
        return;
    }

    if (args[0]) {
        const target = char.entity.LOCATION.CONTAINER
            .filter(c => c.NAME)
            .filter(c => !c.DEAD)
            .filter(c => c.NAME.toLowerCase().startsWith(args[0].toLowerCase()))[0];

        if (!target) {
            char.print('There is no target here by that name.');
        } else {
            char.announce((p) => `${p.subject} ${p.verb} at ${p.object} with ${p.directObject}`, {
                verb: "strike",
                object: target.NAME,
                directObject: char.meleeWeapon.NAME
            });

            char.strike(target);
        }

    } else {
        const target = char.entity.LOCATION.CONTAINER
            .filter(c => !c.DEAD)
            .filter(c => c.ENEMY)[0];

        if (!target) {
            char.print('There is no target here.');
        } else {
            char.announce((p) => `${p.subject} ${p.verb} at ${p.object} with ${p.directObject}`, {
                verb: "strike",
                object: target.NAME,
                directObject: char.meleeWeapon.NAME
            });

            char.strike(target);
        }
    }
}

export default {
    name: "strike",
    aliases: ["t"],
    apply: command.createCharacterCommand(f)
}