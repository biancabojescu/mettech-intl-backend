package mettech_intl.api.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginResponse {
    @NotNull
    @NotBlank
    private String jwt;

    @NotNull
    @NotBlank
    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
