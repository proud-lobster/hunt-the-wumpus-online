const MIN_LINE_WIDTH = 80;

const BOX_L_H = '\u2500';
const BOX_H_H = '\u2501';
const BOX_L_V = '\u2502';
const BOX_H_V = '\u2503';
const BOX_D_H = '\u2550';
const BOX_D_V = '\u2551';
const BOX_D_DR = '\u2554';
const BOX_D_DL = '\u2557';
const BOX_D_UR = '\u255A';
const BOX_D_UL = '\u255D';

function padding(length) {
    return ' '.repeat(length);
}

function padCenter(str, width) {
    const padSize = width - str.length;
    const padStart = Math.floor(padSize / 2);
    return padding(padStart) + str + padding(padSize - padStart);
}

function rowToColumns(t) {
    const columns = t.split("|");
    const columnWidth = Math.floor(MIN_LINE_WIDTH / columns.length);
    return columns.map(c => padCenter(c, columnWidth)).join('');
}

const modes = {
    plain(t) {
        return t;
    },
    doubleBox(t) {
        const top = BOX_D_DR + BOX_D_H.repeat(t.length + 2) + BOX_D_DL;
        const mid = BOX_D_V + " " + t + " " + BOX_D_V;
        const bot = BOX_D_UR + BOX_D_H.repeat(t.length + 2) + BOX_D_UL;
        return top + "\n" + mid + "\n" + bot;
    },
    columns(t) {
        return t.split("\n")
            .map(r => rowToColumns(r))
            .join("\n");
    }
}

function reduceFilters(acc, fn) {
    return function (i) {
        return acc(i) && fn(i);
    }
}

export default {
    modes,

    send(target, command, message, filters = [], mode = modes.plain) {
        if (target) {
            if (target.PLAYER) {
                app.sendMessage(target, command, mode(message));
            } else if (target.PLAYER_REF) {
                this.send(target.PLAYER_REF, command, message, filters, mode);
            } else if (target.CONTAINER) {
                target.CONTAINER
                    .filter(filters.reduce(reduceFilters, () => true))
                    .forEach(t => this.send(t, command, message, filters, mode));
            }
        }
    },

    print(target, message, filters = [], mode = modes.plain) {
        this.send(target, "print", message, filters, mode);
    },

    data(target, data) {
        this.send(target, "data", data);
    }
}