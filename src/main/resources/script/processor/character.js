import character from "../common/character.js";
import processor from "../common/processor.js";

function f(e) {
    if (!e.DEAD) {
        const char = character(e);
        const cre = char.creature;

        if (cre.stamina.isEmpty() && cre.willpower.isEmpty() && cre.energy.isEmpty()) {
            e.DEAD = true;
            char.announce((p) => `${p.subject} ${p.have} died!`);
            if (char.active) {
                char.print("Returning to player lobby...");
                char.player.toLobby();
            }
            return;
        }

        cre.tick();

    }
}

export default {
    component: "CHARACTER",
    apply: processor.createTickProcessor(f)
}