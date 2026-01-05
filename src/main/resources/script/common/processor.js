import clock from "./clock.js";

export default {
    createTickProcessor(f) {
        return function (e) {
            const tick = clock.getTick();
            if (tick > (e.TICK ?? 0)) {
                e.TICK = tick;
                f(e);
            }
        }
    }
}