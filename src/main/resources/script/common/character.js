import player from "./player.js";
import creature from "./creature.js";
import random from "./random.js";
import output from "./output.js";
import narrative from "./narrative.js";
import movement from "./movement.js";
import items from "./items.js";
import combat from "./combat.js";
import hazards from "./hazards.js";

const NAME_SYLLABLES_MIN = 2;
const NAME_SYLLABLES_RANGE = 2;
const AGE_MIN = 16;
const AGE_RANGE = 5;
const RENOWN_RANGE = 3;
const WEALTH_RANGE = 10;
const POOL_MIN = 5;
const POOL_LEVEL_MULT = 3;
const ADVERSITY_CHANCE_MAX = 20;
const ADVERSITY_RANK_PENALTY_MULT = 5;
const ADVERSITY_GROWTH_BONUS = 10;
const EXP_FUZZ_BASE = 30;
const EXP_FUZZ_RANGE = 50;
const ANNUAL_EXP = 400;
const BASE_GROWTHS = [100, 100, 100, 100, 100, 100, 100, 100, 100];
const GROWTH_SET_SIZE = 3;
const APPLIED_XP_DIVISOR = BASE_GROWTHS.length * 100 * 2;

const NAME_SYLLABLES = [
    "al",
    "ack",
    "an",
    "az",
    "bak",
    "bal",
    "ban",
    "bel",
    "ber",
    "bo",
    "del",
    "dak",
    "def",
    "edd",
    "ehl",
    "em",
    "en",
    "est",
    "fen",
    "fig",
    "fo",
    "fon",
    "gho",
    "gul",
    "han",
    "hif",
    "in",
    "ir",
    "ja",
    "jem",
    "ken",
    "kit",
    "maz",
    "mic",
    "mo",
    "nev",
    "nik",
    "ol",
    "pev",
    "tal",
    "to",
    "ty",
    "uf",
    "ul",
    "vy",
    "wen",
    "ya",
    "yen",
    "zan",
    "zin"
];

const GROWTH_CLASSES = [{
    description: "athletic",
    growths: [20, 20, 20, 0, 0, 0, 0, 0, 0]
}, {
    description: "mindful",
    growths: [0, 0, 0, 20, 20, 20, 0, 0, 0]
}, {
    description: "spirited",
    growths: [0, 0, 0, 0, 0, 0, 20, 20, 20]
}, {
    description: "powerful",
    growths: [20, 0, 0, 20, 0, 0, 20, 0, 0]
}, {
    description: "skillful",
    growths: [0, 20, 0, 0, 20, 0, 0, 20, 0]
}, {
    description: "unyielding",
    growths: [0, 0, 20, 0, 0, 20, 0, 0, 20]
}];

const LEVELS = [
    { level: 0, nextAt: 600 },
    { level: 1, nextAt: 800 },
    { level: 2, nextAt: 1100 },
    { level: 3, nextAt: 1600 },
    { level: 4, nextAt: 2400 },
    { level: 5, nextAt: 3700 },
    { level: 6, nextAt: 9999 }
]

function generateName() {
    const syllableCount = NAME_SYLLABLES_MIN + random.getRandomInt(NAME_SYLLABLES_RANGE);
    const name = random.getDifferentRandomInts(NAME_SYLLABLES.length, syllableCount)
        .map(i => NAME_SYLLABLES[i])
        .join("");
    return name.charAt(0).toUpperCase() + name.slice(1);
}

function mergeGrowths(g1, g2) {
    return g1.map((v, i) => v + g2[i]);
}

function levelFromExperience(exp) {
    for (const { nextAt, level } of LEVELS) {
        if (exp < nextAt) {
            return level;
        }
    }
}

class Lifestyle {
    constructor(description, rank) {
        this.description = description;
        this.rank = rank;
    }

    generateSurname() {
        return this.rank > 2 ? generateName() : false;
    }

    generateRenown() {
        return this.rank + random.getRandomInt(RENOWN_RANGE);
    }

    generateWealth() {
        return (this.rank * 5) + random.getRandomInt(WEALTH_RANGE);
    }

    generateAdversityGrowths() {
        const adversityChance = ADVERSITY_CHANCE_MAX - (ADVERSITY_RANK_PENALTY_MULT * this.rank);
        return Array(BASE_GROWTHS.length)
            .fill()
            .map(g => random.getPercent())
            .map(g => g < adversityChance)
            .map(g => g ? ADVERSITY_GROWTH_BONUS : 0);
    }
}

