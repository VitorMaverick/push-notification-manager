package br.edu.acad.ifma.web.rest;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class FcmRequest {

    @NotBlank
    private String token;

    private String titulo;
    private String corpo;
    private String imagemUrl;
    private Map<String, String> dados;

    public FcmRequest() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCorpo() {
        return corpo;
    }

    public void setCorpo(String corpo) {
        this.corpo = corpo;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public Map<String, String> getDados() {
        return dados;
    }

    public void setDados(Map<String, String> dados) {
        this.dados = dados;
    }
}
