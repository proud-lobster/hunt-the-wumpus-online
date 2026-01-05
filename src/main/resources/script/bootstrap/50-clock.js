export default {
    run() {

        if (app.entitiesByComponent("CLOCK").length > 0) {
            return;
        }

        const clock = app.createEntity();
        clock.CLOCK = true;
        clock.TICK = 0;
        clock.LAST_TICK_TIME = 0;

    }
}