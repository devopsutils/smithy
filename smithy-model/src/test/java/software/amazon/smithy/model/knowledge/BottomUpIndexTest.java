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

package software.amazon.smithy.model.knowledge;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.EntityShape;
import software.amazon.smithy.model.shapes.ShapeId;

public class BottomUpIndexTest {
    private static Model model;
    private static ShapeId serviceId = ShapeId.from("smithy.example#Example");

    @BeforeAll
    public static void before() {
        model = Model.assembler()
                .addImport(OperationIndexTest.class.getResource("bottom-up.smithy"))
                .assemble()
                .unwrap();
    }

    @AfterAll
    public static void after() {
        model = null;
    }

    @Test
    public void findsEntityBinding() {
        BottomUpIndex index = model.getKnowledge(BottomUpIndex.class);

        assertThat(index.getEntityBinding(serviceId, serviceId), equalTo(Optional.empty()));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#ServiceOperation")).map(EntityShape::getId),
                   equalTo(Optional.of(serviceId)));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#Resource1")).map(EntityShape::getId),
                   equalTo(Optional.of(serviceId)));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#Resource1Operation")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1"))));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#Resource1_1")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1"))));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#Resource1_2")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1"))));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_Operation")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1"))));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_1")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1"))));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_2")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1"))));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_Operation")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1"))));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_1_Operation")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1_1"))));
        assertThat(index.getEntityBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_2_Operation")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1_2"))));
    }

    @Test
    public void findsResourceBinding() {
        BottomUpIndex index = model.getKnowledge(BottomUpIndex.class);

        assertThat(index.getResourceBinding(serviceId, serviceId), equalTo(Optional.empty()));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#ServiceOperation")).map(EntityShape::getId),
                   equalTo(Optional.empty()));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#Resource1")).map(EntityShape::getId),
                   equalTo(Optional.empty()));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#Resource1Operation")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1"))));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#Resource1_1")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1"))));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#Resource1_2")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1"))));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_Operation")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1"))));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_1")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1"))));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_2")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1"))));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_Operation")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1"))));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_1_Operation")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1_1"))));
        assertThat(index.getResourceBinding(serviceId, ShapeId.from("smithy.example#Resource1_1_2_Operation")).map(EntityShape::getId),
                   equalTo(Optional.of(ShapeId.from("smithy.example#Resource1_1_2"))));
    }

    @Test
    public void findsPathToLeafOperation() {
        BottomUpIndex index = model.getKnowledge(BottomUpIndex.class);
        List<EntityShape> entities = index.getAllParents(
                serviceId, ShapeId.from("smithy.example#Resource1_1_2_Operation"));
        List<String> ids = entities.stream()
                .map(EntityShape::getId)
                .map(ShapeId::toString)
                .collect(Collectors.toList());

        assertThat(ids, contains("smithy.example#Resource1_1_2", "smithy.example#Resource1_1", "smithy.example#Resource1", "smithy.example#Example"));
    }

    @Test
    public void findsPathToLeafResource() {
        BottomUpIndex index = model.getKnowledge(BottomUpIndex.class);
        List<EntityShape> entities = index.getAllParents(
                serviceId, ShapeId.from("smithy.example#Resource1_1_2"));
        List<String> ids = entities.stream()
                .map(EntityShape::getId)
                .map(ShapeId::toString)
                .collect(Collectors.toList());

        assertThat(ids, contains("smithy.example#Resource1_1", "smithy.example#Resource1", "smithy.example#Example"));
    }
}
