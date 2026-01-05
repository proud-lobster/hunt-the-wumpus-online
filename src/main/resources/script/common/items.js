import creature from "./creature.js";

export default {
    createItem(name, desc, count = 1) {
        const i = app.createEntity();
        i.ITEM = true;
        i.COUNT = count;
        i.NAME = name;
        i.DESCRIPTIVE = desc;
        return i;
    },

    createWeapon(name, desc, attackType, powBonus, accBonus) {
        const w = this.createItem(name, desc);
        w.WEAPON = true;
        w.ATTACK_TYPE = attackType;
        w.POWER_BONUS = powBonus ?? 0;
        w.ACCURACY_BONUS = accBonus ?? 0;
        return w;
    },

    createAmmo(name, desc, attackType, powBonus, accBonus, count) {
        const a = this.createItem(name, desc, count);
        a.AMMO = true;
        a.ATTACK_TYPE = attackType;
        a.POWER_BONUS = powBonus ?? 0;
        a.ACCURACY_BONUS = accBonus ?? 0;
        return a;
    },

    weaponEffects: {
        crooked(s, t, mods) {
            if (t.WUMPUS) {
                const newMods = { ...mods };
                newMods.powerBonus = (newMods.powerBonus ?? 0) + creature(t).damageMitigation + 20;
                return newMods;
            } else {
                return mods;
            }
        }
    }
}