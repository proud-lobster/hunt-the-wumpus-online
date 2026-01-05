import output from "../common/output.js";
import player from "../common/player.js";

export default {
    name: "data",
    aliases: [],
    apply(e) {
        const args = e.ARGUMENTS.split(" ");
        const p = e.PLAYER_REF;

        if (args[0].toUpperCase() == "HUD") {
            player(p).refreshHud();
        }

        // TODO handle other data, or "not found"

    }
}