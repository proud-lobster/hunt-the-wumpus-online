export default {
    getTick() {
        return app.entitiesByComponent("CLOCK")[0].TICK;
    }
}