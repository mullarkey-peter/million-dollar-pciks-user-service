package com.glizzy.milliondollarpicks.userservice.exception;

import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
public class GlobalExceptionHandler extends DefaultDataFetcherExceptionHandler {

    @NotNull
    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable exception = handlerParameters.getException();

        if (exception instanceof UserNotFoundException) {
            return CompletableFuture.completedFuture(
                DataFetcherExceptionHandlerResult.newResult()
                    .error(TypedGraphQLError.newBuilder()
                        .message(exception.getMessage())
                        .path(handlerParameters.getPath())
                        .build())
                    .build()
            );
        }

        if (exception instanceof DuplicateUserException) {
            return CompletableFuture.completedFuture(
                DataFetcherExceptionHandlerResult.newResult()
                    .error(TypedGraphQLError.newBuilder()
                        .message(exception.getMessage())
                        .path(handlerParameters.getPath())
                        .build())
                    .build()
            );
        }

        // For any other exceptions, use the default handler
        return super.handleException(handlerParameters);
    }
} 