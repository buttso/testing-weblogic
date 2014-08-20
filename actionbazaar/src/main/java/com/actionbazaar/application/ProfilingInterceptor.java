package com.actionbazaar.application;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Profiled
public class ProfilingInterceptor {

    private static final Logger logger
            = Logger.getLogger(ProfilingInterceptor.class.getName());

    @AroundInvoke
    public Object profile(InvocationContext context) throws Exception {
        Object value;

        long start = System.currentTimeMillis();
        value = context.proceed();
        long end = System.currentTimeMillis();

        logger.log(Level.INFO, "Execution time for: {0} was: {1} milliseconds",
                new Object[]{context.getMethod().getName(), (end - start)});

        return value;
    }
}
