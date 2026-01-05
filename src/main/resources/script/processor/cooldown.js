import clock from "../common/clock.js";

export default {
    component: "COOLDOWN",

    apply(e) {
        const tick = clock.getTick();

        if (!e.TICK || tick > e.TICK) {
            e.TICK = tick;

            if (e.COOLDOWN > 0) {
                e.COOLDOWN -= 1;
            }
        }
    }
}