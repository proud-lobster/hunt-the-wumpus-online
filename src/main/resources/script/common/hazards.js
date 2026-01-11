import character from "./character.js";
import combat from "./combat.js";
import creature from "./creature.js";
import random from "./random.js";

function createHazard(h, desc, prox) {
    const e = app.createEntity();
    e.HAZARD = h;
    e.DESCRIPTIVE = desc;
    e.NEARBY_DESCRIPTIVE = prox;
    e.persist();
    return e;
}

function createEnemy(h, name, desc, prox, stats) {
    const e = createHazard(h, desc, prox);
    e.CREATURE = true;
    e.ENEMY = true;
    e.NAME = name;
    e.STAMINA_CAP = stats.stamina;
    e.STAMINA_POOL = stats.stamina;
    e.WILLPOWER_CAP = stats.willpower;
    e.WILLPOWER_POOL = stats.willpower;
    e.ENERGY_CAP = stats.energy;
    e.ENERGY_POOL = stats.energy;
    e.BASIC_READY = true;
    e.SPECIAL_READY = true;
    e.persist();
    return e;
}

export default {
    trigger(char, target) {
        if (target.HAZARD) {
            this[target.HAZARD].onTrigger(char, target);
        } else if (target.ROOM) {
            target.CONTAINER
                .filter(h => h.HAZARD)
                .filter(h => !h.DEAD)
                .forEach(h => this.trigger(char, h));
        }
    },
    YOUNG_WUMPUS: {

        special(s, t) {
            if (!s.SPECIAL_READY) {
                return;
            }
            const char = character(t);
            const cre = creature(s);
            char.announce((p) => `The young wumpus opens his jaws wide and lunges toward ${p.subject}.`);
            combat.specialAttacks.CHOMP.attack(s, t);
            cre.basicCooldown = 3;
            cre.specialCooldown = 10;
        },

        onTrigger(c, h) {
            this.special(h, c);
        },

        create(lair) {
            const w = createEnemy("YOUNG_WUMPUS", "a young wumpus", "a terrifying and odorous young wumpus", "You smell a foul odor", this);
            w.WUMPUS = 3;
            w.ZONE_REF = lair;
            return w;
        },

        attackPower: 20,
        damageMitigation: 10,
        attackAccuracy: 90,
        evasion: 0,
        deduction: 40,
        resilience: 10,
        determination: 10,
        stamina: 20,
        willpower: 5,
        energy: 10,
    },

    CAVE_SLIME: {
        onTrigger(c, h) { },
        create() {
            return createEnemy("CAVE_SLIME", "a cave slime", "A gooey, green, animated cave slime", "You hear a low squelching noise", this);
        },

        attackPower: 5,
        damageMitigation: 2,
        attackAccuracy: 75,
        evasion: 0,
        deduction: 10,
        resilience: 0,
        determination: 0,
        stamina: 10,
        willpower: 0,
        energy: 5
    },

    SUPER_BAT: {
        special(s, t) {
            if (!s.SPECIAL_READY) {
                return;
            }

            const cre = creature(t);
            character(t).announce((p) => `The bat lunges at ${p.subject} with outstretched claws.`);
            combat.specialAttacks.WOOSH.attack(s, t);
            cre.basicCooldown = 3;
            cre.specialCooldown = 10;
        },

        onTrigger(c, h) {
            this.special(h, c);
        },

        create() {
            return createEnemy("SUPER_BAT", "a super bat", "A gigantic flapping bat", "You hear the sound of flapping wings", this);
        },

        attackPower: 4,
        damageMitigation: 1,
        attackAccuracy: 85,
        evasion: 15,
        deduction: 25,
        resilience: 0,
        determination: 0,
        stamina: 10,
        willpower: 0,
        energy: 5,

    },

    PIT_TRAP: {
        onTrigger(c, h) {
            const char = character(c);
            char.print("The stone ledge beneath your feet starts to crumble...");
            char.printEmit(`The ground underneath ${c.NAME} crumbles away...`);
            if (random.getPercent() < 20 + char.creature.evasion) {
                char.announce((p) => `${p.subject} ${p.verb} swiftly back on to solid ground.`, { verb: "jump" });
            } else {
                char.print("You plummet into the dark chasm below, and after a short while make abrupt contact with the ground...");
                char.printEmit(`${c.NAME} plummets in to the dark pit.`);
                char.creature.inflictDamage(1000);
            }
            h.LOCATION.removeContents(h);
            h.EXPIRED = true;
            // TODO create pit
        },
        create() {
            const p = createHazard("PIT_TRAP", "A rapidly deteriorating cliff ledge", "You feel a gentle breeze nearby");
            // TODO come back and implement concealment/deduction later
            p.CONCEALED = 100;
            return p;
        }
    }
}