const LIFESTYLES = [
    new Lifestyle("squalid", 0),
    new Lifestyle("humble", 1),
    new Lifestyle("moderate", 2),
    new Lifestyle("affluent", 3),
    new Lifestyle("noble", 4)
];

class Attribute {
    constructor(char, attr) {
        this.char = char;
        this.attr = attr;
    }

    get value() {
        return this.char[this.attr];
    }

    get experience() {
        return this.char[this.attr + "_EXP"];
    }

    get growth() {
        return this.char[this.attr + "_GROWTH"];
    }

    get progressTnl() {
        var prev = 0;
        for (const { nextAt } of LEVELS) {
            if (this.experience < nextAt) {
                return Math.floor((this.experience - prev) / (nextAt - prev) * 100);
            } else {
                prev = nextAt;
            }
        }
    }
}

function wrap(e) {
    const c = creature(e);

    const strength = new Attribute(e, "STRENGTH");
    const dexterity = new Attribute(e, "DEXTERITY");
    const stamina = new Attribute(e, "STAMINA");
    const knowledge = new Attribute(e, "KNOWLEDGE");
    const focus = new Attribute(e, "FOCUS");
    const willpower = new Attribute(e, "WILLPOWER");
    const devotion = new Attribute(e, "DEVOTION");
    const presence = new Attribute(e, "PRESENCE");
    const energy = new Attribute(e, "ENERGY");

    return {
        entity: e,
        creature: c,

        strength,
        dexterity,
        stamina,
        knowledge,
        focus,
        willpower,
        devotion,
        presence,
        energy,

        get player() {
            return player(e.PLAYER_REF);
        },
        get active() {
            return e.PLAYER_REF.ACTIVE_CHARACTER_REF == e;
        },
        get fullName() {
            return e.NAME + (e.SURNAME ? " " + e.SURNAME : "");
        },
        get items() {
            return e.CONTAINER
                .filter(i => i.ITEM);
        },
        get equipment() {
            return this.items
                .filter(i => i.EQUIPPED);
        },
        get meleeWeapon() {
            return this.equipment
                .filter(c => c.MELEE)
                .filter(c => c.WEAPON)[0];
        },
        get rangedWeapon() {
            return this.equipment
                .filter(c => c.RANGE)
                .filter(c => c.WEAPON)[0];
        },
        get ammo() {
            return this.equipment
                .filter(c => c.AMMO)[0];
        },
        set active(a) {
            if (a) {
                this.player.entity.ACTIVE_CHARACTER_REF = e;
                if (e.LOCATION) {
                    narrative.examine(e);
                    this.player.refreshHud();
                } else if (e.LAST_LOCATION) {
                    this.move(e.LAST_LOCATION);
                    e.LAST_LOCATION = false;
                } else {
                    this.move(movement.getWaypoint("TAVERN"));
                }
            } else {
                this.player.entity.ACTIVE_CHARACTER_REF = false;
                if (e.LOCATION) {
                    e.LOCATION.removeContents(e);
                    e.LAST_LOCATION = e.LOCATION;
                    e.LOCATION = false;
                }
            }
        },
        move(loc) {
            const newRoom = loc.LINK ?? loc;
            this.creature.move(loc);
            narrative.examine(this.entity, newRoom);
            hazards.trigger(this.entity, newRoom);
            this.player.refreshHud();
        },
        print(m, mode = output.modes.plain) {
            player(e.PLAYER_REF).print(m, mode);
        },
        printEmit(m) {
            output.print(e.LOCATION, m, [x => x != e]);
        },
        announce(f, p = {}) {
            const phrases = narrative.buildPhrases(f, p);
            this.print(phrases.firstPerson());
            this.printEmit(phrases.thirdPerson({ subject: e.NAME }));
        },
        shoot(t) {
            combat.basicAttacks[this.rangedWeapon.ATTACK_TYPE].attack(this.entity, t);
        },
        strike(t) {
            combat.basicAttacks[this.meleeWeapon.ATTACK_TYPE].attack(this.entity, t);
        }
    };
}

