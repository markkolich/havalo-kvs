package com.kolich.havalo.mappers;

import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@ControllerReturnTypeMapper(IllegalArgumentException.class)
public final class IllegalArgumentExceptionHandler
    extends RenderingResponseTypeMapper<IllegalArgumentException> {

    @Override
    public final void render(final AsyncContext context,
        final HttpServletResponse response,
        final @Nonnull IllegalArgumentException entity) throws Exception {
        renderEntity(response, SC_BAD_REQUEST);
    }

}
