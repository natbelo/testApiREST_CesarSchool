package aulara;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class Usuarios {

    public String cadastrarUsuario(String name, String email){

        String id =
                given()
                        .body("{\n" +
                                "  \"nome\": \""+name+"\",\n" +
                                "  \"email\": \""+email+"\",\n" +
                                "  \"password\": \"teste\",\n" +
                                "  \"administrador\": \"true\"\n" +
                                "}")
                        .contentType(ContentType.JSON)
                        .when()
                        .post("usuarios")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.SC_CREATED)
                        .body("message", is("Cadastro realizado com sucesso"))
                        .extract().path("_id");
        return id;

    }

    public String autenticarUsuario(String email){

        String token =
                given()
                        .body("{\n" +
                                "  \"email\": \""+email+"\",\n" +
                                "  \"password\": \"teste\"\n" +
                                "}")
                        .contentType(ContentType.JSON)
                        .when()
                        .post("/login")
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.SC_OK)
                        .body("message", is("Login realizado com sucesso"))
                        .extract().path("authorization");

        return token;
    }
}
