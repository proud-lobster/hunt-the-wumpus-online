import character from "./character.js";
import output from "./output.js";

export default {
    capitalize(s) {
        return s.charAt(0).toUpperCase() + s.slice(1);
    },

    entityTitle(e) {
        return e.DESCRIPTIVE ?? e.NAME;
    },

    entityName(e) {
        return e.NAME ?? e.DESCRIPTIVE;
    },

    entityListingName(e) {
        var prefix = "";
        prefix += e.DEAD ? "The corpse of " : "";
        prefix += e.CHARACTER ? "Adventurer " : "";
        return prefix + this.entityName(e);
    },

    examine(c, target = c.LOCATION) {
        const char = character(c);

        if (target.DETAILED) {
            char.print(this.entityTitle(target), output.modes.doubleBox);
            char.print(target.DETAILED);
        } else {
            char.print(this.entityTitle(target));
        }

        if (target.ROOM) {
            const contents = target.CONTAINER;

            const exits = contents
                .filter(c => c.LINK)
                .map(c => ` ${c.DIRECTION} - ${c.DESCRIPTIVE}`);
            if (exits.length > 0) {
                char.print("You see the following exits...\n" + (exits.join("\n")));
            }

            const prox = contents
                .filter(c => c.LINK)
                .map(c => c.LINK)
                .flatMap(c => c.CONTAINER)
                .filter(c => c.NEARBY_DESCRIPTIVE)
                .map(c => c.NEARBY_DESCRIPTIVE);
            if (prox.length > 0) {
                char.print(prox.join("\n"));
            }

            const named = contents
                .filter(c => c != char.entity)
                .filter(c => c.LOCATION)
                .map(c => this.entityListingName(c))
                .filter(e => !!e)
                .map(n => `${n} is here.`);
            if (named.length > 0) {
                char.print(named.join("\n"));
            }

            char.print("\n \n");
        }
    },

    buildPhrases(f, parts = {}) {
        const cap = this.capitalize;
        const firstPersonParts = {
            ...parts,
            subject: "you",
            subjectPossessive: "your",
            have: "have"
        };
        const thirdPersonParts = {
            ...parts,
            verb: parts.verb + "s",
            have: "has"
        };
        return {
            firstPerson(p = {}) {
                return cap(f({ ...firstPersonParts, ...p }));
            },
            thirdPerson(p = {}) {
                return cap(f({ ...thirdPersonParts, ...p }));
            }
        }

    }
}