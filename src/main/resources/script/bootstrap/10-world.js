const ZONES = [{
    NAME: "Nowhere",
    DESCRIPTIVE: "The Middle of Nowhere",
    rooms: [{
        NAME: "Nowhere",
        DESCRIPTIVE: "The Middle of Nowhere",
        DETAILED: "You appear to be nowhere.  Everywhere you look: nothing.",
        WAYPOINT: "NOWHERE"
    }]
}, {
    NAME: "Tavern",
    DESCRIPTIVE: "The 'Tooth and Ale' Tavern",
    rooms: [{
        ref: "outside",
        NAME: "Outside the Tavern",
        DESCRIPTIVE: "Outside the 'Tooth and Ale' Tavern",
        DETAILED: "You stand outside of a quaint woodland shack. This unassuming structure is perhaps the northernmost stronghold of humanity, standing against the harsh frontier wilderness. All manner of hunter, explorer, and thrill-seeker gather here for a humble send-off, and sometimes for a celebratory return.",
        WAYPOINT: "TAVERN",
        links: [{
            ref: "inside",
            DIRECTION: "west",
            DESCRIPTIVE: "The door to the tavern, outlined in a warm, inviting glow"
        }]
    }, {
        ref: "inside",
        NAME: "Inside the Tavern",
        DESCRIPTIVE: "The Main Room of the 'Tooth and Ale' Tavern",
        DETAILED: "The musty hall of the tavern consists of naught more than a handful of tables, an untuned piano, an unkempt barman, and some sparse, dusty lanterns. However, the air is filled with the palpable legacy of those who have passed through it.",
        links: [{
            ref: "outside",
            DIRECTION: "east",
            DESCRIPTIVE: "The door out of the tavern, rattling in the cold artic wind"
        }]
    }]
}, {
    NAME: "Wilderness",
    DESCRIPTIVE: "The Great Northern Wilderness",
    rooms: []
}]

function getWorld() {
    const world = app.entitiesByComponent("WORLD")[0];
    if (world) {
        return world;
    } else {
        const nw = app.createEntity();
        nw.WORLD = true;
        nw.CONTAINER = [];
        return nw;
    }
}

function buildLinks(roomRefs) {
    Object.values(roomRefs).map((ref) => {
        ref.links.forEach((l) => {
            const le = app.createEntity();
            le.LINK = roomRefs[l.ref].entity;
            le.DIRECTION = l.DIRECTION;
            le.DESCRIPTIVE = l.DESCRIPTIVE;
            ref.entity.addContents(le);
        });
    });
}

function buildRooms(zoneEntity, roomDefs) {
    var roomRefs = {};

    roomDefs.forEach((rd) => {
        const re = app.createEntity();
        re.ROOM = true;
        re.ZONE_REF = zoneEntity;
        re.NAME = rd.NAME;
        re.DESCRIPTIVE = rd.DESCRIPTIVE;
        re.DETAILED = rd.DETAILED;
        re.WAYPOINT = rd.WAYPOINT ?? false;
        re.CONTAINER = [];
        zoneEntity.addContents(re);

        if (rd.ref) {
            roomRefs[rd.ref] = {
                entity: re,
                links: rd.links
            }
        }
    });

    buildLinks(roomRefs);
}

function createZone(def) {
    const z = app.createEntity();
    z.ZONE = true;
    z.NAME = def.NAME;
    z.DESCRIPTIVE = def.DESCRIPTIVE;
    buildRooms(z, def.rooms);
    return z;
}

export default {
    run() {
        const world = getWorld();
        const zoneNames = world.CONTAINER
            .filter(c => c.ZONE)
            .map(c => c.NAME);
        ZONES
            .filter(z => !zoneNames.includes(z.NAME))
            .map(createZone)
            .forEach(z => world.addContents(z));
    }
}