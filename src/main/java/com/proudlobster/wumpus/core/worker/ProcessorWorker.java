package com.proudlobster.wumpus.core.worker;

import java.util.List;
import com.proudlobster.wumpus.core.entity.CoreComponent;
import com.proudlobster.wumpus.core.processor.Processor;
import com.proudlobster.wumpus.core.service.EntityService;

public class ProcessorWorker extends RepeatingListWorker<Processor> {

    private final EntityService entities;

    public ProcessorWorker(final long interval, final List<Processor> procs, final EntityService entities) {
        super(interval, procs);
        this.entities = entities;
    }

    @Override
    public void work(Processor item) {
        entities
                .byComponent(item.component().orElse(CoreComponent.IDENTIFIER))
                .forEach(item);
    }

    @Override
    public void run() {
        super.run();
    }

}
