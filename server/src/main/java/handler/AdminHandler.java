package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.AdminService;

public class AdminHandler {
    private final AdminService adminService;

    public AdminHandler(AdminService adminService) {
        this.adminService = adminService;
    }

    public void clear(Context ctx) throws DataAccessException {
        adminService.clear();
        ctx.status(200);
    }
}
