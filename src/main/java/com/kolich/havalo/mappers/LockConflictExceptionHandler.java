package com.kolich.havalo.mappers;

import com.kolich.bolt.exceptions.LockConflictException;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;

@ControllerReturnTypeMapper(LockConflictException.class)
public final class LockConflictExceptionHandler
    extends RenderingResponseTypeMapper<LockConflictException> {

    @Override
    public final void render(final AsyncContext context,
        final HttpServletResponse response,
        final @Nonnull LockConflictException entity) throws Exception {
        renderEntity(response, SC_CONFLICT);
    }

}
