/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.aws.traits.clientendpointdiscovery;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.OperationShape;
import software.amazon.smithy.model.shapes.ServiceShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.ErrorTrait;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.model.transform.ModelTransformer;
import software.amazon.smithy.model.transform.ModelTransformerPlugin;

/**
 * Removes the endpoint discovery trait from a service if the referenced operation or error are removed.
 */
public class CleanClientDiscoveryTraitTransformer implements ModelTransformerPlugin {
    @Override
    public Model onRemove(ModelTransformer transformer, Collection<Shape> shapes, Model model) {
        Set<ShapeId> removedOperations = shapes.stream()
                .filter(Shape::isOperationShape)
                .map(Shape::getId)
                .collect(Collectors.toSet());
        Set<ShapeId> removedErrors = shapes.stream()
                .filter(shape -> shape.hasTrait(ErrorTrait.class))
                .map(Shape::getId)
                .collect(Collectors.toSet());

        Set<Shape> servicesToUpdate = getServicesToUpdate(model, removedOperations, removedErrors);
        Set<Shape> shapesToUpdate = new HashSet<>(servicesToUpdate);

        Set<Shape> operationsToUpdate = getOperationsToUpdate(
                model, servicesToUpdate.stream().map(Shape::getId).collect(Collectors.toSet()));
        shapesToUpdate.addAll(operationsToUpdate);

        Set<Shape> membersToUpdate = getMembersToUpdate(
                model, operationsToUpdate.stream().map(Shape::getId).collect(Collectors.toSet()));
        shapesToUpdate.addAll(membersToUpdate);
        return transformer.replaceShapes(model, shapesToUpdate);
    }

    private Set<Shape> getServicesToUpdate(Model model, Set<ShapeId> removedOperations, Set<ShapeId> removedErrors) {
        return model.shapes(ServiceShape.class)
                .flatMap(service -> Trait.flatMapStream(service, ClientEndpointDiscoveryTrait.class))
                .filter(pair -> removedOperations.contains(pair.getRight().getOperation())
                        || removedErrors.contains(pair.getRight().getError()))
                .map(pair -> {
                    ServiceShape.Builder builder = pair.getLeft().toBuilder();
                    builder.removeTrait(ClientEndpointDiscoveryTrait.ID);
                    return builder.build();
                })
                .collect(Collectors.toSet());
    }

    private Set<Shape> getOperationsToUpdate(
            Model model,
            Set<ShapeId> updatedServices
    ) {
        ClientEndpointDiscoveryIndex discoveryIndex = model.getKnowledge(ClientEndpointDiscoveryIndex.class);
        Set<ShapeId> stillBoundOperations = model.shapes(ServiceShape.class)
                // Get all endpoint discovery services
                .filter(service -> service.hasTrait(ClientEndpointDiscoveryTrait.class))
                .map(Shape::getId)
                // Get those services who aren't having their discovery traits removed
                .filter(service -> !updatedServices.contains(service))
                // Get all the discovery operations bound to those services
                .flatMap(service -> discoveryIndex.getEndpointDiscoveryOperations(service).stream())
                .collect(Collectors.toSet());

        return model.shapes(OperationShape.class)
                // Get all endpoint discovery operations
                .flatMap(operation -> Trait.flatMapStream(operation, ClientDiscoveredEndpointTrait.class))
                // Only get the ones where discovery is optional, as it is safe to remove in that case
                .filter(pair -> !pair.getRight().isRequired())
                // Only get the ones that aren't still bound to a service that requires endpoint discovery
                .filter(pair -> !stillBoundOperations.contains(pair.getLeft().getId()))
                // Remove the trait
                .map(pair -> pair.getLeft().toBuilder().removeTrait(ClientDiscoveredEndpointTrait.ID).build())
                .collect(Collectors.toSet());
    }

    private Set<Shape> getMembersToUpdate(Model model, Set<ShapeId> updatedOperations) {
        Set<ShapeId> stillBoundMembers = model.shapes(OperationShape.class)
                // Get all endpoint discovery operations
                .filter(operation -> operation.hasTrait(ClientDiscoveredEndpointTrait.class))
                // Filter out the ones which are having their endpoint discovery traits removed
                .filter(operation -> !updatedOperations.contains(operation.getId()))
                // Get the input shapes of those operations
                .filter(operation -> operation.getInput().isPresent())
                .map(operation -> model.getShape(operation.getInput().get()).flatMap(Shape::asStructureShape))
                .filter(Optional::isPresent)
                // Get the input members
                .flatMap(input -> input.get().getAllMembers().values().stream())
                .map(Shape::getId)
                .collect(Collectors.toSet());

        return model.shapes(MemberShape.class)
                // Get all members which have the endpoint discovery id trait
                .filter(member -> member.hasTrait(ClientEndpointDiscoveryIdTrait.class))
                // Get those which are on structures that aren't still bound to endpoint discovery operations
                .filter(member -> !stillBoundMembers.contains(member.getId()))
                // Remove the trait
                .map(member -> member.toBuilder().removeTrait(ClientEndpointDiscoveryIdTrait.ID).build())
                .collect(Collectors.toSet());
    }
}