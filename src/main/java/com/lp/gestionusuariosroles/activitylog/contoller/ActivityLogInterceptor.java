package com.lp.gestionusuariosroles.activitylog.contoller;

import com.lp.gestionusuariosroles.activitylog.service.ActivityLogService;
import com.lp.gestionusuariosroles.activitylog.service.LogActivity;
import com.lp.gestionusuariosroles.user.controller.UserDataResponse;
import com.lp.gestionusuariosroles.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ActivityLogInterceptor implements HandlerInterceptor {

    private final ActivityLogService service;
    private final UserService userService;

    @Override
    public void afterCompletion(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull Object handler,
            Exception ex) {
        if (handler instanceof HandlerMethod handlerMethod) {
            LogActivity annotation = handlerMethod.getMethodAnnotation(LogActivity.class);

            if (annotation != null) {
                UserDataResponse userInSession = userService.getUserInSessionData(request);
                if (userInSession != null) {
                    service.logActivity(
                            userInSession.id(),
                            userInSession.nombres() + " " + userInSession.apellidos(),
                            annotation.action(),
                            annotation.description()
                    );
                }
            }
        }
    }
}
