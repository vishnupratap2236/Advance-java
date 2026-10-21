package com.shopsphere.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfTest {

    @Test
    @DisplayName("a token is created per session and stays stable")
    void tokenIsStablePerSession() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        HttpSession session = fakeSession(sessionAttributes);
        HttpServletRequest request = fakeRequest(session, null);

        String first = Csrf.token(request);
        String second = Csrf.token(request);

        assertNotEquals(null, first);
        assertTrue(first.length() >= 32);
        org.junit.jupiter.api.Assertions.assertEquals(first, second);
    }

    @Test
    @DisplayName("a correct token is accepted, a wrong one is rejected")
    void validatesToken() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        HttpSession session = fakeSession(sessionAttributes);
        HttpServletRequest request = fakeRequest(session, null);
        String token = Csrf.token(request);

        assertTrue(Csrf.isValid(fakeRequest(session, token), token));
        assertFalse(Csrf.isValid(fakeRequest(session, "forged-token"), "forged-token"));
        assertFalse(Csrf.isValid(fakeRequest(session, null), null));
        assertFalse(Csrf.isValid(fakeRequest(session, token), ""));
    }

    @Test
    @DisplayName("a request without a session is never valid")
    void rejectsRequestsWithoutSession() {
        assertFalse(Csrf.isValid(fakeRequest(null, "token"), "token"));
    }

    private static HttpSession fakeSession(Map<String, Object> attributes) {
        return (HttpSession) Proxy.newProxyInstance(CsrfTest.class.getClassLoader(),
                new Class[]{HttpSession.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        switch (method.getName()) {
                            case "getAttribute" -> {
                                return attributes.get(String.valueOf(args[0]));
                            }
                            case "setAttribute" -> {
                                attributes.put(String.valueOf(args[0]), args[1]);
                                return null;
                            }
                            case "getId" -> {
                                return "test-session";
                            }
                            default -> {
                                return null;
                            }
                        }
                    }
                });
    }

    private static HttpServletRequest fakeRequest(HttpSession session, String parameter) {
        return (HttpServletRequest) Proxy.newProxyInstance(CsrfTest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getSession" -> {
                            return session;
                        }
                        case "getParameter" -> {
                            return Csrf.PARAMETER_NAME.equals(args[0]) ? parameter : null;
                        }
                        case "getHeader" -> {
                            return null;
                        }
                        default -> {
                            return null;
                        }
                    }
                });
    }
}
