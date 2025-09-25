package de.farm.app.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    record Window(int count, long reset) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        var req = (HttpServletRequest) request;
        var res = (HttpServletResponse) response;

        String key = req.getRemoteAddr() + ":" + req.getRequestURI();
        var now = Instant.now().getEpochSecond();

        windows.compute(key, (k, w) -> {
            if (w == null || now >= w.reset) {
                // 1-second window
                return new Window(1, now + 1);

            }
            // 30 req/sec per IP per path
            if (w.count >= 30) {
                return w;

            }
            
            return new Window(w.count + 1, w.reset);
        });

        var win = windows.get(key);

        if (win.count > 30) {
            res.setStatus(429);
            return;
        }

        chain.doFilter(request, response);
    }

}
