export default {
    component: "CLOCK",

    apply(e) {
        const time = Date.now();
        if (time >= e.LAST_TICK_TIME + 1000) {
            e.TICK += 1;
            e.LAST_TICK_TIME = time;
        }
    }
}