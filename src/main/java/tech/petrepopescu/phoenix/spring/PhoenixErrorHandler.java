package tech.petrepopescu.phoenix.spring;

import tech.petrepopescu.phoenix.utils.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import tech.petrepopescu.phoenix.format.JsonFormat;
import tech.petrepopescu.phoenix.format.Result;
import tech.petrepopescu.phoenix.spring.config.PhoenixConfiguration;
import tech.petrepopescu.phoenix.views.View;
import tech.petrepopescu.phoenix.views.predefined.View404;
import tech.petrepopescu.phoenix.views.predefined.View500;

@ControllerAdvice
public class PhoenixErrorHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PhoenixErrorHandler.class);
    private final View404 view404 = new View404();
    private final View500 view500 = new View500();
    private final PhoenixConfiguration configuration;

    public PhoenixErrorHandler(PhoenixConfiguration configuration) {
        this.configuration = configuration;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleNoHandlerFound(NoHandlerFoundException e, WebRequest request) {
        if (configuration.getErrorPages() == null || StringUtils.isBlank(configuration.getErrorPages().getCode404())) {
            return new Result(view404, HttpStatus.NOT_FOUND);
        }

        return new Result(View.of(configuration.getErrorPages().getCode404()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handlerAnyException(Exception e, WebRequest request) {
        try {
            log.error("Error occurred", e);
            if (configuration.getErrorPages() == null || StringUtils.isBlank(configuration.getErrorPages().getCode500())) {
                return new Result(view500, HttpStatus.NOT_FOUND);
            }

            return new Result(View.of(configuration.getErrorPages().getCode500()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            log.error("Error rendering error page", ex);
            return new Result(new JsonFormat(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
