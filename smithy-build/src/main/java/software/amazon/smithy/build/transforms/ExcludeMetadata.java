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

package software.amazon.smithy.build.transforms;

import java.util.Collections;
import java.util.Set;
import software.amazon.smithy.build.TransformContext;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.transform.ModelTransformer;

/**
 * {@code excludeMetadata} removes metadata entries when a metadata key
 * matches {@code keys}.
 */
public final class ExcludeMetadata extends BackwardCompatHelper<ExcludeMetadata.Config> {

    /**
     * {@code excludeMetadata} configuration settings.
     */
    public static final class Config {
        private Set<String> keys = Collections.emptySet();

        /**
         * @return the list of keys to remove from metadata.
         */
        public Set<String> getKeys() {
            return keys;
        }

        /**
         * Sets the list of keys to remove from metadata.
         *
         * @param keys Metadata keys to remove.
         */
        public void setKeys(Set<String> keys) {
            this.keys = keys;
        }
    }

    @Override
    public Class<Config> getConfigType() {
        return Config.class;
    }

    @Override
    public String getName() {
        return "excludeMetadata";
    }

    @Override
    String getBackwardCompatibleNameMapping() {
        return "keys";
    }

    @Override
    protected Model transformWithConfig(TransformContext context, Config config) {
        Model model = context.getModel();
        ModelTransformer transformer = context.getTransformer();
        Set<String> keys = config.getKeys();
        return transformer.filterMetadata(model, (key, value) -> !keys.contains(key));
    }
}
