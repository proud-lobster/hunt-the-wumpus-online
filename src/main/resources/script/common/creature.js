import player from "./player.js";
import movement from "./movement.js";
import narrative from "./narrative.js";
import output from "./output.js";
import hazards from "./hazards.js";

const cooldownPeriods = {
    "STAMINA": 2,
    "WILLPOWER": 12,
    "ENERGY": 30
}

class Pool {
    constructor(char, poolName, cooldownF) {
        this.char = char;
        this.poolName = poolName;
        this.cooldownF = cooldownF;
    }

    get value() {
        return this.char[this.poolName + "_POOL"];
    }

    set value(v) {
        this.char[this.poolName + "_POOL"] = v;
    }

    get cap() {
        return this.char[this.poolName + "_CAP"];
    }

    get percent() {
        return Math.trunc(this.value * 100 / this.cap);
    }

    get cooldown() {
        return this.cooldownF(this.poolName);
    }

    set cooldown(duration) {
        return this.cooldownF(this.poolName, duration);
    }

    isEmpty() {
        return this.value <= 0;
    }

    isFull() {
        return this.value >= this.cap;
    }

    restore(amount) {
        this.value = Math.min(this.value + amount, this.cap);
        if (this.char.PLAYER_REF) {
            player(this.char.PLAYER_REF).refreshHud();
        }
    }

    pay(cost) {
        if (this.char.DEAD) {
            return;
        }
        const post = this.value - cost;
        this.value = Math.max(post, 0);
        if (post < 0) {
            this.payOverflow(post * -1);
        }
        if (this.char.PLAYER_REF) {
            player(this.char.PLAYER_REF).refreshHud();
        }
    }

    payOverflow(cost) {
        const pools = [
            new Pool(this.char, "STAMINA"),
            new Pool(this.char, "WILLPOWER"),
            new Pool(this.char, "ENERGY")
        ]

        const remainingPools = pools.filter(p => p.value != 0);

        if (remainingPools.length == 1) {
            remainingPools[0].pay(cost * 2);
        } else if (remainingPools.length > 1) {
            remainingPools.forEach(p => p.pay(cost));
        }
    }

    tick() {
        if (!this.isFull() && !this.cooldown) {
            this.restore(1);
            this.cooldown = cooldownPeriods[this.poolName];
        }
    }
}

export default function (e) {
    if (!e.CREATURE) {
        throw Error(`${e.IDENTIFIER} is not a creature.`);
    }

    function attributePreset(attr) {
        return hazards?.[e.HAZARD]?.[attr];
    }

    function cooldown(flag, duration) {
        const cd = e.CONTAINER?.filter(c => c.COOLDOWN).filter(c => c.COOLDOWN_FLAG == flag)[0];
        if (!duration) {
            return cd?.COOLDOWN ?? 0;
        } else {
            if (cd) {
                cd.COOLDOWN = duration;
            } else {
                const cooldown = app.createEntity();
                cooldown.COOLDOWN = duration;
                cooldown.LINK = e;
                cooldown.COOLDOWN_FLAG = flag;
                e.addContents(cooldown);
            }
        }
    }

    return {
        entity: e,
        stamina: new Pool(e, "STAMINA", cooldown),
        willpower: new Pool(e, "WILLPOWER", cooldown),
        energy: new Pool(e, "ENERGY", cooldown),

        get name() {
            return narrative.entityName(e);
        },

        get zone() {
            if (!!e.LOCATION && !!e.LOCATION.ZONE_REF) {
                return e.LOCATION.ZONE_REF;
            } else {
                return movement.getWaypoint("NOWHERE").ZONE_REF;
            }
        },

        get attackPower() {
            return attributePreset("attackPower") ?? (e.STRENGTH * 2);
        },
        get damageMitigation() {
            return attributePreset("damageMitigation") ?? e.STRENGTH;
        },
        get attackAccuracy() {
            return attributePreset("attackAccuracy") ?? (75 + (e.DEXTERITY * 8));
        },
        get evasion() {
            return attributePreset("evasion") ?? (e.DEXTERITY * 5);
        },
        get spellPower() {
            return attributePreset("spellPower") ?? (e.KNOWLEDGE * 2);
        },
        get deduction() {
            return attributePreset("deduction") ?? (40 + (e.KNOWLEDGE * 10));
        },
        get spellAccuracy() {
            return attributePreset("spellAccuracy") ?? (75 + (e.FOCUS * 8));
        },
        get concentration() {
            return attributePreset("concentration") ?? e.FOCUS;
        },
        get resilience() {
            return attributePreset("resilience") ?? (e.DEVOTION * 3);
        },
        get determination() {
            return attributePreset("determination") ?? (e.DEVOTION * 3);
        },
        get charisma() {
            return attributePreset("charisma") ?? (5 + (e.PRESENCE * 6));
        },
        get composure() {
            return attributePreset("composure") ?? (50 + (e.PRESENCE * 8));
        },
        get basicCooldown() {
            return cooldown("BASIC");
        },
        set basicCooldown(duration) {
            cooldown("BASIC", duration);
        },
        get specialCooldown() {
            return cooldown("SPECIAL");
        },
        set specialCooldown(duration) {
            cooldown("SPECIAL", duration);
        },

        tick() {
            this.stamina.tick();
            this.willpower.tick();
            this.energy.tick();
        },

        inflictDamage(damage) {
            const spreadDamage = Math.floor(damage / 3);
            const staminaDamage = damage % 3;
            this.stamina.pay(spreadDamage + staminaDamage);
            this.willpower.pay(spreadDamage);
            this.energy.pay(spreadDamage);
        },

        printEmit(m) {
            output.print(e.LOCATION, m, [x => x != e]);
        },

        move(loc) {
            const newRoom = loc.LINK ?? loc;
            const direction = loc.DIRECTION ? movement.directions[loc.DIRECTION] : movement.directions.elsewhere;
            this.printEmit(`${this.entity.NAME} has departed ${direction.departureDescription}`);
            movement.transport(this.entity, newRoom);
            this.printEmit(`${this.entity.NAME} has arrived from ${direction.arrivalDescription}`);
            this.stamina.pay(1);
        }

    };

}