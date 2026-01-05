import character from "../common/character.js";
import command from "../common/command.js";
import lair from "../common/lair.js";

const MAX_ROSTER_NUM = 8;

function f(player) {
    const rosterNumber = player.entity.CONTAINER
        .filter(cs => cs.CHARACTER)
        .map(cs => cs.ROSTER_NUMBER)
        .reduce((max, curr) => (curr > max ? curr : max), 0) + 1;

    if (rosterNumber > MAX_ROSTER_NUM) {
        player.print("You have too many active characters!  You must delete one in order to create a new one.");
        return;
    }

    const c = character().create().entity;
    c.PLAYER_REF = player.entity;
    c.ROSTER_NUMBER = rosterNumber;
    player.entity.addContents(c);

    const lairEntrance = lair.create();
    const lairLink = app.createEntity();
    lairLink.LINK = lairEntrance
    lairLink.WAYPOINT = "WUMPUS";
    lairLink.DESCRIPTIVE = lairEntrance.DESCRIPTIVE;
    c.addContents(lairLink);

    player.print(c.INTRODUCTION);

    character(c).active = true;
}

export default {
    name: "create",
    aliases: [],
    apply: command.createPlayerCommand(f)
}