import random from "./random.js";
import hazards from "./hazards.js";
import movement from "./movement.js";

const width = 15;
const height = 15;
const maxRoomCount = 20;

function createRoom(zone) {
    const room = app.createEntity();
    room.ROOM = true;
    room.NAME = "Wumpus Lair Cavern";
    room.DESCRIPTIVE = "A Cavern in a Wumpus Lair";
    room.DETAILED = "A deep, dark cavern tunnel.";
    room.ZONE_REF = zone;
    zone.addContents(room);
    return room;
}

function createLink(room, target, dir) {
    const link = app.createEntity();
    link.LINK = target;
    link.DESCRIPTIVE = target.DESCRIPTIVE;
    link.DIRECTION = dir;
    room.addContents(link);
}

export default {
    create() {

        // Create zone
        const lair = app.createEntity();
        lair.ZONE = true;
        lair.NEW = true;
        lair.NAME = "Lair";
        lair.DESCRIPTIVE = "A Surface-Level Wumpus Lair"

        // Create entrance
        const entrance = app.createEntity();
        entrance.ROOM = true;
        entrance.NAME = "Outside a Wumpus Lair";
        entrance.DESCRIPTIVE = "Outside a Wumpus Lair";
        entrance.DETAILED = "You stand in the midst of the Great Northern Wilderness, facing a dark and odorous descent in to the lair of a wumpus.";
        const wild = app.entitiesByComponentValue("NAME", "Wilderness")[0];
        entrance.ZONE_REF = wild;
        wild.addContents(entrance);

        // Initialize room grid
        const grid = Array.from({ length: width }, () => new Array(height));
        const rooms = [];

        // Create starting room, link to entrance
        let x = Math.floor(width / 2);
        let y = Math.floor(height / 2);
        const start = createRoom(lair);
        grid[x][y] = start;
        rooms.push(start);
        createLink(entrance, grid[x][y], "down");
        createLink(grid[x][y], entrance, "up");

        // Loop until hit room count
        while (rooms.length < maxRoomCount) {
            // Determine next location
            switch (random.getRandomInt(4)) {
                case 0:
                    if (y > 0) y--;
                    break;
                case 1:
                    if (x < width - 1) x++;
                    break;
                case 2:
                    if (y < height - 1) y++;
                    break;
                case 3:
                    if (x > 0) x--;
                    break;
            }

            // If location is new, fill it in
            if (!grid[x][y]) {
                const r = createRoom(lair);
                grid[x][y] = r;
                rooms.push(r);
            }
        }

        // Walk through grid and create room links
        for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                if (grid[x][y]) {
                    if (grid[x]?.[y - 1]) {
                        createLink(grid[x][y], grid[x][y - 1], "north");
                    }
                    if (grid[x + 1]?.[y]) {
                        createLink(grid[x][y], grid[x + 1][y], "east");
                    }
                    if (grid[x]?.[y + 1]) {
                        createLink(grid[x][y], grid[x][y + 1], "south");
                    }
                    if (grid[x - 1]?.[y]) {
                        createLink(grid[x][y], grid[x - 1][y], "west");
                    }
                }
                else {
                }
            }
        }

        rooms.forEach(r => lair.addContents(r));
        const randomRooms = random.getDifferentRandomInts(rooms.length - 2, 3).map(i => rooms[i + 2]);

        const wumpus = hazards.YOUNG_WUMPUS.create(lair);
        movement.transport(wumpus, randomRooms[0]);

        const bat = hazards.SUPER_BAT.create();
        movement.transport(bat, randomRooms[1]);

        const pit = hazards.PIT_TRAP.create();
        movement.transport(pit, randomRooms[2]);

        return entrance;
    },

    findWumpus(lair) {
        return app.entitiesByComponent("WUMPUS")
            .filter(w => w.ZONE_REF == lair)[0]
    },

    startleWumpus(lair) {
        const lairWumpus = this.findWumpus(lair);
        const lairWumpusAdjacent = lairWumpus.LOCATION.CONTAINER;
        if (!lairWumpus.DEAD && lairWumpusAdjacent.filter(c => c.CHARACTER).length == 0) {
            const rooms = lairWumpusAdjacent.filter(c => c.LINK).map(c => c.DIRECTION);
            movement.move(lairWumpus, rooms[random.getRandomInt(rooms.length)]);
        }
    }
}