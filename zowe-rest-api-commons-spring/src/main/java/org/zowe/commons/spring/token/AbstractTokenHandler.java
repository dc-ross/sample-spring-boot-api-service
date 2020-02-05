package org.zowe.commons.spring.token;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractTokenHandler extends OncePerRequestFilter {

    private final TokenFailureHandler failureHandler;
    private final AuthConfigurationProperties authConfigurationProperties;

    /**
     * Extracts the token from the request
     *
     * @param request containing credentials
     * @return credentials
     */
    protected abstract Optional<AbstractAuthenticationToken> extractContent(HttpServletRequest request);

    /**
     * Extracts the token from the request and use the authentication manager to perform authentication.
     * Then set the currently authenticated principal and call the next filter in the chain.
     *
     * @param request     the http request
     * @param response    the http response
     * @param filterChain the filter chain
     * @throws ServletException a general exception
     * @throws IOException      a IO exception
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        Optional<AbstractAuthenticationToken> authenticationToken = extractContent(request);

        if (authenticationToken.isPresent()) {
            try {
                UsernamePasswordAuthenticationToken authentication = getAuthentication(request).get();

                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } catch (AuthenticationException authenticationException) {
                failureHandler.handleException(response, authenticationException);
            } catch (RuntimeException e) {
                failureHandler.handleException(response, e);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private Optional<UsernamePasswordAuthenticationToken> getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(authConfigurationProperties.getTokenProperties().getRequestHeader());
        if (token != null) {

            String username = Jwts.parser()
                .setSigningKey(authConfigurationProperties.getTokenProperties().getSecretKeyToGenJWTs())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

            if (username != null) {
                return Optional.ofNullable(new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>()));
            }
            return null;
        }
        return null;
    }
}