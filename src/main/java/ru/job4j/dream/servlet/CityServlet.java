package ru.job4j.dream.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.job4j.dream.model.City;
import ru.job4j.dream.store.PsqlStore;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

public class CityServlet  extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Collection<City> list = PsqlStore.instOf().findAllCity();
        final Gson gson = new GsonBuilder().create();
        String string = gson.toJson(list);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("json");
        resp.getWriter().write(string);
    }
}
