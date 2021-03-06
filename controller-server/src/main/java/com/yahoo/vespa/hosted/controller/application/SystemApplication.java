// Copyright 2018 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.controller.application;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.yahoo.config.provision.ApplicationId;
import com.yahoo.config.provision.NodeType;
import com.yahoo.vespa.hosted.controller.Controller;
import com.yahoo.vespa.hosted.controller.api.identifiers.DeploymentId;
import com.yahoo.vespa.hosted.controller.api.integration.configserver.ServiceConvergence;
import com.yahoo.vespa.hosted.controller.api.integration.zone.ZoneId;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This represents a system-level application in hosted Vespa. E.g. the zone-application.
 *
 * @author mpolden
 */
public enum SystemApplication {

    configServerHost(ApplicationId.from("hosted-vespa", "configserver-host", "default"), NodeType.confighost),
    proxyHost(ApplicationId.from("hosted-vespa", "proxy-host", "default"), NodeType.proxyhost),
    configServer(ApplicationId.from("hosted-vespa", "zone-config-servers", "default"), NodeType.config),
    zone(ApplicationId.from("hosted-vespa", "routing", "default"), ImmutableSet.of(NodeType.proxy, NodeType.host),
         configServerHost, proxyHost, configServer);

    private final ApplicationId id;
    private final Set<NodeType> nodeTypes;
    private final List<SystemApplication> dependencies;

    SystemApplication(ApplicationId id, NodeType nodeType, SystemApplication... dependencies) {
        this(id, Collections.singleton(nodeType), dependencies);
    }

    SystemApplication(ApplicationId id, Set<NodeType> nodeTypes, SystemApplication... dependencies) {
        if (nodeTypes.isEmpty()) {
            throw new IllegalArgumentException("Node types must be non-empty");
        }
        this.id = id;
        this.nodeTypes = ImmutableSet.copyOf(nodeTypes);
        this.dependencies = ImmutableList.copyOf(dependencies);
    }

    public ApplicationId id() {
        return id;
    }

    /** The node type(s) that are implicitly allocated to this */
    public Set<NodeType> nodeTypes() {
        return nodeTypes;
    }

    /** Returns the system applications that should upgrade before this */
    public List<SystemApplication> dependencies() { return dependencies; }

    /** Returns whether this system application has an application package */
    public boolean hasApplicationPackage() {
        return this == zone;
    }

    /** Returns whether config for this application has converged in given zone */
    public boolean configConvergedIn(ZoneId zone, Controller controller) {
        if (!hasApplicationPackage()) {
            return true;
        }
        // TODO: Docker hosts running host admin cannot be checked. Since a zone can have
        // Docker hosts running either host admin or node-admin, it's not possible to check
        // config convergence, so we need to always return true here.
        // We want to remove the line below and check config convergence for proxy nodes
        // when all Docker hosts are running host admin
        return true;
        /*
        return controller.configServer().serviceConvergence(new DeploymentId(id(), zone))
                         .map(ServiceConvergence::converged)
                         .orElse(false);
                         */
    }

    /** Returns the node types of this that should receive OS upgrades */
    public Set<NodeType> nodeTypesWithUpgradableOs() {
        return nodeTypes().stream().filter(NodeType::isDockerHost).collect(Collectors.toSet());
    }

    /** All known system applications */
    public static List<SystemApplication> all() {
        return ImmutableList.copyOf(values());
    }

    @Override
    public String toString() {
        return String.format("system application %s of type %s", id, nodeTypes);
    }

}
