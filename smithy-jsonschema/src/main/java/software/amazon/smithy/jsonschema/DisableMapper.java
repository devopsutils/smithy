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

package software.amazon.smithy.jsonschema;

import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.Shape;

/**
 * Removes keywords from a Schema builder that have been disabled using
 * the settings object {@code disable.*} flags.
 */
final class DisableMapper implements JsonSchemaMapper {
    @Override
    public byte getOrder() {
        return 120;
    }

    @Override
    public Schema.Builder updateSchema(Shape shape, Schema.Builder schema, ObjectNode config) {
        for (String key : config.getMembersByPrefix("disable.").keySet()) {
            schema.disableProperty(key);
        }

        return schema;
    }
}
