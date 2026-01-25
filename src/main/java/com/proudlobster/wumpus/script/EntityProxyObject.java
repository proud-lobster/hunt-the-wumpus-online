package com.proudlobster.wumpus.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import com.proudlobster.wumpus.core.entity.Component;
import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.service.ComponentService;
import com.proudlobster.wumpus.core.service.EntityService;

/**
 * A GraalVM proxy object for an entity.
 */
public class EntityProxyObject implements ProxyObject, Entity {

    private final ScriptInterface intf;
    private final Entity e;
    private final Map<String, ProxyExecutable> execs;

    public EntityProxyObject(final ScriptInterface intf, final Entity e) {
        this.intf = intf;
        this.e = e;
        this.execs = Map.of(
                "addContents", this::addContents,
                "removeContents", this::removeContents,
                "persist", this::persist,
                "expire", this::expire,
                "vacate", this::vacate);
    }

    private Object addContents(final Value... vs) {
        return e.addContents(Arrays.stream(vs)
                .map(intf::unwrap)
                .toList()
                .toArray(new Entity[0]));
    }

    private Object removeContents(final Value... vs) {
        return e.removeContents(Arrays.stream(vs)
                .map(intf::unwrap)
                .toList()
                .toArray(new Entity[0]));
    }

    private Object persist(final Value... vs) {
        return e.persist();
    }

    private Object expire(final Value... vs) {
        return e.expire();
    }

    public Object vacate(final Value... vs) {
        e.vacate();
        return null;
    }

    private Object memberValue(final Component c) {
        switch (c.type()) {
            case MULTIREF:
                return e.referenceIdentifiers(c).stream()
                        .map(intf::resolveReference)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();
            case NUMBER:
                return e.longValue(c);
            case REFERENCE:
                return intf.resolveReference(e.longValue(c))
                        .map(Object.class::cast)
                        .orElse(false);
            case STRING:
                return e.stringValue(c);
            case NONE:
            default:
                return true;
        }
    }

    private List<Object> memberList() {
        return Stream.concat(
                e.delegate()
                        .keySet()
                        .stream()
                        .map(Component::name),
                execs
                        .keySet()
                        .stream())
                .collect(Collectors.toList());
    }

    public Entity original() {
        return e;
    }

    @Override
    public Map<Component, String> delegate() {
        return e.delegate();
    }

    @Override
    public Object getMember(String key) {
        return e.componentService()
                .get(key)
                .map(this::memberValue)
                .orElseGet(() -> execs.get(key));
    }

    @Override
    public Object getMemberKeys() {
        return ProxyArray.fromList(memberList());
    }

    @Override
    public boolean hasMember(String key) {
        return memberList().contains(key);
    }

    @Override
    public void putMember(String key, Value value) {
        final Component c = e.componentService().getOperational(key);

        if (value.hasArrayElements()) {
            final ArrayList<Long> ids = new ArrayList<>();
            for (int i = 0; i < value.getArraySize(); i++) {
                ids.add(intf.unwrap(value.getArrayElement(i)).identifier());
            }
            e.addComponent(c, ids.toArray(new Long[0]));
        } else if (value.isProxyObject()) {
            e.addComponent(c, intf.unwrap(value).identifier());
        } else if (value.isBoolean()) {
            if (value.asBoolean()) {
                e.addComponent(c);
            } else {
                e.removeComponent(c);
            }
        } else if (value.isNumber()) {
            if (value.fitsInLong()) {
                e.addComponent(c, value.asLong());
            }
        } else {
            e.addComponent(c, value.asString());
        }

    }

    @Override
    public Entity addComponent(Component c, String v) {
        e.addComponent(c, v);
        return this;
    }

    @Override
    public Entity removeComponent(Component c) {
        e.removeComponent(c);
        return this;
    }

    @Override
    public Entity copyFrom(Entity e) {
        this.e.copyFrom(e);
        return this;
    }

    @Override
    public EntityService service() {
        return e.service();
    }

    @Override
    public ComponentService componentService() {
        return e.componentService();
    }

}
