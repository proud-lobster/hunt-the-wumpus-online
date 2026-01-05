import output from "../common/output.js";

const LOBBY_MENU = `
You are in the Player Lobby
The following commands are available:
list - lists the contents of your character roster
create - creates a new character in your roster
select <n> - selects a character to play as, where <n> is its roster number
remove <n> - removes a character from your roster, where <n> is its roster number
logout - logs out from the server (NOTE: will invalidate your session token and require a new login)
`

export default {
    name: "help",
    aliases: ["h"],
    apply(e) {
        const char = e.PLAYER_REF.ACTIVE_CHARACTER_REF;
        var msg = LOBBY_MENU;

        // TODO command listings
        // TODO command help
        if (char) {
            msg = `
You are playing the game as ${char.NAME}.
The following commands are available:
`
        }

        output.print(e.PLAYER_REF, msg);
    }
}