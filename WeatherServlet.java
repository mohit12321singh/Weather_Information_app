package com.weatherapp.servlet;

import com.google.gson.JsonObject;
import com.weatherapp.service.WeatherService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/weather")
public class WeatherServlet extends HttpServlet {
    private final WeatherService weatherService = new WeatherService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String city = request.getParameter("city");
        if (city == null || city.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "City parameter is required");
            return;
        }

        try {
            JsonObject weatherData = weatherService.getWeather(city);
            response.setContentType("application/json");
            response.getWriter().write(weatherData.toString());
        } catch (Exception e) {
            // Log the exception so server logs contain the stacktrace for debugging
            log("Error fetching weather data", e);
            // Return a more helpful message during development (includes cause)
            String msg = e.getMessage() != null ? e.getMessage() : "Error fetching weather data";
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching weather data: " + msg);
        }
    }
}
