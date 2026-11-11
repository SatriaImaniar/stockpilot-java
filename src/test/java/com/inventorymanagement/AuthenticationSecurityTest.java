package com.inventorymanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Akses GET /login harus menghasilkan status HTTP 200")
    void testLoginPageAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Pengguna belum login dilarang mengakses dashboard dan dialihkan ke login")
    void testUnauthenticatedUserRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Login dengan kredensial valid berhasil dan dialihkan ke dashboard")
    void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "testadmin")
                        .param("password", "Test123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("Login dengan kredensial tidak valid harus dialihkan ke halaman login dengan parameter error")
    void testLoginFailure() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "testadmin")
                        .param("password", "WrongPassword!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("Halaman login dengan parameter error menampilkan pesan kesalahan")
    void testLoginPageWithErrorParam() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Halaman login dengan parameter logout menampilkan notifikasi sukses logout")
    void testLoginPageWithLogoutParam() throws Exception {
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("successMessage"))
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Pengguna terautentikasi dapat mengakses dashboard dan merender view dashboard dengan status HTTP 200")
    @org.springframework.security.test.context.support.WithMockUser(username = "testadmin", roles = {"ADMIN"})
    void testAuthenticatedUserCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("dashboard"));
    }
}