function create() {

    // Generate Attributes
    const lifestyle = LIFESTYLES[random.getRandomInt(LIFESTYLES.length)];
    const age = AGE_MIN + random.getRandomInt(AGE_RANGE);

    const growthClasses = random.getDifferentRandomInts(GROWTH_CLASSES.length, GROWTH_SET_SIZE)
        .map(i => GROWTH_CLASSES[i]);
    const growths = [
        ...(growthClasses.map(c => c.growths)),
        lifestyle.generateAdversityGrowths()
    ].reduce(mergeGrowths, BASE_GROWTHS);

    const attributeXp = growths
        .map(g => g * ANNUAL_EXP * age)
        .map(x => x + (random.getFuzz(EXP_FUZZ_BASE, EXP_FUZZ_RANGE) * x))
        .map(x => x / APPLIED_XP_DIVISOR)
        .map(Math.trunc);
    const attributeValues = attributeXp
        .map(levelFromExperience);

    const staminaPool = POOL_MIN + (POOL_LEVEL_MULT * attributeValues[2]);
    const willpowerPool = POOL_MIN + (POOL_LEVEL_MULT * attributeValues[5]);
    const energyPool = POOL_MIN + (POOL_LEVEL_MULT * attributeValues[8]);

    // Create...
    const c = app.createEntity();
    c.CHARACTER = true;
    c.CREATURE = true;

    // Demographics
    c.NAME = generateName();
    c.SURNAME = lifestyle.generateSurname();
    c.AGE = age;
    c.LIFESTYLE = lifestyle.rank;
    c.RENOWN = lifestyle.generateRenown();
    c.WEALTH = lifestyle.generateWealth();

    // Attribute Growths
    c.STRENGTH_GROWTH = growths[0];
    c.DEXTERITY_GROWTH = growths[1];
    c.STAMINA_GROWTH = growths[2];
    c.KNOWLEDGE_GROWTH = growths[3];
    c.FOCUS_GROWTH = growths[4];
    c.WILLPOWER_GROWTH = growths[5];
    c.DEVOTION_GROWTH = growths[6];
    c.PRESENCE_GROWTH = growths[7];
    c.ENERGY_GROWTH = growths[8];

    // Attribute Experience
    c.STRENGTH_EXP = attributeXp[0];
    c.DEXTERITY_EXP = attributeXp[1];
    c.STAMINA_EXP = attributeXp[2];
    c.KNOWLEDGE_EXP = attributeXp[3];
    c.FOCUS_EXP = attributeXp[4];
    c.WILLPOWER_EXP = attributeXp[5];
    c.DEVOTION_EXP = attributeXp[6];
    c.PRESENCE_EXP = attributeXp[7];
    c.ENERGY_EXP = attributeXp[8];

    // Attribute Values
    c.STRENGTH = attributeValues[0];
    c.DEXTERITY = attributeValues[1];
    c.STAMINA = attributeValues[2];
    c.KNOWLEDGE = attributeValues[3];
    c.FOCUS = attributeValues[4];
    c.WILLPOWER = attributeValues[5];
    c.DEVOTION = attributeValues[6];
    c.PRESENCE = attributeValues[7];
    c.ENERGY = attributeValues[8];

    // Attribute Pools
    c.STAMINA_POOL = staminaPool;
    c.STAMINA_CAP = staminaPool;
    c.WILLPOWER_POOL = willpowerPool;
    c.WILLPOWER_CAP = willpowerPool;
    c.ENERGY_POOL = energyPool;
    c.ENERGY_CAP = energyPool;

    const knife = items.createWeapon("a knife", "a family heirloom hunting knife", "KNIFE", 2);
    knife.EQUIPPED = true;
    knife.MELEE = true;
    c.addContents(knife);

    const bow = items.createWeapon("a bow", "a family heirloom hunting bow", "BOW");
    bow.EQUIPPED = true;
    bow.RANGE = true;
    c.addContents(bow);

    const arrow = items.createAmmo("a crooked arrow", "a family heirloom crooked arrow", "ARROW", 6, 20, 10);
    arrow.EQUIPPED = true;
    arrow.EFFECT = "crooked";
    c.addContents(arrow);

    c.INTRODUCTION = `
Your name is ${c.NAME}${c.SURNAME ? " " + c.SURNAME : ""}.
You are known to be ${growthClasses[0].description}, ${growthClasses[1].description}, and ${growthClasses[2].description}.
Leaving behind your ${lifestyle.description} upbringing at ${age} years of age you have turned to a life of adventure.
Equiped with the heirloom hunting bow, knife, lantern, and a single crooked arrow bequeathed to you by your ancestors, you embark for the arctic north in search of legendary prey: The Wumpus!
            `;

    return wrap(c);
}

export default function (e) {
    if (!e) {
        return {
            create
        }
    }

    if (!e.CHARACTER) {
        throw Error(`${e.IDENTIFIER} is not a character.`);
    }

    return wrap(e);
}