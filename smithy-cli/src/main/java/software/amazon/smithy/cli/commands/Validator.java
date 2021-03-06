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

package software.amazon.smithy.cli.commands;

import static java.lang.String.format;

import software.amazon.smithy.cli.Cli;
import software.amazon.smithy.cli.CliError;
import software.amazon.smithy.cli.Colors;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.validation.ContextualValidationEventFormatter;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidatedResult;

/**
 * Shares logic for validating a model and printing out events.
 */
final class Validator {
    private Validator() {}

    static void validate(ValidatedResult<Model> result) {
        ContextualValidationEventFormatter formatter = new ContextualValidationEventFormatter();

        result.getValidationEvents().stream()
                .filter(event -> event.getSeverity() != Severity.SUPPRESSED)
                .sorted()
                .forEach(event -> {
                    if (event.getSeverity() == Severity.WARNING) {
                        Colors.YELLOW.out(formatter.format(event));
                    } else if (event.getSeverity() == Severity.DANGER || event.getSeverity() == Severity.ERROR) {
                        Colors.RED.out(formatter.format(event));
                    } else {
                        Cli.stdout(event);
                    }
                    Cli.stdout("");
                });

        long errors = result.getValidationEvents(Severity.ERROR).size();
        long dangers = result.getValidationEvents(Severity.DANGER).size();

        String line = format(
                "Validation result: %s ERROR(s), %d DANGER(s), %d WARNING(s), %d NOTE(s)",
                errors, dangers, result.getValidationEvents(Severity.WARNING).size(),
                result.getValidationEvents(Severity.NOTE).size());
        Cli.stdout(line);
        result.getResult().ifPresent(model -> Cli.stdout(String.format(
                "Validated %d shapes in model", model.shapes().count())));

        if (!result.getResult().isPresent() || errors + dangers > 0) {
            // Show the error and danger severity events.
            throw new CliError(format("The model is invalid: %s ERROR(s), %d DANGER(s)", errors, dangers));
        }
    }
}
