package com.shopsphere.web.admin;

import com.shopsphere.util.ImageStorage;
import com.shopsphere.util.Json;
import com.shopsphere.web.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;

/**
 * Accepts a product image upload and returns the URL of the stored file.
 */
@WebServlet(name = "AdminUploadServlet", urlPatterns = {"/admin/products/upload"})
public class AdminUploadServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            Part part = request.getPart("file");
            if (part == null || part.getSubmittedFileName() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(Json.object("error", "No file was uploaded."));
                return;
            }
            if (part.getSize() > 2L * 1024 * 1024) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(Json.object("error", "The image is larger than 2 MB."));
                return;
            }
            String url;
            try (InputStream in = part.getInputStream()) {
                url = ImageStorage.store(part.getSubmittedFileName(), part.getContentType(), in);
            }
            response.getWriter().write(Json.object("url", url));
        } catch (IllegalStateException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(Json.object("error", "The upload could not be processed."));
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(Json.object("error", e.getMessage()));
        } catch (ServletException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(Json.object("error", "The upload could not be processed."));
        }
    }
}
