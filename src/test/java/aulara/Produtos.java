package aulara;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class Produtos {

    public String cadastrarProduto(String userToken, String nomeProduto){

        String produto =
                given()
                        .header("authorization", userToken)
                        .body("{\n" +
                                "  \"nome\": \""+nomeProduto+"\",\n" +
                                "  \"preco\": 470,\n" +
                                "  \"descricao\": \"Mouse\",\n" +
                                "  \"quantidade\": 500\n" +
                                "}")
                        .contentType(ContentType.JSON)
                .when()
                        .post("/produtos")
                .then()
                        .log().all()
                        .statusCode(HttpStatus.SC_CREATED)
                        .body("message", is("Cadastro realizado com sucesso"))
                        .extract().path("_id");

        return produto;
    }
}
