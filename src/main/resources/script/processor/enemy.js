import creature from "../common/creature.js";
import output from "../common/output.js";
import processor from "../common/processor.js";

function f(e) {
    if (!e.DEAD) {
        const cre = creature(e);

        if (cre.stamina.isEmpty() && cre.willpower.isEmpty() && cre.energy.isEmpty()) {
            e.DEAD = true;

            if (e.WUMPUS) {
                output.print(e.ZONE_REF, "A horrific bellow rumbles through the tunnels as the wumpus of this lair is felled!");
                // TODO reward all characters in the lair at time of death
            } else {
                output.print(e.LOCATION, `${e.DESCRIPTIVE} has died.`);
                // TODO reward all characters in the room
            }

            return;
        }

        cre.tick();

    }
}

export default {
    component: "ENEMY",
    apply: processor.createTickProcessor(f)
}