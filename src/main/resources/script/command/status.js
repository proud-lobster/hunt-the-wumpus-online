import command from "../common/command.js";
import output from "../common/output.js";

function displayAttribute(a) {
    return `${a.value} (${a.progressTnl}%)`;
}

function characterStatus(c) {
    return `
${c.fullName}|${c.entity.AGE} years old
 
STRENGTH|DEXTERITY|STAMINA
${displayAttribute(c.strength)}|${displayAttribute(c.dexterity)}|${displayAttribute(c.stamina)}
KNOWLEDGE|FOCUS|WILLPOWER
${displayAttribute(c.knowledge)}|${displayAttribute(c.focus)}|${displayAttribute(c.willpower)}
DEVOTION|PRESENCE|ENERGY
${displayAttribute(c.devotion)}|${displayAttribute(c.presence)}|${displayAttribute(c.energy)}
 
WEALTH|RENOWN
${c.entity.WEALTH} gold|${c.entity.RENOWN}
    `
}

function f(char) {
    char.print(characterStatus(char), output.modes.columns);

}

export default {
    name: "status",
    aliases: "st, stat",
    apply: command.createCharacterCommand(f)
}