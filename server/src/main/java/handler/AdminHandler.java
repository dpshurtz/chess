package handler;

import io.javalin.http.Context;
import service.AdminService;

public class AdminHandler {
    private final AdminService adminService;

    public AdminHandler(AdminService adminService) {
        this.adminService = adminService;
    }

    public void clear(Context ctx) {
        adminService.clear();
        ctx.status(200);
    }
}
