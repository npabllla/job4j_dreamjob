package ru.job4j.dream.servlet;

import ru.job4j.dream.model.User;
import ru.job4j.dream.store.PsqlStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RegServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("reg.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        User user = PsqlStore.instOf().findByEmail(email);
        if (user != null) {
            req.setAttribute("error", "Такой пользователь уже сущетствует");
            req.getRequestDispatcher("reg.jsp").forward(req, resp);
        } else {
            User ur = new User();
            ur.setName(name);
            ur.setEmail(email);
            ur.setPassword(password);
            PsqlStore.instOf().save(ur);
            resp.sendRedirect(req.getContextPath());
        }
        resp.sendRedirect(req.getContextPath() + "/reg.do");
    }
}
