const SecureRandom = Java.type('java.security.SecureRandom');
const RAND = new SecureRandom();

export default {
    getRandomInt(max) {
        return RAND.nextInt(max);
    },

    getDifferentRandomInts(max, count) {
        const results = [];
        while (results.length < count) {
            const r = this.getRandomInt(max);
            if (!results.includes(r)) {
                results.push(r);
            }
        }
        return results;
    },

    getPercent() {
        return this.getRandomInt(100) + 1;
    },

    getFuzz(base, range) {
        return (base + this.getRandomInt(range)) / 100;
    }
}