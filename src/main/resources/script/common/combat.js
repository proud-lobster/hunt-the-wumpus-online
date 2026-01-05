import character from "./character.js";
import creature from "./creature.js";
import items from "./items.js";
import movement from "./movement.js";
import narrative from "./narrative.js";
import random from "./random.js";

function hit(s, t, mods = {}) {
    const source = creature(s);
    const target = creature(t);
    const pow = source.attackPower + (mods.powerBonus ?? 0);
    const mit = target.damageMitigation + (mods.mitigationBonus ?? 0);
    const damage = pow - mit;

    if (damage > 0) {
        const critChance = damage + source.determination + (mods.critBonus ?? 0);
        const critReduction = target.resilience + (mods.critReductionBonus ?? 0);

        if (random.getPercent() <= critChance - critReduction) {
            mods.onCrit?.(t, s, damage);
        } else {
            mods.onHit?.(t, s, damage);
        }

        target.inflictDamage(damage);

    } else {
        mods.onMitigate?.(t, s);
    }
}

function attack(s, t, mods = {}) {
    const acc = creature(s).attackAccuracy + (mods.accuracyBonus ?? 0);
    const eva = creature(t).evasion + (mods.evasionBonus ?? 0);
    const perc = random.getPercent();

    if (perc <= acc - eva) {
        hit(s, t, mods);
    } else {
        mods.onMiss?.(t, s);
    }
}

function shoot(s, t, m = {}) {
    const char = character(s);
    const weapon = char.rangedWeapon;
    const ammo = char.ammo;

    var mods = {
        ...m,
        powerBonus: (weapon.POWER_BONUS ?? 0) + (ammo.POWER_BONUS ?? 0),
        accuracyBonus: (weapon.ACCURACY_BONUS ?? 0) + (ammo.ACCURACY_BONUS ?? 0)
    };

    if (t) {
        if (items.weaponEffects[weapon.EFFECT]) {
            mods = items.weaponEffects[weapon.EFFECT](s, t, mods);
        }

        if (items.weaponEffects[ammo.EFFECT]) {
            mods = items.weaponEffects[ammo.EFFECT](s, t, mods);
        }

        attack(s, t, mods);
    }

    if (ammo.COUNT < 2) {
        ammo.EXPIRED = true;
    } else {
        ammo.COUNT = ammo.COUNT - 1;
    }

    char.creature.basicCooldown = 2;
    char.creature.stamina.pay(2);
}

function strike(s, t, m = {}) {
    const char = character(s);
    const weapon = char.meleeWeapon;

    var mods = {
        ...m,
        powerBonus: weapon.POWER_BONUS ?? 0,
        accuracyBonus: weapon.ACCURACY_BONUS ?? 0
    };

    if (items.weaponEffects[weapon.EFFECT]) {
        mods = items.weaponEffects[weapon.EFFECT](s, t, mods);
    }

    attack(s, t, mods);

    char.creature.basicCooldown = 2;
    char.creature.stamina.pay(2);
}

const basicAttacks = {
    KNIFE: {
        mods: {
            onHit(t, s) {
                character(s).announce((p) => `${p.subjectPossessive} knife hits ${p.object} squarely!`, { object: t.NAME });
            },
            onCrit(t, s) {
                this.onHit(t, s);
            },
            onMitigate(t, s) {
                character(s).announce((p) => `${p.subjectPossessive} knife fails to wound ${p.object}!`, { object: t.NAME });
            },
            onMiss(t, s) {
                character(s).announce((p) => `${p.subjectPossessive} knife misses ${p.object}!`, { object: t.NAME });
            }
        },
        attack(s, t) {
            strike(s, t, this.mods);
        }
    },
    BOW: {
        mods: {
            onHit(t, s) {
                const char = character(s);
                const cre = creature(t);

                if (s.LOCATION.IDENTIFIER == t.LOCATION.IDENTIFIER) {
                    char.announce((p) => `${p.subjectPossessive} arrow hits ${p.object} squarely!`, { object: t.NAME });
                }
                else {
                    char.print("You hear your arrow make contact with flesh!");
                    cre.printEmit(`${t.NAME} is struck by an arrow from a distant source.`);
                }
            },

            onCrit(t, s) {
                this.onHit(t, s);
            },

            onMitigate(t, s) {
                const char = character(s);
                const cre = creature(t);

                if (s.LOCATION.IDENTIFIER == t.LOCATION.IDENTIFIER) {
                    char.announce((p) => `${p.subjectPossessive} arrow bounces off of ${p.object}!`, { object: t.NAME });
                }
                else {
                    char.print("You hear the arrow hit a target, but to no effect.");
                    cre.printEmit(`An arrow from a distant source bounces off of ${t.NAME}.`);
                }
            },

            onMiss(t, s) {
                const char = character(s);
                const cre = creature(t);

                if (s.LOCATION.IDENTIFIER == t.LOCATION.IDENTIFIER) {
                    char.announce((p) => `${p.subjectPossessive} arrow misses ${p.object}!`, { object: t.NAME });
                }
                else {
                    char.print("You hear the arrow hit the ground with a soft plunk, finding no target.");
                    cre.printEmit("An arrow hits the ground nearby.");
                }
            }
        },
        attack(s, t) {
            shoot(s, t, this.mods);
        }
    }
};

const specialAttacks = {
    CHOMP: {
        mods: {
            accuracyBonus: -10,
            powerBonus: 15,
            onCrit(t, s, d) {
                // TODO crit effect of chomp (inflict conditions)
                this.onHit(t, s, d);
            },
            onHit(t, s, d) {
                character(t).announce((p) => `CHOMP!  The savage bite of the wumpus punctures ${p.subjectPossessive} flesh.`);
            },
            onMitigate(t, s) {
                character(t).announce((p) => `The wumpus bites into ${p.subject} but fails to cause much harm.`);
            },
            onMiss(t, s) {
                character(t).announce((p) => `${p.subject} ${p.verb} nimbly away from the wumpus's teeth.`, { verb: "move" });
            }
        },
        attack(s, t) {
            attack(s, t, this.mods);
        }
    },
    WOOSH: {
        mods: {
            onCrit(t, s, d) {
                this.onMitigate(t, s);
            },
            onHit(t, s, d) {
                this.onMitigate(t, s);
            },
            onMitigate(t, s) {
                const char = character(t);
                char.announce((p) => `WOOSH!  The bat picks up ${p.subject} and flies off to another area.`);
                const rooms = t.LOCATION.ZONE_REF.CONTAINER.filter(c => c.ROOM);
                var newRoom;
                do {
                    newRoom = rooms[random.getRandomInt(rooms.length)];
                } while (newRoom == t.LOCATION);
                movement.transport(t, newRoom);
                movement.transport(s, newRoom);
                narrative.examine(t, newRoom);
                char.print("The bat discards you harshly on the floor of a new area.");
                char.printEmit(`A giant bat flies in to the room, carrying ${t.NAME}.  The bat then discards them harshly on to the floor.`);
            },
            onMiss(t, s) {
                character(t).announce((p) => `The bat's claws fail to connect with ${p.subject}.`);
            }
        },
        attack(s, t) {
            attack(s, t, this.mods);
        }
    }
};

export default {
    attack,
    hit,
    basicAttacks,
    specialAttacks
}