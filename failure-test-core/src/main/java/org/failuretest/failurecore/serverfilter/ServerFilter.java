package org.failuretest.failurecore.serverfilter;

import org.failuretest.failurecore.servers.Server;

import java.util.function.Predicate;

public interface ServerFilter extends Predicate<Server> {
}
