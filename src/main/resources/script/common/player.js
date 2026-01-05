import output from "./output.js";
import character from "./character.js";

export default function (e) {
    if (!e.PLAYER) {
        throw Error(`${e.IDENTIFIER} is not a player.`);
    }

    return {
        entity: e,

        get character() {
            return character(e.ACTIVE_CHARACTER_REF);
        },

        get characters() {
            return e.CONTAINER
                .filter(c => c.CHARACTER)
                .map(c => character(c));
        },

        print(m, mode = output.modes.plain) {
            output.print(e, m, [], mode);
        },

        refreshHud() {
            var hud;
            if (e.ACTIVE_CHARACTER_REF) {
                const cre = this.character.creature;
                hud = `${this.character.entity.NAME}@${cre.zone.NAME} S:${cre.stamina.percent}% W:${cre.willpower.percent}% E:${cre.energy.percent}%`;
            } else {
                hud = `${e.NAME}@LOBBY`;
            }

            output.data(e, "HUD " + hud);
        },

        toLobby() {
            this.character.active = false;
            this.refreshHud();
            this.print("You are now in the player lobby.  Enter 'help' to view the player lobby menu.");
        }
    }
}