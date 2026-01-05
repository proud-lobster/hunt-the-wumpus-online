import player from "./player.js";
import character from "./character.js";

function parseArgs(input) {
    if (input.ARGUMENTS) {
        return input.ARGUMENTS.split(" ");
    } else {
        return [];
    }
}

export default {
    createPlayerCommand(f) {
        return function (e) {
            if (e.PLAYER_REF.ACTIVE_CHARACTER_REF) {
                throw Error("USAGE:You can only use this command in the Player Lobby.");
            } else {
                f(player(e.PLAYER_REF), parseArgs(e));
            }
        };
    },
    createCharacterCommand(f) {
        return function (e) {
            if (!e.PLAYER_REF.ACTIVE_CHARACTER_REF) {
                throw Error("USAGE:You cannot use this command in the Player Lobby.");
            } else {
                f(character(e.PLAYER_REF.ACTIVE_CHARACTER_REF), parseArgs(e));
            }
        }
    }
}
